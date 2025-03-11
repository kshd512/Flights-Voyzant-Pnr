package com.mmt.flights.odc.service.tasks;


import com.mmt.flights.dotrez.pnr.pnrresponse.*;
import com.mmt.flights.flightsutil.AirportDetailsUtil;
import com.mmt.flights.odc.common.CancelType;
import com.mmt.flights.odc.common.Pair;
import com.mmt.flights.odc.common.enums.PaxType;
import com.mmt.flights.odc.search.SimpleFlight;
import com.mmt.flights.odc.search.SimpleLocationInfo;
import com.mmt.flights.odc.search.SimpleTechnicalStop;
import com.mmt.flights.odc.tasks.RecommendationBuilderOW;
import com.mmt.flights.pnr.config.DotRezConfig;
import com.mmt.flights.pnr.constants.ErrorEnum;
import com.mmt.flights.pnr.exceptions.ServiceGeneralException;
import com.mmt.flights.pnr.util.AdapterUtil;
import com.mmt.flights.pnr.util.DateTimeUtil;
import com.mmt.flights.pnr.util.MMTLogger;
import com.mmt.flights.supply.book.v4.common.SupplyFlightDetailDTO;
import com.mmt.flights.supply.book.v4.response.SupplyBookingJourneyDTO;
import com.mmt.flights.supply.book.v4.response.SupplyBookingResponseDTO;
import com.mmt.flights.supply.book.v4.response.SupplyFareDetailDTO;
import com.mmt.flights.supply.book.v4.response.SupplySegmentProductInfo;
import com.mmt.flights.supply.common.enums.SupplyPaxType;
import com.mmt.flights.supply.search.v4.response.SupplyFlightDTO;
import com.mmt.flights.supply.search.v4.response.SupplyTechnicalStopDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component

public class RecommendationBuilderUtil {


    @Autowired
    CostUtils costUtil;

    @Autowired
    private DotRezConfig dotRezConfig;

    @Autowired
    AirportDetailsUtil airportUtil;

    public List<SimpleFlight> populateFlightsFromJourney(List<Segment> segments) {
        List<SimpleFlight> flights = new ArrayList<>();
        segments.forEach(a -> {
            flights.add(getFlight(a));
        });
        return flights;
    }

    private SimpleFlight getFlight(Segment segment) {
        int noOfLegs = segment.getLegs().size();
        final Leg legLast = segment.getLegs().get(noOfLegs-1);
        SimpleFlight flight = new SimpleFlight();
        Designator designator = segment.getDesignator();

        flight.setDepTime(DateTimeUtil.getStandardDateTime(designator.getDeparture()));
        flight.setArrTime(DateTimeUtil.getStandardDateTime(designator.getArrival()));
        flight.setDepartureInfo(getLocationInfo(segment, true));
        flight.setArrivalInfo(getLocationInfo(segment, false));
        String carrierCode = segment.getIdentifier().getCarrierCode();
        String operatingCarrierCode = "";

        if (StringUtils.isEmpty(operatingCarrierCode))
            flight.setOperatingAirline(carrierCode);
        else
            flight.setOperatingAirline(operatingCarrierCode);

        flight.setMarketingAirline(carrierCode);
        flight.setFlightNum(segment.getIdentifier().getIdentifier().trim());
        flight.setAircraftType(legLast.getLegInfo().getEquipmentType());

        populateTechnicalStopOver(segment, flight);
        long durationInMinutes = DateTimeUtil.getDuration(flight.getDepTime(), segment.getDesignator().getOrigin(), flight.getArrTime(), segment.getDesignator().getDestination(), airportUtil);
        flight.setDuration((int) durationInMinutes);

        return flight;
    }


