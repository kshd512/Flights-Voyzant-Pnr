package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.*;
import com.mmt.flights.entity.odc.EmailContact;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.TravelDocument;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class ODCExchangePriceRequestBuilderTaskTest {

    @InjectMocks
    private ODCExchangePriceRequestBuilderTask odcExchangePriceRequestBuilderTask;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommonDocumentService commonDocumentService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Mock Document creation
        Document document = new Document();
        document.setName("API GATEWAY");
        document.setReferenceVersion("1.2");
        when(commonDocumentService.createDocument()).thenReturn(document);
        
        // Mock Party creation
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("Diva Travels");
        travelAgencySender.setIataNumber("");
        travelAgencySender.setAgencyID("Diva Travels");
        
        // Setup contacts
        Contacts contacts = new Contacts();
        List<Contact> contactList = new ArrayList<>();
        Contact contact = new Contact();
        EmailContact emailContact = new EmailContact();
        contact.setEmailContact("vakarram@gmail.com");
        contactList.add(contact);
        contacts.setContact(contactList);
        travelAgencySender.setContacts(contacts);
        
        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        when(commonDocumentService.createParty()).thenReturn(party);
    }

    @Test
    public void testOutputMatchesExpectedJson() throws Exception {
        // Create sample request data
        DateChangePrePaymentRequest request = createSampleRequest();
        OrderViewRS orderViewRS = createSampleOrderViewRS();
        
        // Create FlowState with the request and PNR response
        FlowState flowState = new FlowState.Builder(System.currentTimeMillis())
                .addValue(FlowStateKey.REQUEST, request)
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, objectMapper.writeValueAsString(orderViewRS))
                .build();
        
        // Execute the task
        FlowState resultState = odcExchangePriceRequestBuilderTask.run(flowState);
        
        // Get the output JSON
        String exchangePriceRequest = resultState.getValue(FlowStateKey.ODC_EXCHANGE_PRICE_REQUEST);
        
        // Parse and verify the structure
        JsonNode jsonNode = objectMapper.readTree(exchangePriceRequest);
        
        // Print for manual verification
        System.out.println("Generated JSON:");
        System.out.println(objectMapper.writeValueAsString(jsonNode));
    }
    
    private DateChangePrePaymentRequest createSampleRequest() {
        DateChangePrePaymentRequest request = new DateChangePrePaymentRequest();
        request.setPnr("5L2YIR");
        request.setMmtId("166901478523"); // This will be used as TraceId
        request.setRKey("1721375083437470641,1227102711721375087885644107"); // Format: shoppingResponseId,offerId
        return request;
    }

    private OrderViewRS createSampleOrderViewRS() {
        OrderViewRS orderViewRS = new OrderViewRS();
        
        // Create DataLists
        com.mmt.flights.entity.pnr.retrieve.response.DataLists dataLists = new com.mmt.flights.entity.pnr.retrieve.response.DataLists();
        
        // Create PassengerList
        com.mmt.flights.entity.pnr.retrieve.response.PassengerList passengerList = new com.mmt.flights.entity.pnr.retrieve.response.PassengerList();
        List<com.mmt.flights.entity.pnr.retrieve.response.Passenger> passengers = new ArrayList<>();
        
        // Add passengers
        com.mmt.flights.entity.pnr.retrieve.response.Passenger pax1 = createPassenger("ADT1", "ADT", "Mr", "RAM", "", "KUMAR", "9101305330348");
        com.mmt.flights.entity.pnr.retrieve.response.Passenger pax2 = createPassenger("ADT2", "ADT", "Mr", "RAJ", "", "KUMAR", "9101305330347");
        com.mmt.flights.entity.pnr.retrieve.response.Passenger pax3 = createPassenger("CHD1", "CHD", "Mr", "SHANTHA", "", "KUMAR", "9101305330349");
        
        passengers.add(pax1);
        passengers.add(pax2);
        passengers.add(pax3);
        
        passengerList.setPassengers(passengers);
        dataLists.setPassengerList(passengerList);
        orderViewRS.setDataLists(dataLists);
        
        return orderViewRS;
    }

    private com.mmt.flights.entity.pnr.retrieve.response.Passenger createPassenger(
            String passengerId, String ptc, String title, String firstName, 
            String middleName, String lastName, String documentNumber) {
        
        com.mmt.flights.entity.pnr.retrieve.response.Passenger passenger = new com.mmt.flights.entity.pnr.retrieve.response.Passenger();
        passenger.setPassengerID(passengerId);
        passenger.setPtc(ptc);
        passenger.setNameTitle(title);
        passenger.setFirstName(firstName);
        passenger.setMiddleName(middleName);
        passenger.setLastName(lastName);
        
        // Set travel document
        TravelDocument travelDocument = new TravelDocument();
        travelDocument.setDocumentNumber(documentNumber);
        passenger.setTravelDocument(travelDocument);
        
        return passenger;
    }
}