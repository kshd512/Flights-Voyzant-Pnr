package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.*;
import com.mmt.flights.entity.pnr.retrieve.response.Order;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class CancelPnrRequestAdapterTaskTest {

    @InjectMocks
    private CancelPnrRequestAdapterTask cancelPnrRequestAdapterTask;

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
        // Create sample OrderViewRS
        OrderViewRS orderViewRS = createSampleOrderViewRS();
        
        // Create FlowState with the PNR response
        FlowState flowState = new FlowState.Builder(System.currentTimeMillis())
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, objectMapper.writeValueAsString(orderViewRS))
                .build();
        
        // Execute the task
        FlowState resultState = cancelPnrRequestAdapterTask.run(flowState);
        
        // Get the output JSON
        String cancelPnrRequest = resultState.getValue(FlowStateKey.CANCEL_PNR_REQUEST);
        
        System.out.println(cancelPnrRequest);
    }

    private OrderViewRS createSampleOrderViewRS() {
        OrderViewRS orderViewRS = new OrderViewRS();
        List<Order> orders = new ArrayList<>();
        
        Order order = new Order();
        order.setOrderID("NB4RGG01");
        order.setGdsBookingReference("24FE3G");
        orders.add(order);
        
        orderViewRS.setOrder(orders);
        return orderViewRS;
    }
}