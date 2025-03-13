package com.mmt.flights.cancel.service.tasks;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.supply.cancel.v4.response.SupplyPnrCancelResponseDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VoidCancelPnrResponseAdapterTaskTest {

    @InjectMocks
    private VoidCancelPnrResponseAdapterTask task;

    private FlowState flowState;
    private String sampleVoidResponse;

    @Before
    public void setUp() {
        sampleVoidResponse = "{\"AirTicketVoidRS\":{\"Document\":{\"Name\":\"API GATEWAY\",\"ReferenceVersion\":\"1.2\"},\"Party\":{\"Sender\":{\"TravelAgencySender\":{\"Name\":\"Diva Travels\",\"IATA_Number\":\"\",\"AgencyID\":\"Diva Travels\",\"Contacts\":{\"Contact\":[{\"EmailContact\":\"vakarram@gmail.com\"}]}}}},\"ShoppingResponseId\":\"1721310336822646160\",\"Success\":{},\"Result\":{\"Status\":\"SUCCESS\",\"ErrorMessage\":\"\",\"TicketDetails\":[{\"DocumentNumber\":\"9106089897685\",\"Status\":\"SUCCESS\",\"Msg\":\"\"}],\"TktRequestId\":\"7JGHGF41\",\"ShoppingResponseId\":\"1721310336822646160\",\"BookingStatus\":\"CANCELED\"}}}";
        flowState = new FlowState.Builder(System.currentTimeMillis())
                .addValue(FlowStateKey.VOID_PNR_RESPONSE, sampleVoidResponse)
                .build();
    }

    @Test
    public void testSuccessfulVoidResponse() throws Exception {
        FlowState resultState = task.run(flowState);
        SupplyPnrCancelResponseDTO response = resultState.getValue(FlowStateKey.SUPPLY_PNR_CANCEL_RESPONSE);
        try {
            System.out.println(JsonFormat.printer().omittingInsignificantWhitespace().print(response));
        } catch (InvalidProtocolBufferException e) {
        }
    }
}