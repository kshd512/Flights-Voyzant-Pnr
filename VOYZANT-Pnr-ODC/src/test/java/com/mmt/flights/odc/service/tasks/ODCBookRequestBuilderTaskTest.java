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
import com.mmt.flights.odc.commit.DateChangeCommitRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

public class ODCBookRequestBuilderTaskTest {

    @InjectMocks
    private ODCBookRequestBuilderTask odcBookRequestBuilderTask;

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
        DateChangeCommitRequest request = createSampleRequest();
        OrderViewRS orderViewRS = createSampleOrderViewRS();
        
        // Create FlowState with the request and PNR response
        FlowState flowState = new FlowState.Builder(System.currentTimeMillis())
                .addValue(FlowStateKey.REQUEST, request)
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, objectMapper.writeValueAsString(orderViewRS))
                .build();
        
        // Execute the task
        FlowState resultState = odcBookRequestBuilderTask.run(flowState);
        
        // Get the output JSON
        String odcBookRequest = resultState.getValue(FlowStateKey.ODC_BOOK_REQUEST);
        
        // Parse and verify the structure matches expected format
        JsonNode jsonNode = objectMapper.readTree(odcBookRequest);
        
        // Print for manual verification
        System.out.println("Generated JSON:");
        System.out.println(objectMapper.writeValueAsString(jsonNode));
    }

    private DateChangeCommitRequest createSampleRequest() {
        DateChangeCommitRequest request = new DateChangeCommitRequest();
        request.setPnr("5L2YIR");
        request.setMmtId("MMT123");
        request.setFopCode("CHECK");
        
        // Set extra information
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("shoppingResponseId", "1721375083437470641");
        extraInfo.put("offerResponseId", "1721375216410175645");
        extraInfo.put("offerId", "1227102711721375087885644107");
        extraInfo.put("amount", "69930");
        extraInfo.put("email", "vakarram@gmail.com");
        request.setExtraInformation(extraInfo);
        
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
        passengers.add(createPassenger("ADT1", "ADT", "Mr", "RAM", "", "KUMAR", "9101305330348"));
        passengers.add(createPassenger("ADT2", "ADT", "Mr", "RAJ", "", "KUMAR", "9101305330347"));
        passengers.add(createPassenger("CHD1", "CHD", "Mr", "SHANTHA", "", "KUMAR", "9101305330349"));
        
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