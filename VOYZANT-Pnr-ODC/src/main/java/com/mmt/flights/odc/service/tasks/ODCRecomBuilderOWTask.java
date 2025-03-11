package com.mmt.flights.odc.service.tasks;

import com.mmt.flights.entity.odc.FareComponent;
import com.mmt.flights.entity.odc.OrderReshopRS;
import com.mmt.flights.entity.odc.ReshopOffer;
import com.mmt.flights.entity.odc.ReshopOfferInstance;
import com.mmt.flights.entity.pnr.retrieve.response.OfferItem;
import com.mmt.flights.entity.pnr.retrieve.response.OrderViewRS;
import com.mmt.flights.entity.pnr.retrieve.response.Service;
import com.mmt.flights.helper.CurrencyConverter;
import com.mmt.flights.odc.common.CancelType;
import com.mmt.flights.odc.common.ErrorDetails;
import com.mmt.flights.odc.common.enums.PaxType;
import com.mmt.flights.odc.config.ODCConfig;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.search.SimpleDateChangeDetails;
import com.mmt.flights.odc.search.SimpleFare;
import com.mmt.flights.odc.search.SimpleJourney;
import com.mmt.flights.odc.util.*;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationGroupV2;
import com.mmt.flights.odc.v2.SimpleSearchRecommendationV2;
import com.mmt.flights.odc.v2.SimpleSearchResponseV2;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.postsales.error.PSErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

import static com.mmt.flights.odc.util.LobSrcUtil.getRequiredCurrency;

@Component
public class ODCRecomBuilderOWTask {

    @Autowired
    private RecommendationBuilderUtil recomBuildUtil;

    @Autowired
    private DateChangePenaltyUtil dateChangePenaltyUtil;

    @Autowired
    private CurrencyConverter currencyConverter;

    @Autowired
    private ODCConfig odcConfig;

    public SimpleSearchResponseV2 buildOW(OrderReshopRS response, OrderViewRS orderViewRS, DateChangeSearchRequest odcRequest) {
        CancelType cancelType = ODCSearchUtil.evaluateCancelType(orderViewRS, odcRequest);
        List<String> allowedFarePrdtClass = ODCSearchUtil.getCancelJourneyBookingPClass(orderViewRS, cancelType);
        ErrorDetails error = new ErrorDetails();
        List<RecommendationHolder> recommendations = getRecommendations(response, orderViewRS, cancelType, allowedFarePrdtClass, odcRequest, error);
        recommendations = limitResEntry(recommendations);
        SimpleSearchResponseV2 res;
        if (error.getErrorCode() != null) {
            res = new SimpleSearchResponseV2();
            res.setError(error);
        } else {
            res = convertToSimpleSearchResponse(recommendations, cancelType, orderViewRS);
        }
        return res;
    }

    private List<RecommendationHolder> getRecommendations(OrderReshopRS searchResponse, OrderViewRS booking,
            CancelType cancelType, List<String> allowedFarePrdtClass, DateChangeSearchRequest odcRequest, ErrorDetails error) {
        List<RecommendationHolder> recommendations = new ArrayList<>();
        if (searchResponse.getReshopOffers() == null || searchResponse.getReshopOffers().isEmpty()) {
            error.setErrorCode(PSCommonErrorEnum.EXT_NO_FLIGHTS_FOUND.getCode());
            error.setErrorDescription(PSCommonErrorEnum.EXT_NO_FLIGHTS_FOUND.getMessage());
            return recommendations;
        }

        PaxCount paxCount = getPaxCount(booking);
        BigDecimal totalDateChangePenalty = dateChangePenaltyUtil.getTotalDateChangePenaltyCanPen(cancelType, booking, odcRequest);
        
        int notAllowedFares = 0;
        int negativeFare = 0;

        // Process each ReshopOfferInstance
        for (ReshopOfferInstance reshopOfferInstance : searchResponse.getReshopOffers()) {
            if (reshopOfferInstance.getReshopOffers() != null) {
                for (ReshopOffer reshopOffer : reshopOfferInstance.getReshopOffers()) {
                    SimpleSearchRecommendationV2 simpleSearchRecom = buildSearchRecommendation(
                        reshopOffer, 
                        paxCount, 
                        booking, 
                        cancelType, 
                        odcRequest, 
                        totalDateChangePenalty,
                        allowedFarePrdtClass
                    );

                    if (simpleSearchRecom == null) {
                        negativeFare++;
                        continue;
                    }

                    boolean sameFare = checkIfSameFare(reshopOffer, allowedFarePrdtClass);
                    SimpleSearchRecommendationGroupV2 recommendationGroup = createRecommendationGroup(simpleSearchRecom, booking);
                    recommendations.add(new RecommendationHolder(recommendationGroup, reshopOffer, sameFare));
                }
            }
        }

        if (recommendations.isEmpty()) {
            setError(error, negativeFare > 0 ? 
                PSCommonErrorEnum.EXT_RECOMMENDATIONS_WITH_NEGATIVE_FARE : 
                PSCommonErrorEnum.EXT_NO_FLIGHTS_FOUND);
        }

        return recommendations;
    }

