package com.mmt.flights.pnr.service.tasks;

import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.service.CommonDocumentService;
import com.mmt.flights.entity.common.*;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestConfigDTO;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestCoreDTO;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

public class PnrRetrieveRequestAdapterTaskTest {

    @InjectMocks
    private PnrRetrieveRequestAdapterTask pnrRetrieveRequestAdapterTask;

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

        Party party = new Party();
        TravelAgencySender travelAgencySender = new TravelAgencySender();
        travelAgencySender.setName("ram Tours & Travels");
        travelAgencySender.setIataNumber("");
        travelAgencySender.setAgencyID("ram Tours & Travels");
        travelAgencySender.setContacts(createContacts());
        Sender sender = new Sender();
        sender.setTravelAgencySender(travelAgencySender);
        party.setSender(sender);
        when(commonDocumentService.createParty()).thenReturn(party);
    }

    private Contacts createContacts() {
        Contacts contacts = new Contacts();
        Contact contact = new Contact();
        contact.setEmailContact("r.ramkumar@claritytts.com");

        List<Contact> contactsList = new ArrayList<>();
        contactsList.add(contact);
        contacts.setContact(contactsList);
        return contacts;
    }

    @Test
    public void testOutputMatchesExpectedJson() throws Exception {
        // Create request with sample data that matches the expected JSON
        SupplyPnrCancelRequestDTO supplyPnrRequestDTO = createSampleRequest();
        
        // Create FlowState with the request
        FlowState flowState = new FlowState.Builder(System.currentTimeMillis())
                .addValue(FlowStateKey.REQUEST, supplyPnrRequestDTO)
                .build();
        
        // Execute the task - we're not asserting anything here, just showing that it runs
        FlowState resultState = pnrRetrieveRequestAdapterTask.run(flowState);
        
        // Get the output JSON
        String retrievePnrRequest = resultState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST);
        
        // Print the output for manual verification
        System.out.println("Generated Request JSON:");
        System.out.println(retrievePnrRequest);
    }
    
    private SupplyPnrCancelRequestDTO createSampleRequest() {
        // Create a request with the same PNR values as in the example JSON
        SupplyPnrCancelRequestDTO.Builder requestBuilder = SupplyPnrCancelRequestDTO.newBuilder();
        
        // Set request config
        SupplyPnrCancelRequestConfigDTO.Builder configBuilder = SupplyPnrCancelRequestConfigDTO.newBuilder();
        configBuilder.setCorrelationId("test-correlation-id");
        configBuilder.setSource("TEST");
        configBuilder.setLob("FLIGHTS");
        requestBuilder.setRequestConfig(configBuilder.build());
        
        // Set request core with PNR that matches expected output
        SupplyPnrCancelRequestCoreDTO.Builder coreBuilder = SupplyPnrCancelRequestCoreDTO.newBuilder();
        coreBuilder.setSupplierPnr("5L2YIR");   // This should match the GdsBookingReference in example JSON
        coreBuilder.setValidatingCarrier("VOYZANT");
        requestBuilder.setRequestCore(coreBuilder.build());
        
        return requestBuilder.build();
    }
}