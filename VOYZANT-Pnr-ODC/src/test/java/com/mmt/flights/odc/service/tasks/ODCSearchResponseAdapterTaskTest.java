package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class ODCSearchResponseAdapterTaskTest {

    @InjectMocks
    private ODCSearchResponseAdapterTask odcSearchResponseAdapterTask;

    //@Spy
    //private ObjectMapper objectMapper = new ObjectMapper();

    private FlowState mockFlowState;
    
    private String orderReshopResponse;

    @Before
    public void setUp() throws IOException {
        // Read the OrderReshopRS.txt file
        ClassPathResource resource = new ClassPathResource("com/mmt/flights/odc/service/OrderReshopRS.txt");
        orderReshopResponse = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);

        // Create test request
        DateChangeSearchRequest request = new DateChangeSearchRequest();
        request.setPnr("ABC123");
        request.setSupplierCode("WY");
        request.setCabinClass("Y");

        FlowState.Builder builder = new FlowState.Builder(System.currentTimeMillis());

        HashMap<String, Object> map = new HashMap<>();
        map.put(FlowStateKey.ODC_SEARCH_RESPONSE, orderReshopResponse);
        map.put(FlowStateKey.REQUEST, request);

        // Setup mock behavior for FlowState
        builder.setValueMap(map);
        mockFlowState = builder.build();
    }

    @Test
    public void testConvertOrderReshopResponseToSimpleSearchResponse() throws Exception {
        // Execute the adapter
        FlowState resultState = odcSearchResponseAdapterTask.run(mockFlowState);
        
        // Get the converted response
        SimpleSearchResponseV2 convertedResponse = resultState.getValue(FlowStateKey.RESPONSE);
        
        // Print the converted response in JSON format
        //String jsonResponse = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(convertedResponse);
        System.out.println("Converted Response:");
        System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(convertedResponse));
    }
}