    private SimpleSearchRecommendationV2 buildSearchRecommendation(
            ReshopOffer reshopOffer,
            PaxCount paxCount,
            OrderViewRS booking,
            CancelType cancelType,
            DateChangeSearchRequest odcRequest,
            BigDecimal totalDateChangePenalty,
            List<String> allowedFarePrdtClass) {

        SimpleSearchRecommendationV2 simpleSearchRecom = new SimpleSearchRecommendationV2();
        String logKey = odcRequest.getPnr();

        simpleSearchRecom.setFareKey(reshopOffer.getOfferID());
        simpleSearchRecom.setHandBaggageFare(false);
        simpleSearchRecom.setRefundable(false);

        BigDecimal originalBookingTotalCost = recomBuildUtil.getOriginalBookingCost(
            cancelType, booking, paxCount, Arrays.asList("ALL"), LobSrcUtil.getRequiredCurrency(odcRequest));

        BigDecimal jrnyCost = BigDecimal.valueOf(reshopOffer.getTotalPrice().getTotalAmount().getAmount());
        String currency = LobSrcUtil.getRequiredCurrency(odcRequest);

        // Handle infant fare if needed
        if (paxCount.getInfant() > 0 && odcConfig.getInfantFare() != null 
            && odcConfig.getInfantFare().get(currency) != null
            && odcConfig.getInfantFare().get(currency).get(odcRequest.getAirline()) != null) {
            jrnyCost = jrnyCost.add(odcConfig.getInfantFare().get(currency)
                .get(odcRequest.getAirline()).get("DOM")
                .multiply(BigDecimal.valueOf(paxCount.getInfant())));
        }

        SimpleDateChangeDetails podcDetails = new SimpleDateChangeDetails();
        podcDetails.setDateChangeFee(totalDateChangePenalty.doubleValue());
        BigDecimal dateChangeTotal = jrnyCost
                .add(totalDateChangePenalty)
                .subtract(originalBookingTotalCost);

        if (dateChangeTotal.doubleValue() < 0 && 
            (odcConfig.getEnableNegativeFare() == null || !odcConfig.getEnableNegativeFare())) {
            return null;
        }

        podcDetails.setDateChangeTotal(dateChangeTotal.doubleValue());
        simpleSearchRecom.setOdcDetails(podcDetails);

        Map<PaxType, SimpleFare> paxWiseFare = calculatePaxWiseFare(
            reshopOffer, booking, cancelType, paxCount, getRequiredCurrency(odcRequest));
        simpleSearchRecom.setPaxWiseFare(paxWiseFare);

        String rKey = RKeyBuilderUtil.buildRKey(paxWiseFare, reshopOffer, paxCount, odcRequest.getCmsId());
        simpleSearchRecom.setrKey(rKey);

        return simpleSearchRecom;
    }

    private SimpleSearchRecommendationGroupV2 createRecommendationGroup(
            SimpleSearchRecommendationV2 simpleSearchRecom, 
            OrderViewRS booking) {
        SimpleSearchRecommendationGroupV2 group = new SimpleSearchRecommendationGroupV2();
        List<SimpleSearchRecommendationV2> recommendations = new ArrayList<>();
        recommendations.add(simpleSearchRecom);
        group.setSearchRecommendations(recommendations);
        group.setAirlines(recomBuildUtil.getAirlineFromBooking(booking));
        group.setSingleAdultFare(simpleSearchRecom.getPaxWiseFare().get(PaxType.ADULT));
        return group;
    }

