package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.OrderReshopRS;
import com.mmt.flights.entity.odc.OrderReshopResponse;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ODCSearchResponseAdapterTask implements MapTask {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ODCRecomBuilderOWTask owBuilder;

    @Autowired
    private ODCRecomBuilderRTTask rtBuilder;

    @Override
    public FlowState run(FlowState state) throws Exception {
        try {
            // Get OrderReshopResponse from state
            String orderReshopResponseJson = state.getValue(FlowStateKey.ODC_SEARCH_RESPONSE);
            if (orderReshopResponseJson == null) {
                throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
            }

            // Deserialize the JSON string to OrderReshopResponse
            OrderReshopResponse orderReshopResponse = objectMapper.readValue(orderReshopResponseJson, OrderReshopResponse.class);
            OrderReshopRS response = orderReshopResponse.getOrderReshopRS();

            String supplierPNRResponse = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
            OrderViewRS orderViewRS = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);
            DateChangeSearchRequest odcRequest = state.getValue(FlowStateKey.REQUEST);

            // Check if the response is successful
            if (!response.isSuccess() || response.getReshopOffers() == null || response.getReshopOffers().isEmpty()) {
                throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
            }

            // Create and populate SimpleSearchResponseV2 object based on itinerary size
            SimpleSearchResponseV2 simpleSearchResponseV2;
            
            if (odcRequest.getItineraryList().size() == 1) {
                simpleSearchResponseV2 = owBuilder.buildOW(response, orderViewRS, odcRequest);
            } else if (odcRequest.getItineraryList().size() > 1) {
                simpleSearchResponseV2 = rtBuilder.buildRT(response, orderViewRS, odcRequest);
            } else {
                throw new PSErrorException("Invalid itinerary size: " + odcRequest.getItineraryList().size(), 
                    ErrorEnum.INVALID_REQUEST);
            }

            // Add response to flow state
            return state.toBuilder()
                    .addValue(FlowStateKey.RESPONSE, simpleSearchResponseV2)
                    .build();
        } catch (Exception e) {
            throw new PSErrorException(e.getMessage(), ErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }
}