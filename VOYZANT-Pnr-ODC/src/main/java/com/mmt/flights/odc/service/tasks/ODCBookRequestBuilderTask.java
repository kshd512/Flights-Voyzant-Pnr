package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.Query;
import com.mmt.flights.entity.odc.*;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Payment;
import com.mmt.flights.entity.pnr.retrieve.response.Payments;
import com.mmt.flights.odc.commit.DateChangeCommitRequest;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ODCBookRequestBuilderTask implements MapTask {

    //@Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private CommonDocumentService commonDocumentService;

    @Override
    public FlowState run(FlowState state) throws Exception {
        DateChangeCommitRequest request = validateAndGetRequest(state);
        OrderViewRS orderViewRS = getOrderViewRS(state);
        
        OrderReshopRequest orderChangeRequest = new OrderReshopRequest();
        OrderReshopRQ rq = buildOrderReshopRQ(request, orderViewRS);
        orderChangeRequest.setOrderReshopRQ(rq);
        
        String orderChangeRequestStr = objectMapper.writeValueAsString(orderChangeRequest);
        return state.toBuilder()
                .addValue(FlowStateKey.ODC_BOOK_REQUEST, orderChangeRequestStr)
                .build();
    }

    private DateChangeCommitRequest validateAndGetRequest(FlowState state) throws PSErrorException {
        DateChangeCommitRequest request = state.getValue(FlowStateKey.REQUEST);
        if (request == null || request.getPnr() == null || request.getPnr().isEmpty()) {
            throw new PSErrorException(ErrorEnum.INVALID_REQUEST);
        }
        return request;
    }

    private OrderViewRS getOrderViewRS(FlowState state) throws Exception {
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        return objectMapper.readValue(pnrResponseData, OrderViewRS.class);
    }

    private OrderReshopRQ buildOrderReshopRQ(DateChangeCommitRequest request, OrderViewRS orderViewRS) {
        OrderReshopRQ rq = new OrderReshopRQ();
        rq.setDocument(commonDocumentService.createDocument());
        rq.setParty(commonDocumentService.createParty());
        rq.setBookingType("BOOK");
        
        setResponseIds(rq, request);
        rq.setQuery(buildQuery(request));
        rq.setPayments(buildPayments(request));
        rq.setDataLists(buildDataLists(orderViewRS, request));
        rq.setMetaData(buildMetaData(request));
        
        return rq;
    }

    private void setResponseIds(OrderReshopRQ rq, DateChangeCommitRequest request) {
        rq.setShoppingResponseId(extractFromExtras(request.getExtraInformation(), "shoppingResponseId"));
        rq.setOfferResponseId(extractFromExtras(request.getExtraInformation(), "offerResponseId"));
    }

    private Query buildQuery(DateChangeCommitRequest request) {
        Query query = new Query();
        OrderServicing orderServicing = new OrderServicing();
        AcceptOffer acceptOffer = new AcceptOffer();
        
        String offerId = extractFromExtras(request.getExtraInformation(), "offerId");
        if (offerId != null && !offerId.isEmpty()) {
            Offer offer = new Offer();
            offer.setOfferID(offerId);
            acceptOffer.getOffer().add(offer);
        }
        
        orderServicing.setAcceptOffer(acceptOffer);
        query.setOrderServicing(orderServicing);
        return query;
    }

    private Payments buildPayments(DateChangeCommitRequest request) {
        Payments payments = new Payments();
        Payment payment = new Payment();
        
        payment.setType(mapPaymentType(request.getFopCode()));
        payment.setPassengerID("ALL");
        
        String amountStr = extractFromExtras(request.getExtraInformation(), "amount");
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                payment.setAmount(Double.parseDouble(amountStr));
            } catch (NumberFormatException e) {
                MMTLogger.error("", "Invalid amount format in extra information: "+ amountStr , this.getClass().getName(), null);
            }
        }

        List<Payment> paymentList = new ArrayList<>();
        paymentList.add(payment);
        payments.setPayment(paymentList);
        return payments;
    }

    private DataLists buildDataLists(OrderViewRS orderViewRS, DateChangeCommitRequest request) {
        DataLists dataLists = new DataLists();
        dataLists.setPassengerList(buildPassengerList(orderViewRS));
        dataLists.setContactList(buildContactList(request));
        return dataLists;
    }

    private PassengerList buildPassengerList(OrderViewRS orderViewRS) {
        PassengerList passengerList = new PassengerList();
        for (com.mmt.flights.entity.pnr.retrieve.response.Passenger pax : 
                orderViewRS.getDataLists().getPassengerList().getPassengers()) {
            passengerList.getPassenger().add(buildPassenger(pax));
        }
        return passengerList;
    }

    private Passenger buildPassenger(com.mmt.flights.entity.pnr.retrieve.response.Passenger pax) {
        Passenger passenger = new Passenger();
        passenger.setPassengerID(pax.getPassengerID());
        passenger.setPtc(pax.getPtc());
        passenger.setNameTitle(pax.getNameTitle());
        passenger.setFirstName(pax.getFirstName());
        passenger.setMiddleName(pax.getMiddleName());
        passenger.setLastName(pax.getLastName());
        passenger.setDocumentNumber(pax.getTravelDocument().getDocumentNumber());
        return passenger;
    }

    private ContactList buildContactList(DateChangeCommitRequest request) {
        ContactList contactList = new ContactList();
        ContactInformation contactInfo = new ContactInformation();
        contactInfo.setContactID("CTC1");
        contactInfo.setAgencyName("MMT");
        contactInfo.setEmailAddress(extractEmail(request.getExtraInformation()));
        
        String phone = extractFromExtras(request.getExtraInformation(), "phone");
        if (phone != null && !phone.isEmpty()) {
            Mobile mobile = new Mobile();
            mobile.setMobileNumber(phone);
            contactInfo.setMobile(mobile);
        }
        
        contactList.getContactInformation().add(contactInfo);
        return contactList;
    }

    private MetaData buildMetaData(DateChangeCommitRequest request) {
        MetaData metadata = new MetaData();
        metadata.setTraceId(request.getMmtId());
        return metadata;
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