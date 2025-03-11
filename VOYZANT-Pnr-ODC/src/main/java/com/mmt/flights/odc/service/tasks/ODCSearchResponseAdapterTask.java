package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.OrderReshopRS;
import com.mmt.flights.entity.odc.OrderReshopResponse;
import com.mmt.flights.entity.odc.ReshopOffer;
import com.mmt.flights.odc.common.ConversionFactor;
import com.mmt.flights.odc.common.enums.PaxType;
import com.mmt.flights.odc.search.*;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationGroupV2;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationV2;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSErrorException;
import com.mmt.flights.supply.search.v4.response.*;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class ODCSearchResponseAdapterTask implements MapTask {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public FlowState run(FlowState state) throws Exception {
        try {
            // Get OrderReshopResponse from state
            String orderReshopResponseJson = state.getValue(FlowStateKey.ODC_SEARCH_RESPONSE);
            if (orderReshopResponseJson == null) {
                throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
            }

            // Deserialize the JSON string to OrderReshopResponse
            OrderReshopResponse orderReshopResponse = objectMapper.readValue(orderReshopResponseJson, OrderReshopResponse.class);
            OrderReshopRS response = orderReshopResponse.getOrderReshopRS();

            // Check if the response is successful
            if (!response.isSuccess() || response.getReshopOffers() == null || response.getReshopOffers().isEmpty()) {
                throw new PSErrorException(ErrorEnum.FLT_UNKNOWN_ERROR);
            }

            // Create SimpleSearchResponseV2 object
            SimpleSearchResponseV2 simpleSearchResponseV2 = new SimpleSearchResponseV2();
            
            // Set conversion factors
            List<ConversionFactor> conversionFactors = new ArrayList<>();
            ConversionFactor conversionFactor = new ConversionFactor();
            conversionFactor.setFromCurrency(response.getReshopOffers().get(0).getReshopOffers().get(0).getBookingCurrencyCode());
            conversionFactor.setToCurrency(response.getReshopOffers().get(0).getReshopOffers().get(0).getBookingCurrencyCode());
            conversionFactor.setRoe(1.0);
            conversionFactors.add(conversionFactor);
            simpleSearchResponseV2.setConversionFactors(conversionFactors);

            // Process recommendations and create groups
            List<SimpleSearchRecommendationGroupV2> sameFareRcomGrps = new ArrayList<>();
            List<SimpleSearchRecommendationGroupV2> otherFareRcomGrps = new ArrayList<>();
            List<List<SimpleJourney>> itineraryJourneyList = new ArrayList<>();
            List<SimpleJourney> allJourneys = new ArrayList<>(); // All journeys will go in this list
            
            // Process each ReshopOffer
            Map<String, Integer> flightKeyToJourneyIndex = new HashMap<>();
            int journeyIndex = 0;
            
            for (ReshopOffer reshopOffer : response.getReshopOffers().get(0).getReshopOffers()) {
                SimpleSearchRecommendationGroupV2 recommendationGroup = new SimpleSearchRecommendationGroupV2();
                
                // Set airline list
                List<String> airlines = new ArrayList<>();
                airlines.add(reshopOffer.getOwner());
                recommendationGroup.setAirlines(airlines);

                // Create recommendation
                SimpleSearchRecommendationV2 recommendation = new SimpleSearchRecommendationV2();
                recommendation.setHandBaggageFare(false);
                recommendation.setRefundable(Boolean.parseBoolean(reshopOffer.getAddOfferItem().get(0).getRefundable()));
                
                // Get all flight references from service node
                String[] flightRefs = reshopOffer.getAddOfferItem().get(0).getService().get(0).getFlightRefs().split(" ");
                
                // Create journeys for each flight if not already created
                List<Integer> journeyIndices = new ArrayList<>();
                for (String flightRef : flightRefs) {
                    if (!flightKeyToJourneyIndex.containsKey(flightRef)) {
                        // Process this flight and create journey
                        String segmentRefs = response.getDataLists().getFlightList().getFlight().stream()
                                .filter(f -> f.getFlightKey().equals(flightRef))
                                .findFirst()
                                .map(f -> f.getSegmentReferences())
                                .orElse("");
                                
                        if (!segmentRefs.isEmpty()) {
                            SimpleJourney journey = new SimpleJourney();
                            StringBuilder journeyKeyBuilder = new StringBuilder();
                            List<SimpleFlight> flights = new ArrayList<>();
                            boolean isFirst = true;
                            
                            String[] segmentKeys = segmentRefs.split(" ");
                            for (String segmentKey : segmentKeys) {
                                com.mmt.flights.entity.pnr.retrieve.response.FlightSegment segment = 
                                        response.getDataLists().getFlightSegmentList().getFlightSegment().stream()
                                                .filter(s -> s.getSegmentKey().equals(segmentKey))
                                                .findFirst()
                                                .orElse(null);
                                                
                                if (segment != null) {
                                    // Build journey key part for this segment
                                    if (!isFirst) {
                                        journeyKeyBuilder.append("|");
                                    }
                                    
                                    // Format date for journey key (DDMMYY)
                                    String[] dateParts = segment.getDeparture().getDate().split("-");
                                    String dateStr = String.format("%s%s%s", 
                                        dateParts[2],  // DD
                                        dateParts[1],  // MM
                                        dateParts[0].substring(2));  // YY
                                    String timeStr = segment.getDeparture().getTime().substring(0, 5).replace(":", "");
                                    String dateTime = dateStr + timeStr;
                                    
                                    // Build journey key
                                    journeyKeyBuilder.append(segment.getDeparture().getAirportCode())
                                            .append("$")
                                            .append(segment.getArrival().getAirportCode())
                                            .append("$")
                                            .append(dateTime)
                                            .append("$")
                                            .append(segment.getMarketingCarrier().getAirlineID())
                                            .append("-")
                                            .append(segment.getMarketingCarrier().getFlightNumber());
                                    
                                    isFirst = false;

                                    // Create flight details
                                    SimpleFlight flight = createSimpleFlight(segment);
                                    flights.add(flight);
                                }
                            }
                            
                            journey.setFlights(flights);
                            journey.setJourneyKey(journeyKeyBuilder.toString());

                            if (!flights.isEmpty()) {
                                journey.setDepDate(flights.get(0).getDepTime());
                                journey.setArrDate(flights.get(flights.size()-1).getArrTime());
                                journey.setDuration(calculateJourneyDuration(flights));
                                
                                // Add journey to all journeys list
                                allJourneys.add(journey);
                                flightKeyToJourneyIndex.put(flightRef, journeyIndex++);
                            }
                        }
                    }
                    // Add this flight's journey index to the recommendation's indices
                    Integer index = flightKeyToJourneyIndex.get(flightRef);
                    if (index != null) {
                        journeyIndices.add(index);
                    }
                }
                
                // Set journey indices for this recommendation
                recommendation.setItneraryJrnyIndex(journeyIndices);
                
                // Set fare details for each passenger type
                Map<PaxType, SimpleFare> paxWiseFare = new HashMap<>();
                for (com.mmt.flights.entity.odc.OfferItem offerItem : reshopOffer.getAddOfferItem()) {
                    PaxType paxType = null;
                    // Match passenger type string with enum paxtype value
                    for (PaxType type : PaxType.values()) {
                        if (type.getPaxType().equals(offerItem.getPassengerType())) {
                            paxType = type;
                            break;
                        }
                    }
                    if (paxType != null) {
                        SimpleFare simpleFare = new SimpleFare();
                        // Divide by passenger quantity to get per-passenger fare
                        int paxCount = offerItem.getPassengerQuantity();
                        simpleFare.setBase(offerItem.getFareDetail().getPrice().getBaseAmount().getBookingCurrencyPrice() / paxCount);
                        simpleFare.setTaxes(offerItem.getFareDetail().getPrice().getTaxAmount().getBookingCurrencyPrice() / paxCount);
                        paxWiseFare.put(paxType, simpleFare);
                    }
                }
                recommendation.setPaxWiseFare(paxWiseFare);

                // Set ODC details
                SimpleDateChangeDetails odcDetails = new SimpleDateChangeDetails();
                odcDetails.setRefundAllowed(false); // Based on the sample JSON
                odcDetails.setDateChangeFee(2999.0); // Example value from sample JSON
                recommendation.setOdcDetails(odcDetails);

                // Set fare family name and fare type from PriceClassRef
                if (!reshopOffer.getAddOfferItem().isEmpty() && 
                    !reshopOffer.getAddOfferItem().get(0).getFareComponent().isEmpty()) {
                    String priceClassRef = reshopOffer.getAddOfferItem().get(0).getFareComponent().get(0).getPriceClassRef();
                    String fareBasisCode = reshopOffer.getAddOfferItem().get(0).getFareComponent().get(0).getFareBasis().getFareBasisCode().getCode();
                    recommendation.setFareFamilyName(determineFareFamily(priceClassRef, state));
                    recommendation.setFareType(determineFareType(fareBasisCode));
                }

                // Prepare inputs for rKey generation
                Journey journey = new Journey();
                List<com.mmt.flights.supply.search.v4.response.Segment> segments = new ArrayList<>();
                
                // Process segments from the flight references
                for (String ref : flightRefs) {  // Using existing flightRefs from above
                    com.mmt.flights.entity.pnr.retrieve.response.Flight flight = response.getDataLists().getFlightList().getFlight().stream()
                            .filter(f -> f.getFlightKey().equals(ref))
                            .findFirst()
                            .orElse(null);
                            
                    if (flight != null) {
                        String segmentRefs = flight.getSegmentReferences();
                        String[] segmentKeys = segmentRefs.split(" ");
                        
                        for (String segmentKey : segmentKeys) {
                            com.mmt.flights.entity.pnr.retrieve.response.FlightSegment flightSegment = 
                                    response.getDataLists().getFlightSegmentList().getFlightSegment().stream()
                                            .filter(s -> s.getSegmentKey().equals(segmentKey))
                                            .findFirst()
                                            .orElse(null);
                                            
                            if (flightSegment != null) {
                                com.mmt.flights.supply.search.v4.response.Segment segment = new com.mmt.flights.supply.search.v4.response.Segment();
                                
                                // Set identifier
                                Identifier identifier = new Identifier();
                                identifier.setCarrierCode(flightSegment.getMarketingCarrier().getAirlineID());
                                identifier.setIdentifier(flightSegment.getMarketingCarrier().getFlightNumber());
                                segment.setIdentifier(identifier);
                                
                                // Set designator
                                Designator designator = new Designator();
                                designator.setOrigin(flightSegment.getDeparture().getAirportCode());
                                designator.setDestination(flightSegment.getArrival().getAirportCode());
                                designator.setDeparture(flightSegment.getDeparture().getDate() + " " + flightSegment.getDeparture().getTime());
                                designator.setArrival(flightSegment.getArrival().getDate() + " " + flightSegment.getArrival().getTime());
                                segment.setDesignator(designator);
                                
                                // Create legs
                                List<Leg> legs = new ArrayList<>();
                                Leg leg = new Leg();
                                LegInfo legInfo = new LegInfo();
                                legInfo.setDepartureTerminal(flightSegment.getDeparture().getTerminal().getName());
                                legInfo.setArrivalTerminal(flightSegment.getArrival().getTerminal().getName());
                                leg.setLegInfo(legInfo);
                                legs.add(leg);
                                segment.setLegs(legs);
                                
                                segments.add(segment);
                            }
                        }
                    }
                }
                
                journey.setSegments(segments);
                
                // Create JourneyFare
                JourneyFare journeyFare = new JourneyFare();
                List<com.mmt.flights.supply.search.v4.response.Fare> supplyFares = new ArrayList<>();
                
                // Map fare details from reshopOffer
                for (com.mmt.flights.entity.odc.OfferItem offerItem : reshopOffer.getAddOfferItem()) {
                    if (!offerItem.getFareComponent().isEmpty()) {
                        // Set fare basis code from the first fare component
                        String fareBasisCode = offerItem.getFareComponent().get(0).getFareBasis().getFareBasisCode().getCode();
                        // Set cabin class
                        String cabinClass = offerItem.getFareComponent().get(0).getFareBasis().getFareBasisCode().getCode().substring(0, 1);

                        // Map passenger fares
                        List<PassengerFare> passengerFares = new ArrayList<>();
                        PassengerFare passengerFare = new PassengerFare();
                        passengerFare.setPassengerType(offerItem.getPassengerType());

                        // Map service charges
                        List<ServiceCharge> serviceCharges = new ArrayList<>();
                        
                        // Add base amount
                        ServiceCharge baseCharge = new ServiceCharge();
                        baseCharge.setAmount(offerItem.getFareDetail().getPrice().getBaseAmount().getBookingCurrencyPrice());
                        //baseCharge.setType(ServiceChargeType.valueOf("BASE"));
                        serviceCharges.add(baseCharge);
                        
                        // Add tax amount
                        ServiceCharge taxCharge = new ServiceCharge();
                        taxCharge.setAmount(offerItem.getFareDetail().getPrice().getTaxAmount().getBookingCurrencyPrice());
                        //taxCharge.setType(ServiceChargeType.valueOf("TAX"));
                        serviceCharges.add(taxCharge);

                        passengerFare.setServiceCharges(serviceCharges);
                        passengerFares.add(passengerFare);
                        
                        // Create and add fare with correct field mappings
                        com.mmt.flights.supply.search.v4.response.Fare supplyFare = new com.mmt.flights.supply.search.v4.response.Fare();
                        supplyFare.setFareBasisCode(fareBasisCode);
                        supplyFare.setClassOfService(offerItem.getFareComponent().get(0).getFareBasis().getRbd());
                        supplyFare.setFareClassOfService(fareBasisCode);
                        supplyFare.setProductClass(offerItem.getFareComponent().get(0).getFareBasis().getCabinType());
                        supplyFare.setTravelClassCode(cabinClass);
                        supplyFare.setPassengerFares(passengerFares);
                        supplyFares.add(supplyFare);
                    }
                }
                journeyFare.setFares(supplyFares);

                // Create PaxCount
                PaxCount paxCount = new PaxCount();
                Map<String, Integer> paxTypeCount = new HashMap<>();
                for (com.mmt.flights.entity.odc.OfferItem offerItem : reshopOffer.getAddOfferItem()) {
                    paxTypeCount.put(offerItem.getPassengerType(), offerItem.getPassengerQuantity());
                }
                paxCount.setAdult(paxTypeCount.getOrDefault("ADULT", 0));
                paxCount.setChild(paxTypeCount.getOrDefault("CHILD", 0));
                paxCount.setInfant(paxTypeCount.getOrDefault("INFANT", 0));

                // Generate rKey using RKeyBuilderUtil
                String rKey;
                String cmsId = "DOTREZ";
                
                // Check if this is a return trip by looking at origin/destination pairs
                boolean isReturnTrip = false;
                if (segments.size() >= 2) {
                    String firstOrigin = segments.get(0).getDesignator().getOrigin();
                    String lastDestination = segments.get(segments.size()-1).getDesignator().getDestination();
                    isReturnTrip = firstOrigin.equals(lastDestination);
                }
                
                if (isReturnTrip) {
                    // Split segments into onward and return journeys
                    List<com.mmt.flights.supply.search.v4.response.Segment> rtSegments = new ArrayList<>();
                    Journey rtJourney = new Journey();
                    JourneyFare rtFare = new JourneyFare();
                    
                    // Simple split in half for return journey
                    int mid = segments.size() / 2;
                    rtSegments.addAll(segments.subList(mid, segments.size()));
                    segments = new ArrayList<>(segments.subList(0, mid));
                    
                    journey.setSegments(segments);
                    rtJourney.setSegments(rtSegments);
                    
                    // Split fares between onward and return
                    List<com.mmt.flights.supply.search.v4.response.Fare> rtFares = 
                            new ArrayList<>(supplyFares.subList(supplyFares.size()/2, supplyFares.size()));
                    supplyFares = new ArrayList<>(supplyFares.subList(0, supplyFares.size()/2));
                    
                    journeyFare.setFares(supplyFares);
                    rtFare.setFares(rtFares);
                    
                    rKey = RKeyBuilderUtil.buildRKey(recommendation.getPaxWiseFare(), journeyFare, rtFare, paxCount, journey, rtJourney, cmsId);
                } else {
                    rKey = RKeyBuilderUtil.buildRKey(recommendation.getPaxWiseFare(), journeyFare, paxCount, journey, cmsId);
                }
                
                recommendation.setrKey(rKey);
                
                // Generate fareKey and set it
                String fareKey = response.getShoppingResponseId() + "," + reshopOffer.getOfferID();
                recommendation.setFareKey(fareKey);

                // Set single adult fare for recommendation group - already per passenger
                SimpleFare singleAdultFare = paxWiseFare.get(PaxType.ADULT);
                if (singleAdultFare != null) {
                    recommendationGroup.setSingleAdultFare(singleAdultFare);
                }

                List<SimpleSearchRecommendationV2> recs = new ArrayList<>();
                recs.add(recommendation);
                recommendationGroup.setSearchRecommendations(recs);

                // Add recommendation group to appropriate list based on fare
                if (recommendationGroup.getSingleAdultFare() != null) {
                    sameFareRcomGrps.add(recommendationGroup);
                }
            }

            // Add all journeys as a single list to itineraryJourneyList
            if (!allJourneys.isEmpty()) {
                itineraryJourneyList.add(allJourneys);
            }

            simpleSearchResponseV2.setSameFareRcomGrps(sameFareRcomGrps);
            simpleSearchResponseV2.setOtherFareRcomGrps(otherFareRcomGrps);
            simpleSearchResponseV2.setItineraryJourneyList(itineraryJourneyList);

            // Add response to flow state
            return state.toBuilder()
                    .addValue(FlowStateKey.RESPONSE, simpleSearchResponseV2)
                    .build();
        } catch (Exception e) {
            throw new PSErrorException("",ErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }

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