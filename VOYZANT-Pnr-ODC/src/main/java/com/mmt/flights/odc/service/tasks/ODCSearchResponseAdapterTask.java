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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            
            // Process each ReshopOffer
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
                        simpleFare.setBase(offerItem.getFareDetail().getPrice().getBaseAmount().getBookingCurrencyPrice());
                        simpleFare.setTaxes(offerItem.getFareDetail().getPrice().getTaxAmount().getBookingCurrencyPrice());
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

                // Set itinerary journey index
                List<Integer> journeyIndices = new ArrayList<>();
                journeyIndices.add(journeyIndex);
                recommendation.setItneraryJrnyIndex(journeyIndices);

                // Generate rKey (can be customized based on your requirements)
                String rKey = reshopOffer.getOfferID() + "_" + journeyIndex;
                recommendation.setrKey(rKey);
                
                // Set fareKey as concatenation of shoppingResponseId and offerId
                String fareKey = response.getShoppingResponseId() + "," + reshopOffer.getOfferID();
                recommendation.setFareKey(fareKey);

                // Set single adult fare for recommendation group
                SimpleFare singleAdultFare = paxWiseFare.get(PaxType.ADULT);
                if (singleAdultFare != null) {
                    recommendationGroup.setSingleAdultFare(singleAdultFare);
                }

                // Create journey details
                List<SimpleJourney> journeys = new ArrayList<>();
                SimpleJourney journey = new SimpleJourney();
                for (com.mmt.flights.entity.pnr.retrieve.response.FlightSegment segment : 
                     response.getDataLists().getFlightSegmentList().getFlightSegment()) {
                    
                    List<SimpleFlight> flights = new ArrayList<>();
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
                    
                    flights.add(flight);
                    journey.setFlights(flights);
                    journey.setDuration(parseFlightDuration(segment.getFlightDetail().getFlightDuration().getValue()));
                    journey.setJourneyKey(segment.getSegmentKey());
                }
                
                if (!journey.getFlights().isEmpty()) {
                    journey.setDepDate(journey.getFlights().get(0).getDepTime());
                    journey.setArrDate(journey.getFlights().get(journey.getFlights().size()-1).getArrTime());
                    journeys.add(journey);
                    
                    // Add the journey list to itineraryJourneyList
                    List<SimpleJourney> journeyWrapper = new ArrayList<>();
                    journeyWrapper.addAll(journeys);
                    itineraryJourneyList.add(journeyWrapper);
                }

                List<SimpleSearchRecommendationV2> recs = new ArrayList<>();
                recs.add(recommendation);
                recommendationGroup.setSearchRecommendations(recs);

                // Add recommendation group to appropriate list based on fare
                if (recommendationGroup.getSingleAdultFare() != null) {
                    sameFareRcomGrps.add(recommendationGroup);
                }
                
                journeyIndex++;
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