    private SimpleFlight getFlight(SupplyFlightDetailDTO supplyFlight, Map<String, SupplyFlightDTO> flightLookUpListMap) {
        SimpleFlight flight = new SimpleFlight();
        for (Map.Entry<String, SupplyFlightDTO> entry : flightLookUpListMap.entrySet()) {
            if (supplyFlight.getFltLookUpKey().equalsIgnoreCase(entry.getKey())) {
                flight.setDepTime(entry.getValue().getDepTime());
                flight.setArrTime(entry.getValue().getArrTime());
                flight.setDepartureInfo(getDepatureInfo(entry.getValue()));
                flight.setArrivalInfo(getArrivalInfo(entry.getValue()));
                flight.setOperatingAirline(entry.getValue().getOprAl());
                flight.setMarketingAirline(entry.getValue().getMrkAl());
                flight.setFlightNum(entry.getValue().getFltNo());
                flight.setAircraftType(entry.getValue().getArcrfTyp());
                flight.setDuration((int) entry.getValue().getDurInMins());
                populateTechnicalStopOver(entry.getValue().getTchStpList(), flight);
            }
        }
        return flight;
    }

    private void populateTechnicalStopOver(List<SupplyTechnicalStopDTO> tchStpList, SimpleFlight flight) {
        List<SimpleTechnicalStop> simpleTechStop = new ArrayList<>();
        tchStpList.forEach(x -> {
            SimpleTechnicalStop technicalStopover = new SimpleTechnicalStop();
            technicalStopover.setCode(x.getLocInfo().getArpCd());
            technicalStopover.setDuration((int) x.getDurInMins());
            simpleTechStop.add(technicalStopover);
        });
        flight.setTechStops(simpleTechStop);
    }

    private void populateTechnicalStopOver(Segment segment, SimpleFlight flight) {
        if (segment != null && segment.getLegs() != null
                && segment.getLegs().size() > 1) {
            final MutableInt count = new MutableInt(0);
            List<SimpleTechnicalStop> simpleTechStop = new ArrayList<>();
            segment.getLegs().forEach(a -> {
                if (segment.getLegs().size()-1 > count.intValue()) {
                    SimpleTechnicalStop technicalStopover = new SimpleTechnicalStop();
                    Designator designator = a.getDesignator();
                    technicalStopover.setCode(designator.getDestination());
                    String arrival = DateTimeUtil.getStandardDateTime(designator.getArrival());

                    String nextDeparture = DateTimeUtil.getStandardDateTime(segment.getLegs().get(count.intValue()+1).getDesignator().getDeparture());
                    long duration = DateTimeUtil.getDuration(arrival, designator.getDestination(), nextDeparture, segment.getLegs().get(count.intValue()+1).getDesignator().getOrigin(), airportUtil);
                    technicalStopover.setDuration((int) duration);
                    SimpleLocationInfo techstopLocationInfo = new SimpleLocationInfo();
                    techstopLocationInfo.setCityCode(designator.getDestination());
                    simpleTechStop.add(technicalStopover);
                    count.add(1);
                }
            });
            flight.setTechStops(simpleTechStop);
        }
    }

    private SimpleLocationInfo getDepatureInfo(SupplyFlightDTO flight) {
        SimpleLocationInfo locationInfo = new SimpleLocationInfo();
        locationInfo.setCityCode(flight.getDepInfo().getArpCd());
        locationInfo.setTerminal(flight.getDepInfo().getTrmnl());
        return locationInfo;
    }

    private SimpleLocationInfo getArrivalInfo(SupplyFlightDTO flight) {
        SimpleLocationInfo locationInfo = new SimpleLocationInfo();
        locationInfo.setCityCode(flight.getArrInfo().getArpCd());
        locationInfo.setTerminal(flight.getArrInfo().getTrmnl());
        return locationInfo;
    }

