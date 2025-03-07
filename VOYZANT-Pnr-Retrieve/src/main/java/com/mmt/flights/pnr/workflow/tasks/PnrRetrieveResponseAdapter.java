package com.mmt.flights.pnr.workflow.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.CommonConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.exceptions.ServiceErrorException;
import com.mmt.flights.common.util.AdapterUtil;
import com.mmt.flights.common.util.IPAddressUtil;
import com.mmt.flights.common.util.JaxbHandlerService;
import com.mmt.flights.entity.cms.CMSMapHolder;
import com.mmt.flights.entity.pnr.retrieve.response.*;
import com.mmt.flights.supply.book.v4.common.SupplyFlightDetailDTO;
import com.mmt.flights.supply.book.v4.request.SupplyContactInfo;
import com.mmt.flights.supply.book.v4.request.SupplyGSTInfo;
import com.mmt.flights.supply.book.v4.response.*;
import com.mmt.flights.supply.common.SupplyTcsStatus;
import com.mmt.flights.supply.common.enums.*;
import com.mmt.flights.supply.common.enums.SupplyPnrStatusTypeOuterClass.SupplyPnrStatusType;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import io.grpc.xds.shaded.io.envoyproxy.envoy.api.v2.core.ApiVersion;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Adapter for processing PNR retrieve responses from supplier
 */
