package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class ODCSearchResponseAdapterTaskTest {

    @InjectMocks
    private ODCSearchResponseAdapterTask odcSearchResponseAdapterTask;

    private ObjectMapper objectMapper = new ObjectMapper();
    private String orderReshopResponseJson;

    @Before
    public void setUp() throws Exception {
        // Load the sample JSON from resources
        try (InputStream is = getClass().getResourceAsStream("/1.OrderReshopRS.txt")) {
            orderReshopResponseJson = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Test
    public void testConvertOrderReshopResponseToSimpleSearchResponse() throws Exception {
        // Create flow state with required values
        FlowState.Builder builder = FlowState.builder(System.currentTimeMillis());
        HashMap<String, Object> map = new HashMap<>();
        map.put(FlowStateKey.ODC_SEARCH_RESPONSE, orderReshopResponseJson);

        // Add mock request
        DateChangeSearchRequest request = new DateChangeSearchRequest();
        request.setPnr("ABC123");
        request.setSupplierCode("WY");
        request.setCabinClass("Y");
        map.put(FlowStateKey.REQUEST, request);

        // Setup mock behavior for FlowState and run the adapter
        FlowState resultState = odcSearchResponseAdapterTask.run(builder.setValueMap(map).build());
        
        // Get and print the converted response
        SimpleSearchResponseV2 response = resultState.getValue(FlowStateKey.RESPONSE);
        System.out.println("Converted Response:");
        System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));
    }
}