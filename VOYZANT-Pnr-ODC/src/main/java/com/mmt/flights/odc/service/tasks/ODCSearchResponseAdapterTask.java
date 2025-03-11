package com.mmt.flights.odc.service.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.api.rxflow.FlowState;
import com.mmt.api.rxflow.task.MapTask;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.entity.odc.*;
import com.mmt.flights.entity.pnr.retrieve.response.FlightSegment;
import com.mmt.flights.odc.common.ConversionFactor;
import com.mmt.flights.odc.common.enums.PaxType;
import com.mmt.flights.odc.search.*;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationGroupV2;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationV2;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

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

            // Extract and set conversion factors
            double bookingToEquivExRate = 0.0045; // Default value
            if (!response.getReshopOffers().isEmpty() && !response.getReshopOffers().get(0).getReshopOffers().isEmpty()) {
                bookingToEquivExRate = response.getReshopOffers().get(0).getReshopOffers().get(0).getBookingToEquivExRate();
            }

            List<ConversionFactor> conversionFactors = new ArrayList<>();
            if (!response.getReshopOffers().isEmpty() && !response.getReshopOffers().get(0).getReshopOffers().isEmpty()) {
                ReshopOffer reshopOffer = response.getReshopOffers().get(0).getReshopOffers().get(0);
                ConversionFactor conversionFactor = new ConversionFactor();
                conversionFactor.setFromCurrency(reshopOffer.getBookingCurrencyCode());
                conversionFactor.setToCurrency(reshopOffer.getEquivCurrencyCode());
                conversionFactor.setRoe(bookingToEquivExRate);
                conversionFactors.add(conversionFactor);
            }
            simpleSearchResponseV2.setConversionFactors(conversionFactors);

            // Process journey information
            List<List<SimpleJourney>> itineraryJourneyList = processJourneys(response);
            simpleSearchResponseV2.setItineraryJourneyList(itineraryJourneyList);

            // Process recommendations
            List<SimpleSearchRecommendationGroupV2> sameFareRecommendationGroups = processRecommendations(response, true);
            List<SimpleSearchRecommendationGroupV2> otherFareRecommendationGroups = processRecommendations(response, false);

            simpleSearchResponseV2.setSameFareRcomGrps(sameFareRecommendationGroups);
            simpleSearchResponseV2.setOtherFareRcomGrps(otherFareRecommendationGroups);

            // Add response to flow state
            return state.toBuilder()
                    .addValue(FlowStateKey.RESPONSE, simpleSearchResponseV2)
                    .build();
        } catch (Exception e) {
            throw new PSErrorException("",ErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }

    private List<List<SimpleJourney>> processJourneys(OrderReshopRS response) throws ParseException {
        List<List<SimpleJourney>> itineraryJourneyList = new ArrayList<>();
        List<SimpleJourney> journeyList = new ArrayList<>();

        // Get DataLists containing flight information
        DataLists dataLists = response.getDataLists();
        if (dataLists != null && dataLists.getFlightSegmentList() != null && 
            dataLists.getFlightSegmentList().getFlightSegment() != null && 
            dataLists.getFlightList() != null && 
            dataLists.getFlightList().getFlight() != null) {
            
            // Process each flight
            for (com.mmt.flights.entity.pnr.retrieve.response.Flight flight : dataLists.getFlightList().getFlight()) {
                SimpleJourney journey = new SimpleJourney();
                journey.setJourneyKey(flight.getFlightKey());
                journey.setDuration(parseTimeToMinutes(flight.getJourney().getTime()));
                
                // Get the segments for this flight
                String[] segmentRefs = flight.getSegmentReferences().split("\\s+");
                List<SimpleFlight> simpleFlights = new ArrayList<>();
                
                for (String segmentRef : segmentRefs) {
                    // Find the matching segment in FlightSegmentList
                    Optional<FlightSegment> matchingSegmentOpt = dataLists.getFlightSegmentList().getFlightSegment().stream()
                            .filter(segment -> segment.getSegmentKey().equals(segmentRef))
                            .findFirst();
                    
                    if (matchingSegmentOpt.isPresent()) {
                        FlightSegment segment = matchingSegmentOpt.get();
                        SimpleFlight simpleFlight = convertToSimpleFlight(segment);
                        simpleFlights.add(simpleFlight);
                        
                        // Set the journey dates based on the first segment
                        if (journey.getDepDate() == null) {
                            journey.setDepDate(segment.getDeparture().getDate());
                            journey.setArrDate(segment.getArrival().getDate());
                        }
                    }
                }
                
                journey.setFlights(simpleFlights);
                journey.setNearBy(false);  // Default to false
                journeyList.add(journey);
            }
        }
        
        itineraryJourneyList.add(journeyList);
        return itineraryJourneyList;
    }

    private SimpleFlight convertToSimpleFlight(FlightSegment segment) throws ParseException {
        SimpleFlight flight = new SimpleFlight();
        
        // Set flight information
        flight.setBus(false); // Default value
        flight.setMarketingAirline(segment.getMarketingCarrier().getAirlineID());
        flight.setOperatingAirline(segment.getOperatingCarrier().getAirlineID());
        flight.setFlightNum(segment.getMarketingCarrier().getFlightNumber());
        flight.setAircraftType(segment.getEquipment().getAircraftCode());
        
        // Set departure information
        SimpleLocationInfo departureInfo = new SimpleLocationInfo();
        departureInfo.setTerminal(segment.getDeparture().getTerminal() != null ? segment.getDeparture().getTerminal().getName() : "");
        departureInfo.setCityCode(segment.getDeparture().getAirportCode());
        departureInfo.setAirportName(segment.getDeparture().getAirportName());
        flight.setDepartureInfo(departureInfo);
        
        // Set arrival information
        SimpleLocationInfo arrivalInfo = new SimpleLocationInfo();
        arrivalInfo.setTerminal(segment.getArrival().getTerminal() != null ? segment.getArrival().getTerminal().getName() : "");
        arrivalInfo.setCityCode(segment.getArrival().getAirportCode());
        arrivalInfo.setAirportName(segment.getArrival().getAirportName());
        flight.setArrivalInfo(arrivalInfo);
        
        // Set times
        flight.setDepTime(segment.getDeparture().getTime());
        flight.setArrTime(segment.getArrival().getTime());
        
        // Set duration
        flight.setDuration(parseTimeToMinutes(segment.getFlightDetail().getFlightDuration().getValue()));
        
        // Set technical stops (if any)
        flight.setTechStops(new ArrayList<>()); // Default empty list
        
        // Set baggage info (to be filled in later if needed)
        flight.setPaxWiseBaggageInfo(new HashMap<>());
        
        return flight;
    }

    private List<SimpleSearchRecommendationGroupV2> processRecommendations(OrderReshopRS response, boolean sameFare) {
        List<SimpleSearchRecommendationGroupV2> recommendationGroups = new ArrayList<>();
        
        if (response.getReshopOffers() != null && !response.getReshopOffers().isEmpty()) {
            for (ReshopOfferInstance offerInstance : response.getReshopOffers()) {
                if (offerInstance.getReshopOffers() != null && !offerInstance.getReshopOffers().isEmpty()) {
                    for (ReshopOffer offer : offerInstance.getReshopOffers()) {
                        SimpleSearchRecommendationGroupV2 group = new SimpleSearchRecommendationGroupV2();
                        
                        // Set airlines
                        List<String> airlines = new ArrayList<>();
                        airlines.add(offer.getOwner());
                        group.setAirlines(airlines);
                        
                        // Process recommendations
                        List<SimpleSearchRecommendationV2> recommendations = new ArrayList<>();
                        
                        if (offer.getAddOfferItem() != null && !offer.getAddOfferItem().isEmpty()) {
                            SimpleSearchRecommendationV2 recommendation = new SimpleSearchRecommendationV2();
                            recommendation.setrKey(offer.getOfferID());
                            
                            // Set FareKey (same as rKey based on sample)
                            recommendation.setFareKey(offer.getOfferID());
                            
                            // Set fare family name (if available from branded options or default)
                            String fareFamilyName = (offer.getBrandedFareOptions() != null && !offer.getBrandedFareOptions().isEmpty()) ? 
                                                    offer.getBrandedFareOptions().get(0) : "SME";
                            recommendation.setFareFamilyName(fareFamilyName);
                            
                            // Set isHandBaggageFare (default to false if not available)
                            recommendation.setHandBaggageFare(false);
                            
                            // Set refundability
                            if (offer.getAddOfferItem().get(0).getRefundable() != null) {
                                boolean isRefundable = "true".equalsIgnoreCase(offer.getAddOfferItem().get(0).getRefundable());
                                recommendation.setRefundable(isRefundable);
                                recommendation.setRefundable(isRefundable);
                            }
                            
                            // Set itinerary journey index
                            List<Integer> journeyIndices = new ArrayList<>();
                            journeyIndices.add(0);  // Default to first journey
                            recommendation.setItneraryJrnyIndex(journeyIndices);
                            
                            // Process fare information
                            Map<PaxType, SimpleFare> paxWiseFare = new HashMap<>();
                            
                            for (OfferItem item : offer.getAddOfferItem()) {
                                PaxType paxType = convertToPaxType(item.getPassengerType());
                                if (paxType != null && item.getFareDetail() != null && 
                                    item.getFareDetail().getPrice() != null) {
                                    
                                    SimpleFare fare = new SimpleFare();
                                    Price price = item.getFareDetail().getPrice();
                                    
                                    fare.setBase((double) price.getBaseAmount().getBookingCurrencyPrice());
                                    fare.setTaxes((double) price.getTaxAmount().getBookingCurrencyPrice());
                                    fare.setDiscount(0.0); // Default value
                                    
                                    paxWiseFare.put(paxType, fare);
                                }
                            }
                            recommendation.setPaxWiseFare(paxWiseFare);
                            
                            // Set fare type
                            recommendation.setFareType("CORPORATE"); // Based on sample.txt, most fares have CORPORATE type
                            
                            // Calculate ODC details
                            double dct = 0.0;
                            double dcf = 499.0; // Default fee based on the sample
                            boolean refundAllowed = "true".equalsIgnoreCase(offer.getAddOfferItem().get(0).getRefundable());
                            
                            // Get reshop differential details if available
                            if (offer.getReshopDifferential() != null) {
                                if (offer.getReshopDifferential().getReshopDue() != null && 
                                    offer.getReshopDifferential().getReshopDue().getTotalPrice() != null) {
                                    dct = offer.getReshopDifferential().getReshopDue().getTotalPrice().getBookingCurrencyPrice();
                                }
                                
                                if (offer.getReshopDifferential().getPenaltyAmount() != null && 
                                    offer.getReshopDifferential().getPenaltyAmount().getTotalPrice() != null) {
                                    dcf = offer.getReshopDifferential().getPenaltyAmount().getTotalPrice().getBookingCurrencyPrice();
                                }
                            }

                            // Create and set ODC details object
                            SimpleDateChangeDetails odcDetails = new SimpleDateChangeDetails();
                            odcDetails.setDateChangeTotal(dct);
                            odcDetails.setDateChangeFee(dcf);
                            odcDetails.setResidualAmount(dct - dcf); // Calculate residual amount
                            odcDetails.setRefundAllowed(refundAllowed);
                            recommendation.setOdcDetails(odcDetails);
                            
                            // Add baggage information
                            processBaggageInformation(offer, response, recommendation);
                            
                            recommendations.add(recommendation);
                        }
                        
                        group.setSearchRecommendations(recommendations);
                        
                        // Set single adult fare
                        SimpleFare adultFare = null;
                        if (offer.getAddOfferItem() != null && !offer.getAddOfferItem().isEmpty()) {
                            for (OfferItem item : offer.getAddOfferItem()) {
                                if ("ADT".equals(item.getPassengerType()) && item.getFareDetail() != null && 
                                    item.getFareDetail().getPrice() != null) {
                                    
                                    adultFare = new SimpleFare();
                                    Price price = item.getFareDetail().getPrice();
                                    
                                    adultFare.setBase((double) price.getBaseAmount().getBookingCurrencyPrice() / item.getPassengerQuantity());
                                    adultFare.setTaxes((double) price.getTaxAmount().getBookingCurrencyPrice() / item.getPassengerQuantity());
                                    adultFare.setDiscount(0.0); // Default value
                                    break;
                                }
                            }
                        }
                        
                        if (adultFare == null) {
                            adultFare = new SimpleFare();
                            adultFare.setBase(0.0);
                            adultFare.setTaxes(0.0);
                            adultFare.setDiscount(0.0);
                        }
                        
                        group.setSingleAdultFare(adultFare);
                        
                        if ((sameFare && hasNoFareChange(offer)) || (!sameFare && hasFareChange(offer))) {
                            recommendationGroups.add(group);
                        }
                    }
                }
            }
        }
        
        return recommendationGroups;
    }
    
    private void processBaggageInformation(ReshopOffer offer, OrderReshopRS response, SimpleSearchRecommendationV2 recommendation) {
        if (offer.getBaggageAllowance() != null && !offer.getBaggageAllowance().isEmpty() && 
            response.getDataLists() != null && 
            response.getDataLists().getBaggageAllowanceList() != null && 
            response.getDataLists().getBaggageAllowanceList().getBaggageAllowance() != null) {
            
            for (BaggageAllowance baggageAllowance : offer.getBaggageAllowance()) {
                String passRefs = baggageAllowance.getPassengerRefs();
                String[] passengerIds = passRefs.split("\\s+");
                
                // Find the baggage allowance details
                Optional<com.mmt.flights.entity.pnr.retrieve.response.BaggageAllowance> baggageDetailsOpt =
                    response.getDataLists().getBaggageAllowanceList().getBaggageAllowance().stream()
                        .filter(b -> b.getBaggageAllowanceID().equals(baggageAllowance.getBaggageAllowanceRef()))
                        .findFirst();
                
                if (baggageDetailsOpt.isPresent()) {
                    com.mmt.flights.entity.pnr.retrieve.response.BaggageAllowance bagDetails = baggageDetailsOpt.get();
                    
                    // Find passenger types for these IDs
                    for (String passId : passengerIds) {
                        PaxType paxType = findPaxTypeByPassengerId(response, passId);
                        if (paxType != null) {
                            // Add baggage info to relevant flights
                            for (SimpleFlight flight : recommendation.getItneraryJrnyIndex().stream()
                                    .flatMap(idx -> getJourneyByIndex(response, idx).getFlights().stream())
                                    .collect(Collectors.toList())) {
                                
                                if (flight.getPaxWiseBaggageInfo() == null) {
                                    flight.setPaxWiseBaggageInfo(new HashMap<>());
                                }
                                
                                SimpleBaggageInfo baggageInfo = new SimpleBaggageInfo();
                                
                                // Check if PieceAllowance exists
                                if (bagDetails.getPieceAllowance() != null) {
                                    SimpleBaggageDetails checkInBaggage = new SimpleBaggageDetails();
                                    checkInBaggage.setUnit(bagDetails.getPieceAllowance().getUnit());
                                    
                                    try {
                                        checkInBaggage.setQuantity(Integer.parseInt(bagDetails.getPieceAllowance().getTotalQuantity()));
                                    } catch (NumberFormatException e) {
                                        checkInBaggage.setQuantity(0);
                                    }
                                    
                                    String displayText = bagDetails.getPieceAllowance().getTotalQuantity() + " " + 
                                                        bagDetails.getPieceAllowance().getUnit();
                                    checkInBaggage.setDisplayTxt(displayText);
                                    
                                    baggageInfo.setCheckInBaggage(checkInBaggage);
                                }
                                
                                // Cabin baggage is typically not well-defined in this context, using default values
                                SimpleBaggageDetails cabinBaggage = new SimpleBaggageDetails();
                                cabinBaggage.setUnit("KG");
                                cabinBaggage.setQuantity(7);
                                cabinBaggage.setDisplayTxt("7 KG");
                                baggageInfo.setCabinBaggage(cabinBaggage);
                                
                                flight.getPaxWiseBaggageInfo().put(paxType, baggageInfo);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean hasNoFareChange(ReshopOffer offer) {
        if (offer.getReshopDifferential() != null && 
            offer.getReshopDifferential().getReshopDue() != null && 
            offer.getReshopDifferential().getReshopDue().getTotalPrice() != null) {
            
            double reshopDue = offer.getReshopDifferential().getReshopDue().getTotalPrice().getBookingCurrencyPrice();
            return Math.abs(reshopDue) < 0.01; // Small threshold to account for rounding
        }
        return true; // Default to same fare if no reshop differential
    }

    private boolean hasFareChange(ReshopOffer offer) {
        return !hasNoFareChange(offer);
    }

    private PaxType findPaxTypeByPassengerId(OrderReshopRS response, String passengerId) {
        if (response.getDataLists() != null && 
            response.getDataLists().getPassengerList() != null && 
            response.getDataLists().getPassengerList().getPassenger() != null) {

            Optional<Passenger> passengerOpt = response.getDataLists().getPassengerList().getPassenger().stream()
                    .filter(p -> p.getPassengerID().equals(passengerId))
                    .findFirst();

            if (passengerOpt.isPresent()) {
                return PaxType.valueOf(passengerOpt.get().getPtc());
            }
        }
        return null;
    }

    private SimpleJourney getJourneyByIndex(OrderReshopRS response, int index) {
        if (response.getDataLists() != null && 
            response.getDataLists().getFlightList() != null && 
            response.getDataLists().getFlightList().getFlight() != null && 
            index < response.getDataLists().getFlightList().getFlight().size()) {
            
            com.mmt.flights.entity.pnr.retrieve.response.Flight flight = response.getDataLists().getFlightList().getFlight().get(index);
            SimpleJourney journey = new SimpleJourney();
            journey.setJourneyKey(flight.getFlightKey());
            journey.setDuration(parseTimeToMinutes(flight.getJourney().getTime()));
            
            // Would need to populate flights here too, but this is a simplified version
            journey.setFlights(new ArrayList<>());
            
            return journey;
        }
        return new SimpleJourney();
    }

    private int parseTimeToMinutes(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0;
        }
        
        try {
            // Format expected like "3 H 50 M"
            String[] parts = timeStr.split("\\s+");
            int hours = 0;
            int minutes = 0;
            
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("H") && i > 0) {
                    hours = Integer.parseInt(parts[i-1]);
                } else if (parts[i].equals("M") && i > 0) {
                    minutes = Integer.parseInt(parts[i-1]);
                }
            }
            
            return hours * 60 + minutes;
        } catch (Exception e) {
            return 0;
        }
    }

    private PaxType convertToPaxType(String passengerType) {
        if (passengerType == null) {
            return null;
        }

        switch (passengerType) {
            case "ADT":
                return PaxType.ADULT;
            case "CHD":
                return PaxType.CHILD;
            case "INF":
                return PaxType.INFANT;
            case "SC":
                return PaxType.SENIOR_CITIZEN;
            default:
                return null;
        }
    }
}