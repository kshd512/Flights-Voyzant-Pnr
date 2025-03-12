package com.mmt.flights.cancel.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.pnr.retrieve.response.FlightSegment;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Passenger;
import com.mmt.flights.flightsutil.AirportDetailsUtil;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelFlightDTO;
import com.mmt.flights.supply.cancel.v4.request.SupplyPnrCancelRequestDTO;
import com.mmt.flights.supply.cancel.v4.response.SupplyValidateCancelResponseDTO;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPaxInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class ValidateCancelPnrTask implements MapTask {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AirportDetailsUtil airportUtil;

    private static final String CANCELLED = "CANCELLED";
    private static final String SUSPENDED = "SUSPENDED";

    @Override
    public FlowState run(FlowState flowState) throws Exception {
        SupplyPnrCancelRequestDTO request = flowState.getValue(FlowStateKey.REQUEST);
        String supplierPNRResponse = flowState.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        
        OrderViewRS orderViewRS = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);
        validatePnr(orderViewRS, request);

        return createSuccessResponse(flowState);
    }

    private void validatePnr(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        validatePnrExists(orderViewRS);
        validatePnrNotCancelled(orderViewRS);

        //In case of partial pax cancellation, validate details for passengers
        if (!request.getRequestCore().getPaxInfoList().isEmpty()) {
            validatePassengerDetails(orderViewRS, request);
        }

        //In case of partial segment cancellation, validate details for segments
        if (!request.getRequestCore().getFlightsList().isEmpty()) {
            validateFlightDetails(orderViewRS, request);
        }
    }

    private void validatePnrExists(OrderViewRS orderViewRS) {
        if (orderViewRS == null || orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty()) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_DOES_NOT_EXIST);
        }
    }

    private void validatePnrNotCancelled(OrderViewRS orderViewRS) {
        if (orderViewRS.getOrder().stream().anyMatch(order -> CANCELLED.equalsIgnoreCase(order.getOrderStatus()))) {
            throw new PSErrorException(ErrorEnum.EXT_PNR_CANCELLED);
        }
    }

    private void validatePassengerDetails(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getPassengerList() == null) {
            throw new PSErrorException(ErrorEnum.EXT_PAX_DOES_NOT_EXIST);
        }

        List<Passenger> pnrPassengers = orderViewRS.getDataLists().getPassengerList().getPassengers();
        Map<String, Passenger> passengerMap = createPassengerMap(pnrPassengers);

        //validate if all requested passengers are present in the PNR.
        validateRequestedPassengers(request, passengerMap);

        //validate if requested passengers are not just infants only.
        validateNotOnlyInfants(request, passengerMap);
        validatePartialCancellation(request.getRequestCore().getPaxInfoCount(), pnrPassengers.size());
    }

    private Map<String, Passenger> createPassengerMap(List<Passenger> passengers) {
        Map<String, Passenger> passengerMap = new HashMap<>();
        for (Passenger pax : passengers) {
            String paxKey = (pax.getFirstName() + " " + pax.getLastName()).toLowerCase();
            passengerMap.put(paxKey, pax);
        }
        return passengerMap;
    }

    private void validateRequestedPassengers(SupplyPnrCancelRequestDTO request, Map<String, Passenger> passengerMap) {
        for (SupplyPaxInfo requestPax : request.getRequestCore().getPaxInfoList()) {
            String requestPaxKey = (requestPax.getFname() + " " + requestPax.getLname()).toLowerCase();
            if (!passengerMap.containsKey(requestPaxKey)) {
                throw new PSErrorException(ErrorEnum.EXT_PAX_DOES_NOT_EXIST);
            }
        }
    }

    private void validateNotOnlyInfants(SupplyPnrCancelRequestDTO request, Map<String, Passenger> passengerMap) {
        boolean hasNonInfant = request.getRequestCore().getPaxInfoList().stream()
            .map(pax -> (pax.getFname() + " " + pax.getLname()).toLowerCase())
            .map(passengerMap::get)
            .anyMatch(pax -> !"INF".equals(pax.getPtc()));

        if (!hasNonInfant) {
            throw new PSErrorException(ErrorEnum.EXT_ONLY_INFANT_CANCELLATION_NOT_ALLOWED);
        }
    }

    private void validatePartialCancellation(int requestPaxCount, int totalPaxCount) {
        if (requestPaxCount < totalPaxCount && requestPaxCount == totalPaxCount - 1) {
            throw new PSErrorException(ErrorEnum.INVALID_PARTIAL_PAX_CANCEL_REQUEST);
        }
    }

    private void validateFlightDetails(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getFlightSegmentList() == null) {
            throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
        }

        HashSet<String> flightNoSet = new HashSet<>();
        List<FlightSegment> pnrSegments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
        if(isFlightNotMatch(orderViewRS, request, flightNoSet)){
            throw new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST);
        }
        validateRequestedFlights(pnrSegments, request);
        validateNoShowWindow(orderViewRS, request, flightNoSet);
    }

    private void validateRequestedFlights(List<FlightSegment> pnrSegments, SupplyPnrCancelRequestDTO request) {
        for (SupplyPnrCancelFlightDTO requestFlight : request.getRequestCore().getFlightsList()) {
            FlightSegment segment = findMatchingSegment(pnrSegments, requestFlight);
            validateFlightStatus(segment);
        }
    }

    public static boolean isFlightNotMatch(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request, HashSet<String> flightNoSet) {
        if (orderViewRS.getDataLists() == null || orderViewRS.getDataLists().getFlightSegmentList() == null) {
            return true;
        }

        for (SupplyPnrCancelFlightDTO flightDTO : request.getRequestCore().getFlightsList()) {
            boolean isFlightFound = false;
            
            for (FlightSegment segment : orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment()) {
                if (flightDTO.getFrom().trim().equalsIgnoreCase(segment.getDeparture().getAirportCode().trim())
                    && flightDTO.getTo().trim().equalsIgnoreCase(segment.getArrival().getAirportCode().trim())
                    && flightDTO.getFltNo().trim().equalsIgnoreCase(segment.getMarketingCarrier().getFlightNumber().trim())
                ) {
                    flightNoSet.add(flightDTO.getFltNo().trim());
                    isFlightFound = true;
                    break;
                }
            }
            
            if (!isFlightFound) {
                return true;
            }
        }
        return false;
    }

    private FlightSegment findMatchingSegment(List<FlightSegment> segments, SupplyPnrCancelFlightDTO flight) {
        return segments.stream()
            .filter(segment -> 
                segment.getDeparture().getAirportCode().equals(flight.getFrom()) &&
                segment.getArrival().getAirportCode().equals(flight.getTo()) &&
                segment.getMarketingCarrier().getFlightNumber().equals(flight.getFltNo()))
            .findFirst()
            .orElseThrow(() -> new PSErrorException(ErrorEnum.EXT_FLIGHT_DOES_NOT_EXIST));
    }

    private void validateNoShowWindow(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request, HashSet<String> flightNoSet) {
        try {
            if (isInternational(orderViewRS) && isFourHoursToDeparture(orderViewRS, request, flightNoSet)) {
                throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
            } else if (!isInternational(orderViewRS) && isThreeHoursToDeparture(orderViewRS, request, flightNoSet)) {
                throw new PSErrorException(PSCommonErrorEnum.EXT_PNR_IN_NO_SHOW_WINDOW);
            }
        } catch (Exception e) {
            // Log error and continue
        }
    }

    private FlowState createSuccessResponse(FlowState flowState) {
        SupplyValidateCancelResponseDTO.Builder validateCancelResponse = SupplyValidateCancelResponseDTO.newBuilder();
        validateCancelResponse.setStatus(SupplyStatus.SUCCESS);
        return flowState.toBuilder()
            .addValue(FlowStateKey.RESPONSE, validateCancelResponse.build())
            .build();
    }

    public boolean isInternational(OrderViewRS orderViewRS) {
        List<FlightSegment> segments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
        for (FlightSegment segment : segments) {
            String fromCountry = airportUtil.getAirportDetailsFromCityCode(segment.getDeparture().getAirportCode()).getCountry();
            String toCountry = airportUtil.getAirportDetailsFromCityCode(segment.getArrival().getAirportCode()).getCountry();

            if (!"India".equals(fromCountry) || !"India".equals(toCountry)) {
                return true;
            }
        }
        return false;
    }

    public boolean isThreeHoursToDeparture(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request,
                                                  HashSet<String> flightNoSet) {
        List<FlightSegment> segments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
        for (FlightSegment segment : segments) {
            try {
                String timeZoneString = airportUtil.getAirportDetailsFromCityCode(segment.getDeparture().getAirportCode()).getTimeZone();
                
                // Parse departure date and time
                LocalDateTime departureDateTime = LocalDateTime.parse(
                    segment.getDeparture().getDate() + "T" + segment.getDeparture().getTime()
                );
                
                LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of(timeZoneString));
                long remainingWindow = ChronoUnit.MILLIS.between(currentDateTime, departureDateTime);

                if (request.getRequestCore().getFlightsList() != null && request.getRequestCore().getFlightsCount() > 0) {
                    if (flightNoSet.contains(segment.getMarketingCarrier().getFlightNumber().trim())) {
                        if (remainingWindow < 3 * 60 * 60 * 1000) { //evaluate for 3 hours
                            return true;
                        }
                    }
                } else {
                    if (remainingWindow < 3 * 60 * 60 * 1000) { //evaluate for 3 hours
                        return true;
                    }
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean isFourHoursToDeparture(OrderViewRS orderViewRS, SupplyPnrCancelRequestDTO request,
                                                 Set<String> flightNoSet) {
        List<FlightSegment> segments = orderViewRS.getDataLists().getFlightSegmentList().getFlightSegment();
        for (FlightSegment segment : segments) {
            try {
                String timeZoneString = airportUtil.getAirportDetailsFromCityCode(segment.getDeparture().getAirportCode()).getTimeZone();
                
                // Parse departure date and time
                LocalDateTime departureDateTime = LocalDateTime.parse(
                    segment.getDeparture().getDate() + "T" + segment.getDeparture().getTime()
                );
                
                LocalDateTime currentDateTime = LocalDateTime.now(ZoneId.of(timeZoneString));
                long remainingWindow = ChronoUnit.MILLIS.between(currentDateTime, departureDateTime);

                if (request.getRequestCore().getFlightsList() != null && request.getRequestCore().getFlightsCount() > 0) {
                    if (flightNoSet.contains(segment.getMarketingCarrier().getFlightNumber().trim())) {
                        if (remainingWindow < 4 * 60 * 60 * 1000) { //evaluate for 4 hours
                            return true;
                        }
                    }
                } else {
                    if (remainingWindow < 4 * 60 * 60 * 1000) { //evaluate for 4 hours
                        return true;
                    }
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    private void validateFlightStatus(FlightSegment segment) {
        //String flightStatus = segment.getFlightDetail().getFlightStatus().getValue();
        //if (CANCELLED.equalsIgnoreCase(flightStatus) || SUSPENDED.equalsIgnoreCase(flightStatus)) {
        //  throw new PSErrorException(ErrorEnum.EXT_SEGMENT_CANCELLED_BY_AIRLINE);
        //}
    }
}
