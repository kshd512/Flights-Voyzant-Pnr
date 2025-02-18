package com.mmt.flights.cancel.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.pnr.retrieve.response.FlightSegment;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Passenger;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelFlightDTO;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPaxInfo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidateCancelPnrTask implements MapTask {

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
        // Check if order exists and not already cancelled
        if (orderViewRS == null || orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty()) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_DOES_NOT_EXIST);
        }

        // Check PNR status
        if (isPnrCancelled(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_CANCELLED);
        }

        if (!request.getRequestCore().getPaxInfoList().isEmpty()) {
            validatePassengers(orderViewRS, request);
        }

        if (!request.getRequestCore().getFlightsList().isEmpty()) {
            validateFlights(orderViewRS, request);
        }
    }

    private boolean isPnrCancelled(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().stream()
                .anyMatch(order -> "CANCELLED".equalsIgnoreCase(order.getOrderStatus()));
    }

    private void validatePassengers(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getPassengerList() == null) {
            throw new PSErrorException(ErrorEnum.EXT_PAX_DOES_NOT_EXIST);
        }

        List<Passenger> pnrPassengers = orderViewRS.getDataLists().getPassengerList().getPassengers();
        Set<String> pnrPassengerNames = pnrPassengers.stream()
                .map(pax -> (pax.getFirstName() + " " + pax.getLastName()).toLowerCase())
                .collect(Collectors.toSet());

        // Validate passenger existence
        for (SupplyPaxInfo requestPax : request.getRequestCore().getPaxInfoList()) {
            String requestPaxName = (requestPax.getFname() + " " + requestPax.getLname()).toLowerCase();
            if (!pnrPassengerNames.contains(requestPaxName)) {
                throw new PSErrorException(ErrorEnum.EXT_PAX_DOES_NOT_EXIST);
            }
        }

        // Validate partial cancellation
        if (request.getRequestCore().getPaxInfoCount() < pnrPassengers.size()) {
            validatePartialPaxCancellation(request.getRequestCore().getPaxInfoCount(), pnrPassengers.size());
        }
    }

    private void validatePartialPaxCancellation(int requestPaxCount, int totalPaxCount) {
        // Validate that we're not trying to cancel everyone except one passenger
        if (requestPaxCount >= totalPaxCount || totalPaxCount - requestPaxCount < 2) {
            throw new PSErrorException(ErrorEnum.INVALID_PARTIAL_PAX_CANCEL_REQUEST);
        }
    }

    private void validateFlights(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getFlightSegmentList() == null) {
            throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
        }

        List<FlightSegment> pnrSegments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
        for (SupplyPnrCancelFlightDTO requestFlight : request.getRequestCore().getFlightsList()) {
            boolean flightFound = pnrSegments.stream()
                    .anyMatch(segment -> matchesFlightSegment(segment, requestFlight));
            
            if (!flightFound) {
                throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
            }
        }
    }

    private boolean matchesFlightSegment(FlightSegment segment, SupplyPnrCancelFlightDTO requestFlight) {
        return segment.getDeparture().getAirportCode().equals(requestFlight.getFrom()) &&
               segment.getArrival().getAirportCode().equals(requestFlight.getTo()) &&
               segment.getMarketingCarrier().getFlightNumber().equals(requestFlight.getFltNo());
    }
}
