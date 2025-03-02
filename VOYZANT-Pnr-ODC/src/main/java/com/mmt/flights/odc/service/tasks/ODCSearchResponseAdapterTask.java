package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.*;
import com.mmt.flights.entity.pnr.retrieve.response.FlightSegment;
import com.mmt.flights.odc.common.ErrorDetails;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.search.Flight;
import com.mmt.flights.odc.search.Fare;
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
            Flight flight = convertToFlight(offer, request, response.getDataLists());
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

    private Flight convertToFlight(ReshopOffer offer, DateChangeSearchRequest request, DataLists dataLists) {
        Flight flight = new Flight();
        
        // Set basic flight info from the ReshopOffer
        flight.setAirline(offer.getOwner());
        flight.setAirlineName(offer.getOwnerName());
        
        // Get offer item details - use first add item since we're creating a single flight
        if (offer.getAddOfferItem() != null && !offer.getAddOfferItem().isEmpty()) {
            OfferItem offerItem = offer.getAddOfferItem().get(0);
            
            // Find flight reference from service
            String flightRef = null;
            if (offerItem.getService() != null && !offerItem.getService().isEmpty()) {
                flightRef = offerItem.getService().get(0).getFlightRefs();
            }
            
            // Set flight details from DataLists->FlightSegmentList
            if (flightRef != null && dataLists != null && dataLists.getFlightSegmentList() != null && 
                dataLists.getFlightSegmentList().getFlightSegment() != null && 
                !dataLists.getFlightSegmentList().getFlightSegment().isEmpty()) {
                
                FlightSegment segment = dataLists.getFlightSegmentList().getFlightSegment().get(0);
                
                // Set flight number
                if (segment.getMarketingCarrier() != null) {
                    flight.setFlightNumber(offer.getOwner() + "-" + segment.getMarketingCarrier().getFlightNumber());
                }
                
                // Set departure details
                if (segment.getDeparture() != null) {
                    flight.setOrigin(segment.getDeparture().getAirportCode());
                    StringBuilder depDateTime = new StringBuilder();
                    depDateTime.append(segment.getDeparture().getDate());
                    if (segment.getDeparture().getTime() != null) {
                        depDateTime.append(" ").append(segment.getDeparture().getTime());
                    }
                    flight.setDepartureTime(depDateTime.toString());
                }
                
                // Set arrival details
                if (segment.getArrival() != null) {
                    flight.setDestination(segment.getArrival().getAirportCode());
                    StringBuilder arrDateTime = new StringBuilder();
                    arrDateTime.append(segment.getArrival().getDate());
                    if (segment.getArrival().getTime() != null) {
                        arrDateTime.append(" ").append(segment.getArrival().getTime());
                    }
                    flight.setArrivalTime(arrDateTime.toString());
                }
                
                // Set stops and duration
                if (segment.getFlightDetail() != null) {
                    if (segment.getFlightDetail().getStops() != null) {
                        flight.setStops(segment.getFlightDetail().getStops().getValue());
                    }
                    if (segment.getFlightDetail().getFlightDuration() != null) {
                        flight.setDuration(segment.getFlightDetail().getFlightDuration().getValue());
                    }
                }
            }
            
            // Set fare details
            Fare fare = new Fare();
            if (offerItem.getTotalPriceDetail() != null && offerItem.getTotalPriceDetail().getTotalAmount() != null) {
                fare.setTotalFare(offerItem.getTotalPriceDetail().getTotalAmount().getBookingCurrencyPrice());
            }
            
            if (offerItem.getFareDetail() != null && offerItem.getFareDetail().getPrice() != null) {
                Price price = offerItem.getFareDetail().getPrice();
                if (price.getBookingCurrencyPrice() > 0) {
                    fare.setBaseFare(price.getBookingCurrencyPrice());
                }
            }
            
            fare.setCurrency(offer.getBookingCurrencyCode());
            flight.setFare(fare);
            
            // Set cabin class and refundable status
            flight.setCabinClass(request.getCabinClass());
            flight.setRefundable("true".equalsIgnoreCase(offerItem.getRefundable()));
            
            // Set seats available
            if (offerItem.getFareComponent() != null && !offerItem.getFareComponent().isEmpty() &&
                offerItem.getFareComponent().get(0).getFareBasis() != null &&
                offerItem.getFareComponent().get(0).getFareBasis().getSeatLeft() != null) {
                try {
                    flight.setSeatsAvailable(Integer.parseInt(
                        offerItem.getFareComponent().get(0).getFareBasis().getSeatLeft()));
                } catch (NumberFormatException e) {
                    flight.setSeatsAvailable(0);
                }
            }
        }
        
        return flight;
    }
}