package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.entity.odc.OrderChangeResponse;
import com.mmt.flights.entity.pnr.retrieve.response.Order;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.odc.commit.DateChangeCommitResponse;
import com.mmt.flights.odc.common.ConversionFactor;
import com.mmt.flights.odc.common.ErrorDetails;
import com.mmt.flights.odc.common.enums.Status;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ODCBookResponseAdapterTask implements MapTask {

    //@Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState state) throws Exception {
        String bookResponse = state.getValue(FlowStateKey.ODC_BOOK_RESPONSE);
        if (bookResponse == null) {
            return createErrorResponse(state, ErrorEnum.FLT_UNKNOWN_ERROR, "Book response is null");
        }

        try {
            OrderChangeResponse response = objectMapper.readValue(bookResponse, OrderChangeResponse.class);
            DateChangeCommitResponse commitResponse = new DateChangeCommitResponse();

            // Check if response has OrderViewRS
            if (response.getOrderViewRS() == null) {
                return createErrorResponse(state, ErrorEnum.FLT_UNKNOWN_ERROR, "Failed to complete the date change booking");
            }

            OrderViewRS orderViewRS = response.getOrderViewRS();
            
            // Validate order data exists
            if (orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty()) {
                return createErrorResponse(state, ErrorEnum.FLT_UNKNOWN_ERROR, "No orders found in the booking response");
            }

            // Extract order information from the first order in the list
            Order order = orderViewRS.getOrder().get(0);

            commitResponse.setPnr(order.getGdsBookingReference());
            commitResponse.setStatus(Status.SUCCESS);

            // Set ticketing required flag
            commitResponse.setIsTicketingRequired(order != null &&
                "Y".equalsIgnoreCase(order.getNeedToTicket()));

            // Add conversion factors if available
            if (order != null) {
                List<ConversionFactor> conversionFactors = new ArrayList<>();
                ConversionFactor factor = new ConversionFactor();
                factor.setFromCurrency(order.getBookingCurrencyCode());
                factor.setToCurrency(order.getEquivCurrencyCode());
                factor.setRoe(order.getBookingToEquivExRate());
                conversionFactors.add(factor);
                commitResponse.setConversionFactors(conversionFactors);
            }

            // Add extra information
            Map<String, Object> extraInfo = new HashMap<>();
            if (order != null) {
                extraInfo.put("totalPrice", order.getTotalPrice().getBookingCurrencyPrice());
                extraInfo.put("basePrice", order.getBasePrice().getBookingCurrencyPrice());
                extraInfo.put("taxPrice", order.getTaxPrice().getBookingCurrencyPrice());
            }
            if (order.getTimeLimits() != null) {
                extraInfo.put("timeLimits", order.getTimeLimits());
            }
            commitResponse.setExtraInformation(extraInfo);
            
            return state.toBuilder()
                    .addValue(FlowStateKey.RESPONSE, commitResponse)
                    .build();

        } catch (Exception e) {
            MMTLogger.error("", "Error processing book response" , this.getClass().getName(), e);
            return createErrorResponse(state, ErrorEnum.FLT_UNKNOWN_ERROR, "Error processing book response: " + e.getMessage());
        }
    }

    private FlowState createErrorResponse(FlowState state, ErrorEnum errorEnum, String message) {
        DateChangeCommitResponse commitResponse = new DateChangeCommitResponse();
        ErrorDetails error = new ErrorDetails();
        error.setErrorCode(errorEnum.getCode());
        error.setErrorMessage(message);
        commitResponse.setError(error);
        return state.toBuilder()
                .addValue(FlowStateKey.RESPONSE, commitResponse)
                .build();
    }
}