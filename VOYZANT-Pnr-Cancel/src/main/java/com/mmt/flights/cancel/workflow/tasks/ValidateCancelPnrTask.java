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

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ValidateCancelPnrTask implements MapTask {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final long FOUR_HOURS_IN_MILLIS = 4 * 60 * 60 * 1000;
    private static final long THREE_HOURS_IN_MILLIS = 3 * 60 * 60 * 1000;

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

        // Validate unticket codeshare check
        if (isUnticketedCodeShare(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_NOT_TICKETED);
        }

        // Validate passengers if present in request
        if (!request.getRequestCore().getPaxInfoList().isEmpty()) {
            validatePassengers(orderViewRS, request);
        }

        // Validate flights if present in request
        if (!request.getRequestCore().getFlightsList().isEmpty()) {
            validateFlights(orderViewRS, request);
        }

        // Check balance due
        if (hasBalanceDue(orderViewRS)) {
            throw new PSErrorException(ErrorEnum.EXT_BALANCE_DUE_ERROR);
        }
    }

    private boolean isPnrCancelled(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().stream()
                .anyMatch(order -> "CANCELLED".equalsIgnoreCase(order.getOrderStatus()));
    }

    private boolean isUnticketedCodeShare(OrderViewRS orderViewRS) {
        if (orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty()) {
            return false;
        }
        
        String ticketStatus = orderViewRS.getOrder().get(0).getTicketStatus();
        return "UNTICKET".equalsIgnoreCase(ticketStatus);
    }

    private void validatePassengers(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getPassengerList() == null) {
            throw new PSErrorException(ErrorEnum.EXT_PAX_DOES_NOT_EXIST);
        }

        List<Passenger> pnrPassengers = orderViewRS.getDataLists().getPassengerList().getPassengers();
        Map<String, Passenger> passengerMap = new HashMap<>();
        
        for (Passenger pax : pnrPassengers) {
            String paxKey = (pax.getFirstName() + " " + pax.getLastName()).toLowerCase();
            passengerMap.put(paxKey, pax);
        }

        // Check if only infants are being cancelled
        boolean onlyInfants = true;
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

        // Validate partial cancellation
        if (request.getRequestCore().getPaxInfoCount() < pnrPassengers.size()) {
            validatePartialPaxCancellation(request.getRequestCore().getPaxInfoCount(), pnrPassengers.size());
        }
    }

    private void validatePartialPaxCancellation(int requestPaxCount, int totalPaxCount) {
        if (requestPaxCount == totalPaxCount - 1) {
            throw new PSErrorException(ErrorEnum.INVALID_PARTIAL_PAX_CANCEL_REQUEST);
        }
    }

    private void validateFlights(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getFlightSegmentList() == null) {
            throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
        }

        List<FlightSegment> pnrSegments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
        String validatingCarrier = request.getRequestCore().getValidatingCarrier();

        for (SupplyPnrCancelFlightDTO requestFlight : request.getRequestCore().getFlightsList()) {
            Optional<FlightSegment> matchingSegment = pnrSegments.stream()
                .filter(segment -> matchesFlightSegment(segment, requestFlight))
                .findFirst();

            if (!matchingSegment.isPresent()) {
                throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
            }

            FlightSegment segment = matchingSegment.get();

            // Check if flight is cancelled
            if ("CANCELLED".equalsIgnoreCase(segment.getFlightDetail().getFlightDuration().getValue())) {
                throw new PSErrorException(ErrorEnum.EXT_SEGMENT_CANCELLED_BY_AIRLINE);
            }

            // Check departure time for no-show window
            try {
                Date departureTime = DATE_FORMAT.parse(segment.getDeparture().getDate() + "T" + segment.getDeparture().getTime());
                Date now = new Date();
                long timeDiff = departureTime.getTime() - now.getTime();

                if ("6E".equals(validatingCarrier)) {
                    boolean isInternational = !segment.getDeparture().getAirportCode().startsWith("IN") || 
                                           !segment.getArrival().getAirportCode().startsWith("IN");
                    
                    if (isInternational && timeDiff <= FOUR_HOURS_IN_MILLIS) {
                        throw new PSErrorException(ErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
                    } else if (!isInternational && timeDiff <= THREE_HOURS_IN_MILLIS) {
                        throw new PSErrorException(ErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
                    }
                }
            } catch (Exception e) {
                // Log error and continue
            }
        }
    }

    private boolean hasBalanceDue(OrderViewRS orderViewRS) {
        return orderViewRS.getOrder().stream()
            .anyMatch(order -> {
                if (order.getTotalPrice() != null && order.getTotalPrice().getEquivCurrencyPrice() != null) {
                    return order.getTotalPrice().getEquivCurrencyPrice() > 0;
                }
                return false;
            });
    }

    private boolean matchesFlightSegment(FlightSegment segment, SupplyPnrCancelFlightDTO requestFlight) {
        return segment.getDeparture().getAirportCode().equals(requestFlight.getFrom()) &&
               segment.getArrival().getAirportCode().equals(requestFlight.getTo()) &&
               segment.getMarketingCarrier().getFlightNumber().equals(requestFlight.getFltNo());
    }
}
