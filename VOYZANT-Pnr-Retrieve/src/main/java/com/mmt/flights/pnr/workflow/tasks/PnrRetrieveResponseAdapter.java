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
import org.apache.commons.math3.analysis.function.Add;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class PnrRetrieveResponseAdapter implements MapTask {
    
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
                                              FlowState state) throws IOException {
        
        ApiVersion version = state.getValue(FlowStateKey.VERSION);
        SupplyBookingResponseDTO.Builder builder = SupplyBookingResponseDTO.newBuilder();
        Order order = orderViewRS.getOrder().get(0);
        DataLists dataLists = orderViewRS.getDataLists();
        
        Map<String, String> segmentRefMap = new HashMap<>();
        Map<String, String> flightKeyMap = new HashMap<>();
        int segmentId = 0;
        int journeyId = 0;

        if (dataLists != null && dataLists.getFlightSegmentList() != null) {
            for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
                SupplyFlightDTO flight = getSimpleFlight(segment, segmentId, journeyId);
                String fltkey = AdapterUtil.getJourneyKey(Arrays.asList(flight));
                
                builder.putFlightLookUpList(fltkey, flight);
                flightKeyMap.put(flight.getDepInfo().getArpCd() + flight.getArrInfo().getArpCd(), fltkey);
                
                if (flight.getTchStpList() != null) {
                    String lastAirport = flight.getDepInfo().getArpCd();
                    for (SupplyTechnicalStopDTO techStop : flight.getTchStpList()) {
                        String currentAirPort = techStop.getLocInfo().getArpCd();
                        flightKeyMap.put(lastAirport + currentAirPort, fltkey);
                        lastAirport = currentAirPort;
                    }
                    flightKeyMap.put(lastAirport + flight.getArrInfo().getArpCd(), fltkey);
                }
                
                segmentRefMap.put(segment.getSegmentKey(), fltkey);
                segmentId++;
                journeyId++;
            }

            builder.setPnrStatus(SupplyPnrStatusType.ACTIVE);
            builder.setGstInfo(getGSTInfo(order));
            builder.setContactInfo(getContactInfo(dataLists));
        } else {
            builder.setPnrStatus(SupplyPnrStatusType.CUSTOMER_CANCELED);
        }

        builder.setBookingInfo(getBookingInfo(order, dataLists, segmentRefMap, flightKeyMap,
                builder.getFlightLookUpListMap(), 0, cmsMapHolder, version));
                
        builder.setMetaData(getMetaData(order, cmsMapHolder.getCmsId(), supplierLatency, state,
                supplyPnrRequestDTO.getEnableTrace()));
                
        builder.setMiscData(getMiscInfo(order, builder.getFlightLookUpListMap()));
        builder.setStatus(SupplyStatus.SUCCESS);

        return builder.build();
    }

    private SupplyFlightDTO getSimpleFlight(FlightSegment segment, int segmentId, int journeyId) throws IOException {
        SupplyFlightDTO.Builder builder = SupplyFlightDTO.newBuilder();
        
        // Set departure info
        AirportInfo departure = segment.getDeparture();
        SupplyLocationInfoDTO.Builder depBuilder = SupplyLocationInfoDTO.newBuilder();
        depBuilder.setArpCd(departure.getAirportCode());
        depBuilder.setArpNm(departure.getAirportName());
        if (departure.getTerminal() != null) {
            depBuilder.setTrmnl(departure.getTerminal().getName());
        }
        builder.setDepTime(departure.getDate() + "T" + departure.getTime());
        builder.setDepInfo(depBuilder.build());
        
        // Set arrival info
        AirportInfo arrival = segment.getArrival();
        SupplyLocationInfoDTO.Builder arrBuilder = SupplyLocationInfoDTO.newBuilder();
        arrBuilder.setArpCd(arrival.getAirportCode());
        arrBuilder.setArpNm(arrival.getAirportName());
        if (arrival.getTerminal() != null) {
            arrBuilder.setTrmnl(arrival.getTerminal().getName());
        }
        builder.setArrTime(arrival.getDate() + "T" + arrival.getTime());
        builder.setArrInfo(arrBuilder.build());

        // Set carrier info
        Carrier marketingCarrier = segment.getMarketingCarrier();
        builder.setMrkAl(marketingCarrier.getAirlineID());
        builder.setFltNo(marketingCarrier.getFlightNumber().replaceAll("\\s", ""));
        
        Carrier operatingCarrier = segment.getOperatingCarrier();
        builder.setOprAl(operatingCarrier != null ? operatingCarrier.getAirlineID() : marketingCarrier.getAirlineID());
        
        // Set equipment and duration
        Equipment equipment = segment.getEquipment();
        if (equipment != null) {
            builder.setArcrfTyp(equipment.getAircraftCode());
        }
        
        FlightDetail flightDetail = segment.getFlightDetail();
        if (flightDetail != null && flightDetail.getFlightDuration() != null) {
           // builder.setDurInMins(Integer.parseInt(flightDetail.getFlightDuration().getValue()));
        }

        // Set segment identifiers
        builder.setSuppSegKey(segment.getSegmentKey());
        builder.setSuppid(String.valueOf(segmentId));
        builder.setMarriedSegId(String.valueOf(journeyId));
        
        // Set cabin and booking class
        if (segment.getCode() != null) {
            builder.setClassOfService(segment.getCode().getMarriageGroup());
        }
        
        return builder.build();
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
                builder.putTraceInfo("Response",
                    jaxbHandlerService.marshall(state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE)));
            } catch (Exception e) {
                // Log error but continue
            }
        }
        
        return builder.build();
    }

    private SupplyPnrMiscInfo getMiscInfo(Order order, Map<String, SupplyFlightDTO> flightLookupListMap) {
        SupplyPnrMiscInfo.Builder builder = SupplyPnrMiscInfo.newBuilder();
        builder.setIssuingAgent(order.getOwnerName());
        
        for (SupplyFlightDTO supplyFlightDTO : flightLookupListMap.values()) {
            if (supplyFlightDTO.getIsInternational()) {
                builder.setIsInternational(true);
                break;
            }
        }
        return builder.build();
    }

    private SupplyBookingInfoDTO getBookingInfo(Order order, DataLists dataLists,
                                               Map<String, String> segmentRefMap,
                                               Map<String, String> flightKeyMap,
                                               Map<String, SupplyFlightDTO> flightlookupMap,
                                               int pnrGroupNo,
                                               CMSMapHolder cmsMapHolder,
                                               ApiVersion version) throws IOException {
        SupplyBookingInfoDTO.Builder builder = SupplyBookingInfoDTO.newBuilder();
        
        List<SupplyBookingJourneyDTO> journeys = getJourneys(dataLists, segmentRefMap, flightlookupMap, pnrGroupNo);
        builder.addAllJourneys(journeys);

        Map<String, String> flightToJourneyMap = new HashMap<>();
        if (version != null) {
            for (SupplyBookingJourneyDTO journey : journeys) {
                for (SupplyFlightDetailDTO flight : journey.getFlightDtlsInfoList()) {
                    flightToJourneyMap.put(flight.getFltLookUpKey(), journey.getJrnyKey());
                }
            }
        }

        builder.setFrInfo(getFareInfo(String.valueOf(pnrGroupNo), order, dataLists, segmentRefMap, flightKeyMap, cmsMapHolder, flightToJourneyMap));
        builder.setPaxSegmentInfo(getPaxSegmentInfo(order, dataLists));
        return builder.build();
    }

    private List<SupplyBookingJourneyDTO> getJourneys(DataLists dataLists,
                                                     Map<String, String> segmentRefMap,
                                                     Map<String, SupplyFlightDTO> flightLookupMap,
                                                     int pnrGroupNo) {
        List<SupplyBookingJourneyDTO> journeys = new ArrayList<>();
        
        if (dataLists != null && dataLists.getFlightList() != null) {
            FlightList flightList = dataLists.getFlightList();
            for (Flight flight : flightList.getFlight()) {
                // Each flight can contain multiple segments
                SupplyBookingJourneyDTO journey = getJourneyFromFlight(flight, dataLists, flightLookupMap, segmentRefMap, pnrGroupNo);
                journeys.add(journey);
            }
        }
        return journeys;
    }

    private SupplyBookingJourneyDTO getJourneyFromFlight(Flight flight,
                                                      DataLists dataLists,
                                                      Map<String, SupplyFlightDTO> flightLookupMap,
                                                      Map<String, String> segmentRefMap,
                                                      int pnrGroupNo) {
        SupplyBookingJourneyDTO.Builder builder = SupplyBookingJourneyDTO.newBuilder();
        
        String[] segmentRefs = flight.getSegmentReferences().split("\\s+");
        FlightSegment firstSegment = null;
        FlightSegment lastSegment = null;
        List<String> segmentKeys = new ArrayList<>();
        
        // Find first and last segments and collect all segment keys
        for (String segmentRef : segmentRefs) {
            FlightSegment segment = findSegmentByRef(dataLists, segmentRef);
            if (segment != null) {
                if (firstSegment == null) {
                    firstSegment = segment;
                }
                lastSegment = segment;
                
                String lookupKey = segmentRefMap.get(segmentRef);
                if (lookupKey != null) {
                    segmentKeys.add(lookupKey);
                } else {
                    // Fallback to building key if not found in map
                    lookupKey = buildFlightKey(segment);
                    segmentKeys.add(lookupKey);
                }
            }
        }
        
        if (firstSegment != null && lastSegment != null) {
            builder.setDepDate(firstSegment.getDeparture().getDate() + "T" + firstSegment.getDeparture().getTime());
            builder.setArrDate(lastSegment.getArrival().getDate() + "T" + lastSegment.getArrival().getTime());
            
            // Add all segments' flight details
            for (String lookupKey : segmentKeys) {
                builder.addFlightDtlsInfo(getFlightDetailsInfo(lookupKey, pnrGroupNo));
            }
            
            // Set journey key as all segment lookup keys joined by |
            String journeyKey = String.join("|", segmentKeys);
            builder.setJrnyKey(journeyKey);
        }

        return builder.build();
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
        fareInfoBuilder.setCreationDate(order.getTimeLimits() != null ? order.getTimeLimits().getOfferExpirationDateTime() : "");
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
                                        Map<String, String> segmentRefMap,
                                        Map<String, String> flightKeyMap,
                                        CMSMapHolder cmsMapHolder,
                                        Map<String, String> flightToJourneyMap) {
        SupplyFareInfoDTO.Builder builder = SupplyFareInfoDTO.newBuilder();
        SupplyPnrFareInfoDTO.Builder fareInfoBuilder = buildBasicFareInfo(order);
        
        // Track unique passenger references per pax type
        Map<String, Set<String>> paxTypeRefsMap = new HashMap<>();

        if (order.getOfferItem() != null) {
            Map<String, SupplyFareDetailDTO.Builder> paxTypeFareBuilderMap = new HashMap<>();
            Map<String, Integer> paxTypeCountMap = new HashMap<>();
            Map<String, Map<String, Double>> paxTypeTaxBreakupMap = new HashMap<>();
            
            // Track totals for the entire order
            double totalBs = 0.0;
            double totalTx = 0.0;
            double totalAmount = 0.0;
            Map<String, Double> orderTotalTaxBreakupMap = new HashMap<>();
            
            // First pass: accumulate pax counts and fares
            for (OfferItem offerItem : order.getOfferItem()) {
                String paxType = offerItem.getPassengerType();
                // Collect unique passenger references from FareDetail or Service passengerRefs
                if (offerItem.getFareDetail() != null 
                    && StringUtils.isNotBlank(offerItem.getFareDetail().getPassengerRefs())) {
                    String[] paxRefs = offerItem.getFareDetail().getPassengerRefs().split(",");
                    Set<String> paxRefSet = paxTypeRefsMap.computeIfAbsent(paxType, k -> new HashSet<>());
                    for (String ref : paxRefs) {
                        paxRefSet.add(ref.trim());
                    }
                }

                // Instead of summing passengerQuantity directly, skip paxTypeCountMap.merge
                // ...existing fare accumulation code for base/tax/total...

                SupplyFareDetailDTO.Builder fareDetailBuilder = paxTypeFareBuilderMap.computeIfAbsent(
                    paxType, k -> initializeFareDetailBuilder());
                
                FareDetail fareDetail = offerItem.getFareDetail();
                if (fareDetail != null && fareDetail.getPrice() != null) {
                    Price price = fareDetail.getPrice();
                    
                    // Accumulate fares for this passenger type
                    fareDetailBuilder.setBs(fareDetailBuilder.getBs() + price.getBaseAmount().getBookingCurrencyPrice());
                    fareDetailBuilder.setTx(fareDetailBuilder.getTx() + price.getTaxAmount().getBookingCurrencyPrice());
                    fareDetailBuilder.setTot(fareDetailBuilder.getTot() + price.getTotalAmount().getBookingCurrencyPrice());
                    
                    // Accumulate order totals
                    totalBs += price.getBaseAmount().getBookingCurrencyPrice();
                    totalTx += price.getTaxAmount().getBookingCurrencyPrice();
                    totalAmount += price.getTotalAmount().getBookingCurrencyPrice();
                    
                    // Initialize tax breakup map for this passenger type if not exists
                    Map<String, Double> taxBreakupMap = paxTypeTaxBreakupMap.computeIfAbsent(paxType, k -> new HashMap<>());
                    
                    // Add tax breakups
                    if (price.getTaxes() != null) {
                        for (Tax tax : price.getTaxes()) {
                            String taxCode = tax.getTaxCode();
                            double amount = tax.getBookingCurrencyPrice();
                            
                            // Accumulate tax breakups for passenger type
                            taxBreakupMap.merge(taxCode, amount, Double::sum);
                            
                            // Accumulate tax breakups for order total
                            orderTotalTaxBreakupMap.merge(taxCode, amount, Double::sum);
                        }
                    }
                }
                
                // Process segments and fare components
                if (offerItem.getFareComponent() != null) {
                    String flightRef = getFlightRefForOfferItem(offerItem, dataLists);
                    Flight flight = findFlightByRef(dataLists, flightRef);
                    
                    if (flight != null) {
                        processFlightFares(flight, offerItem, fareDetail, segmentRefMap, dataLists, fareDetailBuilder);
                    }
                }
            }
            
            // Second pass: build final FareDetails with accumulated values
            paxTypeFareBuilderMap.forEach((paxType, fareDetailBuilder) -> {
                Set<String> uniqueRefs = paxTypeRefsMap.getOrDefault(paxType, Collections.emptySet());
                fareDetailBuilder.setNoOfPax(uniqueRefs.size());
                fareDetailBuilder.setAirlineFixedFee(0.0);
                fareDetailBuilder.addAllAirlineFixedFees(Collections.emptyList());
                
                // Clear existing tax breakups and add accumulated ones
                fareDetailBuilder.clearTaxBreakups();
                Map<String, Double> taxBreakupMap = paxTypeTaxBreakupMap.get(paxType);
                if (taxBreakupMap != null) {
                    taxBreakupMap.forEach((code, amount) -> {
                        SupplyTaxBreakupDTO.Builder taxBreakupBuilder = SupplyTaxBreakupDTO.newBuilder();
                        taxBreakupBuilder.setAmnt(amount);
                        taxBreakupBuilder.setCode(code);
                        taxBreakupBuilder.setMsg("");
                        fareDetailBuilder.addTaxBreakups(taxBreakupBuilder.build());
                    });
                }
                
                fareInfoBuilder.putPaxFares(paxType, fareDetailBuilder.build());
            });
            
            // Set total fare info with accumulated values
            SupplyTotalFareDTO.Builder totFrBuilder = SupplyTotalFareDTO.newBuilder();
            totFrBuilder.setBs(totalBs);
            totFrBuilder.setTx(totalTx);
            totFrBuilder.setTot(totalAmount);
            totFrBuilder.setAirlineFixedFee(0.0);
            
            // Add accumulated tax breakups to total fare
            orderTotalTaxBreakupMap.forEach((code, amount) -> {
                SupplyTaxBreakupDTO.Builder taxBreakupBuilder = SupplyTaxBreakupDTO.newBuilder();
                taxBreakupBuilder.setAmnt(amount);
                taxBreakupBuilder.setCode(code);
                taxBreakupBuilder.setMsg("");
            });
            
            fareInfoBuilder.setTotFr(totFrBuilder.build());
            
            // Add traveler addons
            fareInfoBuilder.putTravelerAddons("0", buildTravelerAddons(dataLists, segmentRefMap));

            //Add traveler infos
            addTravelerInfos(fareInfoBuilder, dataLists);
        }
        
        builder.putPnrGrpdFrInfo(Integer.parseInt(pnrGroupNo), fareInfoBuilder.build());
        return builder.build();
    }

    private String getFlightRefForOfferItem(OfferItem offerItem, DataLists dataLists) {
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
            
            if (segment != null) {
                String flightKey = segmentRefMap.get(segmentRef);
                if (flightKey != null) {
                    SupplySegmentProductInfo.Builder segProductBuilder = SupplySegmentProductInfo.newBuilder();
                    
                    // Set fare basis info
                    if (!offerItem.getFareComponent().isEmpty()) {
                        FareComponent fareComponent = offerItem.getFareComponent().get(0);
                        FareBasis fareBasis = fareComponent.getFareBasis();
                        
                        segProductBuilder.setFareBasis(fareBasis.getFareBasisCode().getCode());
                        segProductBuilder.setFareClass(fareBasis.getRbd());
                        segProductBuilder.setProductClass("J");
                        segProductBuilder.setCabin("ECONOMY");
                    }
                    
                    // Set baggage info
                    SupplySegmentProductInfo.SupplyBagGroup.Builder baggageBuilder = SupplySegmentProductInfo.SupplyBagGroup.newBuilder();
                    SupplyBagInfo.Builder cabinBagBuilder = SupplyBagInfo.newBuilder();
                    cabinBagBuilder.setNumOfPieces(1);
                    cabinBagBuilder.setWeightPerPiece(15);
                    cabinBagBuilder.setWeightUnit("Kilograms");
                    cabinBagBuilder.setTotalWeight(0);
                    baggageBuilder.setCabinBag(cabinBagBuilder.build());
                    segProductBuilder.setBaggageInfo(baggageBuilder.build());
                    
                    // Set segment fare - full amount for first segment only
                    SupplySegmentFare.Builder sgFareBuilder = SupplySegmentFare.newBuilder();
                    
                    if (i == 0) {
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
                    
                    // Set fare key
                    String fareKey = String.format("0~%s~~%s~%s~2002~~0~0~~X",
                        offerItem.getFareComponent().get(0).getFareBasis().getRbd(),
                        offerItem.getFareComponent().get(0).getFareBasis().getFareBasisCode().getCode(),
                        segment.getMarketingCarrier().getAirlineID());
                    segProductBuilder.setFareKey(fareKey);
                    segProductBuilder.setFareExpDate("");
                    
                    fareDetailBuilder.putSegPrdctInfo(flightKey, segProductBuilder.build());
                }
            }
        }
    }

    private String getFlightDirection(Flight flight, DataLists dataLists) {
        String[] segmentRefs = flight.getSegmentReferences().split("\\s+");
        if (segmentRefs.length > 0) {
            FlightSegment firstSegment = findSegmentByRef(dataLists, segmentRefs[0]);
            if (firstSegment != null) {
                return firstSegment.getDeparture().getAirportCode() + "-" + 
                       firstSegment.getArrival().getAirportCode();
            }
        }
        return "";
    }

    private void setPnrs(SupplyPnrFareInfoDTO.Builder builder, Order order) {
        builder.setAPnr(order.getGdsBookingReference());
        builder.setSPnr(order.getOrderID());
        builder.setValidatingCarrier(order.getOwner());
        builder.setCreationDate(order.getTimeLimits() != null ? order.getTimeLimits().getOfferExpirationDateTime() : "");
        builder.setTimeZoneOffset("+00:00"); // Set appropriate timezone if available
    }

    private SupplyPaxSegmentInfo getPaxSegmentInfo(Order order, DataLists dataLists) {
        SupplyPaxSegmentInfo.Builder builder = SupplyPaxSegmentInfo.newBuilder();
        SupplyPnrStatusDTO.Builder statusBuilder = SupplyPnrStatusDTO.newBuilder();
        
        if (dataLists.getFlightSegmentList() != null) {
            for (FlightSegment segment : dataLists.getFlightSegmentList().getFlightSegment()) {
                String segmentKey = segment.getSegmentKey();
                SupplyPnrLiftStatusDTOList.Builder liftStatusList = SupplyPnrLiftStatusDTOList.newBuilder();
                statusBuilder.putSegmentLiftStatus(segmentKey, liftStatusList.build());
            }
        }
        
        builder.setPnrStatus(statusBuilder.build());
        return builder.build();
    }

    private SupplyFlightDetailDTO getFlightDetailsInfo(String fltkey, int pnrGroupNo) {
        return SupplyFlightDetailDTO.newBuilder()
            .setFltLookUpKey(fltkey)
            .setPnrGroupNum(pnrGroupNo)
            .build();
    }

    private SupplyTravelerAddons buildTravelerAddons(DataLists dataLists, Map<String, String> segmentRefMap) {
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
                        
                     /*   // Add SEATS addon
                        addonsMap.put("SEATS", SupplyAddons.newBuilder()
                            .setAddonsType("SEATS")
                            .setAmount(0.0)
                            .setCode("26F")
                            .setStatus("SUCCESS")
                            .setChargeable(false)
                            .setQuantity(0)
                            .setValue(0)
                            .setUnit("")
                            .setDescription("")
                            .setWeightPerPiece(0)
                            .setWeightUnit("")
                            .setSsrType("NOT_SET")
                            .setEmdRequired(false)
                            .setPreference("SPT_NOT_SET")
                            .build());
                        
                        // Add MEALS addon
                        addonsMap.put("MEALS", SupplyAddons.newBuilder()
                            .setAddonsType("MEALS")
                            .setAmount(0.0)
                            .setCode("FRCK")
                            .setStatus("SUCCESS")
                            .setChargeable(false)
                            .setQuantity(0)
                            .setValue(0)
                            .setUnit("")
                            .setDescription("")
                            .setWeightPerPiece(0)
                            .setWeightUnit("")
                            .setSsrType("NOT_SET")
                            .setEmdRequired(false)
                            .setPreference("SPT_NOT_SET")
                            .build());*/
                        
                        addonsMapBuilder.putAllAddons(addonsMap);
                        flightLevelAddonsMap.put(flightKey, addonsMapBuilder.build());
                    }
                }
            }
        }
        
        travelerAddonsBuilder.putAllFlightLevelAddons(flightLevelAddonsMap);
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