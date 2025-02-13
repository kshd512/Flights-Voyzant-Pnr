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
import com.mmt.flights.supply.common.enums.SupplyPnrStatusTypeOuterClass.SupplyPnrStatusType;
import com.mmt.flights.supply.common.enums.SupplyStatus;
import com.mmt.flights.supply.pnr.v4.request.SupplyPnrRequestDTO;
import io.grpc.xds.shaded.io.envoyproxy.envoy.api.v2.core.ApiVersion;
import org.apache.commons.lang3.StringUtils;
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
                builder.getFlightLookUpListMap(), "0", cmsMapHolder, version));
                
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
                                               String pnrGroupNo,
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

        builder.setFrInfo(getFareInfo(pnrGroupNo, order, dataLists, segmentRefMap, flightKeyMap, cmsMapHolder, flightToJourneyMap));
        builder.setPaxSegmentInfo(getPaxSegmentInfo(order, dataLists));
        return builder.build();
    }

    private List<SupplyBookingJourneyDTO> getJourneys(DataLists dataLists,
                                                     Map<String, String> segmentRefMap,
                                                     Map<String, SupplyFlightDTO> flightLookupMap,
                                                     String pnrGroupNo) {
        List<SupplyBookingJourneyDTO> journeys = new ArrayList<>();
        
        if (dataLists != null && dataLists.getFlightSegmentList() != null) {
            FlightSegmentList segmentList = dataLists.getFlightSegmentList();
            for (FlightSegment segment : segmentList.getFlightSegment()) {
                SupplyBookingJourneyDTO journey = getJourney(segment, flightLookupMap, segmentRefMap, pnrGroupNo);
                journeys.add(journey);
            }
        }
        
        return journeys;
    }

    private SupplyBookingJourneyDTO getJourney(FlightSegment segment,
                                              Map<String, SupplyFlightDTO> flightLookupMap,
                                              Map<String, String> segmentRefMap,
                                              String pnrGroupNo) {
        SupplyBookingJourneyDTO.Builder builder = SupplyBookingJourneyDTO.newBuilder();
        
        builder.setDepDate(segment.getDeparture().getDate() + "T" + segment.getDeparture().getTime());
        builder.setArrDate(segment.getArrival().getDate() + "T" + segment.getArrival().getTime());
        
        if (segment.getFlightDetail() != null && segment.getFlightDetail().getFlightDuration() != null) {
           // builder.setDurInMins(Integer.parseInt(segment.getFlightDetail().getFlightDuration().getValue()));
        }

        String fltkey = segmentRefMap.get(segment.getSegmentKey());
        SupplyFlightDTO flight = flightLookupMap.get(fltkey);
        
        if (flight != null) {
            builder.addFlightDtlsInfo(getFlightDetailsInfo(fltkey, pnrGroupNo));
            builder.setJrnyKey(AdapterUtil.getJourneyKey(Arrays.asList(flight)));
        }

        return builder.build();
    }

    private SupplyFareInfoDTO getFareInfo(String pnrGroupNo, Order order, DataLists dataLists,
                                        Map<String, String> segmentRefMap,
                                        Map<String, String> flightKeyMap,
                                        CMSMapHolder cmsMapHolder,
                                        Map<String, String> flightToJourneyMap) {
        SupplyFareInfoDTO.Builder builder = SupplyFareInfoDTO.newBuilder();
        SupplyPnrFareInfoDTO.Builder fareInfoBuilder = SupplyPnrFareInfoDTO.newBuilder();
        setPnrs(fareInfoBuilder, order);
        
        if (order.getOfferItem() != null) {
            Map<String, SupplyFareDetailDTO.Builder> paxTypeFareBuilderMap = new HashMap<>();
            
            for (OfferItem offerItem : order.getOfferItem()) {
                String paxType = offerItem.getPassengerType();
                SupplyFareDetailDTO.Builder fareDetailBuilder = paxTypeFareBuilderMap.computeIfAbsent(
                    paxType, k -> SupplyFareDetailDTO.newBuilder());

                if (offerItem.getTotalPriceDetail() != null) {
                    fareDetailBuilder.setBs(offerItem.getTotalPriceDetail().getTotalAmount().getBookingCurrencyPrice());
                    fareDetailBuilder.setTx(0.0); // Set tax if available
                    fareDetailBuilder.setTot(offerItem.getTotalPriceDetail().getTotalAmount().getBookingCurrencyPrice());
                }

                fareDetailBuilder.setNoOfPax(offerItem.getPassengerQuantity());

                if (offerItem.getFareComponent() != null) {
                    for (FareComponent fareComponent : offerItem.getFareComponent()) {
                        processSegmentFares(fareComponent, segmentRefMap, fareDetailBuilder);
                    }
                }
            }

            paxTypeFareBuilderMap.forEach((paxType, fareDetailBuilder) -> 
                fareInfoBuilder.putPaxFares(paxType, fareDetailBuilder.build()));
        }
        
        builder.putPnrGrpdFrInfo(Integer.parseInt(pnrGroupNo), fareInfoBuilder.build());
        return builder.build();
    }

    private void processSegmentFares(FareComponent fareComponent, 
                                   Map<String, String> segmentRefMap,
                                   SupplyFareDetailDTO.Builder fareDetailBuilder) {
        String segmentKey = fareComponent.getSegmentRefs();
        String flightKey = segmentRefMap.get(segmentKey);
        
        if (flightKey != null && fareComponent.getFareBasis() != null) {
            SupplySegmentProductInfo.Builder segProductBuilder = SupplySegmentProductInfo.newBuilder();
            segProductBuilder.setFareBasis(fareComponent.getFareBasis().getFareBasisCode().getCode());
            segProductBuilder.setFareClass(fareComponent.getFareBasis().getRbd());
            segProductBuilder.setCabin(fareComponent.getFareBasis().getCabinType());
            fareDetailBuilder.putSegPrdctInfo(flightKey, segProductBuilder.build());
        }
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

    private SupplyFlightDetailDTO getFlightDetailsInfo(String fltKey, String pnrGroupNo) {
        return SupplyFlightDetailDTO.newBuilder()
            .setFltLookUpKey(fltKey)
            .setPnrGroupNum(Integer.parseInt(pnrGroupNo))
            .build();
    }
}