    private SimpleLocationInfo getLocationInfo(Segment segment, Boolean forDeparture) {
        SimpleLocationInfo locationInfo = new SimpleLocationInfo();
        if (forDeparture) {
            locationInfo.setCityCode(segment.getDesignator().getOrigin());
            if (segment.getLegs() != null
                    && segment.getLegs().size() > 0 && segment.getLegs().get(0).getLegInfo() != null)
                locationInfo.setTerminal(segment.getLegs().get(0).getLegInfo().getDepartureTerminal());
        } else {
            locationInfo.setCityCode(segment.getDesignator().getDestination());
            if (segment.getLegs() != null
                    && segment.getLegs().size() > 0) {
                int legCount = segment.getLegs().size();
                if (segment.getLegs().get(legCount-1).getLegInfo() != null)
                    locationInfo.setTerminal(
                            segment.getLegs().get(legCount-1).getLegInfo().getArrivalTerminal());
            }
        }
        return locationInfo;
    }


    public String getAirlineFromJrny(Journey journey) {
        return journey.getSegments().get(0).getIdentifier().getCarrierCode().trim();
    }

    public List<String> getAirlineFromBooking(Booking booking) {
        return Collections.singletonList(booking.getJourneys().get(0).getSegments().get(0).getIdentifier().getCarrierCode());
    }

    public String getBookingPClass(Journey journey) {
        List<Segment> segments = journey.getSegments();
        List<Fare> segmentFares = segments.stream()
                .flatMap(segment -> segment.getFares().stream())
                .collect(Collectors.toList());
        Optional<String> governingPclass = getGoverningPclass(segmentFares);
        return governingPclass.orElse(null);
    }

    public Optional<String> getGoverningPclass(List<Fare> segmentfares) {
        List<Fare> fares = segmentfares.stream()
                .filter(fare -> org.apache.commons.lang3.StringUtils.isNotBlank(fare.getProductClass()))
                .collect(Collectors.toList());
        List<String> pClasses =
                fares.stream()
                        .map(Fare::getProductClass)
                        .collect(Collectors.toList());

        if (new HashSet<>(pClasses).size() == 1) {// check all entry are same
            return Optional.of(pClasses.get(0));
        }
        // fter governing pClass
        Optional<Fare> governing = fares.stream()
                .filter(f -> f.getFareApplicationType().equals(FareApplicationType.Governing))
                .findFirst();
        return governing.map(Fare::getProductClass);
    }


    public String getRKey(Journey journey, RecommendationBuilderOW.FaresHolder fare) {

        return journey.getJourneyKey()+"$"+fare.journeyFare.getFareAvailabilityKey();
    }

    public BigDecimal getOriginalBookingCost(CancelType cancelType, Booking bking, PaxCount paxCount, List<String> includedChrCode, String requiredCur) {
        List<Journey> jrnyToConsider = new ArrayList<>();
        if (cancelType.equals(CancelType.OW)) {
            jrnyToConsider.add(bking.getJourneys().get(0));
        } else if (cancelType.equals(CancelType.ForwardJourneyOfRoundTrip)) {
            jrnyToConsider.add(bking.getJourneys().get(0));
        } else if (cancelType.equals(CancelType.ReturnJourneyOfRoundTrip)) {
            jrnyToConsider.add(bking.getJourneys().get(1));
        } else if (cancelType.equals(CancelType.BothLegOfRT)) {
            jrnyToConsider.add(bking.getJourneys().get(0));
            jrnyToConsider.add(bking.getJourneys().get(1));
        }
        BigDecimal cost = BigDecimal.ZERO;
        for (Journey journey : jrnyToConsider) {
            BigDecimal costOfJrny = costOfJourney(bking, journey, paxCount, includedChrCode, requiredCur);
            cost = cost.add(costOfJrny);
        }
        return cost;
    }

