package com.mmt.flights.odc.util;

import com.mmt.flights.flightutil.review.key.*;
import com.mmt.flights.odc.common.enums.PaxType;
import com.mmt.flights.odc.search.SimpleFare;
import com.mmt.flights.supply.search.v4.response.*;
import com.mmt.flights.supply.common.SupplyPaxType;

import java.math.BigDecimal;
import java.util.*;

public class RKeyBuilderUtil {

    public static String buildRKey(Map<PaxType, SimpleFare> paxWiseFare, JourneyFare fare, PaxCount paxCount, Journey journey, String cmsId) {
        String fareKey = getFareKey(paxWiseFare, fare, paxCount);
        String recomKey = getRecomKey(journey,cmsId);

        ReviewKey.Builder builder = new ReviewKey.Builder();
        return builder.ver(2).fareKey(fareKey).recommendationKey(recomKey).build();
    }

    public static String buildRKey(Map<PaxType, SimpleFare> paxWiseFare, JourneyFare fare,JourneyFare rtFare, PaxCount paxCount, Journey journey,Journey rtJourney, String cmsId) {
        String fareKey = getFareKey(paxWiseFare, fare,rtFare, paxCount);
        String recomKey = getRecomKey(journey,rtJourney,cmsId);

        ReviewKey.Builder builder = new ReviewKey.Builder();
        return builder.ver(2).fareKey(fareKey).recommendationKey(recomKey).build();
    }

    private static String getRecomKey(Journey journey, String cmsId){
        return getRecomKey(journey,null,cmsId);
    }

    private static String getRecomKey(Journey journey,Journey rtJourney, String cmsId) {
        RecommendationKey recommendationKey = new RecommendationKey();

        Map<Integer, RecommendationKeyFragment> recommendationKeyMap = new HashMap<>();

        recommendationKeyMap.put(0, getBuilder(journey,cmsId,false).build());

        if(rtJourney!=null){
            recommendationKeyMap.put(1, getBuilder(rtJourney,cmsId,true).build());
        }

        recommendationKey.setRecommendationKeyMap(recommendationKeyMap);

        return recommendationKey.build();
    }

    private static RecommendationKeyFragment.Builder getBuilder(Journey journey,String cmsId, boolean rtJourney) {
        RecommendationKeyFragment.Builder recomBuilder = new RecommendationKeyFragment.Builder();

        List<RecommKeySegment> segments = new ArrayList<>();

        journey.getSegments().forEach(seg -> {
            RecommKeySegment recommKeySegment = new RecommKeySegment();

            recommKeySegment.setArrCityCode(seg.getDesignator().getDestination());
            recommKeySegment.setArrDate(formatDateTime(seg.getDesignator().getArrival(), true));
            recommKeySegment.setArrTime(formatDateTime(seg.getDesignator().getArrival(), false));

            recommKeySegment.setDepCityCode(seg.getDesignator().getOrigin());
            recommKeySegment.setDepDate(formatDateTime(seg.getDesignator().getDeparture(), true));
            recommKeySegment.setDepTime(formatDateTime(seg.getDesignator().getDeparture(), false));

            recommKeySegment.setFlightNumber(seg.getIdentifier().getCarrierCode()+"-"+seg.getIdentifier().getIdentifier());

            recommKeySegment.setMarketingAirline(seg.getIdentifier().getCarrierCode());
            recommKeySegment.setOperatingAirline(seg.getIdentifier().getCarrierCode());
            recommKeySegment.setValidatingAirline(seg.getIdentifier().getCarrierCode());
            recommKeySegment.setStopType(RecommKeySegment.StopType.PLANE_CHANGE);
            recommKeySegment.setCmsId(cmsId);

            List<Leg> legs = seg.getLegs();
            if(legs != null && legs.size()>0) {
                recommKeySegment.setDepTerminal(legs.get(0).getLegInfo().getDepartureTerminal());
                recommKeySegment.setArrTerminal(legs.get(legs.size()-1).getLegInfo().getArrivalTerminal());
            }
            recommKeySegment.setFlightServiceName("DOTREZ");
            segments.add(recommKeySegment);
        });

        if(!segments.isEmpty()){
            segments.get(segments.size()-1).setStopType(RecommKeySegment.StopType.JOURNEY_OVER);
        }

        recomBuilder.segments(segments);
        RecommKeyOtherInfo otherInfo = new RecommKeyOtherInfo();
        otherInfo.setFlightServiceName("DOTREZ");
        otherInfo.setPnrGroup(0);
        otherInfo.setRtFareFlag(rtJourney?1:0);
        recomBuilder.otherInfo(otherInfo);

        return recomBuilder;
    }