@Component
public class PnrRetrieveResponseAdapter implements MapTask {
    private static final Logger LOG = LoggerFactory.getLogger(PnrRetrieveResponseAdapter.class);
    private static final DateTimeFormatter INPUT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Locale.US);
    private static final DateTimeFormatter OUTPUT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withLocale(Locale.US);
    private static final String TIME_DELIMITER = " ";
    private static final String T_DELIMITER = "T";
    private static final String DEFAULT_FARE_FAMILY = "FLEXI PLUS";
    private static final int DEFAULT_MAP_CAPACITY = 16;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JaxbHandlerService jaxbHandlerService;

    @Override
    public FlowState run(FlowState state) throws Exception {
        SupplyPnrRequestDTO supplyPnrRequest = state.getValue(FlowStateKey.REQUEST);
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        OrderViewRS orderViewRS = objectMapper.readValue(pnrResponseData, OrderViewRS.class);
        CMSMapHolder cmsMapHolder = state.getValue(FlowStateKey.CMS_MAP);

        validatePnrResponse(orderViewRS);

        return state.toBuilder()
                .addValue(FlowStateKey.RESPONSE, getResponse(supplyPnrRequest, orderViewRS, cmsMapHolder, 0L, state))
                .build();
    }

    private void validatePnrResponse(OrderViewRS orderViewRS) {
        if (orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty() ||
                StringUtils.isEmpty(orderViewRS.getOrder().get(0).getOrderID())) {
            throw new ServiceErrorException("PNR_NOT_VALID", ErrorEnum.PNR_PARTIALLY_REFUNDED, HttpStatus.BAD_REQUEST);
        }
    }

    private SupplyBookingResponseDTO getResponse(SupplyPnrRequestDTO supplyPnrRequestDTO,
                                                 OrderViewRS orderViewRS,
                                                 CMSMapHolder cmsMapHolder,
                                                 long supplierLatency,
                                                 FlowState state) {
        ApiVersion version = state.getValue(FlowStateKey.VERSION);
        SupplyBookingResponseDTO.Builder builder = SupplyBookingResponseDTO.newBuilder();
        Order order = orderViewRS.getOrder().get(0);
        DataLists dataLists = orderViewRS.getDataLists();

        // Initialize segment reference map
        Map<String, String> segmentRefMap = new HashMap<>(DEFAULT_MAP_CAPACITY);

        // Process flight segments
        processFlightSegments(builder, dataLists, order, segmentRefMap);

        // Set booking information
        builder.setBookingInfo(getBookingInfo(order, dataLists, segmentRefMap, 0, version));

        // Set metadata
        builder.setMetaData(getMetaData(order, cmsMapHolder.getCmsId(), supplierLatency, state,
                supplyPnrRequestDTO.getEnableTrace()));

        builder.setStatus(SupplyStatus.SUCCESS);

        return builder.build();
    }

    /**
     * Process flight segments and populate the response builder
     */
    private void processFlightSegments(SupplyBookingResponseDTO.Builder builder, DataLists dataLists,
                                       Order order, Map<String, String> segmentRefMap) {
        if (dataLists == null || dataLists.getFlightSegmentList() == null || 
            dataLists.getFlightSegmentList().getFlightSegment() == null) {
            return;
        }

        Map<String, String> flightKeyMap = new HashMap<>(DEFAULT_MAP_CAPACITY);
        int segmentId = 0;
        int journeyId = 0;

        for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
            SupplyFlightDTO flight = getSimpleFlight(segment, segmentId, journeyId);
            String fltkey = AdapterUtil.getJourneyKey(Collections.singletonList(flight));

            builder.putFlightLookUpList(fltkey, flight);
            flightKeyMap.put(flight.getDepInfo().getArpCd() + flight.getArrInfo().getArpCd(), fltkey);
            processTechnicalStops(flight, flightKeyMap);
            segmentRefMap.put(segment.getSegmentKey(), fltkey);
            segmentId++;
            journeyId++;
        }

        builder.setPnrStatus(SupplyPnrStatusType.ACTIVE)
               .setGstInfo(getGSTInfo(order))
               .setContactInfo(getContactInfo(dataLists));
    }

    /**
     * Process technical stops for a flight
     */
    private void processTechnicalStops(SupplyFlightDTO flight, Map<String, String> flightKeyMap) {
        if (flight.getTchStpList() == null || flight.getTchStpList().isEmpty()) {
            return;
        }

        String lastAirport = flight.getDepInfo().getArpCd();
        String fltkey = AdapterUtil.getJourneyKey(Collections.singletonList(flight));

        for (SupplyTechnicalStopDTO techStop : flight.getTchStpList()) {
            String currentAirPort = techStop.getLocInfo().getArpCd();
            flightKeyMap.put(lastAirport + currentAirPort, fltkey);
            lastAirport = currentAirPort;
        }
        flightKeyMap.put(lastAirport + flight.getArrInfo().getArpCd(), fltkey);
    }

    private SupplyFlightDTO getSimpleFlight(FlightSegment segment, int segmentId, int journeyId) {
        SupplyFlightDTO.Builder builder = SupplyFlightDTO.newBuilder();

        // Set departure and arrival info
        setFlightDepartureInfo(builder, segment.getDeparture());
        setFlightArrivalInfo(builder, segment.getArrival());

        // Set carrier info
        setFlightCarrierInfo(builder, segment);

        // Set equipment and duration
        setFlightEquipmentInfo(builder, segment);

        // Set segment identifiers
        builder.setSuppSegKey(segment.getSegmentKey())
                .setSuppid(String.valueOf(segmentId))
                .setMarriedSegId(String.valueOf(journeyId));

        // Set cabin and booking class
        if (segment.getCode() != null) {
            builder.setClassOfService(segment.getCode().getMarriageGroup());
        }

        return builder.build();
    }

    /**
     * Sets departure airport and time information for a flight
     */
    private void setFlightDepartureInfo(SupplyFlightDTO.Builder builder, AirportInfo departure) {
        if (departure == null) {
            return;
        }

        SupplyLocationInfoDTO.Builder depBuilder = SupplyLocationInfoDTO.newBuilder()
                .setArpCd(departure.getAirportCode())
                .setArpNm(departure.getAirportName());

        if (departure.getTerminal() != null) {
            depBuilder.setTrmnl(departure.getTerminal().getName());
        }

        String formattedTime = formatFlightTime(departure.getTime());
        builder.setDepTime(departure.getDate() + TIME_DELIMITER + formattedTime)
                .setDepInfo(depBuilder.build());
    }

    /**
     * Sets arrival airport and time information for a flight
     */
    private void setFlightArrivalInfo(SupplyFlightDTO.Builder builder, AirportInfo arrival) {
        if (arrival == null) {
            return;
        }

        SupplyLocationInfoDTO.Builder arrBuilder = SupplyLocationInfoDTO.newBuilder()
                .setArpCd(arrival.getAirportCode())
                .setArpNm(arrival.getAirportName());

        if (arrival.getTerminal() != null) {
            arrBuilder.setTrmnl(arrival.getTerminal().getName());
        }

        String formattedTime = formatFlightTime(arrival.getTime());
        builder.setArrTime(arrival.getDate() + TIME_DELIMITER + formattedTime)
                .setArrInfo(arrBuilder.build());
    }

    /**
     * Sets marketing and operating carrier information
     */
    private void setFlightCarrierInfo(SupplyFlightDTO.Builder builder, FlightSegment segment) {
        if (segment == null) {
            return;
        }

        Carrier marketingCarrier = segment.getMarketingCarrier();
        if (marketingCarrier != null) {
            builder.setMrkAl(marketingCarrier.getAirlineID())
                    .setFltNo(marketingCarrier.getFlightNumber().replaceAll("\\s", ""));

            Carrier operatingCarrier = segment.getOperatingCarrier();
            builder.setOprAl(operatingCarrier != null ?
                    operatingCarrier.getAirlineID() : marketingCarrier.getAirlineID());
        }
    }

    /**
     * Sets equipment information for a flight
     */
    private void setFlightEquipmentInfo(SupplyFlightDTO.Builder builder, FlightSegment segment) {
        if (segment != null && segment.getEquipment() != null) {
            builder.setArcrfTyp(segment.getEquipment().getAircraftCode());
        }
    }

    /**
     * Formats a flight time string from HH:mm:ss to HH:mm format
     * Falls back to substring extraction if parsing fails
     */
    private String formatFlightTime(String timeString) {
        if (StringUtils.isEmpty(timeString)) {
            return "";
        }

        try {
            LocalTime time = LocalTime.parse(timeString, INPUT_TIME_FORMATTER);
            return time.format(OUTPUT_TIME_FORMATTER);
        } catch (Exception e) {
            // Just return original string if parsing fails
            return timeString;
        }
    }

    private SupplyGSTInfo getGSTInfo(Order order) {
        return SupplyGSTInfo.newBuilder().build();
    }

    private SupplyContactInfo getContactInfo(DataLists dataLists) {
        SupplyContactInfo.Builder builder = SupplyContactInfo.newBuilder();
        if (dataLists != null) {
            if (dataLists.getContactNumber() != null && !dataLists.getContactNumber().isEmpty()) {
                builder.setMobileNumber(dataLists.getContactNumber().get(0));
            }
            if (dataLists.getContactEmail() != null && !dataLists.getContactEmail().isEmpty()) {
                builder.setEmailId(dataLists.getContactEmail().get(0));
            }
        }
        return builder.build();
    }

    private SupplyBookingResponseMetaDataDTO getMetaData(Order order, String cmsId, long supplierLatency,
                                                         FlowState state, boolean enableTrace) {
        SupplyBookingResponseMetaDataDTO.Builder builder = SupplyBookingResponseMetaDataDTO.newBuilder()
                .setCurrency(order.getBookingCurrencyCode())
                .setServiceName(CommonConstants.SERVICE_NAME)
                .setSupplierName(CommonConstants.SUPPLIER_NAME)
                .setCredentialId(cmsId)
                .setSupplierLatency(String.valueOf(supplierLatency))
                .setIpAddress(IPAddressUtil.getIPAddress());

        if (enableTrace) {
            addTraceInfo(builder, state);
        }

        return builder.build();
    }

    private void addTraceInfo(SupplyBookingResponseMetaDataDTO.Builder builder, FlowState state) {
        try {
            builder.putTraceInfo("Request",
                    jaxbHandlerService.marshall(state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST)));
            builder.putTraceInfo("Response", state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE));
        } catch (Exception e) {
            LOG.warn("Failed to add trace information", e);
        }
    }

    private SupplyBookingInfoDTO getBookingInfo(Order order, DataLists dataLists,
                                                Map<String, String> segmentRefMap,
                                                int pnrGroupNo,
                                                ApiVersion version) {
        SupplyBookingInfoDTO.Builder builder = SupplyBookingInfoDTO.newBuilder();

        List<SupplyBookingJourneyDTO> journeys = getJourneys(dataLists, segmentRefMap, pnrGroupNo);
        builder.addAllJourneys(journeys);

        // Only collect flight to journey mapping if version is not null
        if (version != null) {
            buildFlightToJourneyMap(journeys);
        }

        builder.setFrInfo(getFareInfo(String.valueOf(pnrGroupNo), order, dataLists, segmentRefMap))
                .setPaxSegmentInfo(getPaxSegmentInfo(dataLists));

        return builder.build();
    }

    private Map<String, String> buildFlightToJourneyMap(List<SupplyBookingJourneyDTO> journeys) {
        Map<String, String> flightToJourneyMap = new HashMap<>(DEFAULT_MAP_CAPACITY);
        for (SupplyBookingJourneyDTO journey : journeys) {
            String journeyKey = journey.getJrnyKey();
            for (SupplyFlightDetailDTO flight : journey.getFlightDtlsInfoList()) {
                flightToJourneyMap.put(flight.getFltLookUpKey(), journeyKey);
            }
        }
        return flightToJourneyMap;
    }

    private List<SupplyBookingJourneyDTO> getJourneys(DataLists dataLists,
                                                      Map<String, String> segmentRefMap,
                                                      int pnrGroupNo) {
        if (dataLists == null || dataLists.getFlightList() == null) {
            return Collections.emptyList();
        }

        FlightList flightList = dataLists.getFlightList();
        if (flightList.getFlight() == null) {
            return Collections.emptyList();
        }

        return flightList.getFlight().stream()
                .map(flight -> getJourneyFromFlight(flight, dataLists, segmentRefMap, pnrGroupNo))
                .collect(Collectors.toList());
    }

    private SupplyBookingJourneyDTO getJourneyFromFlight(Flight flight,
                                                         DataLists dataLists,
                                                         Map<String, String> segmentRefMap,
                                                         int pnrGroupNo) {
        SupplyBookingJourneyDTO.Builder builder = SupplyBookingJourneyDTO.newBuilder();

        // Find segments and collect keys
        SegmentBoundaryInfo segmentInfo = findJourneySegments(flight, dataLists, segmentRefMap);

        if (segmentInfo.hasValidBoundaries()) {
            // Set departure and arrival info
            String depFormattedTime = formatFlightTime(segmentInfo.firstSegment.getDeparture().getTime());
            builder.setDepDate(segmentInfo.firstSegment.getDeparture().getDate() + TIME_DELIMITER + depFormattedTime);

            String arrFormattedTime = formatFlightTime(segmentInfo.lastSegment.getArrival().getTime());
            builder.setArrDate(segmentInfo.lastSegment.getArrival().getDate() + TIME_DELIMITER + arrFormattedTime);

            // Add flight details
            for (String lookupKey : segmentInfo.segmentKeys) {
                builder.addFlightDtlsInfo(getFlightDetailsInfo(lookupKey, pnrGroupNo));
            }

            // Set journey key
            builder.setJrnyKey(String.join("|", segmentInfo.segmentKeys));
        }

        return builder.build();
    }

    /**
     * Helper class to store segment boundary information
     */
    private static class SegmentBoundaryInfo {
        FlightSegment firstSegment;
        FlightSegment lastSegment;
        final List<String> segmentKeys = new ArrayList<>();

        boolean hasValidBoundaries() {
            return firstSegment != null && lastSegment != null;
        }
    }

    /**
     * Find first and last segments and collect segment keys
     */
    private SegmentBoundaryInfo findJourneySegments(Flight flight, DataLists dataLists, Map<String, String> segmentRefMap) {
        SegmentBoundaryInfo result = new SegmentBoundaryInfo();
        if (flight == null || StringUtils.isEmpty(flight.getSegmentReferences())) {
            return result;
        }

        String[] segmentRefs = flight.getSegmentReferences().split("\\s+");

        for (String segmentRef : segmentRefs) {
            FlightSegment segment = findSegmentByRef(dataLists, segmentRef);
            if (segment != null) {
                if (result.firstSegment == null) {
                    result.firstSegment = segment;
                }
                result.lastSegment = segment;

                String lookupKey = segmentRefMap.get(segmentRef);
                if (lookupKey != null) {
                    result.segmentKeys.add(lookupKey);
                } else {
                    lookupKey = buildFlightKey(segment);
                    result.segmentKeys.add(lookupKey);
                }
            }
        }

        return result;
    }

    private String buildFlightKey(String depCode, String arrCode, String depDate,
                                  String depTime, String airlineCode, String flightNumber) {
        return String.format("%s$%s$%s %s$%s-%s",
                depCode, arrCode, depDate, depTime, airlineCode, flightNumber);
    }

    private FlightSegment findSegmentByRef(DataLists dataLists, String segmentRef) {
        if (dataLists == null || dataLists.getFlightSegmentList() == null ||
                dataLists.getFlightSegmentList().getFlightSegment() == null) {
            return null;
        }

        for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
            if (segmentRef.equals(segment.getSegmentKey())) {
                return segment;
            }
        }
        return null;
    }

    private SupplyPnrFareInfoDTO.Builder buildBasicFareInfo(Order order) {
        SupplyPnrFareInfoDTO.Builder fareInfoBuilder = SupplyPnrFareInfoDTO.newBuilder()
                .setStatus(SupplyStatus.SUCCESS)
                .setRfndStatus(SupplyRefundStatusDTO.RS_NOT_SET)
                .setPnrKey("")
                .setSPnr(order.getOrderID())
                .setAPnr(order.getGdsBookingReference())
                .setValidatingCarrier(order.getOwner())
                .setFareFamily(DEFAULT_FARE_FAMILY)
                .setAccountCode("")
                .setMaxTicketingTime("")
                .setTicketDelayInterval(0)
                .setIsCouponFare(false)
                .setFareType(SupplyFareType.REGULAR)
                .setTcsStatus(SupplyTcsStatus.TCS_NOT_SET)
                .setTimeZoneOffset("")
                .addAllTicketInfos(Collections.emptyList())
                .putAllScheduleChangeInfo(Collections.emptyMap());

        // Format creation date
        String existingCreationDate = "";
        if (order.getTimeLimits() != null) {
            existingCreationDate = order.getTimeLimits().getOfferExpirationDateTime();
            if (existingCreationDate != null && existingCreationDate.contains(T_DELIMITER) &&
                    existingCreationDate.length() >= 16) {
                existingCreationDate = existingCreationDate.substring(0, 10) + TIME_DELIMITER +
                        existingCreationDate.substring(11, 16);
            }
        }
        fareInfoBuilder.setCreationDate(existingCreationDate);

        return fareInfoBuilder;
    }

    private SupplyFareDetailDTO.Builder initializeFareDetailBuilder() {
        return SupplyFareDetailDTO.newBuilder()
                .setBs(0)
                .setTot(0)
                .setTx(0);
    }

    private SupplyFareInfoDTO getFareInfo(String pnrGroupNo, Order order, DataLists dataLists,
                                          Map<String, String> segmentRefMap) {
        SupplyFareInfoDTO.Builder builder = SupplyFareInfoDTO.newBuilder();
        SupplyPnrFareInfoDTO.Builder fareInfoBuilder = buildBasicFareInfo(order);

        if (order.getOfferItem() != null && !order.getOfferItem().isEmpty()) {
            // Process offer items and calculate fares
            FareCalculationResult calculationResult = calculateFaresFromOfferItems(order, dataLists, segmentRefMap);

            // Build individual passenger fare details
            buildPassengerFareDetails(fareInfoBuilder, calculationResult);

            // Build total fare info
            buildTotalFareInfo(fareInfoBuilder, calculationResult);

            // Add traveler addons and info
            fareInfoBuilder.putTravelerAddons("0", buildTravelerAddons(dataLists));
            addTravelerInfos(fareInfoBuilder, dataLists);
        }

        builder.putPnrGrpdFrInfo(Integer.parseInt(pnrGroupNo), fareInfoBuilder.build());
        return builder.build();
    }

    /**
     * Helper class to store fare calculation results
     */
    private static class FareCalculationResult {
        // Passenger type specific data
        final Map<String, Set<String>> paxTypeRefsMap = new HashMap<>();
        final Map<String, SupplyFareDetailDTO.Builder> paxTypeFareBuilderMap = new HashMap<>();
        final Map<String, Map<String, Double>> paxTypeTaxBreakupMap = new HashMap<>();

        // Order total data
        double totalBs = 0.0;
        double totalTx = 0.0;
        double totalAmount = 0.0;
        final Map<String, Double> orderTotalTaxBreakupMap = new ConcurrentHashMap<>();
    }

    /**
     * Calculate fares from offer items
     */
    private FareCalculationResult calculateFaresFromOfferItems(Order order, DataLists dataLists,
                                                               Map<String, String> segmentRefMap) {
        FareCalculationResult result = new FareCalculationResult();

        for (OfferItem offerItem : order.getOfferItem()) {
            if (offerItem == null) {
                continue;
            }

            // Get passenger type and references
            String paxType = offerItem.getPassengerType();
            if (StringUtils.isBlank(paxType)) {
                continue;
            }

            collectPassengerReferences(offerItem, paxType, result.paxTypeRefsMap);

            // Get fare detail builder
            SupplyFareDetailDTO.Builder fareDetailBuilder = result.paxTypeFareBuilderMap.computeIfAbsent(
                    paxType, k -> initializeFareDetailBuilder());

            // Process fare details if available
            if (offerItem.getFareDetail() != null && offerItem.getFareDetail().getPrice() != null) {
                processFarePrice(offerItem.getFareDetail().getPrice(), paxType, fareDetailBuilder,
                        result.paxTypeRefsMap, result.paxTypeTaxBreakupMap, result);
            }

            // Process segments and fare components
            processOfferItemFareComponents(offerItem, dataLists, segmentRefMap, fareDetailBuilder);
        }

        return result;
    }

    /**
     * Collect passenger references from offer item
     */
    private void collectPassengerReferences(OfferItem offerItem, String paxType, Map<String, Set<String>> paxTypeRefsMap) {
        Set<String> paxRefSet = paxTypeRefsMap.computeIfAbsent(paxType, k -> new HashSet<>());
        
        if (offerItem.getFareDetail() != null && StringUtils.isNotBlank(offerItem.getFareDetail().getPassengerRefs())) {
            Arrays.stream(offerItem.getFareDetail().getPassengerRefs().split("\\s+"))
                  .map(String::trim)
                  .forEach(paxRefSet::add);
        } else if (offerItem.getPassengerQuantity() != null && offerItem.getPassengerQuantity() > 0) {
            IntStream.rangeClosed(1, offerItem.getPassengerQuantity())
                     .mapToObj(i -> paxType + i)
                     .forEach(paxRefSet::add);
        }
    }

    /**
     * Process fare price information
     * Modified to correctly handle passenger quantities and per-passenger fare calculation
     */
    private void processFarePrice(Price price, String paxType, SupplyFareDetailDTO.Builder fareDetailBuilder,
                                  Map<String, Set<String>> paxTypeRefsMap,
                                  Map<String, Map<String, Double>> paxTypeTaxBreakupMap,
                                  FareCalculationResult result) {
        if (price == null || price.getBaseAmount() == null || price.getTaxAmount() == null ||
                price.getTotalAmount() == null) {
            return;
        }

        // Get passenger count from passenger references
        Set<String> uniqueRefs = paxTypeRefsMap.getOrDefault(paxType, Collections.emptySet());
        int paxCount = uniqueRefs.size();
        
        if (paxCount == 0) {
            // Safety check to prevent division by zero
            return;
        }

        // Total amounts from offer item (already includes all passengers of this type)
        double totalBaseAmount = price.getBaseAmount().getBookingCurrencyPrice();
        double totalTaxAmount = price.getTaxAmount().getBookingCurrencyPrice();
        double totalAmountAllPax = price.getTotalAmount().getBookingCurrencyPrice();
        
        // Calculate per passenger amounts
        double baseAmountPerPax = totalBaseAmount / paxCount;
        double taxAmountPerPax = totalTaxAmount / paxCount;
        double totalAmountPerPax = totalAmountAllPax / paxCount;

        // Set per passenger amounts to fare detail builder
        fareDetailBuilder.setBs(baseAmountPerPax)
                .setTx(taxAmountPerPax)
                .setTot(totalAmountPerPax)
                .setNoOfPax(paxCount);

        // Accumulate order totals
        result.totalBs += totalBaseAmount;
        result.totalTx += totalTaxAmount;
        result.totalAmount += totalAmountAllPax;

        // Process tax breakups
        processTaxBreakups(price.getTaxes(), paxType, paxTypeTaxBreakupMap, result.orderTotalTaxBreakupMap, paxCount);
    }

    /**
     * Process tax breakups from price
     */
    private void processTaxBreakups(List<Tax> taxes, String paxType,
                                    Map<String, Map<String, Double>> paxTypeTaxBreakupMap,
                                    Map<String, Double> orderTotalTaxBreakupMap,
                                    int paxCount) {
        if (taxes == null || taxes.isEmpty() || paxCount == 0) {
            return;
        }

        Map<String, Double> taxBreakupMap = paxTypeTaxBreakupMap.computeIfAbsent(paxType, k -> new HashMap<>());

        for (Tax tax : taxes) {
            if (tax == null || StringUtils.isEmpty(tax.getTaxCode())) {
                continue;
            }

            String taxCode = tax.getTaxCode();
            double totalTaxAmount = tax.getBookingCurrencyPrice();
            
            // Store per passenger tax amount
            taxBreakupMap.put(taxCode, totalTaxAmount / paxCount);
            // Store total tax amount
            orderTotalTaxBreakupMap.put(taxCode, totalTaxAmount);
        }
    }

    /**
     * Build passenger fare details from calculation results
     */
    private void buildPassengerFareDetails(SupplyPnrFareInfoDTO.Builder fareInfoBuilder, FareCalculationResult result) {
        result.paxTypeFareBuilderMap.forEach((paxType, fareDetailBuilder) -> {
            Set<String> uniqueRefs = result.paxTypeRefsMap.getOrDefault(paxType, Collections.emptySet());
            fareDetailBuilder.setNoOfPax(uniqueRefs.size());
            fareDetailBuilder.setAirlineFixedFee(0.0);
            fareDetailBuilder.addAllAirlineFixedFees(Collections.emptyList());
            
            // Clear existing tax breakups and add accumulated ones
            fareDetailBuilder.clearTaxBreakups();
            Map<String, Double> taxBreakupMap = result.paxTypeTaxBreakupMap.get(paxType);
            if (taxBreakupMap != null) {
                addTaxBreakupsToBuilder(fareDetailBuilder, taxBreakupMap);
            }
            
            fareInfoBuilder.putPaxFares(paxType, fareDetailBuilder.build());
        });
    }

    /**
     * Add tax breakups to a builder
     */
    private void addTaxBreakupsToBuilder(SupplyFareDetailDTO.Builder builder, Map<String, Double> taxBreakupMap) {
        taxBreakupMap.forEach((code, amount) -> 
            builder.addTaxBreakups(SupplyTaxBreakupDTO.newBuilder()
                .setAmnt(amount)
                .setCode(code)
                .setMsg("")
                .build()));
    }

    /**
     * Build total fare info from calculation results
     */
    private void buildTotalFareInfo(SupplyPnrFareInfoDTO.Builder fareInfoBuilder, FareCalculationResult result) {
        SupplyTotalFareDTO totFare = SupplyTotalFareDTO.newBuilder()
            .setBs(result.totalBs)
            .setTx(result.totalTx)
            .setTot(result.totalAmount)
            .setAirlineFixedFee(0.0)
            .build();
        
        fareInfoBuilder.setTotFr(totFare);
    }

    /**
     * Process offer item fare components
     * Modified to handle multi-segment journeys with a single fare
     */
    private void processOfferItemFareComponents(OfferItem offerItem, DataLists dataLists,
                                      Map<String, String> segmentRefMap,
                                      SupplyFareDetailDTO.Builder fareDetailBuilder) {
        if (offerItem.getFareComponent() == null || offerItem.getFareComponent().isEmpty()) {
            return;
        }
        
        String flightRef = getFlightRefForOfferItem(offerItem);
        Flight flight = flightRef == null ? null : findFlightByRef(dataLists, flightRef);
        if (flight == null || StringUtils.isEmpty(flight.getSegmentReferences())) {
            return;
        }

        // Process all segments in the flight
        String[] segmentRefs = flight.getSegmentReferences().split("\\s+");
        for (int i = 0; i < segmentRefs.length; i++) {
            String segmentRef = segmentRefs[i];
            FlightSegment segment = findSegmentByRef(dataLists, segmentRef);
            
            if (segment != null && segmentRefMap.containsKey(segmentRef)) {
                fareDetailBuilder.putSegPrdctInfo(
                    segmentRefMap.get(segmentRef),
                    createSegmentProductInfo(offerItem, i, offerItem.getFareDetail().getPrice()).build()
                );
            }
        }
    }

    private String getFlightRefForOfferItem(OfferItem offerItem) {
        if (offerItem.getService() != null && !offerItem.getService().isEmpty()) {
            return offerItem.getService().get(0).getFlightRefs();
        }
        return null;
    }

    private Flight findFlightByRef(DataLists dataLists, String flightRef) {
        if (dataLists.getFlightList() != null) {
            for (Flight flight : dataLists.getFlightList().getFlight()) {
                if (flightRef.equals(flight.getFlightKey())) {
                    return flight;
                }
            }
        }
        return null;
    }

    private SupplyPaxSegmentInfo getPaxSegmentInfo(DataLists dataLists) {
        SupplyPaxSegmentInfo.Builder builder = SupplyPaxSegmentInfo.newBuilder();
        
        // Only create status if there are flight segments
        if (dataLists != null && dataLists.getFlightSegmentList() != null 
            && !dataLists.getFlightSegmentList().getFlightSegment().isEmpty()) {
            
            SupplyPnrStatusDTO.Builder statusBuilder = SupplyPnrStatusDTO.newBuilder();
            
            for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
                // Only add segment if it has relevant status data
                if (segment != null && segment.getSegmentKey() != null) {
                    String segmentKey = segment.getSegmentKey();
                    // Here you can add additional checks for actual lift status data
                    // if such data exists in your FlightSegment object
                    
                    // For now, only add if segment key is valid
                    if (StringUtils.isNotEmpty(segmentKey)) {
                        SupplyPnrLiftStatusDTOList.Builder liftStatusList = SupplyPnrLiftStatusDTOList.newBuilder();
                        statusBuilder.putSegmentLiftStatus(segmentKey, liftStatusList.build());
                    }
                }
            }
        }
        
        return builder.build();
    }

    private SupplyFlightDetailDTO getFlightDetailsInfo(String fltkey, int pnrGroupNo) {
        return SupplyFlightDetailDTO.newBuilder()
            .setFltLookUpKey(fltkey)
            .setPnrGroupNum(pnrGroupNo)
            .build();
    }

    private SupplyTravelerAddons buildTravelerAddons(DataLists dataLists) {
        SupplyTravelerAddons.Builder travelerAddonsBuilder = SupplyTravelerAddons.newBuilder();
        travelerAddonsBuilder.setPtcType("");
        
        Map<String, SupplyTravelerAddons.AddonsMap> flightLevelAddonsMap = new HashMap<>();
        
        if (dataLists != null && dataLists.getFlightList() != null) {
            for (Flight flight : dataLists.getFlightList().getFlight()) {
                String[] segmentRefs = flight.getSegmentReferences().split("\\s+");
                for (String segmentRef : segmentRefs) {
                    FlightSegment segment = findSegmentByRef(dataLists, segmentRef);
                    if (segment != null) {
                        String flightKey = buildFlightKey(segment);
                        
                        SupplyTravelerAddons.AddonsMap.Builder addonsMapBuilder = SupplyTravelerAddons.AddonsMap.newBuilder();
                        Map<String, SupplyAddons> addonsMap = new HashMap<>();
                        
                        addonsMapBuilder.putAllAddons(addonsMap);
                        flightLevelAddonsMap.put(flightKey, addonsMapBuilder.build());
                    }
                }
            }
        }
        
        //travelerAddonsBuilder.putAllFlightLevelAddons(flightLevelAddonsMap);
        travelerAddonsBuilder.putAllJourneyLevelAddons(new HashMap<>()); // Empty journey level addons
        
        return travelerAddonsBuilder.build();
    }

    private String buildFlightKey(FlightSegment segment) {
        return buildFlightKey(
            segment.getDeparture().getAirportCode(),
            segment.getArrival().getAirportCode(),
            segment.getDeparture().getDate(),
            segment.getDeparture().getTime(),
            segment.getMarketingCarrier().getAirlineID(),
            segment.getMarketingCarrier().getFlightNumber()
        );
    }

    private void addTravelerInfos(SupplyPnrFareInfoDTO.Builder fareInfoBuilder, DataLists dataLists) {
        if (dataLists != null && dataLists.getPassengerList() != null && dataLists.getPassengerList().getPassengers() != null) {
            for (Passenger passenger : dataLists.getPassengerList().getPassengers()) {
                SupplyTravelerInfoDTO.Builder travelerBuilder = SupplyTravelerInfoDTO.newBuilder();
                travelerBuilder.setId(passenger.getPassengerID())
                    .setTitle(passenger.getNameTitle())
                    .setPaxId(passenger.getPassengerID())
                    .setFirstName(passenger.getFirstName())
                    .setMiddleName(StringUtils.defaultString(passenger.getMiddleName(), ""))
                    .setLastName(passenger.getLastName())
                    .setMealPreference("")
                    .setGender(mapGender(passenger.getGender()))
                    .setEmailId(getFirstOrEmpty(dataLists.getContactEmail()))
                    .setMobileNumber(getFirstOrEmpty(dataLists.getContactNumber()))
                    .setMobileNumberCountryCode("")
                    .setPaxType(mapPassengerType(passenger.getPtc()))
                    .setDateOfBirth("")
                    .setNationality("")
                    .setPwdLine("")
                    .setPtcCode("");
                
                fareInfoBuilder.addTravelerInfos(travelerBuilder.build());
            }
        }
    }

    private SupplyPaxType mapPassengerType(String ptc) {
        if (StringUtils.isEmpty(ptc)) {
            return SupplyPaxType.ADULT; // Default to ADULT if empty
        }

        switch (ptc.toUpperCase()) {
            case "ADT":
                return SupplyPaxType.ADULT;
            case "CHD":
                return SupplyPaxType.CHILD;
            case "INF":
                return SupplyPaxType.INFANT;
            default:
                LOG.warn("Unknown passenger type code: {}. Defaulting to ADULT", ptc);
                return SupplyPaxType.ADULT;
        }
    }

    /**
     * Maps gender string to SupplyGender enum
     */
    private SupplyGenderOuterClass.SupplyGender mapGender(String gender) {
        if ("Female".equalsIgnoreCase(gender)) {
            return SupplyGenderOuterClass.SupplyGender.FEMALE;
        } else {
            return SupplyGenderOuterClass.SupplyGender.MALE;
        }
    }

    private String getFirstOrEmpty(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }

    /**
     * Create segment product info for a flight segment
     */
    private SupplySegmentProductInfo.Builder createSegmentProductInfo(
        OfferItem offerItem, int index, Price price) {
        
        SupplySegmentProductInfo.Builder builder = SupplySegmentProductInfo.newBuilder();
        setFareBasisInfo(builder, offerItem, index);
        setBaggageInfo(builder);
        setSegmentFare(builder);
        builder.setFareExpDate("");
        
        return builder;
    }

    /**
     * Set segment fare info - Modified to not distribute fares at segment level
     */
    private void setSegmentFare(SupplySegmentProductInfo.Builder segProductBuilder) {
        segProductBuilder.setSgFare(
            SupplySegmentFare.newBuilder()
                .setBs(0.0)
                .setTx(0.0)
                .setTot(0.0)
                .setDiscount(0.0)
                .setAirlineFixedFee(0.0)
                .addAllTaxBreakups(Collections.emptyList())
                .addAllAirlineFixedFees(Collections.emptyList())
                .build()
        );
    }

    /**
     * Set fare basis info for a segment
     */
    private void setFareBasisInfo(SupplySegmentProductInfo.Builder segProductBuilder, 
                            OfferItem offerItem, int segmentIndex) {
    // Default empty values
    String fareBasisCode = "";
    String rbd = "";
    String cabinType = "";
    
    // Extract values if available
    if (offerItem.getFareComponent() != null && !offerItem.getFareComponent().isEmpty() &&
        offerItem.getFareComponent().get(0) != null && offerItem.getFareComponent().get(0).getFareBasis() != null) {
        
        FareBasis fareBasis = offerItem.getFareComponent().get(0).getFareBasis();
        
        // Split space-separated values
        String[] fareBasisCodes = (fareBasis.getFareBasisCode() != null && fareBasis.getFareBasisCode().getCode() != null) ?
                                 fareBasis.getFareBasisCode().getCode().split("\\s+") : new String[0];
        String[] rbdValues = fareBasis.getRbd() != null ? fareBasis.getRbd().split("\\s+") : new String[0];
        String[] cabinTypes = fareBasis.getCabinType() != null ? fareBasis.getCabinType().split("\\s+") : new String[0];
        
        // Get values for this segment
        fareBasisCode = getValueAtIndexOrLast(fareBasisCodes, segmentIndex);
        rbd = getValueAtIndexOrLast(rbdValues, segmentIndex);
        cabinType = getValueAtIndexOrLast(cabinTypes, segmentIndex);
    }
    
    // Set values to builder
    segProductBuilder.setFareBasis(fareBasisCode.trim())
                     .setFareClass(rbd.trim())
                     .setProductClass(rbd.trim())
                     .setCabin(cabinType.trim());
}

    /**
     * Set baggage info for a segment
     */
    private void setBaggageInfo(SupplySegmentProductInfo.Builder segProductBuilder) {
        SupplySegmentProductInfo.SupplyBagGroup.Builder baggageBuilder = 
            SupplySegmentProductInfo.SupplyBagGroup.newBuilder();
            
        SupplyBagInfo.Builder cabinBagBuilder = SupplyBagInfo.newBuilder();
        cabinBagBuilder.setNumOfPieces(1);
        cabinBagBuilder.setWeightPerPiece(15);
        cabinBagBuilder.setWeightUnit("Kilograms");
        cabinBagBuilder.setTotalWeight(0);
        
        baggageBuilder.setCabinBag(cabinBagBuilder.build());
        segProductBuilder.setBaggageInfo(baggageBuilder.build());
    }

    private String getValueAtIndexOrLast(String[] values, int index) {
        if (values == null || values.length == 0) {
            return "";
        }
        return index < values.length ? values[index] : values[values.length - 1];
    }
}