    public BigDecimal getOriginalBookingCost(CancelType cancelType, Booking bking, PaxCount paxCount, List<String> includedChrCode, String requiredCur, double[] roe) {
        List<Journey> jrnyToConsider = new ArrayList<>();
        if (cancelType.equals(CancelType.OW)) {
            jrnyToConsider.add(bking.getJourneys().get(0));
        } else if (cancelType.equals(CancelType.ForwardJourneyOfRoundTrip)) {
            jrnyToConsider.add(bking.getJourneys().get(0));
        } else if (cancelType.equals(CancelType.ReturnJourneyOfRoundTrip)) {
            jrnyToConsider.add(bking.getJourneys().get(1));
        } else if (cancelType.equals(CancelType.BothLegOfRT)) {
            jrnyToConsider.add(bking.getJourneys().get(0));
            jrnyToConsider.add(bking.getJourneys().get(1));
        }
        BigDecimal cost = BigDecimal.ZERO;
        for (Journey journey : jrnyToConsider) {
            BigDecimal costOfJrny = costOfJourney(bking, journey, paxCount, includedChrCode, requiredCur, roe);
            cost = cost.add(costOfJrny);
        }
        return cost;
    }

    private boolean skipFare(SupplyPaxType supplyPaxType, PaxCount paxCount, PassengerFare paxFare, String logKey) {
        if(dotRezConfig.getMembershipDiscountCode().contains(paxFare.getDiscountCode())){
            MMTLogger.info(logKey,"Found Membership discount fare",this.getClass().getName());
            return (supplyPaxType.equals(SupplyPaxType.ADULT) && paxCount.getAdult() > 1) || (supplyPaxType.equals(SupplyPaxType.CHILD) && paxCount.getChild() > 1);
        }
        return false;
    }
    private BigDecimal costOfJourney(Booking bking, Journey journey, PaxCount paxCount, List<String> includedChrCode, String requiredCur) {
        BigDecimal ans = BigDecimal.ZERO;
        List<ServiceCharge> allServiceCharges = journey.getSegments().stream()
                .flatMap(seg -> seg.getFares().get(0).getPassengerFares().stream())
                .filter(paxFare -> {
                    SupplyPaxType supplyPaxType = AdapterUtil.getSupplyPaxType(paxFare.getPassengerType());
                    if(supplyPaxType==SupplyPaxType.ADULT){
                        boolean skip = skipFare(supplyPaxType, paxCount, paxFare, bking.getRecordLocator());
                        if(skip){
                            MMTLogger.info(bking.getRecordLocator(),"Skipping Membership discount fare",this.getClass().getName());
                        }
                        return !skip;
                    } else {
                        return false;
                    }
                })
                .flatMap(adtPaxFare -> adtPaxFare.getServiceCharges().stream())
                .collect(Collectors.toList());

        for (ServiceCharge bkingServiceCharge : allServiceCharges) {
            if (includedChrCode.contains("ALL")
                    || includedChrCode.contains(bkingServiceCharge.getCode())
                    || includedChrCode.contains(bkingServiceCharge.getType().name())) {
                BigDecimal amount = BigDecimal.valueOf(bkingServiceCharge.getAmount());
                String currencyCodeInXML = bkingServiceCharge.getCurrencyCode();
                BigDecimal conversionRate = null;
                try {
                    conversionRate = costUtil.getRateOfConversion(requiredCur, currencyCodeInXML);
                } catch (Exception e) {
                    throw new ServiceGeneralException(e.getMessage(), ErrorEnum.CURRENCY_CONVERSION_ERROR);
                }
                if (bkingServiceCharge.getType().equals(ServiceChargeType.Discount) || bkingServiceCharge.getType().equals(ServiceChargeType.PromotionDiscount)) {
                    ans = ans.subtract(amount.multiply(conversionRate));
                } else {
                    ans = ans.add(amount.multiply(conversionRate));
                }
            }
        }
        ans = ans.multiply(new BigDecimal(paxCount.getAdult()+paxCount.getChild()));

        if (paxCount.getInfant() > 0) {
            Pair<BigDecimal, BigDecimal> infFare = costUtil.computeSingleInfFare(bking, journey, requiredCur);
            BigDecimal infFareTotal = infFare.getFirst().add(infFare.getSecond()).multiply(new BigDecimal(paxCount.getInfant()));
            ans = ans.add(infFareTotal);
        }
        return ans;
    }

