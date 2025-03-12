package com.mmt.flights.pnr.service.tasks;

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
        builder.setBookingInfo(getBookingInfo(order, dataLists, segmentRefMap, 0, version, orderViewRS));

        // Set metadata
        builder.setMetaData(getMetaData(order, cmsMapHolder.getCmsId(), supplierLatency, state,
                supplyPnrRequestDTO.getEnableTrace()));

        builder.setStatus(SupplyStatus.SUCCESS);

        return builder.build();
    }

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

    private void setFlightEquipmentInfo(SupplyFlightDTO.Builder builder, FlightSegment segment) {
        if (segment != null && segment.getEquipment() != null) {
            builder.setArcrfTyp(segment.getEquipment().getAircraftCode());
        }
    }

    private String formatFlightTime(String timeString) {
        if (StringUtils.isEmpty(timeString)) {
            return "";
        }

        try {
            LocalTime time = LocalTime.parse(timeString, INPUT_TIME_FORMATTER);
            return time.format(OUTPUT_TIME_FORMATTER);
        } catch (Exception e) {
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
                                                ApiVersion version,
                                                OrderViewRS orderViewRS) {
        SupplyBookingInfoDTO.Builder builder = SupplyBookingInfoDTO.newBuilder();

        List<SupplyBookingJourneyDTO> journeys = getJourneys(dataLists, segmentRefMap, pnrGroupNo);
        builder.addAllJourneys(journeys);

        if (version != null) {
            buildFlightToJourneyMap(journeys);
        }

        builder.setFrInfo(getFareInfo(String.valueOf(pnrGroupNo), order, dataLists, segmentRefMap, orderViewRS))
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

        SegmentBoundaryInfo segmentInfo = findJourneySegments(flight, dataLists, segmentRefMap);

        if (segmentInfo.hasValidBoundaries()) {
            String depFormattedTime = formatFlightTime(segmentInfo.firstSegment.getDeparture().getTime());
            builder.setDepDate(segmentInfo.firstSegment.getDeparture().getDate() + TIME_DELIMITER + depFormattedTime);

            String arrFormattedTime = formatFlightTime(segmentInfo.lastSegment.getArrival().getTime());
            builder.setArrDate(segmentInfo.lastSegment.getArrival().getDate() + TIME_DELIMITER + arrFormattedTime);

            for (String lookupKey : segmentInfo.segmentKeys) {
                builder.addFlightDtlsInfo(getFlightDetailsInfo(lookupKey, pnrGroupNo));
            }

            builder.setJrnyKey(String.join("|", segmentInfo.segmentKeys));
        }

        return builder.build();
    }

    private static class SegmentBoundaryInfo {
        FlightSegment firstSegment;
        FlightSegment lastSegment;
        final List<String> segmentKeys = new ArrayList<>();

        boolean hasValidBoundaries() {
            return firstSegment != null && lastSegment != null;
        }
    }

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

    private List<String> getAllFlightRefsForOfferItem(OfferItem offerItem) {
        Set<String> flightRefsSet = new HashSet<>();

        if (offerItem.getService() != null) {
            for (Service service : offerItem.getService()) {
                if (service != null && StringUtils.isNotEmpty(service.getFlightRefs())) {
                    String[] refs = service.getFlightRefs().split("\\s+");
                    for (String ref : refs) {
                        if (StringUtils.isNotEmpty(ref)) {
                            flightRefsSet.add(ref.trim());
                        }
                    }
                }
            }
        }

        if (flightRefsSet.isEmpty()) {
            LOG.warn("No flight references found for offer item: {}", offerItem.getOfferItemID());
        } else {
            LOG.debug("Found flight references for offer item {}: {}",
                    offerItem.getOfferItemID(), String.join(", ", flightRefsSet));
        }

        return new ArrayList<>(flightRefsSet);
    }

    private Flight findFlightByRef(DataLists dataLists, String flightRef) {
        if (dataLists == null || dataLists.getFlightList() == null ||
                dataLists.getFlightList().getFlight() == null) {
            return null;
        }

        for (Flight flight : dataLists.getFlightList().getFlight()) {
            if (flightRef.equals(flight.getFlightKey())) {
                return flight;
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
                                          Map<String, String> segmentRefMap, OrderViewRS orderViewRS) {
        SupplyFareInfoDTO.Builder builder = SupplyFareInfoDTO.newBuilder();
        SupplyPnrFareInfoDTO.Builder fareInfoBuilder = buildBasicFareInfo(order);

        List<SupplyTicketDetailsDTO> ticketInfos = buildTicketInfos(orderViewRS);
        fareInfoBuilder.addAllTicketInfos(ticketInfos);

        if (order.getOfferItem() != null && !order.getOfferItem().isEmpty()) {
            FareCalculationResult calculationResult = calculateFaresFromOfferItems(order, dataLists, segmentRefMap);

            buildPassengerFareDetails(fareInfoBuilder, calculationResult);

            buildTotalFareInfo(fareInfoBuilder, calculationResult);

            fareInfoBuilder.putTravelerAddons("0", buildTravelerAddons(dataLists));
            addTravelerInfos(fareInfoBuilder, dataLists);
        }

        builder.putPnrGrpdFrInfo(Integer.parseInt(pnrGroupNo), fareInfoBuilder.build());
        return builder.build();
    }

    private static class FareCalculationResult {
        final Map<String, Set<String>> paxTypeRefsMap = new HashMap<>();
        final Map<String, SupplyFareDetailDTO.Builder> paxTypeFareBuilderMap = new HashMap<>();
        final Map<String, Map<String, Double>> paxTypeTaxBreakupMap = new HashMap<>();

        double totalBs = 0.0;
        double totalTx = 0.0;
        double totalAmount = 0.0;
        final Map<String, Double> orderTotalTaxBreakupMap = new ConcurrentHashMap<>();
    }

    private FareCalculationResult calculateFaresFromOfferItems(Order order, DataLists dataLists,
                                                               Map<String, String> segmentRefMap) {
        FareCalculationResult result = new FareCalculationResult();

        for (OfferItem offerItem : order.getOfferItem()) {
            if (offerItem == null) {
                continue;
            }

            String paxType = offerItem.getPassengerType();
            if (StringUtils.isBlank(paxType)) {
                continue;
            }

            collectPassengerReferences(offerItem, paxType, result.paxTypeRefsMap);

            SupplyFareDetailDTO.Builder fareDetailBuilder = result.paxTypeFareBuilderMap.computeIfAbsent(
                    paxType, k -> initializeFareDetailBuilder());

            if (offerItem.getFareDetail() != null && offerItem.getFareDetail().getPrice() != null) {
                processFarePrice(offerItem.getFareDetail().getPrice(), paxType, fareDetailBuilder,
                        result.paxTypeRefsMap, result.paxTypeTaxBreakupMap, result);
            }

            processOfferItemFareComponents(offerItem, dataLists, segmentRefMap, fareDetailBuilder);
        }

        return result;
    }

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

    private void processFarePrice(Price price, String paxType, SupplyFareDetailDTO.Builder fareDetailBuilder,
                                  Map<String, Set<String>> paxTypeRefsMap,
                                  Map<String, Map<String, Double>> paxTypeTaxBreakupMap,
                                  FareCalculationResult result) {
        if (price == null || price.getBaseAmount() == null || price.getTaxAmount() == null ||
                price.getTotalAmount() == null) {
            return;
        }

        Set<String> uniqueRefs = paxTypeRefsMap.getOrDefault(paxType, Collections.emptySet());
        int paxCount = uniqueRefs.size();

        if (paxCount == 0) {
            return;
        }

        double totalBaseAmount = price.getBaseAmount().getBookingCurrencyPrice();
        double totalTaxAmount = price.getTaxAmount().getBookingCurrencyPrice();
        double totalAmountAllPax = price.getTotalAmount().getBookingCurrencyPrice();

        double baseAmountPerPax = totalBaseAmount / paxCount;
        double taxAmountPerPax = totalTaxAmount / paxCount;
        double totalAmountPerPax = totalAmountAllPax / paxCount;

        fareDetailBuilder.setBs(baseAmountPerPax)
                .setTx(taxAmountPerPax)
                .setTot(totalAmountPerPax)
                .setNoOfPax(paxCount);

        result.totalBs += totalBaseAmount;
        result.totalTx += totalTaxAmount;
        result.totalAmount += totalAmountAllPax;

        processTaxBreakups(price.getTaxes(), paxType, paxTypeTaxBreakupMap, result.orderTotalTaxBreakupMap, paxCount);
    }

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

            taxBreakupMap.put(taxCode, totalTaxAmount / paxCount);
            orderTotalTaxBreakupMap.put(taxCode, totalTaxAmount);
        }
    }

    private void buildPassengerFareDetails(SupplyPnrFareInfoDTO.Builder fareInfoBuilder, FareCalculationResult result) {
        result.paxTypeFareBuilderMap.forEach((paxType, fareDetailBuilder) -> {
            Set<String> uniqueRefs = result.paxTypeRefsMap.getOrDefault(paxType, Collections.emptySet());
            fareDetailBuilder.setNoOfPax(uniqueRefs.size());
            fareDetailBuilder.setAirlineFixedFee(0.0);
            fareDetailBuilder.addAllAirlineFixedFees(Collections.emptyList());

            fareDetailBuilder.clearTaxBreakups();
            Map<String, Double> taxBreakupMap = result.paxTypeTaxBreakupMap.get(paxType);
            if (taxBreakupMap != null) {
                addTaxBreakupsToBuilder(fareDetailBuilder, taxBreakupMap);
            }

            fareInfoBuilder.putPaxFares(paxType, fareDetailBuilder.build());
        });
    }

    private void addTaxBreakupsToBuilder(SupplyFareDetailDTO.Builder builder, Map<String, Double> taxBreakupMap) {
        taxBreakupMap.forEach((code, amount) ->
                builder.addTaxBreakups(SupplyTaxBreakupDTO.newBuilder()
                        .setAmnt(amount)
                        .setCode(code)
                        .setMsg("")
                        .build()));
    }

    private void buildTotalFareInfo(SupplyPnrFareInfoDTO.Builder fareInfoBuilder, FareCalculationResult result) {
        SupplyTotalFareDTO totFare = SupplyTotalFareDTO.newBuilder()
                .setBs(result.totalBs)
                .setTx(result.totalTx)
                .setTot(result.totalAmount)
                .setAirlineFixedFee(0.0)
                .build();

        fareInfoBuilder.setTotFr(totFare);
    }

    private void processOfferItemFareComponents(OfferItem offerItem, DataLists dataLists,
                                                Map<String, String> segmentRefMap,
                                                SupplyFareDetailDTO.Builder fareDetailBuilder) {
        if (offerItem.getFareComponent() == null || offerItem.getFareComponent().isEmpty()) {
            return;
        }

        List<String> flightRefs = getAllFlightRefsForOfferItem(offerItem);
        if (flightRefs.isEmpty()) {
            LOG.warn("No flight references found for offer item");
            return;
        }

        int segmentCounter = 0;
        for (String flightRef : flightRefs) {
            Flight flight = findFlightByRef(dataLists, flightRef);
            if (flight == null || StringUtils.isEmpty(flight.getSegmentReferences())) {
                LOG.debug("Could not find flight or segments for flight ref: {}", flightRef);
                continue;
            }

            String[] segmentRefs = flight.getSegmentReferences().split("\\s+");
            for (String segmentRef : segmentRefs) {
                FlightSegment segment = findSegmentByRef(dataLists, segmentRef);

                if (segment != null && segmentRefMap.containsKey(segmentRef)) {
                    fareDetailBuilder.putSegPrdctInfo(
                            segmentRefMap.get(segmentRef),
                            createSegmentProductInfo(offerItem, segmentCounter++, offerItem.getFareDetail().getPrice()).build()
                    );
                } else {
                    LOG.debug("Could not find valid segment for segment ref: {}", segmentRef);
                }
            }
        }

        if (segmentCounter == 0) {
            LOG.warn("No segments were processed for fare with offer item ID: {}",
                    offerItem.getOfferItemID());
        }
    }

    private SupplyPaxSegmentInfo getPaxSegmentInfo(DataLists dataLists) {
        SupplyPaxSegmentInfo.Builder builder = SupplyPaxSegmentInfo.newBuilder();

        if (dataLists != null && dataLists.getFlightSegmentList() != null
                && !dataLists.getFlightSegmentList().getFlightSegment().isEmpty()) {

            SupplyPnrStatusDTO.Builder statusBuilder = SupplyPnrStatusDTO.newBuilder();

            for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
                if (segment != null && segment.getSegmentKey() != null) {
                    String segmentKey = segment.getSegmentKey();

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

        travelerAddonsBuilder.putAllJourneyLevelAddons(new HashMap<>());

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
            // Use incremental traveler IDs starting from 0
            int travelerId = 0;

            for (Passenger passenger : dataLists.getPassengerList().getPassengers()) {
                SupplyTravelerInfoDTO.Builder travelerBuilder = SupplyTravelerInfoDTO.newBuilder();
                String incrementalId = String.valueOf(travelerId++);

                travelerBuilder.setId(incrementalId)
                        .setTitle(passenger.getNameTitle())
                        .setPaxId(incrementalId)  // Also set paxId to the same incremental ID
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
            return SupplyPaxType.ADULT;
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

    private SupplySegmentProductInfo.Builder createSegmentProductInfo(
            OfferItem offerItem, int index, Price price) {

        SupplySegmentProductInfo.Builder builder = SupplySegmentProductInfo.newBuilder();
        setFareBasisInfo(builder, offerItem, index);
        setSegmentFare(builder);
        builder.setFareExpDate("");

        return builder;
    }

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

    private void setFareBasisInfo(SupplySegmentProductInfo.Builder segProductBuilder,
                                  OfferItem offerItem, int segmentIndex) {
        String fareBasisCode = "";
        String rbd = "";
        String cabinType = "";

        if (offerItem.getFareComponent() != null && !offerItem.getFareComponent().isEmpty()) {
            int processedSegmentCount = 0;

            for (FareComponent fareComponent : offerItem.getFareComponent()) {
                if (fareComponent == null || fareComponent.getFareBasis() == null) {
                    continue;
                }

                String[] segmentRefs = fareComponent.getSegmentRefs() != null ?
                        fareComponent.getSegmentRefs().split("\\s+") : new String[0];
                int segmentsInComponent = segmentRefs.length;

                if (segmentIndex >= processedSegmentCount &&
                        segmentIndex < processedSegmentCount + segmentsInComponent) {

                    FareBasis fareBasis = fareComponent.getFareBasis();

                    int relativeIndex = segmentIndex - processedSegmentCount;

                    String[] fareBasisCodes = (fareBasis.getFareBasisCode() != null &&
                            fareBasis.getFareBasisCode().getCode() != null) ?
                            fareBasis.getFareBasisCode().getCode().split("\\s+") : new String[0];
                    String[] rbdValues = fareBasis.getRbd() != null ?
                            fareBasis.getRbd().split("\\s+") : new String[0];
                    String[] cabinTypes = fareBasis.getCabinType() != null ?
                            fareBasis.getCabinType().split("\\s+") : new String[0];

                    fareBasisCode = getValueAtIndexOrLast(fareBasisCodes, relativeIndex);
                    rbd = getValueAtIndexOrLast(rbdValues, relativeIndex);
                    cabinType = getValueAtIndexOrLast(cabinTypes, relativeIndex);

                    break;
                }

                processedSegmentCount += segmentsInComponent;
            }
        }

        segProductBuilder.setFareBasis(fareBasisCode.trim())
                .setFareClass(rbd.trim())
                .setProductClass(rbd.trim())
                .setCabin(cabinType.trim());
    }

    private String getValueAtIndexOrLast(String[] values, int index) {
        if (values == null || values.length == 0) {
            return "";
        }
        return index < values.length ? values[index] : values[values.length - 1];
    }

    private List<SupplyTicketDetailsDTO> buildTicketInfos(OrderViewRS orderViewRS) {
        List<SupplyTicketDetailsDTO> ticketInfos = new ArrayList<>();

        if (orderViewRS == null || orderViewRS.getTicketDocInfos() == null ||
                orderViewRS.getTicketDocInfos().getTicketDocInfo() == null ||
                orderViewRS.getTicketDocInfos().getTicketDocInfo().isEmpty()) {
            return ticketInfos;
        }

        for (TicketDocInfo ticketDocInfo : orderViewRS.getTicketDocInfos().getTicketDocInfo()) {
            if (ticketDocInfo == null || ticketDocInfo.getTicketDocument() == null ||
                    StringUtils.isEmpty(ticketDocInfo.getTicketDocument().getTicketDocNbr()) ||
                    StringUtils.isEmpty(ticketDocInfo.getPassengerReference())) {
                continue;
            }

            SupplyTicketDetailsDTO.Builder ticketBuilder = SupplyTicketDetailsDTO.newBuilder()
                    .setTicketNumber(ticketDocInfo.getTicketDocument().getTicketDocNbr())
                    .setTravellerId(ticketDocInfo.getPassengerReference())
                    .setStatus("ISSUED");

            ticketInfos.add(ticketBuilder.build());
        }

        return ticketInfos;
    }
}