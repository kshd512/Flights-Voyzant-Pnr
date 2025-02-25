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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class PnrRetrieveResponseAdapter implements MapTask {
    private static final DateTimeFormatter INPUT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter OUTPUT_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

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
        long duration = new Long(0);//state.getValue(FlowStateKey.SESSION_DURATION) + state.getValue(FlowStateKey.RESPONSE_DURATION);

        if (orderViewRS.getOrder() == null || orderViewRS.getOrder().isEmpty() || 
            StringUtils.isEmpty(orderViewRS.getOrder().get(0).getOrderID())) {
            throw new ServiceErrorException("PNR_NOT_VALID", ErrorEnum.PNR_PARTIALLY_REFUNDED, HttpStatus.BAD_REQUEST);
        }

        return state.toBuilder().addValue(FlowStateKey.RESPONSE, 
                getResponse(supplyPnrRequest, orderViewRS, cmsMapHolder, duration, state)).build();
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
        Map<String, String> segmentRefMap = new HashMap<>();
        
        // Process flight segments if available
        if (dataLists != null && dataLists.getFlightSegmentList() != null) {
            processFlightSegments(builder, dataLists, order, segmentRefMap);
        } else {
            builder.setPnrStatus(SupplyPnrStatusType.CUSTOMER_CANCELED);
        }

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
    private void processFlightSegments(SupplyBookingResponseDTO.Builder builder, DataLists dataLists, Order order, Map<String, String> segmentRefMap) {
        Map<String, String> flightKeyMap = new HashMap<>();
        int segmentId = 0;
        int journeyId = 0;

        for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
            SupplyFlightDTO flight = getSimpleFlight(segment, segmentId, journeyId);
            String fltkey = AdapterUtil.getJourneyKey(Arrays.asList(flight));
            
            builder.putFlightLookUpList(fltkey, flight);
            flightKeyMap.put(flight.getDepInfo().getArpCd() + flight.getArrInfo().getArpCd(), fltkey);
            
            processTechnicalStops(flight, flightKeyMap);
            
            segmentRefMap.put(segment.getSegmentKey(), fltkey);
            segmentId++;
            journeyId++;
        }

        builder.setPnrStatus(SupplyPnrStatusType.ACTIVE);
        builder.setGstInfo(getGSTInfo(order));
        builder.setContactInfo(getContactInfo(dataLists));
    }

    /**
     * Process technical stops for a flight
     */
    private void processTechnicalStops(SupplyFlightDTO flight, Map<String, String> flightKeyMap) {
        if (flight.getTchStpList() != null) {
            String lastAirport = flight.getDepInfo().getArpCd();
            String fltkey = AdapterUtil.getJourneyKey(Arrays.asList(flight));
            
            for (SupplyTechnicalStopDTO techStop : flight.getTchStpList()) {
                String currentAirPort = techStop.getLocInfo().getArpCd();
                flightKeyMap.put(lastAirport + currentAirPort, fltkey);
                lastAirport = currentAirPort;
            }
            flightKeyMap.put(lastAirport + flight.getArrInfo().getArpCd(), fltkey);
        }
    }

    private SupplyFlightDTO getSimpleFlight(FlightSegment segment, int segmentId, int journeyId) {
        SupplyFlightDTO.Builder builder = SupplyFlightDTO.newBuilder();
        
        // Set departure info
        setFlightDepartureInfo(builder, segment.getDeparture());
        
        // Set arrival info
        setFlightArrivalInfo(builder, segment.getArrival());

        // Set carrier info
        setFlightCarrierInfo(builder, segment);
        
        // Set equipment and duration
        setFlightEquipmentInfo(builder, segment);

        // Set segment identifiers
        setFlightSegmentIdentifiers(builder, segment, segmentId, journeyId);
        
        // Set cabin and booking class
        setFlightCabinInfo(builder, segment);
        
        return builder.build();
    }

    /**
     * Sets departure airport and time information for a flight
     */
    private void setFlightDepartureInfo(SupplyFlightDTO.Builder builder, AirportInfo departure) {
        SupplyLocationInfoDTO.Builder depBuilder = SupplyLocationInfoDTO.newBuilder();
        depBuilder.setArpCd(departure.getAirportCode());
        depBuilder.setArpNm(departure.getAirportName());
        if (departure.getTerminal() != null) {
            depBuilder.setTrmnl(departure.getTerminal().getName());
        }

        String formattedTime = formatFlightTime(departure.getTime());
        builder.setDepTime(departure.getDate() + " " + formattedTime);
        builder.setDepInfo(depBuilder.build());
    }

    /**
     * Sets arrival airport and time information for a flight
     */
    private void setFlightArrivalInfo(SupplyFlightDTO.Builder builder, AirportInfo arrival) {
        SupplyLocationInfoDTO.Builder arrBuilder = SupplyLocationInfoDTO.newBuilder();
        arrBuilder.setArpCd(arrival.getAirportCode());
        arrBuilder.setArpNm(arrival.getAirportName());
        if (arrival.getTerminal() != null) {
            arrBuilder.setTrmnl(arrival.getTerminal().getName());
        }

        String formattedTime = formatFlightTime(arrival.getTime());
        builder.setArrTime(arrival.getDate() + " " + formattedTime);
        builder.setArrInfo(arrBuilder.build());
    }

    /**
     * Sets marketing and operating carrier information
     */
    private void setFlightCarrierInfo(SupplyFlightDTO.Builder builder, FlightSegment segment) {
        Carrier marketingCarrier = segment.getMarketingCarrier();
        builder.setMrkAl(marketingCarrier.getAirlineID());
        builder.setFltNo(marketingCarrier.getFlightNumber().replaceAll("\\s", ""));
        
        Carrier operatingCarrier = segment.getOperatingCarrier();
        builder.setOprAl(operatingCarrier != null ? operatingCarrier.getAirlineID() : marketingCarrier.getAirlineID());
    }

    /**
     * Sets equipment information for a flight
     */
    private void setFlightEquipmentInfo(SupplyFlightDTO.Builder builder, FlightSegment segment) {
        Equipment equipment = segment.getEquipment();
        if (equipment != null) {
            builder.setArcrfTyp(equipment.getAircraftCode());
        }
    }

    /**
     * Sets segment identifiers for a flight
     */
    private void setFlightSegmentIdentifiers(SupplyFlightDTO.Builder builder, FlightSegment segment, int segmentId, int journeyId) {
        builder.setSuppSegKey(segment.getSegmentKey());
        builder.setSuppid(String.valueOf(segmentId));
        builder.setMarriedSegId(String.valueOf(journeyId));
    }

    /**
     * Sets cabin and booking class information for a flight
     */
    private void setFlightCabinInfo(SupplyFlightDTO.Builder builder, FlightSegment segment) {
        if (segment.getCode() != null) {
            builder.setClassOfService(segment.getCode().getMarriageGroup());
        }
    }

    /**
     * Formats a flight time string from HH:mm:ss to HH:mm format
     * Falls back to substring extraction if parsing fails
     */
    private String formatFlightTime(String timeString) {
        try {
            LocalTime parsedTime = LocalTime.parse(timeString, INPUT_TIME_FORMATTER);
            return parsedTime.format(OUTPUT_TIME_FORMATTER);
        } catch (Exception e) {
            // Fallback to the original substring extraction
            return timeString.length() >= 5 ? 
                    timeString.substring(0, 5) : timeString;
        }
    }

    private SupplyGSTInfo getGSTInfo(Order order) {
        return SupplyGSTInfo.newBuilder().build(); // Implement GST info extraction if available
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
        SupplyBookingResponseMetaDataDTO.Builder builder = SupplyBookingResponseMetaDataDTO.newBuilder();
        
        builder.setCurrency(order.getBookingCurrencyCode());
        builder.setServiceName(CommonConstants.SERVICE_NAME);
        builder.setSupplierName(CommonConstants.SUPPLIER_NAME);
        builder.setCredentialId(cmsId);
        builder.setSupplierLatency(String.valueOf(supplierLatency));
        builder.setIpAddress(IPAddressUtil.getIPAddress());
        
        if (enableTrace) {
            try {
                builder.putTraceInfo("Request", 
                    jaxbHandlerService.marshall(state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_REQUEST)));
                builder.putTraceInfo("Response",state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE));
            } catch (Exception e) {
                // Log error but continue
            }
        }
        
        return builder.build();
    }

    private SupplyBookingInfoDTO getBookingInfo(Order order, DataLists dataLists,
                                               Map<String, String> segmentRefMap,
                                               int pnrGroupNo,
                                               ApiVersion version) {
        SupplyBookingInfoDTO.Builder builder = SupplyBookingInfoDTO.newBuilder();
        
        List<SupplyBookingJourneyDTO> journeys = getJourneys(dataLists, segmentRefMap, pnrGroupNo);
        builder.addAllJourneys(journeys);

        Map<String, String> flightToJourneyMap = new HashMap<>();
        if (version != null) {
            for (SupplyBookingJourneyDTO journey : journeys) {
                for (SupplyFlightDetailDTO flight : journey.getFlightDtlsInfoList()) {
                    flightToJourneyMap.put(flight.getFltLookUpKey(), journey.getJrnyKey());
                }
            }
        }

        builder.setFrInfo(getFareInfo(String.valueOf(pnrGroupNo), order, dataLists, segmentRefMap));
        builder.setPaxSegmentInfo(getPaxSegmentInfo(dataLists));
        return builder.build();
    }

    private List<SupplyBookingJourneyDTO> getJourneys(DataLists dataLists,
                                                     Map<String, String> segmentRefMap,
                                                     int pnrGroupNo) {
        List<SupplyBookingJourneyDTO> journeys = new ArrayList<>();
        
        if (dataLists != null && dataLists.getFlightList() != null) {
            FlightList flightList = dataLists.getFlightList();
            for (Flight flight : flightList.getFlight()) {
                // Each flight can contain multiple segments
                SupplyBookingJourneyDTO journey = getJourneyFromFlight(flight, dataLists, segmentRefMap, pnrGroupNo);
                journeys.add(journey);
            }
        }
        return journeys;
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
            setJourneyDepartureInfo(builder, segmentInfo.firstSegment);
            setJourneyArrivalInfo(builder, segmentInfo.lastSegment);
            
            // Add flight details
            for (String lookupKey : segmentInfo.segmentKeys) {
                builder.addFlightDtlsInfo(getFlightDetailsInfo(lookupKey, pnrGroupNo));
            }
            
            // Set journey key
            String journeyKey = String.join("|", segmentInfo.segmentKeys);
            builder.setJrnyKey(journeyKey);
        }

        return builder.build();
    }

    /**
     * Helper class to store segment boundary information
     */
    private static class SegmentBoundaryInfo {
        FlightSegment firstSegment;
        FlightSegment lastSegment;
        List<String> segmentKeys = new ArrayList<>();
        
        boolean hasValidBoundaries() {
            return firstSegment != null && lastSegment != null;
        }
    }

    /**
     * Find first and last segments and collect segment keys
     */
    private SegmentBoundaryInfo findJourneySegments(Flight flight, DataLists dataLists, Map<String, String> segmentRefMap) {
        SegmentBoundaryInfo result = new SegmentBoundaryInfo();
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

    /**
     * Set departure information for journey
     */
    private void setJourneyDepartureInfo(SupplyBookingJourneyDTO.Builder builder, FlightSegment segment) {
        String formattedTime = formatFlightTime(segment.getDeparture().getTime());
        builder.setDepDate(segment.getDeparture().getDate() + " " + formattedTime);
    }

    /**
     * Set arrival information for journey
     */
    private void setJourneyArrivalInfo(SupplyBookingJourneyDTO.Builder builder, FlightSegment segment) {
        String formattedTime = formatFlightTime(segment.getArrival().getTime());
        builder.setArrDate(segment.getArrival().getDate() + " " + formattedTime);
    }

    private String buildFlightKey(String depCode, String arrCode, String depDate, 
                                String depTime, String airlineCode, String flightNumber) {
        return String.format("%s$%s$%s %s$%s-%s", 
            depCode, arrCode, depDate, depTime, airlineCode, flightNumber);
    }

    private FlightSegment findSegmentByRef(DataLists dataLists, String segmentRef) {
        if (dataLists.getFlightSegmentList() != null) {
            for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
                if (segmentRef.equals(segment.getSegmentKey())) {
                    return segment;
                }
            }
        }
        return null;
    }

    private SupplyPnrFareInfoDTO.Builder buildBasicFareInfo(Order order) {
        SupplyPnrFareInfoDTO.Builder fareInfoBuilder = SupplyPnrFareInfoDTO.newBuilder();
        fareInfoBuilder.setStatus(SupplyStatus.SUCCESS);
        fareInfoBuilder.setRfndStatus(SupplyRefundStatusDTO.RS_NOT_SET);
        fareInfoBuilder.setPnrKey("");
        fareInfoBuilder.setSPnr(order.getOrderID());
        fareInfoBuilder.setAPnr(order.getGdsBookingReference());
        fareInfoBuilder.setValidatingCarrier(order.getOwner());
        String existingCreationDate = order.getTimeLimits() != null ? order.getTimeLimits().getOfferExpirationDateTime() : "";
        if (existingCreationDate.contains("T") && existingCreationDate.length() >= 16) {
            existingCreationDate = existingCreationDate.substring(0, 10) + " " + existingCreationDate.substring(11, 16);
        }
        fareInfoBuilder.setCreationDate(existingCreationDate);
        fareInfoBuilder.setFareFamily("FLEXI PLUS");  // Based on sample
        fareInfoBuilder.setAccountCode("");
        fareInfoBuilder.setMaxTicketingTime("");
        fareInfoBuilder.setTicketDelayInterval(0);
        fareInfoBuilder.setIsCouponFare(false);
        fareInfoBuilder.setFareType(SupplyFareType.REGULAR);
        fareInfoBuilder.setTcsStatus(SupplyTcsStatus.TCS_NOT_SET);
        fareInfoBuilder.setTimeZoneOffset("");
        fareInfoBuilder.addAllTicketInfos(Collections.emptyList()); // Initialize empty ticket infos list
        fareInfoBuilder.putAllScheduleChangeInfo(new HashMap<>()); // Initialize empty schedule change info
        return fareInfoBuilder;
    }

    private SupplyFareDetailDTO.Builder initializeFareDetailBuilder() {
        SupplyFareDetailDTO.Builder builder = SupplyFareDetailDTO.newBuilder();
        builder.setBs(0.0);
        builder.setTx(0.0);
        builder.setTot(0.0);
        return builder;
    }

    private SupplyFareInfoDTO getFareInfo(String pnrGroupNo, Order order, DataLists dataLists,
                                        Map<String, String> segmentRefMap) {
        SupplyFareInfoDTO.Builder builder = SupplyFareInfoDTO.newBuilder();
        SupplyPnrFareInfoDTO.Builder fareInfoBuilder = buildBasicFareInfo(order);
        
        if (order.getOfferItem() != null) {
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
        Map<String, Set<String>> paxTypeRefsMap = new HashMap<>();
        Map<String, SupplyFareDetailDTO.Builder> paxTypeFareBuilderMap = new HashMap<>();
        Map<String, Map<String, Double>> paxTypeTaxBreakupMap = new HashMap<>();
        
        // Order total data
        double totalBs = 0.0;
        double totalTx = 0.0;
        double totalAmount = 0.0;
        Map<String, Double> orderTotalTaxBreakupMap = new HashMap<>();
    }

    /**
     * Calculate fares from offer items
     */
    private FareCalculationResult calculateFaresFromOfferItems(Order order, DataLists dataLists, 
                                                            Map<String, String> segmentRefMap) {
        FareCalculationResult result = new FareCalculationResult();
        
        for (OfferItem offerItem : order.getOfferItem()) {
            // Get passenger type and references
            String paxType = offerItem.getPassengerType();
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
        if (offerItem.getFareDetail() != null && StringUtils.isNotBlank(offerItem.getFareDetail().getPassengerRefs())) {
            String[] paxRefs = offerItem.getFareDetail().getPassengerRefs().split(",");
            Set<String> paxRefSet = paxTypeRefsMap.computeIfAbsent(paxType, k -> new HashSet<>());
            for (String ref : paxRefs) {
                paxRefSet.add(ref.trim());
            }
        }
    }

    /**
     * Process fare price information
     */
    private void processFarePrice(Price price, String paxType, SupplyFareDetailDTO.Builder fareDetailBuilder,
                               Map<String, Set<String>> paxTypeRefsMap, 
                               Map<String, Map<String, Double>> paxTypeTaxBreakupMap,
                               FareCalculationResult result) {
        double baseAmount = price.getBaseAmount().getBookingCurrencyPrice();
        double taxAmount = price.getTaxAmount().getBookingCurrencyPrice();
        double totalAmountPerPax = price.getTotalAmount().getBookingCurrencyPrice();
        
        // Accumulate fares for this passenger type (per passenger)
        fareDetailBuilder.setBs(fareDetailBuilder.getBs() + baseAmount);
        fareDetailBuilder.setTx(fareDetailBuilder.getTx() + taxAmount);
        fareDetailBuilder.setTot(fareDetailBuilder.getTot() + totalAmountPerPax);
        
        // Calculate fares for all passengers of this type
        Set<String> uniqueRefs = paxTypeRefsMap.getOrDefault(paxType, Collections.emptySet());
        int paxCount = uniqueRefs.size();
        
        // Accumulate order totals (multiply by passenger count)
        result.totalBs += baseAmount * paxCount;
        result.totalTx += taxAmount * paxCount;
        result.totalAmount += totalAmountPerPax * paxCount;
        
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
        if (taxes == null) return;
        
        // Initialize tax breakup map for this passenger type if not exists
        Map<String, Double> taxBreakupMap = paxTypeTaxBreakupMap.computeIfAbsent(paxType, k -> new HashMap<>());
        
        for (Tax tax : taxes) {
            String taxCode = tax.getTaxCode();
            double amount = tax.getBookingCurrencyPrice();
            
            // Accumulate tax breakups for passenger type
            taxBreakupMap.merge(taxCode, amount, Double::sum);
            
            // Accumulate tax breakups for order total (multiply by passenger count)
            orderTotalTaxBreakupMap.merge(taxCode, amount * paxCount, Double::sum);
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
        taxBreakupMap.forEach((code, amount) -> {
            SupplyTaxBreakupDTO.Builder taxBreakupBuilder = SupplyTaxBreakupDTO.newBuilder();
            taxBreakupBuilder.setAmnt(amount);
            taxBreakupBuilder.setCode(code);
            taxBreakupBuilder.setMsg("");
            builder.addTaxBreakups(taxBreakupBuilder.build());
        });
    }

    /**
     * Build total fare info from calculation results
     */
    private void buildTotalFareInfo(SupplyPnrFareInfoDTO.Builder fareInfoBuilder, FareCalculationResult result) {
        SupplyTotalFareDTO.Builder totFrBuilder = SupplyTotalFareDTO.newBuilder();
        totFrBuilder.setBs(result.totalBs);
        totFrBuilder.setTx(result.totalTx);
        totFrBuilder.setTot(result.totalAmount);
        totFrBuilder.setAirlineFixedFee(0.0);
        
        // Add accumulated tax breakups to total fare (already multiplied by pax count)
        List<SupplyTaxBreakupDTO> totalTaxBreakups = new ArrayList<>();
        result.orderTotalTaxBreakupMap.forEach((code, amount) -> {
            SupplyTaxBreakupDTO taxBreakup = SupplyTaxBreakupDTO.newBuilder()
                .setAmnt(amount)
                .setCode(code)
                .setMsg("")
                .build();
            totalTaxBreakups.add(taxBreakup);
        });
        //totFrBuilder.addAllTaxBreakup(totalTaxBreakups);
        
        fareInfoBuilder.setTotFr(totFrBuilder.build());
    }

    private void processOfferItemFareComponents(OfferItem offerItem, DataLists dataLists,
                                              Map<String, String> segmentRefMap,
                                              SupplyFareDetailDTO.Builder fareDetailBuilder) {
        if (offerItem.getFareComponent() != null) {
            String flightRef = getFlightRefForOfferItem(offerItem);
            Flight flight = findFlightByRef(dataLists, flightRef);
            
            if (flight != null) {
                processFlightFares(flight, offerItem, offerItem.getFareDetail(), segmentRefMap, dataLists, fareDetailBuilder);
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

    private void processFlightFares(Flight flight, 
                                  OfferItem offerItem,
                                  FareDetail fareDetail,
                                  Map<String, String> segmentRefMap,
                                  DataLists dataLists,
                                  SupplyFareDetailDTO.Builder fareDetailBuilder) {
        if (flight == null || fareDetail == null || fareDetail.getPrice() == null) {
            return;
        }

        String[] segmentRefs = flight.getSegmentReferences().split("\\s+");
        Price price = fareDetail.getPrice();
        
        for (int i = 0; i < segmentRefs.length; i++) {
            String segmentRef = segmentRefs[i];
            FlightSegment segment = findSegmentByRef(dataLists, segmentRef);
            
            if (segment != null && segmentRefMap.containsKey(segmentRef)) {
                String flightKey = segmentRefMap.get(segmentRef);
                SupplySegmentProductInfo.Builder segProductBuilder = createSegmentProductInfo(
                    offerItem, i, price, (i == 0));
                    
                fareDetailBuilder.putSegPrdctInfo(flightKey, segProductBuilder.build());
            }
        }
    }

    /**
     * Create segment product info for a flight segment
     */
    private SupplySegmentProductInfo.Builder createSegmentProductInfo(
        OfferItem offerItem, int index,
        Price price, boolean isFirstSegment) {
        
        SupplySegmentProductInfo.Builder segProductBuilder = SupplySegmentProductInfo.newBuilder();
        
        // Set fare basis info if available
        setFareBasisInfo(segProductBuilder, offerItem, index);
        
        // Set baggage info
        setBaggageInfo(segProductBuilder);
        
        // Set segment fare
        setSegmentFare(segProductBuilder, price, isFirstSegment);
        
        segProductBuilder.setFareExpDate("");
        
        return segProductBuilder;
    }

    /**
     * Set fare basis info for a segment
     */
    private void setFareBasisInfo(SupplySegmentProductInfo.Builder segProductBuilder, 
                                OfferItem offerItem, int segmentIndex) {
        if (!offerItem.getFareComponent().isEmpty()) {
            FareComponent fareComponent = offerItem.getFareComponent().get(0);
            FareBasis fareBasis = fareComponent.getFareBasis();
            
            // Split space-separated values and get the corresponding value for this segment
            String[] fareBasisCodes = fareBasis.getFareBasisCode().getCode().split("\\s+");
            String[] rbdValues = fareBasis.getRbd().split("\\s+");
            String[] cabinTypes = fareBasis.getCabinType().split("\\s+");
            
            // Use the value at index i, or the last value if i is out of bounds
            String fareBasisCode = getValueAtIndexOrLast(fareBasisCodes, segmentIndex);
            String rbd = getValueAtIndexOrLast(rbdValues, segmentIndex);
            String cabinType = getValueAtIndexOrLast(cabinTypes, segmentIndex);
            
            segProductBuilder.setFareBasis(fareBasisCode.trim());
            segProductBuilder.setFareClass(rbd.trim());
            segProductBuilder.setProductClass(rbd.trim());
            segProductBuilder.setCabin(cabinType.trim());
        }
    }

    /**
     * Get value at specified index or the last value if index is out of bounds
     */
    private String getValueAtIndexOrLast(String[] values, int index) {
        if (values.length == 0) return "";
        return index < values.length ? values[index] : values[values.length - 1];
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

    /**
     * Set segment fare info
     */
    private void setSegmentFare(SupplySegmentProductInfo.Builder segProductBuilder, 
                              Price price, boolean isFirstSegment) {
        SupplySegmentFare.Builder sgFareBuilder = SupplySegmentFare.newBuilder();
        
        if (isFirstSegment) {
            // First segment gets the full fare
            sgFareBuilder.setBs(price.getBaseAmount().getBookingCurrencyPrice())
                .setTx(price.getTaxAmount().getBookingCurrencyPrice())
                .setTot(price.getTotalAmount().getBookingCurrencyPrice())
                .setDiscount(0.0)
                .setAirlineFixedFee(0.0);
                
            // Copy tax breakups to first segment
            if (price.getTaxes() != null) {
                for (Tax tax : price.getTaxes()) {
                    SupplyTaxBreakupDTO.Builder taxBreakupBuilder = SupplyTaxBreakupDTO.newBuilder();
                    taxBreakupBuilder.setAmnt(tax.getBookingCurrencyPrice());
                    taxBreakupBuilder.setCode(tax.getTaxCode());
                    taxBreakupBuilder.setMsg("");
                    sgFareBuilder.addTaxBreakups(taxBreakupBuilder.build());
                }
            }
        } else {
            // Other segments get zero fare
            sgFareBuilder.setBs(0.0)
                .setTx(0.0)
                .setTot(0.0)
                .setDiscount(0.0)
                .setAirlineFixedFee(0.0);
        }
        
        sgFareBuilder.addAllAirlineFixedFees(Collections.emptyList());
        segProductBuilder.setSgFare(sgFareBuilder.build());
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
                    .setGender(SupplyGenderOuterClass.SupplyGender.MALE)
                    .setEmailId(getFirstOrEmpty(dataLists.getContactEmail()))
                    .setMobileNumber(getFirstOrEmpty(dataLists.getContactNumber()))
                    .setMobileNumberCountryCode("")
                    .setPaxType(SupplyPaxType.ADULT)
                    .setDateOfBirth("")
                    .setNationality("")
                    .setPwdLine("")
                    .setPtcCode("");
                
                fareInfoBuilder.addTravelerInfos(travelerBuilder.build());
            }
        }
    }

    private String getFirstOrEmpty(List<String> list) {
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }
}