    private static String formatDateTime(String dateTime, boolean isDate) {
        if (dateTime == null) return "";
        String[] parts = dateTime.split(" ");
        if (parts.length != 2) return "";
        
        String[] dateParts = parts[0].split("-");
        if (dateParts.length != 3) return "";
        
        if (isDate) {
            return dateParts[2] + dateParts[1] + dateParts[0].substring(2); // DDMMYY
        } else {
            return parts[1].substring(0, 5); // HH:mm
        }
    }

    private static String getFareKey(Map<PaxType, SimpleFare> paxWiseFare, JourneyFare fare, PaxCount paxCount) {
       return getFareKey(paxWiseFare,fare,null,paxCount);
    }

    private static String getFareKey(Map<PaxType, SimpleFare> paxWiseFare, JourneyFare fare,JourneyFare rtJourneyFare, PaxCount paxCount) {
        FareKey fKey = new FareKey();

        FlightLevelInformation.Builder builder = new FlightLevelInformation.Builder();
        builder.adultNumber((int) paxCount.getAdult())
               .childNumber((int) paxCount.getChild())
               .infantNumber((int) paxCount.getInfant());

        fKey.setPaxFareInformation(getPaxFareInformation(paxWiseFare));
        fKey.setFlightLevelInformation(builder.build());

        Map<Integer, List<FareKeySegment>> fareKeyMap = new HashMap<>();
        fareKeyMap.put(0, Collections.singletonList(getFareKeySegment(fare)));
        if(rtJourneyFare != null){
            fareKeyMap.put(1, Collections.singletonList(getFareKeySegment(rtJourneyFare)));
        }
        fKey.setFareKeyMap(fareKeyMap);

        return fKey.build();
    }

    private static FareKeySegment getFareKeySegment(JourneyFare fare) {
        FareKeySegment.Builder fareKeySegment = new FareKeySegment.Builder();
        if (fare != null && fare.getFares() != null && !fare.getFares().isEmpty()) {
            fareKeySegment.fareBasis(fare.getFares().get(0).getFareBasisCode())
                         .fareClass(fare.getFares().get(0).getClassOfService())
                         .productClass(fare.getFares().get(0).getProductClass())
                         .pax("A");

            fare.getFares().forEach(fare1 -> {
                if (fare1.getPassengerFares() != null) {
                    fare1.getPassengerFares().forEach(passengerFare -> {
                        if (passengerFare.getServiceCharges() != null && 
                            passengerFare.getPassengerType() != null && 
                            passengerFare.getPassengerType().equals(SupplyPaxType.ADULT)) {
                            
                            passengerFare.getServiceCharges().forEach(c -> {
                                if (c.getType() == ServiceChargeType.Tax && c.getCode() != null) {
                                    switch (c.getCode().toLowerCase()) {
                                        case "udf":
                                            fareKeySegment.udf(String.valueOf(c.getAmount()));
                                            break;
                                        case "psf":
                                            fareKeySegment.psf(String.valueOf(c.getAmount()));
                                            break;
                                        case "yq":
                                            fareKeySegment.yq(String.valueOf(c.getAmount()));
                                            break;
                                        case "yr":
                                            fareKeySegment.yr(String.valueOf(c.getAmount()));
                                            break;
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
        return fareKeySegment.build();
    }

    private static PaxFareInformation getPaxFareInformation(Map<PaxType, SimpleFare> paxWiseFare) {
        double adultTotal = 0;
        double childTotal = 0;
        double infantTotal = 0;
        double adultBase = 0;
        double childBase = 0;
        double infantBase = 0;

        SimpleFare adultFare = paxWiseFare.get(PaxType.ADULT);
        SimpleFare childFare = paxWiseFare.get(PaxType.CHILD);
        SimpleFare infantFare = paxWiseFare.get(PaxType.INFANT);

        if (adultFare != null) {
            adultTotal = adultFare.getBase() + adultFare.getTaxes();
            adultBase = adultFare.getBase();
        }

        if (childFare != null) {
            childTotal = childFare.getBase() + childFare.getTaxes();
            childBase = childFare.getBase();
        }

        if (infantFare != null) {
            infantTotal = infantFare.getBase() + infantFare.getTaxes();
            infantBase = infantFare.getBase();
        }

        return new PaxFareInformation(new BigDecimal(adultTotal),
                new BigDecimal(childTotal), new BigDecimal(infantTotal), 
                new BigDecimal(adultBase), new BigDecimal(childBase), new BigDecimal(infantBase));
    }
}
