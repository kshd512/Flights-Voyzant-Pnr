package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.Contact;
import com.mmt.flights.entity.common.Contacts;
import com.mmt.flights.entity.common.*;
import com.mmt.flights.entity.odc.EmailContact;
import com.mmt.flights.entity.pnr.retrieve.response.*;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.search.Itinerary;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ODCSearchRequestBuilderTaskTest {

    @InjectMocks
    private ODCSearchRequestBuilderTask odcSearchRequestBuilderTask;

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
        DateChangeSearchRequest request = createSampleRequest();
        OrderViewRS orderViewRS = createSampleOrderViewRS();
        
        // Create FlowState with the request and PNR response
        FlowState flowState = new FlowState.Builder(System.currentTimeMillis())
                .addValue(FlowStateKey.REQUEST, request)
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, objectMapper.writeValueAsString(orderViewRS))
                .build();
        
        // Execute the task
        FlowState resultState = odcSearchRequestBuilderTask.run(flowState);
        
        // Get the output JSON
        String searchRequest = resultState.getValue(FlowStateKey.ODC_SEARCH_REQUEST);
        
        // Parse and verify the structure
        JsonNode jsonNode = objectMapper.readTree(searchRequest);
        
        // Print for manual verification
        System.out.println("Generated JSON:");
        System.out.println(objectMapper.writeValueAsString(jsonNode));
    }
    
    private DateChangeSearchRequest createSampleRequest() {
        DateChangeSearchRequest request = new DateChangeSearchRequest();
        request.setPnr("PTE26Y");
        request.setMmtId("IP423APU");
        request.setCabinClass("Y"); // Economy class
        
        // Create itinerary
        List<Itinerary> itineraries = new ArrayList<>();
        Itinerary itinerary = new Itinerary();
        itinerary.setFrom("MAA");
        itinerary.setTo("MCT");
        itinerary.setDepDate(new java.util.Date(LocalDate.of(2024, 11, 16).atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli())); // New date
        itineraries.add(itinerary);
        
        request.setItineraryList(itineraries);
        return request;
    }

    private OrderViewRS createSampleOrderViewRS() {
        OrderViewRS orderViewRS = new OrderViewRS();
        
        // Create DataLists
        DataLists dataLists = new DataLists();
        PassengerList passengerList = new PassengerList();
        List<Passenger> passengers = new ArrayList<>();
        
        // Add passengers
        passengers.add(createPassenger("ADT1", "ADT", "Mr", "RAM", "", "KUMAR", "9101305330348"));
        passengers.add(createPassenger("ADT2", "ADT", "Mr", "RAJ", "", "KUMAR", "9101305330347"));
        passengers.add(createPassenger("CHD1", "CHD", "Mr", "SHANTHA", "", "KUMAR", "9101305330349"));
        
        passengerList.setPassengers(passengers);
        dataLists.setPassengerList(passengerList);
        orderViewRS.setDataLists(dataLists);
        
        return orderViewRS;
    }

    private Passenger createPassenger(
            String passengerId, String ptc, String title, String firstName, 
            String middleName, String lastName, String documentNumber) {
        
        Passenger passenger = new Passenger();
        passenger.setPassengerID(passengerId);
        passenger.setPtc(ptc);
        passenger.setNameTitle(title);
        passenger.setFirstName(firstName);
        passenger.setMiddleName(middleName);
        passenger.setLastName(lastName);
        
        TravelDocument travelDocument = new TravelDocument();
        travelDocument.setDocumentNumber(documentNumber);
        passenger.setTravelDocument(travelDocument);
        
        return passenger;
    }
}