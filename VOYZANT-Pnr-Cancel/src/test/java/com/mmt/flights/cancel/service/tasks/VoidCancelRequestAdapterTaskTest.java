package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.Document;
import com.mmt.flights.entity.common.Party;
import com.mmt.flights.entity.common.Sender;
import com.mmt.flights.entity.common.TravelAgencySender;
import com.mmt.flights.entity.pnr.retrieve.response.*;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VoidCancelRequestAdapterTaskTest {

    @InjectMocks
    private VoidCancelRequestAdapterTask voidCancelRequestAdapterTask;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CommonDocumentService commonDocumentService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Mock Document creation
        Document document = new Document();
        document.setName("Diva Travels");
        document.setReferenceVersion("1.0");
        when(commonDocumentService.createDocument()).thenReturn(document);
        
        // Mock Party creation
        Party party = new Party();
        Sender sender = new Sender();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("Diva Travels");
        travelAgencySender.setIataNumber("");
        travelAgencySender.setAgencyID("");
        
        // Setup contacts
        com.mmt.flights.entity.common.Contacts contacts = new com.mmt.flights.entity.common.Contacts();
        List<com.mmt.flights.entity.common.Contact> contactList = new ArrayList<>();
        com.mmt.flights.entity.common.Contact contact = new com.mmt.flights.entity.common.Contact();
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
        //SupplyPnrCancelRequestDTO supplyPnrRequestDTO = mock(SupplyPnrCancelRequestDTO.class);
        
        // Create FlowState with the PNR response and request
        FlowState flowState = new FlowState.Builder(System.currentTimeMillis())
                .addValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE, objectMapper.writeValueAsString(orderViewRS))
                //.addValue(FlowStateKey.REQUEST, supplyPnrRequestDTO)
                .build();
        
        // Execute the task
        FlowState resultState = voidCancelRequestAdapterTask.run(flowState);
        
        // Get the output JSON
        String voidPnrRequest = resultState.getValue(FlowStateKey.VOID_PNR_REQUEST);
        System.out.println(voidPnrRequest);
    }

    private OrderViewRS createSampleOrderViewRS() {
        OrderViewRS orderViewRS = new OrderViewRS();
        
        // Set Order details
        List<Order> orders = new ArrayList<>();
        Order order = new Order();
        order.setOrderID("CE3OBNCH");
        order.setGdsBookingReference("24BMBJ");
        orders.add(order);
        orderViewRS.setOrder(orders);
        
        // Set Ticket details
        TicketDocInfos ticketDocInfos = new TicketDocInfos();
        List<TicketDocInfo> ticketDocInfoList = new ArrayList<>();
        
        TicketDocInfo ticketDocInfo = new TicketDocInfo();
        TicketDocument ticketDocument = new TicketDocument();
        ticketDocument.setTicketDocNbr("9106089897685");
        ticketDocInfo.setTicketDocument(ticketDocument);
        ticketDocInfoList.add(ticketDocInfo);
        
        ticketDocInfos.setTicketDocInfo(ticketDocInfoList);
        orderViewRS.setTicketDocInfos(ticketDocInfos);
        
        return orderViewRS;
    }
}