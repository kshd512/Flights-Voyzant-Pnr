package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.odc.common.ErrorDetails;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ODCSearchResponseAdapterTask implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public FlowState run(FlowState state) throws Exception {
        String searchResponse = state.getValue(FlowStateKey.ODC_SEARCH_RESPONSE);
        DateChangeSearchRequest request = state.getValue(FlowStateKey.REQUEST);

        if (searchResponse == null) {
            throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
        }

        OrderReshopRS response = objectMapper.readValue(searchResponse, OrderReshopRS.class);
        SimpleSearchResponseV2 searchResponseV2 = new SimpleSearchResponseV2();

        if (!response.isSuccess() || response.getReshopOffers() == null || response.getReshopOffers().isEmpty()) {
            ErrorDetails error = new ErrorDetails();
            error.setErrorCode(ErrorEnum.FLT_UNKNOWN_ERROR.getCode());
            error.setErrorMessage("No offers found for date change");
            searchResponseV2.setError(error);
            return state.toBuilder()
                    .addValue(FlowStateKey.RESPONSE, searchResponseV2)
                    .build();
        }

        // Process reshop offers
        for (ReshopOffer offer : response.getReshopOffers()) {
            Flight flight = convertToFlight(offer, request);
            searchResponseV2.getFlights().add(flight);
        }

        // Set success status and metadata
        searchResponseV2.setSuccess(true);
        searchResponseV2.setTraceId(response.getMetaData().getTraceId());
        searchResponseV2.setPnr(request.getPnr());
        searchResponseV2.setSupplierCode(request.getSupplierCode());

        return state.toBuilder()
                .addValue(FlowStateKey.RESPONSE, searchResponseV2)
                .build();
    }

    private Flight convertToFlight(ReshopOffer offer, DateChangeSearchRequest request) {
        Flight flight = new Flight();
        
        // Set basic flight info
        flight.setFlightNumber(offer.getOwner() + "-" + offer.getFlightNumber());
        flight.setAirline(offer.getOwner());
        flight.setAirlineName(offer.getOwnerName());
        
        // Set departure and arrival details
        FlightSegment segment = offer.getFlightSegment();
        flight.setOrigin(segment.getDeparture().getAirportCode());
        flight.setDestination(segment.getArrival().getAirportCode());
        flight.setDepartureTime(segment.getDeparture().getDate() + " " + segment.getDeparture().getTime());
        flight.setArrivalTime(segment.getArrival().getDate() + " " + segment.getArrival().getTime());
        
        // Set fare details
        Fare fare = new Fare();
        fare.setBaseFare(offer.getBasePrice().getBookingCurrencyPrice());
        fare.setTotalTax(offer.getTaxPrice().getBookingCurrencyPrice());
        fare.setTotalFare(offer.getTotalPrice().getBookingCurrencyPrice());
        fare.setCurrency(offer.getBookingCurrencyCode());
        flight.setFare(fare);
        
        // Set cabin class and availability
        flight.setCabinClass(request.getCabinClass());
        flight.setSeatsAvailable(Integer.parseInt(segment.getFareBasis().getSeatLeft()));
        
        // Set additional info
        flight.setRefundable("true".equalsIgnoreCase(offer.getRefundable()));
        flight.setStops(segment.getFlightDetail().getStops().getValue());
        flight.setDuration(segment.getFlightDetail().getFlightDuration().getValue());
        
        return flight;
    }
}