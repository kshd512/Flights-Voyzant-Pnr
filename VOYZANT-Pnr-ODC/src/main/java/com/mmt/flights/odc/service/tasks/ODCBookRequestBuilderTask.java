package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.common.*;
import com.mmt.flights.entity.odc.*;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Payment;
import com.mmt.flights.entity.pnr.retrieve.response.Payments;
import com.mmt.flights.odc.commit.DateChangeCommitRequest;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ODCBookRequestBuilderTask implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FlowState run(FlowState state) throws Exception {
        DateChangeCommitRequest request = state.getValue(FlowStateKey.REQUEST);
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        OrderViewRS orderViewRS = objectMapper.readValue(pnrResponseData, OrderViewRS.class);
        
        if (request == null || request.getPnr() == null || request.getPnr().isEmpty()) {
            throw new PSErrorException(ErrorEnum.INVALID_REQUEST);
        }

        // Build OrderChangeRequest
        OrderReshopRequest orderChangeRequest = new OrderReshopRequest();
        OrderReshopRQ rq = new OrderReshopRQ();

        // Set document info
        Document document = new Document();
        document.setName("API GATEWAY");
        document.setReferenceVersion("1.2");
        rq.setDocument(document);

        // Set party info
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender agencySender = new TravelAgencySender();
        agencySender.setName("MMT");
        agencySender.setAgencyID("MMT");
        
        // Add contacts if available
        Contacts contacts = new Contacts();
        Contact contact = new Contact();
        
        // Try to get email from extra information or default to a placeholder
        String email = extractEmail(request.getExtraInformation());
        contact.setEmailContact(email);
        contacts.getContact().add(contact);
        
        agencySender.setContacts(contacts);
        sender.setTravelAgencySender(agencySender);
        party.setSender(sender);
        rq.setParty(party);

        // Extract shopping response ID and offer response ID from extras if available
        String shoppingResponseId = extractFromExtras(request.getExtraInformation(), "shoppingResponseId");
        String offerResponseId = extractFromExtras(request.getExtraInformation(), "offerResponseId");
        
        rq.setShoppingResponseId(shoppingResponseId);
        rq.setOfferResponseId(offerResponseId);

        // Set metadata
        MetaData metadata = new MetaData();
        metadata.setTraceId(request.getMmtId());
        rq.setMetaData(metadata);

        // Set query with OrderServicing
        Query query = new Query();
        OrderServicing orderServicing = new OrderServicing();
        AcceptOffer acceptOffer = new AcceptOffer();
        
        // Add offer IDs from the request's extra information
        String offerId = extractFromExtras(request.getExtraInformation(), "offerId");
        if (offerId != null && !offerId.isEmpty()) {
            Offer offer = new Offer();
            offer.setOfferID(offerId);
            acceptOffer.getOffer().add(offer);
        }
        
        orderServicing.setAcceptOffer(acceptOffer);
        query.setOrderServicing(orderServicing);
        rq.setQuery(query);
        
        // Set booking type
        rq.setBookingType("BOOK");
        
        // Set payment details
        Payments payments = new Payments();
        Payment payment = new Payment();
        
        // Set payment type based on fopCode
        payment.setType(mapPaymentType(request.getFopCode()));
        payment.setPassengerID("ALL");
        
        // Set amount from extras if available
        String amountStr = extractFromExtras(request.getExtraInformation(), "amount");
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                payment.setAmount(Double.parseDouble(amountStr));
            } catch (NumberFormatException e) {
                // Default amount or handle error
            }
        }
        
        payments.getPayment().add(payment);
        rq.setPayments(payments);
        
        // Set passenger and contact information from the PNR response
        DataLists dataLists = new DataLists();
        PassengerList passengerList = new PassengerList();
        
        // Copy passengers from order view
        for (com.mmt.flights.entity.pnr.retrieve.response.Passenger pax : 
                orderViewRS.getDataLists().getPassengerList().getPassengers()) {
            Passenger passenger = new Passenger();
            passenger.setPassengerID(pax.getPassengerID());
            passenger.setPtc(pax.getPtc());
            passenger.setNameTitle(pax.getNameTitle());
            passenger.setFirstName(pax.getFirstName());
            passenger.setMiddleName(pax.getMiddleName());
            passenger.setLastName(pax.getLastName());
            passenger.setDocumentNumber(pax.getTravelDocument().getDocumentNumber());
            passengerList.getPassenger().add(passenger);
        }
        
        dataLists.setPassengerList(passengerList);
        
        // Add contact information
        ContactList contactList = new ContactList();
        ContactInformation contactInfo = new ContactInformation();
        contactInfo.setContactID("CTC1");
        contactInfo.setAgencyName("MMT");
        contactInfo.setEmailAddress(email);
        
        // Add phone/mobile if available from request
        String phone = extractFromExtras(request.getExtraInformation(), "phone");
        if (phone != null && !phone.isEmpty()) {
            Mobile mobile = new Mobile();
            mobile.setMobileNumber(phone);
            contactInfo.setMobile(mobile);
        }
        
        contactList.getContactInformation().add(contactInfo);
        dataLists.setContactList(contactList);
        
        rq.setDataLists(dataLists);
        
        // Set the full request
        orderChangeRequest.setOrderChangeRQ(rq);
        
        // Convert to JSON string
        String orderChangeRequestStr = objectMapper.writeValueAsString(orderChangeRequest);
        
        return state.toBuilder()
                .addValue(FlowStateKey.ODC_BOOK_REQUEST, orderChangeRequestStr)
                .build();
    }
    
    private String extractEmail(Map<String, Object> extraInfo) {
        if (extraInfo != null) {
            Object email = extraInfo.get("email");
            if (email != null) {
                return email.toString();
            }
        }
        return "customer@example.com"; // Default placeholder
    }
    
    private String extractFromExtras(Map<String, Object> extraInfo, String key) {
        if (extraInfo != null && extraInfo.containsKey(key)) {
            Object value = extraInfo.get(key);
            return value != null ? value.toString() : null;
        }
        return null;
    }
    
    private String mapPaymentType(String fopCode) {
        if (fopCode == null) return "CHECK";
        
        switch (fopCode.toUpperCase()) {
            case "CC":
                return "CREDIT_CARD";
            case "DC":
                return "DEBIT_CARD";
            case "NB":
                return "NET_BANKING";
            case "UPI":
                return "UPI";
            default:
                return "CHECK";
        }
    }
}