    private boolean checkIfSameFare(ReshopOffer reshopOffer, List<String> allowedFarePrdtClass) {
        if (reshopOffer.getAddOfferItem() == null || reshopOffer.getAddOfferItem().isEmpty()) {
            return false;
        }
        
        for (com.mmt.flights.entity.odc.OfferItem offerItem : reshopOffer.getAddOfferItem()) {
            if (offerItem.getFareComponent() != null) {
                for (FareComponent fareComponent : offerItem.getFareComponent()) {
                    // Get the governing fare class from fare component
                    String fareClass = fareComponent.getFareBasis().getFareBasisCode().getCode();
                    if (allowedFarePrdtClass.contains(fareClass)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Map<PaxType, SimpleFare> calculatePaxWiseFare(
            ReshopOffer reshopOffer,
            OrderViewRS booking,
            CancelType cancelType,
            PaxCount paxCount,
            String requiredCurrency) {
        Map<PaxType, SimpleFare> paxFare = new HashMap<>();
        
        // Calculate fares for each passenger type
        if (paxCount.getAdult() > 0) {
            paxFare.put(PaxType.ADULT, calculateFareForType(reshopOffer, paxCount.getAdult(), PaxType.ADULT));
        }
        if (paxCount.getChild() > 0) {
            paxFare.put(PaxType.CHILD, calculateFareForType(reshopOffer, paxCount.getChild(), PaxType.CHILD));
        }
        if (paxCount.getInfant() > 0) {
            paxFare.put(PaxType.INFANT, calculateFareForType(reshopOffer, paxCount.getInfant(), PaxType.INFANT));
        }
        
        return paxFare;
    }

    private SimpleFare calculateFareForType(ReshopOffer reshopOffer, int count, PaxType type) {
        SimpleFare fare = new SimpleFare();
        BigDecimal paxCount = BigDecimal.valueOf(count);
        
        // Find the offer item matching the passenger type
        BigDecimal baseAmount = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;
        
        if (reshopOffer.getAddOfferItem() != null) {
            for (com.mmt.flights.entity.odc.OfferItem item : reshopOffer.getAddOfferItem()) {
                if (type.name().equals(item.getPassengerType())) {
                    // Extract base and tax amounts
                    if (item.getTotalPriceDetail() != null) {
                        baseAmount = BigDecimal.valueOf(item.getTotalPriceDetail().getTotalAmount().getBookingCurrencyPrice());
                        taxAmount = BigDecimal.valueOf(item.getTotalPriceDetail().getTotalAmount().getBookingCurrencyPrice());
                        break;
                    }
                }
            }
        }
        
        // If no specific offer item found, use the total price scaled by pax count
        if (baseAmount.equals(BigDecimal.ZERO) && taxAmount.equals(BigDecimal.ZERO)) {
            if (reshopOffer.getBasePrice() != null && reshopOffer.getTaxPrice() != null) {
                baseAmount = BigDecimal.valueOf(reshopOffer.getBasePrice().getBaseAmount().getBookingCurrencyPrice());
                taxAmount = BigDecimal.valueOf(reshopOffer.getTaxPrice().getTaxAmount().getBookingCurrencyPrice());
            }
        }
        
        fare.setBase(baseAmount.multiply(paxCount).doubleValue());
        fare.setTaxes(taxAmount.multiply(paxCount).doubleValue());
        return fare;
    }

    private void processRecommendations(List<RecommendationHolder> recommendations,
            List<SimpleSearchRecommendationGroupV2> sameFareGroupRecom,
            List<SimpleSearchRecommendationGroupV2> otherFareGroupRecom,
            List<SimpleJourney> itinearysToPopulate,
            CancelType cancelType) {
        
        for (RecommendationHolder recomHolder : recommendations) {
            SimpleSearchRecommendationGroupV2 simpleSrchRecom = recomHolder.recom;
            ReshopOffer reshopOffer = (ReshopOffer) recomHolder.onwardJourney;
            
            // Convert ReshopOffer to SimpleJourney
            SimpleJourney simpleJourney = convertReshopOfferToSimpleJourney(reshopOffer);
            int itneryIndex = -1;

            if (itinearysToPopulate.contains(simpleJourney)) {
                itneryIndex = itinearysToPopulate.indexOf(simpleJourney);
            } else {
                itinearysToPopulate.add(simpleJourney);
                itneryIndex = itinearysToPopulate.size() - 1;
            }

            List<Integer> itneraryJnryIndex = getJourneyIndices(itneryIndex, cancelType);
            simpleSrchRecom.getSearchRecommendations().get(0).setItneraryJrnyIndex(itneraryJnryIndex);

            if (recomHolder.sameFare) {
                sameFareGroupRecom.add(simpleSrchRecom);
            } else {
                otherFareGroupRecom.add(simpleSrchRecom);
            }
        }
    }

    private List<Integer> getJourneyIndices(int itneryIndex, CancelType cancelType) {
        if (cancelType.equals(CancelType.OW)) {
            return Arrays.asList(itneryIndex);
        } else if (cancelType.equals(CancelType.ForwardJourneyOfRoundTrip)) {
            return Arrays.asList(itneryIndex, 0);
        } else {
            return Arrays.asList(0, itneryIndex);
        }
    }

    private SimpleJourney convertReshopOfferToSimpleJourney(ReshopOffer reshopOffer) {
        SimpleJourney simpleJourney = new SimpleJourney();
        
        // Extract journey details from ReshopOffer's OfferItems
        if (reshopOffer.getAddOfferItem() != null && !reshopOffer.getAddOfferItem().isEmpty()) {
            com.mmt.flights.entity.odc.OfferItem firstOffer = reshopOffer.getAddOfferItem().get(0);
            if (firstOffer.getService() != null && !firstOffer.getService().isEmpty()) {
                com.mmt.flights.entity.odc.Service service = firstOffer.getService().get(0);
                
                // Set journey details
                simpleJourney.setOrigin(service.getDepartureAirport());
                simpleJourney.setDestination(service.getArrivalAirport());
                simpleJourney.setDepartureTime(service.getDepartureDateTime());
                simpleJourney.setArrivalTime(service.getArrivalDateTime());
                simpleJourney.setFlightNumber(service.getMarketingFlightNumber());
                simpleJourney.setAirlineCode(service.getMarketingCarrierCode());
            }
        }
        
        return simpleJourney;
    }

    private SimpleJourney convertOrderToSimpleJourney(com.mmt.flights.entity.pnr.retrieve.response.Order order) {
        SimpleJourney simpleJourney = new SimpleJourney();
        
        // Extract journey details from Order's OfferItems
        if (order.getOfferItem() != null && !order.getOfferItem().isEmpty()) {
            OfferItem firstOffer = order.getOfferItem().get(0);
            if (firstOffer.getService() != null && !firstOffer.getService().isEmpty()) {
                Service service = firstOffer.getService().get(0);
                
                // Set journey details
                simpleJourney.setOrigin(service.getDepartureAirport());
                simpleJourney.setDestination(service.getArrivalAirport());
                simpleJourney.setDepartureTime(service.getDepartureDateTime());
                simpleJourney.setArrivalTime(service.getArrivalDateTime());
                simpleJourney.setFlightNumber(service.getMarketingFlightNumber());
                simpleJourney.setAirlineCode(service.getMarketingCarrierCode());
            }
        }
        
        return simpleJourney;
    }

    private List<SimpleJourney> convertToSimpleJourneyList(com.mmt.flights.entity.pnr.retrieve.response.Order order) {
        List<SimpleJourney> simpleJourneyList = new ArrayList<>();
        SimpleJourney simpleJourney = convertOrderToSimpleJourney(order);
        simpleJourneyList.add(simpleJourney);
        return simpleJourneyList;
    }

    private List<RecommendationHolder> limitResEntry(List<RecommendationHolder> recommendations) {
        recommendations.sort((o1, o2) -> 
            o1.recom.getSingleAdultFare().getBase().compareTo(o2.recom.getSingleAdultFare().getBase()));
        return recommendations.stream()
                .limit(200)
                .collect(java.util.stream.Collectors.toList());
    }

    // Helper class for holding recommendations
    static class RecommendationHolder {
        public final SimpleSearchRecommendationGroupV2 recom;
        public final ReshopOffer onwardJourney;
        public final boolean sameFare;

        public RecommendationHolder(SimpleSearchRecommendationGroupV2 recom, ReshopOffer onwardJourney, boolean sameFare) {
            this.recom = recom;
            this.onwardJourney = onwardJourney;
            this.sameFare = sameFare;
        }
    }

    private void setError(ErrorDetails error, PSCommonErrorEnum errorEnum) {
        error.setErrorCode(errorEnum.getCode());
        error.setErrorDescription(errorEnum.getMessage());
    }

    private PaxCount getPaxCount(OrderViewRS booking) {
        PaxCount paxCount = new PaxCount();
        
        // Extract passenger counts from passenger list in DataLists
        if (booking.getDataLists() != null && 
            booking.getDataLists().getPassengerList() != null &&
            booking.getDataLists().getPassengerList().getPassengers() != null) {
                
            for (com.mmt.flights.entity.pnr.retrieve.response.Passenger passenger : 
                    booking.getDataLists().getPassengerList().getPassengers()) {
                
                String paxType = passenger.getPassengerTypeCode();
                if ("ADT".equals(paxType)) {
                    paxCount.setAdult(paxCount.getAdult() + 1);
                } else if ("CHD".equals(paxType)) {
                    paxCount.setChild(paxCount.getChild() + 1);
                } else if ("INF".equals(paxType)) {
                    paxCount.setInfant(paxCount.getInfant() + 1);
                }
            }
        }
        
        return paxCount;
    }

    private SimpleSearchResponseV2 convertToSimpleSearchResponse(List<RecommendationHolder> recommendations, 
            CancelType cancelType, OrderViewRS orderViewRS) {
        SimpleSearchResponseV2 simpleSearchResponse = new SimpleSearchResponseV2();
        List<SimpleSearchRecommendationGroupV2> sameFareGroupRecom = new ArrayList<>();
        List<SimpleSearchRecommendationGroupV2> otherFareGroupRecom = new ArrayList<>();
        List<List<SimpleJourney>> itineraryJourneyList = new ArrayList<>();
        
        // Set up lists based on cancelType
        List<SimpleJourney> itinearysToPopulate = setupItineraryLists(cancelType, itineraryJourneyList, orderViewRS);
        
        simpleSearchResponse.setItineraryJourneyList(itineraryJourneyList);
        simpleSearchResponse.setSameFareRcomGrps(sameFareGroupRecom);
        simpleSearchResponse.setOtherFareRcomGrps(otherFareGroupRecom);
        
        // Process recommendations and populate response
        processRecommendations(recommendations, sameFareGroupRecom, otherFareGroupRecom, itinearysToPopulate, cancelType);
        
        return simpleSearchResponse;
    }

    private List<SimpleJourney> setupItineraryLists(CancelType cancelType, List<List<SimpleJourney>> itineraryJourneyList, 
            OrderViewRS orderViewRS) {
        List<SimpleJourney> itinearysToPopulate;
        if (cancelType.equals(CancelType.OW)) {
            itinearysToPopulate = new ArrayList<>();
            itineraryJourneyList.add(itinearysToPopulate);
        } else if (cancelType.equals(CancelType.ForwardJourneyOfRoundTrip)) {
            List<SimpleJourney> onwardItnery = new ArrayList<>();
            List<SimpleJourney> rtItnery = convertToSimpleJourneyList(orderViewRS.getOrder().get(0));
            itineraryJourneyList.add(onwardItnery);
            itineraryJourneyList.add(rtItnery);
            itinearysToPopulate = onwardItnery;
        } else if (cancelType.equals(CancelType.ReturnJourneyOfRoundTrip)) {
            List<SimpleJourney> onwardItnery = convertToSimpleJourneyList(orderViewRS.getOrder().get(0));
            List<SimpleJourney> rtItnery = new ArrayList<>();
            itineraryJourneyList.add(onwardItnery);
            itineraryJourneyList.add(rtItnery);
            itinearysToPopulate = rtItnery;
        } else {
            throw new PSErrorException(PSCommonErrorEnum.EXT_NO_FLIGHTS_FOUND);
        }
        return itinearysToPopulate;
    }
}