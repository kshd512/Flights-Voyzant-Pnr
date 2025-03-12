package com.mmt.flights.odc.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.OrderReshopRS;
import com.mmt.flights.entity.odc.OrderReshopResponse;
import com.mmt.flights.entity.odc.ReshopOffer;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.odc.common.ConversionFactor;
import com.mmt.flights.odc.common.enums.PaxType;
import com.mmt.flights.odc.search.*;
import com.mmt.flights.odc.util.RKeyBuilderUtil;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationGroupV2;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationV2;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.search.v4.response.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ODCSearchResponseAdapterTask implements MapTask {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState state) throws Exception {
        try {
            OrderReshopResponse orderReshopResponse = getOrderReshopResponse(state);
            OrderReshopRS response = orderReshopResponse.getOrderReshopRS();
            validateResponse(response);

            DateChangeSearchRequest request = state.getValue(FlowStateKey.REQUEST);
            String cmsId = request.getCmsId();

            SimpleSearchResponseV2 searchResponse = new SimpleSearchResponseV2();
            searchResponse.setConversionFactors(createConversionFactors(response));

            FlightJourneyContext journeyContext = processFlightJourneys(response);
            Pair<List<SimpleSearchRecommendationGroupV2>, List<SimpleSearchRecommendationGroupV2>> recommendationGroups =
                processRecommendationGroups(response, journeyContext, state, cmsId);

            searchResponse.setSameFareRcomGrps(recommendationGroups.getLeft());
            searchResponse.setOtherFareRcomGrps(recommendationGroups.getRight());
            searchResponse.setItineraryJourneyList(journeyContext.getItineraryJourneyList());

            return state.toBuilder()
                    .addValue(FlowStateKey.RESPONSE, searchResponse)
                    .build();
        } catch (Exception e) {
            throw new PSErrorException("", ErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }

    private OrderReshopResponse getOrderReshopResponse(FlowState state) throws Exception {
        String responseJson = state.getValue(FlowStateKey.ODC_SEARCH_RESPONSE);
        if (responseJson == null) {
            throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
        }
        return objectMapper.readValue(responseJson, OrderReshopResponse.class);
    }

    private void validateResponse(OrderReshopRS response) throws PSErrorException {
        if (!response.isSuccess() || response.getReshopOffers() == null || response.getReshopOffers().isEmpty()) {
            throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }

    private List<ConversionFactor> createConversionFactors(OrderReshopRS response) {
        ConversionFactor factor = new ConversionFactor();
        String currency = response.getReshopOffers().get(0).getReshopOffers().get(0).getBookingCurrencyCode();
        factor.setFromCurrency(currency);
        factor.setToCurrency(currency);
        factor.setRoe(1.0);
        return Collections.singletonList(factor);
    }

    private FlightJourneyContext processFlightJourneys(OrderReshopRS response) {
        FlightJourneyContext context = new FlightJourneyContext();
        for (ReshopOffer reshopOffer : response.getReshopOffers().get(0).getReshopOffers()) {
            String[] flightRefs = reshopOffer.getAddOfferItem().get(0).getService().get(0).getFlightRefs().split(" ");
            for (String flightRef : flightRefs) {
                if (!context.getFlightKeyToJourneyIndex().containsKey(flightRef)) {
                    SimpleJourney journey = createJourneyFromFlight(flightRef, response);
                    if (journey != null) {
                        context.addJourney(flightRef, journey);
                    }
                }
            }
        }
        return context;
    }

    private SimpleJourney createJourneyFromFlight(String flightRef, OrderReshopRS response) {
        String segmentRefs = findSegmentReferences(flightRef, response);
        if (segmentRefs.isEmpty()) {
            return null;
        }

        List<SimpleFlight> flights = new ArrayList<>();
        StringBuilder journeyKeyBuilder = new StringBuilder();
        boolean isFirst = true;

        for (String segmentKey : segmentRefs.split(" ")) {
            com.mmt.flights.entity.pnr.retrieve.response.FlightSegment segment = findFlightSegment(segmentKey, response);
            if (segment != null) {
                appendJourneyKeyPart(journeyKeyBuilder, segment, isFirst);
                flights.add(createSimpleFlight(segment));
                isFirst = false;
            }
        }

        if (flights.isEmpty()) {
            return null;
        }

        SimpleJourney journey = new SimpleJourney();
        journey.setFlights(flights);
        journey.setJourneyKey(journeyKeyBuilder.toString());
        journey.setDepDate(flights.get(0).getDepTime());
        journey.setArrDate(flights.get(flights.size()-1).getArrTime());
        journey.setDuration(calculateJourneyDuration(flights));

        return journey;
    }

    private String findSegmentReferences(String flightRef, OrderReshopRS response) {
        return response.getDataLists().getFlightList().getFlight().stream()
                .filter(f -> f.getFlightKey().equals(flightRef))
                .findFirst()
                .map(f -> f.getSegmentReferences())
                .orElse("");
    }

    private com.mmt.flights.entity.pnr.retrieve.response.FlightSegment findFlightSegment(String segmentKey, OrderReshopRS response) {
        return response.getDataLists().getFlightSegmentList().getFlightSegment().stream()
                .filter(s -> s.getSegmentKey().equals(segmentKey))
                .findFirst()
                .orElse(null);
    }

    private void appendJourneyKeyPart(StringBuilder journeyKeyBuilder, com.mmt.flights.entity.pnr.retrieve.response.FlightSegment segment, boolean isFirst) {
        if (!isFirst) {
            journeyKeyBuilder.append("|");
        }
        String[] dateParts = segment.getDeparture().getDate().split("-");
        String dateStr = String.format("%s%s%s", dateParts[2], dateParts[1], dateParts[0].substring(2));
        String timeStr = segment.getDeparture().getTime().substring(0, 5).replace(":", "");
        String dateTime = dateStr + timeStr;

        journeyKeyBuilder.append(segment.getDeparture().getAirportCode())
                .append("$")
                .append(segment.getArrival().getAirportCode())
                .append("$")
                .append(dateTime)
                .append("$")
                .append(segment.getMarketingCarrier().getAirlineID())
                .append("-")
                .append(segment.getMarketingCarrier().getFlightNumber());
    }

    private Pair<List<SimpleSearchRecommendationGroupV2>, List<SimpleSearchRecommendationGroupV2>>
            processRecommendationGroups(OrderReshopRS response, FlightJourneyContext journeyContext, FlowState state, String cmsId) throws Exception {
        List<SimpleSearchRecommendationGroupV2> sameFareGroups = new ArrayList<>();
        List<SimpleSearchRecommendationGroupV2> otherFareGroups = new ArrayList<>();

        // Get original PNR fare family
        String pnrResponseData = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
        OrderViewRS orderViewRS = objectMapper.readValue(pnrResponseData, OrderViewRS.class);
        String originalFareFamily = getOriginalPnrFareFamily(orderViewRS);

        for (ReshopOffer reshopOffer : response.getReshopOffers().get(0).getReshopOffers()) {
            SimpleSearchRecommendationGroupV2 group = createRecommendationGroup(reshopOffer, response, journeyContext, state, cmsId);
            if (group.getSingleAdultFare() != null) {
                // Get fare family for current offer
                String offerFareFamily = getFareFamily(reshopOffer, response);
                
                // Compare fare families
                if (originalFareFamily != null && originalFareFamily.equals(offerFareFamily)) {
                    sameFareGroups.add(group);
                } else {
                    otherFareGroups.add(group);
                }
            }
        }
        return Pair.of(sameFareGroups, otherFareGroups);
    }

    private String getOriginalPnrFareFamily(OrderViewRS orderViewRS) {
        if (orderViewRS != null && orderViewRS.getDataLists() != null 
            && orderViewRS.getDataLists().getPriceClassList() != null 
            && orderViewRS.getDataLists().getPriceClassList().getPriceClass() != null 
            && !orderViewRS.getDataLists().getPriceClassList().getPriceClass().isEmpty()) {
            return orderViewRS.getDataLists().getPriceClassList().getPriceClass().get(0).getName();
        }
        return null;
    }

    private String getFareFamily(ReshopOffer reshopOffer, OrderReshopRS response) {
        if (!reshopOffer.getAddOfferItem().isEmpty() 
            && !reshopOffer.getAddOfferItem().get(0).getFareComponent().isEmpty()) {
            String priceClassRef = reshopOffer.getAddOfferItem().get(0).getFareComponent().get(0).getPriceClassRef();
            if (response.getDataLists() != null 
                && response.getDataLists().getPriceClassList() != null 
                && response.getDataLists().getPriceClassList().getPriceClass() != null) {
                return response.getDataLists().getPriceClassList().getPriceClass().stream()
                    .filter(priceClass -> priceClass.getPriceClassID().equals(priceClassRef))
                    .map(priceClass -> priceClass.getName())
                    .findFirst()
                    .orElse(null);
            }
        }
        return null;
    }

    private SimpleSearchRecommendationGroupV2 createRecommendationGroup(ReshopOffer reshopOffer, OrderReshopRS response, FlightJourneyContext journeyContext, FlowState state, String cmsId) {
        SimpleSearchRecommendationGroupV2 group = new SimpleSearchRecommendationGroupV2();
        group.setAirlines(Collections.singletonList(reshopOffer.getOwner()));

        SimpleSearchRecommendationV2 recommendation = createRecommendation(reshopOffer, response, journeyContext, state, cmsId);
        group.setSearchRecommendations(Collections.singletonList(recommendation));
        group.setSingleAdultFare(recommendation.getPaxWiseFare().get(PaxType.ADULT));

        return group;
    }

    private SimpleSearchRecommendationV2 createRecommendation(ReshopOffer reshopOffer, OrderReshopRS response, FlightJourneyContext journeyContext, FlowState state, String cmsId) {
        SimpleSearchRecommendationV2 recommendation = new SimpleSearchRecommendationV2();
        recommendation.setHandBaggageFare(false);
        recommendation.setRefundable(Boolean.parseBoolean(reshopOffer.getAddOfferItem().get(0).getRefundable()));
        
        String[] flightRefs = reshopOffer.getAddOfferItem().get(0).getService().get(0).getFlightRefs().split(" ");
        List<Integer> journeyIndices = getJourneyIndices(flightRefs, journeyContext);
        recommendation.setItneraryJrnyIndex(journeyIndices);

        Map<PaxType, SimpleFare> paxWiseFare = createPaxWiseFare(reshopOffer);
        recommendation.setPaxWiseFare(paxWiseFare);

        SimpleDateChangeDetails odcDetails = new SimpleDateChangeDetails();
        odcDetails.setRefundAllowed(false);

        //we need to call penalties API and set penalty here.
        //odcDetails.setDateChangeFee(2999.0);
        recommendation.setOdcDetails(odcDetails);

        setFareDetails(recommendation, reshopOffer, state);
        
        String rKey = generateRKey(reshopOffer, response, recommendation, cmsId);
        recommendation.setrKey(rKey);
        
        String fareKey = response.getShoppingResponseId() + "," + reshopOffer.getOfferID();
        recommendation.setFareKey(fareKey);

        return recommendation;
    }

    private List<Integer> getJourneyIndices(String[] flightRefs, FlightJourneyContext journeyContext) {
        List<Integer> indices = new ArrayList<>();
        for (String flightRef : flightRefs) {
            Integer index = journeyContext.getFlightKeyToJourneyIndex().get(flightRef);
            if (index != null) {
                indices.add(index);
            }
        }
        return indices;
    }

    private Map<PaxType, SimpleFare> createPaxWiseFare(ReshopOffer reshopOffer) {
        Map<PaxType, SimpleFare> paxWiseFare = new HashMap<>();
        for (com.mmt.flights.entity.odc.OfferItem offerItem : reshopOffer.getAddOfferItem()) {
            PaxType paxType = getPaxType(offerItem.getPassengerType());
            if (paxType != null) {
                int paxCount = offerItem.getPassengerQuantity();
                SimpleFare simpleFare = new SimpleFare();
                simpleFare.setBase(offerItem.getFareDetail().getPrice().getBaseAmount().getBookingCurrencyPrice() / paxCount);
                simpleFare.setTaxes(offerItem.getFareDetail().getPrice().getTaxAmount().getBookingCurrencyPrice() / paxCount);
                paxWiseFare.put(paxType, simpleFare);
            }
        }
        return paxWiseFare;
    }

    private PaxType getPaxType(String passengerType) {
        for (PaxType type : PaxType.values()) {
            if (type.getPaxType().equals(passengerType)) {
                return type;
            }
        }
        return null;
    }

    private void setFareDetails(SimpleSearchRecommendationV2 recommendation, ReshopOffer reshopOffer, FlowState state) {
        if (!reshopOffer.getAddOfferItem().isEmpty() && !reshopOffer.getAddOfferItem().get(0).getFareComponent().isEmpty()) {
            String priceClassRef = reshopOffer.getAddOfferItem().get(0).getFareComponent().get(0).getPriceClassRef();
            String fareBasisCode = reshopOffer.getAddOfferItem().get(0).getFareComponent().get(0).getFareBasis().getFareBasisCode().getCode();
            recommendation.setFareFamilyName(determineFareFamily(priceClassRef, state));
            recommendation.setFareType(determineFareType(fareBasisCode));
        }
    }

    private String generateRKey(ReshopOffer reshopOffer, OrderReshopRS response, SimpleSearchRecommendationV2 recommendation, String cmsId) {
        Journey journey = createJourneyForRKey(reshopOffer, response);
        PaxCount paxCount = createPaxCount(reshopOffer);

        // Check if return trip
        List<Segment> segments = journey.getSegments();
        if (isReturnTrip(segments)) {
            Journey rtJourney = splitReturnJourney(journey);
            JourneyFare onwardFare = createJourneyFare(reshopOffer, true);
            JourneyFare returnFare = createJourneyFare(reshopOffer, false);
            return RKeyBuilderUtil.buildRKey(recommendation.getPaxWiseFare(), onwardFare, returnFare, paxCount, journey, rtJourney, cmsId);
        } else {
            JourneyFare journeyFare = createJourneyFare(reshopOffer, false);
            return RKeyBuilderUtil.buildRKey(recommendation.getPaxWiseFare(), journeyFare, paxCount, journey, cmsId);
        }
    }

    private Journey createJourneyForRKey(ReshopOffer reshopOffer, OrderReshopRS response) {
        Journey journey = new Journey();
        List<Segment> segments = new ArrayList<>();

        String[] flightRefs = reshopOffer.getAddOfferItem().get(0).getService().get(0).getFlightRefs().split(" ");
        for (String ref : flightRefs) {
            segments.addAll(createSegmentsFromFlightRef(ref, response));
        }

        journey.setSegments(segments);
        return journey;
    }

    private List<Segment> createSegmentsFromFlightRef(String flightRef, OrderReshopRS response) {
        List<Segment> segments = new ArrayList<>();
        String segmentRefs = findSegmentReferences(flightRef, response);
        
        for (String segmentKey : segmentRefs.split(" ")) {
            com.mmt.flights.entity.pnr.retrieve.response.FlightSegment flightSegment = findFlightSegment(segmentKey, response);
            if (flightSegment != null) {
                segments.add(createSegment(flightSegment));
            }
        }
        
        return segments;
    }

    private Segment createSegment(com.mmt.flights.entity.pnr.retrieve.response.FlightSegment flightSegment) {
        Segment segment = new Segment();
        
        Identifier identifier = new Identifier();
        identifier.setCarrierCode(flightSegment.getMarketingCarrier().getAirlineID());
        identifier.setIdentifier(flightSegment.getMarketingCarrier().getFlightNumber());
        segment.setIdentifier(identifier);
        
        Designator designator = new Designator();
        designator.setOrigin(flightSegment.getDeparture().getAirportCode());
        designator.setDestination(flightSegment.getArrival().getAirportCode());
        designator.setDeparture(flightSegment.getDeparture().getDate() + " " + flightSegment.getDeparture().getTime());
        designator.setArrival(flightSegment.getArrival().getDate() + " " + flightSegment.getArrival().getTime());
        segment.setDesignator(designator);
        
        List<Leg> legs = new ArrayList<>();
        Leg leg = new Leg();
        LegInfo legInfo = new LegInfo();
        legInfo.setDepartureTerminal(flightSegment.getDeparture().getTerminal().getName());
        legInfo.setArrivalTerminal(flightSegment.getArrival().getTerminal().getName());
        leg.setLegInfo(legInfo);
        legs.add(leg);
        segment.setLegs(legs);
        
        return segment;
    }

    private boolean isReturnTrip(List<Segment> segments) {
        if (segments.size() >= 2) {
            String firstOrigin = segments.get(0).getDesignator().getOrigin();
            String lastDestination = segments.get(segments.size()-1).getDesignator().getDestination();
            return firstOrigin.equals(lastDestination);
        }
        return false;
    }

    private Journey splitReturnJourney(Journey journey) {
        Journey rtJourney = new Journey();
        List<Segment> segments = journey.getSegments();
        int mid = segments.size() / 2;
        
        rtJourney.setSegments(new ArrayList<>(segments.subList(mid, segments.size())));
        journey.setSegments(new ArrayList<>(segments.subList(0, mid)));
        
        return rtJourney;
    }

    private JourneyFare createJourneyFare(ReshopOffer reshopOffer, boolean isOnward) {
        JourneyFare journeyFare = new JourneyFare();
        List<com.mmt.flights.supply.search.v4.response.Fare> fares = createFares(reshopOffer);
        
        if (isOnward) {
            journeyFare.setFares(new ArrayList<>(fares.subList(0, fares.size()/2)));
        } else {
            journeyFare.setFares(fares);
        }
        
        return journeyFare;
    }

    private List<com.mmt.flights.supply.search.v4.response.Fare> createFares(ReshopOffer reshopOffer) {
        List<com.mmt.flights.supply.search.v4.response.Fare> fares = new ArrayList<>();
        
        for (com.mmt.flights.entity.odc.OfferItem offerItem : reshopOffer.getAddOfferItem()) {
            if (!offerItem.getFareComponent().isEmpty()) {
                com.mmt.flights.supply.search.v4.response.Fare fare = new com.mmt.flights.supply.search.v4.response.Fare();
                
                String fareBasisCode = offerItem.getFareComponent().get(0).getFareBasis().getFareBasisCode().getCode();
                fare.setFareBasisCode(fareBasisCode);
                fare.setClassOfService(offerItem.getFareComponent().get(0).getFareBasis().getRbd());
                fare.setFareClassOfService(fareBasisCode);
                fare.setProductClass(offerItem.getFareComponent().get(0).getFareBasis().getCabinType());
                fare.setTravelClassCode(fareBasisCode.substring(0, 1));
                
                fare.setPassengerFares(createPassengerFares(offerItem));
                
                fares.add(fare);
            }
        }
        
        return fares;
    }

    private List<PassengerFare> createPassengerFares(com.mmt.flights.entity.odc.OfferItem offerItem) {
        List<PassengerFare> passengerFares = new ArrayList<>();
        PassengerFare passengerFare = new PassengerFare();
        passengerFare.setPassengerType(offerItem.getPassengerType());
        
        List<ServiceCharge> serviceCharges = new ArrayList<>();
        
        ServiceCharge baseCharge = new ServiceCharge();
        baseCharge.setAmount(offerItem.getFareDetail().getPrice().getBaseAmount().getBookingCurrencyPrice());
        serviceCharges.add(baseCharge);
        
        ServiceCharge taxCharge = new ServiceCharge();
        taxCharge.setAmount(offerItem.getFareDetail().getPrice().getTaxAmount().getBookingCurrencyPrice());
        serviceCharges.add(taxCharge);
        
        passengerFare.setServiceCharges(serviceCharges);
        passengerFares.add(passengerFare);
        
        return passengerFares;
    }

    private PaxCount createPaxCount(ReshopOffer reshopOffer) {
        PaxCount paxCount = new PaxCount();
        Map<String, Integer> paxTypeCount = new HashMap<>();
        
        for (com.mmt.flights.entity.odc.OfferItem offerItem : reshopOffer.getAddOfferItem()) {
            paxTypeCount.put(offerItem.getPassengerType(), offerItem.getPassengerQuantity());
        }
        
        paxCount.setAdult(paxTypeCount.getOrDefault("ADULT", 0));
        paxCount.setChild(paxTypeCount.getOrDefault("CHILD", 0));
        paxCount.setInfant(paxTypeCount.getOrDefault("INFANT", 0));
        
        return paxCount;
    }

    // Existing helper methods remain unchanged
    private SimpleFlight createSimpleFlight(com.mmt.flights.entity.pnr.retrieve.response.FlightSegment segment) {
        SimpleFlight flight = new SimpleFlight();
        flight.setMarketingAirline(segment.getMarketingCarrier().getAirlineID());
        flight.setOperatingAirline(segment.getOperatingCarrier().getAirlineID());
        flight.setFlightNum(segment.getMarketingCarrier().getFlightNumber());
        
        SimpleLocationInfo depInfo = new SimpleLocationInfo();
        depInfo.setCityCode(segment.getDeparture().getAirportCode());
        depInfo.setTerminal(segment.getDeparture().getTerminal().getName());
        flight.setDepartureInfo(depInfo);

        SimpleLocationInfo arrInfo = new SimpleLocationInfo();
        arrInfo.setCityCode(segment.getArrival().getAirportCode());
        arrInfo.setTerminal(segment.getArrival().getTerminal().getName());
        flight.setArrivalInfo(arrInfo);

        flight.setDepTime(segment.getDeparture().getDate() + " " + segment.getDeparture().getTime());
        flight.setArrTime(segment.getArrival().getDate() + " " + segment.getArrival().getTime());
        
        String duration = segment.getFlightDetail().getFlightDuration().getValue();
        flight.setDuration(parseFlightDuration(duration));
        
        return flight;
    }

    private int calculateJourneyDuration(List<SimpleFlight> flights) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date depDate = sdf.parse(flights.get(0).getDepTime());
            Date arrDate = sdf.parse(flights.get(flights.size()-1).getArrTime());
            return (int) ((arrDate.getTime() - depDate.getTime()) / (60 * 1000));
        } catch (Exception e) {
            // If date parsing fails, sum up individual flight durations
            return flights.stream()
                    .mapToInt(SimpleFlight::getDuration)
                    .sum();
        }
    }

    private Integer parseFlightDuration(String duration) {
        try {
            // Parse duration in format "X H Y M"
            String[] parts = duration.split(" ");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[2]);
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0;
        }
    }

    private String determineFareFamily(String priceClassRef, FlowState state) {
        // Get the PriceClass from DataLists.PriceClassList that matches the priceClassRef
        try {
            String orderReshopResponseJson = state.getValue(FlowStateKey.ODC_SEARCH_RESPONSE); 
            if (orderReshopResponseJson != null) {
                OrderReshopResponse orderReshopResponse = objectMapper.readValue(orderReshopResponseJson, OrderReshopResponse.class);
                OrderReshopRS response = orderReshopResponse.getOrderReshopRS();

                // Find matching PriceClass
                if (response.getDataLists() != null && 
                    response.getDataLists().getPriceClassList() != null &&
                    response.getDataLists().getPriceClassList().getPriceClass() != null) {
                    
                    return response.getDataLists().getPriceClassList().getPriceClass().stream()
                        .filter(priceClass -> priceClass.getPriceClassID().equals(priceClassRef))
                        .map(priceClass -> priceClass.getName())
                        .findFirst()
                        .orElse("SAVER"); // Default to SAVER if not found
                }
            }
        } catch (Exception e) {
            // In case of any error, return default
            return "SAVER";
        }
        return "SAVER";
    }

    private String determineFareType(String fareBasisCode) {
        // Implement logic to determine fare type based on fare basis code
        // This is a placeholder implementation
        return "REGULAR";
    }
}