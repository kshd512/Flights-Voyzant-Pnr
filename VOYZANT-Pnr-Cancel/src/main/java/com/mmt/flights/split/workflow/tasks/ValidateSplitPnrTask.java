package com.mmt.flights.split.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Passenger;
import com.mmt.flights.entity.split.request.AirSplitPnrRequest;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidateSplitPnrTask implements MapTask {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        String splitPnrRequest = flowState.getValue(FlowStateKey.SPLIT_PNR_REQUEST);
        
        OrderViewRS orderViewRS = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);
        AirSplitPnrRequest airSplitPnrRequest = objectMapper.readValue(splitPnrRequest, AirSplitPnrRequest.class);

        validatePnrForSplit(orderViewRS, airSplitPnrRequest);

        return flowState;
    }

    private void validatePnrForSplit(OrderViewRS orderViewRS, AirSplitPnrRequest splitRequest) {
        // Check if PNR exists
        if (orderViewRS == null || orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty()) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_DOES_NOT_EXIST);
        }

        // Check if PNR is not cancelled
        if (isPnrCancelled(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_CANCELLED);
        }

        // Check if PNR is ticketed
        if (!isPnrTicketed(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_NOT_TICKETED);
        }

        // Check if PNR is not checked in
        if (isPnrCheckedIn(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_ALREADY_CHECKED_IN);
        }

        // Check if PNR has minimum required passengers for split
        validatePassengerCountForSplit(orderViewRS);

        // Validate passenger selection for split
        validatePassengerSelectionForSplit(orderViewRS, splitRequest);
    }

    private void validatePassengerSelectionForSplit(OrderViewRS orderViewRS, AirSplitPnrRequest splitRequest) {
        if (orderViewRS.getDataLists() == null || 
            orderViewRS.getDataLists().getPassengerList() == null ||
            orderViewRS.getDataLists().getPassengerList().getPassengers() == null ||
            splitRequest.getAirSplitPnrRQ() == null ||
            splitRequest.getAirSplitPnrRQ().getDataLists() == null ||
            splitRequest.getAirSplitPnrRQ().getDataLists().getPassengerList() == null ||
            splitRequest.getAirSplitPnrRQ().getDataLists().getPassengerList().getPassenger() == null) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_INVALID_PASSENGER_SELECTION);
        }

        List<Passenger> pnrPassengers = orderViewRS.getDataLists().getPassengerList().getPassengers();
        List<com.mmt.flights.entity.split.request.Passenger> selectedPassengers = 
            splitRequest.getAirSplitPnrRQ().getDataLists().getPassengerList().getPassenger();

        // Check if selected passengers exist in PNR
        Set<String> pnrPassengerIds = pnrPassengers.stream()
            .map(Passenger::getPassengerID)
            .collect(Collectors.toSet());

        boolean allPassengersExist = selectedPassengers.stream()
            .map(com.mmt.flights.entity.split.request.Passenger::getPassengerId)
            .allMatch(pnrPassengerIds::contains);

        if (!allPassengersExist) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_INVALID_PASSENGER_SELECTION);
        }

        // Ensure we're not trying to split all passengers
        if (selectedPassengers.size() >= pnrPassengers.size()) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_INVALID_PASSENGER_COUNT);
        }

        // Ensure at least one passenger remains in original PNR
        if (pnrPassengers.size() - selectedPassengers.size() < 1) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_INVALID_PASSENGER_COUNT);
        }

        // Validate infant-adult relationships are maintained
        //validateInfantAdultRelationships(pnrPassengers, selectedPassengers);
    }

    /*private void validateInfantAdultRelationships(List<Passenger> pnrPassengers,
                                                List<com.mmt.flights.entity.split.request.Passenger> selectedPassengers) {
        Set<String> selectedPassengerIds = selectedPassengers.stream()
            .map(com.mmt.flights.entity.split.request.Passenger::getPassengerId)
            .collect(Collectors.toSet());

        // Check if any infant is being split from their accompanying adult
        for (Passenger pnrPassenger : pnrPassengers) {
            if ("INF".equals(pnrPassenger.getPtc())) {
                String associatedAdultId = pnrPassenger.getAssociatedAdultID();
                if (associatedAdultId != null) {
                    boolean infantSelected = selectedPassengerIds.contains(pnrPassenger.getPassengerID());
                    boolean adultSelected = selectedPassengerIds.contains(associatedAdultId);
                    
                    // If infant is selected, their adult must also be selected (and vice versa)
                    if (infantSelected != adultSelected) {
                        throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_INVALID_PASSENGER_SELECTION);
                    }
                }
            }
        }
    }*/

    private boolean isPnrCancelled(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().stream()
                .anyMatch(order -> "CANCELLED".equalsIgnoreCase(order.getOrderStatus()));
    }

    private boolean isPnrTicketed(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().stream()
                .anyMatch(order -> "TICKETED".equalsIgnoreCase(order.getOrderStatus()));
    }

    private boolean isPnrCheckedIn(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().stream()
                .anyMatch(order -> "CHECKED_IN".equalsIgnoreCase(order.getOrderStatus()));
    }

    private void validatePassengerCountForSplit(OrderViewRS orderViewRS) {
        if (orderViewRS.getDataLists() == null || 
            orderViewRS.getDataLists().getPassengerList() == null ||
            orderViewRS.getDataLists().getPassengerList().getPassengers() == null) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_INVALID_PASSENGER_COUNT);
        }

        List<Passenger> passengers = orderViewRS.getDataLists().getPassengerList().getPassengers();
        
        // Check if PNR has at least 2 passengers for split
        if (passengers.size() < 2) {
            throw new PSErrorException(ErrorEnum.EXT_SPLIT_PNR_SINGLE_PAX);
        }

        // Additional validation for passenger types if needed
        boolean hasOnlyInfants = passengers.stream()
                .allMatch(pax -> "INF".equals(pax.getPtc()));
        
        if (hasOnlyInfants) {
            throw new PSErrorException(ErrorEnum.EXT_ONLY_INFANT_CANCELLATION_NOT_ALLOWED);
        }
    }
}