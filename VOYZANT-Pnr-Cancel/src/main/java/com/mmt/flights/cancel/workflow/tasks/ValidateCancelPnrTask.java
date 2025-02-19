package com.mmt.flights.cancel.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.pnr.retrieve.response.*;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelFlightDTO;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPaxInfo;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ValidateCancelPnrTask implements MapTask {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final long FOUR_HOURS_IN_MILLIS = 4 * 60 * 60 * 1000;
    private static final long THREE_HOURS_IN_MILLIS = 3 * 60 * 60 * 1000;
    private static final Set<String> UNDO_CHECKIN_ENABLED_AIRLINES = new HashSet<>(Arrays.asList("6E", "G8"));

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
            
            // Validate all segments are present in journey
            if (!areAllSegmentsPresent(orderViewRS, request)) {
                throw new PSErrorException(ErrorEnum.EXT_MISSING_SEGMENTS_IN_JOURNEY);
            }
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

            // Check if flight is cancelled or suspended
            if ("CANCELLED".equalsIgnoreCase(segment.getFlightDetail().getFlightDuration().getValue()) ||  
                "SUSPENDED".equalsIgnoreCase(segment.getFlightDetail().getFlightDuration().getValue())) {
                throw new PSErrorException(ErrorEnum.EXT_SEGMENT_CANCELLED_BY_AIRLINE);
            }

            // Check for check-in status if airline doesn't support undo check-in
            if (!UNDO_CHECKIN_ENABLED_AIRLINES.contains(validatingCarrier) && 
                isCheckedIn(orderViewRS, segment)) {
                throw new PSErrorException(ErrorEnum.EXT_CHECKED_IN_PNR_CANCELLATION_UNSUPPORTED);
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

    private boolean isCheckedIn(OrderViewRS orderViewRS, FlightSegment segment) {
        // Check if any of the offer items indicate check-in status
        return orderViewRS.getOrder().stream()
            .filter(Objects::nonNull)
            .flatMap(order -> order.getOfferItem() != null ? order.getOfferItem().stream() : Stream.empty())
            .filter(Objects::nonNull)
            .flatMap(offerItem -> offerItem.getService() != null ? offerItem.getService().stream() : Stream.empty())
            .filter(Objects::nonNull)
            .anyMatch(service -> 
                service.getFlightRefs() != null && 
                segment != null && 
                segment.getSegmentKey() != null &&
                service.getFlightRefs().contains(segment.getSegmentKey()) && 
                service.getServiceID() != null &&
                service.getServiceID().toUpperCase().contains("CHECKED_IN"));
    }

    private boolean areAllSegmentsPresent(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        // Get all flights from request
        Set<String> requestedFlights = request.getRequestCore().getFlightsList().stream()
            .map(f -> f.getFrom() + "-" + f.getTo() + "-" + f.getFltNo())
            .collect(Collectors.toSet());

        // Check if all flight segments for each journey are included
        return orderViewRS.getDataLists().getFlightList().getFlight().stream()
            .allMatch(flight -> {
                String[] segmentRefs = flight.getSegmentReferences().split(" ");
                List<FlightSegment> segments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
                
                // Get all segments in this journey
                List<FlightSegment> journeySegments = Arrays.stream(segmentRefs)
                    .map(ref -> segments.stream()
                        .filter(s -> s.getSegmentKey().equals(ref))
                        .findFirst()
                        .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                // If any segment in journey is in request, all segments should be in request
                boolean hasRequestedSegment = journeySegments.stream()
                    .anyMatch(segment -> requestedFlights.contains(
                        segment.getDeparture().getAirportCode() + "-" + 
                        segment.getArrival().getAirportCode() + "-" + 
                        segment.getMarketingCarrier().getFlightNumber()));

                if (!hasRequestedSegment) return true;

                return journeySegments.stream()
                    .allMatch(segment -> requestedFlights.contains(
                        segment.getDeparture().getAirportCode() + "-" + 
                        segment.getArrival().getAirportCode() + "-" + 
                        segment.getMarketingCarrier().getFlightNumber()));
            });
    }
}