    private BigDecimal costOfJourney(Booking bking, Journey journey, PaxCount paxCount, List<String> includedChrCode, String requiredCur, double[] roe) {
        BigDecimal ans = BigDecimal.ZERO;
        List<ServiceCharge> allServiceCharges = journey.getSegments().stream()
                .flatMap(seg -> seg.getFares().get(0).getPassengerFares().stream())
                .filter(paxFare -> {
                    SupplyPaxType supplyPaxType = AdapterUtil.getSupplyPaxType(paxFare.getPassengerType());
                    if(supplyPaxType==SupplyPaxType.ADULT){
                        boolean skip = skipFare(supplyPaxType, paxCount, paxFare, bking.getRecordLocator());
                        if(skip){
                            MMTLogger.info(bking.getRecordLocator(),"Skipping Membership discount fare",this.getClass().getName());
                        }
                        return !skip;
                    } else {
                        return false;
                    }
                })
                .flatMap(adtPaxFare -> adtPaxFare.getServiceCharges().stream())
                .collect(Collectors.toList());

        for (ServiceCharge bkingServiceCharge : allServiceCharges) {
            if (includedChrCode.contains("ALL")
                    || includedChrCode.contains(bkingServiceCharge.getCode())
                    || includedChrCode.contains(bkingServiceCharge.getType().name())) {
                BigDecimal amount = BigDecimal.valueOf(bkingServiceCharge.getAmount());
                String currencyCodeInXML = bkingServiceCharge.getCurrencyCode();
                BigDecimal conversionRate = null;
                try {
                    conversionRate = costUtil.getRateOfConversion(requiredCur, currencyCodeInXML);
                    roe[0] = conversionRate.doubleValue();
                } catch (Exception e) {
                    throw new ServiceGeneralException(e.getMessage(), ErrorEnum.CURRENCY_CONVERSION_ERROR);
                }
                if (bkingServiceCharge.getType().equals(ServiceChargeType.Discount) || bkingServiceCharge.getType().equals(ServiceChargeType.PromotionDiscount)) {
                    ans = ans.subtract(amount.multiply(conversionRate));
                } else {
                    ans = ans.add(amount.multiply(conversionRate));
                }
            }
        }
        ans = ans.multiply(new BigDecimal(paxCount.getAdult()+paxCount.getChild()));

        if (paxCount.getInfant() > 0) {
            Pair<BigDecimal, BigDecimal> infFare = costUtil.computeSingleInfFare(bking, journey, requiredCur);
            BigDecimal infFareTotal = infFare.getFirst().add(infFare.getSecond()).multiply(new BigDecimal(paxCount.getInfant()));
            ans = ans.add(infFareTotal);
        }
        return ans;
    }

    public Pair<Double, Double> getUnchangedJourneyCost(SupplyBookingResponseDTO getPnrResponse, SupplyBookingJourneyDTO journey, PaxType paxType, String requiredCur) {
        double base = 0.0;
        double tax = 0.0;

        BigDecimal conversionRate = null;
        try {
            conversionRate = costUtil.getRateOfConversion(requiredCur, getPnrResponse.getMetaData().getCurrency());
        } catch (Exception e) {
            throw new ServiceGeneralException(e.getMessage(), ErrorEnum.CURRENCY_CONVERSION_ERROR);
        }

        Map<String, SupplyFareDetailDTO> paxFaresMap = getPnrResponse.getBookingInfo().getFrInfo().getPnrGrpdFrInfoMap().get(0).getPaxFaresMap();
        for(SupplyFlightDetailDTO flight : journey.getFlightDtlsInfoList()){
            SupplySegmentProductInfo segmentProductInfo = paxFaresMap.get(paxType.getPaxTypeName().toUpperCase()).getSegPrdctInfoMap().get(flight.getFltLookUpKey());
            base = base + (segmentProductInfo.getSgFare().getBs() * conversionRate.doubleValue());
            tax =  tax + (segmentProductInfo.getSgFare().getTx() * conversionRate.doubleValue());
        }


        return new Pair<Double, Double>(base, tax);
    }
}
