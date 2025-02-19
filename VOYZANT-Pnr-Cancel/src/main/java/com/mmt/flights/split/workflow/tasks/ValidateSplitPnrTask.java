package com.mmt.flights.split.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.pnr.retrieve.response.FlightSegment;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Passenger;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPaxInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ValidateSplitPnrTask implements MapTask {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelRequestDTO request = flowState.getValue(FlowStateKey.REQUEST);
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        
        OrderViewRS orderViewRS = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);
        validateRequestFromResponse(orderViewRS, request);

        SupplyValidateCancelResponseDTO.Builder validateCancelResponse = SupplyValidateCancelResponseDTO.newBuilder();
        validateCancelResponse.setStatus(SupplyStatus.SUCCESS);
        return flowState.toBuilder().addValue(FlowStateKey.RESPONSE, validateCancelResponse.build()).build();
    }

    private void validateRequestFromResponse(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        // Basic PNR existence check
        if (orderViewRS == null || orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty()) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_DOES_NOT_EXIST);
        }

        // PNR status check
        if (isPnrCancelled(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_CANCELLED);
        }

        // Check if PNR is ticketed
        if (!isPnrTicketed(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_NOT_TICKETED);
        }

        // Validate if requested passengers exist in splitted PNR
        validatePassengersInSplitPnr(orderViewRS, request);

        // Validate flights if present in request
        if (!request.getRequestCore().getFlightsList().isEmpty()) {
            validateFlights(orderViewRS, request);
        }

        // Check balance due
        if (hasBalanceDue(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_BALANCE_DUE_ERROR);
        }
    }

    private void validatePassengersInSplitPnr(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || 
            orderViewRS.getDataLists().getPassengerList() == null || 
            orderViewRS.getDataLists().getPassengerList().getPassengers() == null || 
            request.getRequestCore().getPaxInfoList().isEmpty()) {
            throw new PSErrorException(ErrorEnum.EXT_PAX_DOES_NOT_EXIST);
        }

        List<Passenger> pnrPassengers = orderViewRS.getDataLists().getPassengerList().getPassengers();
        Map<String, Passenger> passengerMap = new HashMap<>();
        boolean onlyInfants = true;

        // Create map of passengers in splitted PNR
        for (Passenger pax : pnrPassengers) {
            String paxKey = (pax.getFirstName() + " " + pax.getLastName()).toLowerCase();
            passengerMap.put(paxKey, pax);
        }

        // Verify each requested passenger exists in splitted PNR
        for (SupplyPaxInfo requestPax : request.getRequestCore().getPaxInfoList()) {
            String requestPaxKey = (requestPax.getFname() + " " + requestPax.getLname()).toLowerCase();
            Passenger pnrPax = passengerMap.get(requestPaxKey);
            
            if (pnrPax == null) {
                throw new PSErrorException(ErrorEnum.EXT_PAX_DOES_NOT_EXIST);
            }
            
            if (!"INF".equals(pnrPax.getPtc())) {
                onlyInfants = false;
            }
        }

        if (onlyInfants) {
            throw new PSErrorException(ErrorEnum.EXT_ONLY_INFANT_CANCELLATION_NOT_ALLOWED);
        }
    }

    private void validateFlights(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getFlightSegmentList() == null) {
            throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
        }

        List<FlightSegment> pnrSegments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
        for (int i = 0; i < request.getRequestCore().getFlightsList().size(); i++) {
            boolean found = false;
            for (FlightSegment segment : pnrSegments) {
                if (matchesFlightSegment(segment, request.getRequestCore().getFlightsList().get(i))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
            }
        }
    }

    private boolean matchesFlightSegment(FlightSegment segment, com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelFlightDTO flight) {
        String flightNumber = flight.getFltNo();
        String from = flight.getFrom();
        String to = flight.getTo();
        return segment.getMarketingCarrier().getFlightNumber().equals(flightNumber) &&
               segment.getDeparture().getAirportCode().equals(from) &&
               segment.getArrival().getAirportCode().equals(to);
    }

    private boolean isPnrCancelled(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().get(0).getOrderStatus() != null &&
               "CANCELLED".equalsIgnoreCase(orderViewRS.getOrder().get(0).getOrderStatus());
    }

    private boolean isPnrTicketed(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().get(0).getTicketStatus() != null &&
               "TICKETED".equalsIgnoreCase(orderViewRS.getOrder().get(0).getTicketStatus());
    }

    private boolean hasBalanceDue(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().get(0).getPaymentStatus() != null &&
               "PENDING".equalsIgnoreCase(orderViewRS.getOrder().get(0).getPaymentStatus());
    }
}