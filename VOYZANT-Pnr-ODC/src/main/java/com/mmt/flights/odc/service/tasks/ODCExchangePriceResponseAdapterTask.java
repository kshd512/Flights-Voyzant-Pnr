package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.OrderReshopRS;
import com.mmt.flights.entity.odc.OrderReshopResponse;
import com.mmt.flights.entity.odc.ReshopOffer;
import com.mmt.flights.entity.odc.ReshopOfferInstance;
import com.mmt.flights.odc.common.ConversionFactor;
import com.mmt.flights.odc.common.enums.Status;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentRequest;
import com.mmt.flights.odc.prepayment.DateChangePrePaymentResponse;
import com.mmt.flights.odc.prepayment.Gpm;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ODCExchangePriceResponseAdapterTask implements MapTask {

    //@Autowired
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState state) throws Exception {
        String priceResponse = state.getValue(FlowStateKey.ODC_EXCHANGE_PRICE_RESPONSE);
        DateChangePrePaymentRequest request = state.getValue(FlowStateKey.REQUEST);

        if (priceResponse == null) {
            throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
        }

        OrderReshopResponse orderReshopResponse = objectMapper.readValue(priceResponse, OrderReshopResponse.class);
        OrderReshopRS response = orderReshopResponse.getOrderReshopRS();
        DateChangePrePaymentResponse paymentResponse = new DateChangePrePaymentResponse();

        // Set PNR from request
        paymentResponse.setPnr(request.getPnr());

        // Check if the response is successful
        if (response.isSuccess() && response.getReshopOffers() != null && !response.getReshopOffers().isEmpty()) {
            paymentResponse.setStatus(Status.SUCCESS);

            // Process the first reshop offer (assuming we're dealing with the selected offer)
            ReshopOfferInstance offerInstance = response.getReshopOffers().get(0);
            if (offerInstance.getReshopOffers() != null && !offerInstance.getReshopOffers().isEmpty()) {
                ReshopOffer offer = offerInstance.getReshopOffers().get(0);
                
                // Extract fare change information from ReshopDifferential
                if (offer.getReshopDifferential() != null) {
                    // Determine if fare has changed
                    double originalTotal = offer.getReshopDifferential().getOriginalOrder().getTotalPrice().getBookingCurrencyPrice();
                    double newTotal = offer.getReshopDifferential().getNewOffer().getTotalPrice().getBookingCurrencyPrice();
                    double reshopDue = 0;
                    
                    if (offer.getReshopDifferential().getReshopDue() != null && 
                        offer.getReshopDifferential().getReshopDue().getTotalPrice() != null) {
                        reshopDue = offer.getReshopDifferential().getReshopDue().getTotalPrice().getBookingCurrencyPrice();
                    }
                    
                    boolean fareChanged = Math.abs(reshopDue) > 0.01; // Use small threshold to account for rounding
                    paymentResponse.setFareChanged(fareChanged);
                    paymentResponse.setFareChangeDiff(reshopDue);
                    
                    // Create GPM information
                    Gpm gpm = new Gpm();
                    gpm.setSellPrice((double)newTotal);
                    gpm.setCostPrice((double)originalTotal);
                    gpm.setProfitOrLoss(reshopDue);
                    gpm.setProvider(offer.getOwner());
                    //gpm.setType("EXCHANGE");
                    paymentResponse.setGpm(gpm);
                }
                
                // Create conversion factor information if available
                if (offer.getBookingToEquivExRate() > 0) {
                    List<ConversionFactor> conversionFactors = new ArrayList<>();
                    ConversionFactor factor = new ConversionFactor();
                    factor.setFromCurrency(offer.getBookingCurrencyCode());
                    factor.setToCurrency(offer.getEquivCurrencyCode());
                    factor.setRoe(offer.getBookingToEquivExRate());
                    conversionFactors.add(factor);
                    paymentResponse.setConversionFactors(conversionFactors);
                }
                
                // Create extra information map
                Map<String, Object> extraInfo = new HashMap<>();
                extraInfo.put("offerId", offer.getOfferID());
                extraInfo.put("shoppingResponseId", response.getShoppingResponseId());
                
                // Add baggage information if available
                if (offer.getBaggageAllowance() != null && !offer.getBaggageAllowance().isEmpty() && 
                    response.getDataLists() != null && 
                    response.getDataLists().getBaggageAllowanceList() != null) {
                    extraInfo.put("baggageInfo", response.getDataLists().getBaggageAllowanceList());
                }
                
                paymentResponse.setExtraInformation(extraInfo);
                
                // Add debug information if needed
                Map<String, String> debugInfo = new HashMap<>();
                debugInfo.put("apiResponse", objectMapper.writeValueAsString(response));
                paymentResponse.setDebugInfo(debugInfo);
            }
        } else {
            paymentResponse.setStatus(Status.FAILED);
        }

        return state.toBuilder()
                .addValue(FlowStateKey.RESPONSE, paymentResponse)
                .build();
    }
}