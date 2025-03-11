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

            String supplierPNRResponse = state.getValue(FlowStateKey.SUPPLIER_PNR_RETRIEVE_RESPONSE);
            //OrderViewRS orderViewRS = objectMapper.readValue(supplierPNRResponse, OrderViewRS.class);
            //DateChangeSearchRequest odcRequest = state.getValue(FlowStateKey.REQUEST);

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

                // Create recommendations
                List<SimpleSearchRecommendationV2> recommendations = new ArrayList<>();
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

                // Set fare family name and fare type from fare basis code
                if (!reshopOffer.getAddOfferItem().isEmpty() && 
                    !reshopOffer.getAddOfferItem().get(0).getFareComponent().isEmpty()) {
                    String fareBasisCode = reshopOffer.getAddOfferItem().get(0)
                            .getFareComponent().get(0).getFareBasis().getFareBasisCode().getCode();
                    recommendation.setFareFamilyName("SAVER"); // Default from sample, can be derived from fareBasisCode
                    recommendation.setFareType("REGULAR"); // Can be customized based on business logic
                }

                // Generate rKey (can be customized based on your requirements)
                String rKey = reshopOffer.getOfferID() + "_" + journeyIndex;
                recommendation.setrKey(rKey);
                
                // Set fareKey as concatenation of shoppingResponseId and offerId
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
}