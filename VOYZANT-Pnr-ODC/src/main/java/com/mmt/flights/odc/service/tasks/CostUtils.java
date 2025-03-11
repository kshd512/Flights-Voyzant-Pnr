package com.mmt.flights.odc.service.tasks;

import com.mmt.flights.addOns.AddOnsDetails;
import com.mmt.flights.dotrez.pnr.pnrresponse.*;
import com.mmt.flights.dotrez.search.JourneyFare;
import com.mmt.flights.flightsutil.AirportDetailsUtil;
import com.mmt.flights.flightsutil.DateUtil;
import com.mmt.flights.helper.CurrencyConverter;
import com.mmt.flights.odc.common.CancelType;
import com.mmt.flights.odc.common.Pair;
import com.mmt.flights.odc.config.SkipAncillaryConfig;
import com.mmt.flights.pnr.config.DotRezConfig;
import com.mmt.flights.pnr.constants.ErrorEnum;
import com.mmt.flights.pnr.exceptions.ServiceGeneralException;
import com.mmt.flights.pnr.metrics.MetricServices;
import com.mmt.flights.pnr.util.*;
import com.mmt.flights.postsales.error.PSCommonErrorEnum;
import com.mmt.flights.supply.common.enums.SupplyAddonsTypeOuterClass;
import com.mmt.flights.supply.common.enums.SupplyPaxType;
import com.mmt.flights.util.AddOnsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.mmt.flights.dotrez.pnr.pnrresponse.ServiceChargeType.*;

@Component
public class CostUtils {

    @Autowired
    AirportDetailsUtil airportUtil;
    @Autowired
    private CurrencyConverter currencyConverter;
    @Autowired
    private DotRezConfig config;
    @Autowired
    private AddOnsUtilWrapper addOnsUtilWrapper;

    private static boolean isPaxFeeBelongToJrny(Fee paxFee, Journey jrny) {
        long applicablePaxFeeCount =
                jrny.getSegments().stream()
                        .map(seg -> DateTimeUtil.formatDate(seg.getDesignator().getDeparture(), DateUtil.YYYMMDD))
                        .filter(stdString -> paxFee.getFlightReference() != null && paxFee.getFlightReference().contains(stdString))
                        .count();
        return applicablePaxFeeCount > 0;
    }


    private static boolean isAncillaryFee(Fee paxFee) {
        if (FeeType.SeatFee.equals(paxFee.getType()))
            return true;
        // blank ssr code generally not ancillary
        return StringUtils.isNotBlank(paxFee.getSsrCode());
    }


    public static BigDecimal computeAncillaryCost(CancelType cancelType, Booking booking) {
        if (cancelType.equals(CancelType.OW) ||
                cancelType.equals(CancelType.ForwardJourneyOfRoundTrip)) {
            return ancillaryCost(booking.getJourneys().get(0), booking);
        } else if (cancelType.equals(CancelType.ReturnJourneyOfRoundTrip)) {
            return ancillaryCost(booking.getJourneys().get(1), booking);
        } else if (cancelType.equals(CancelType.BothLegOfRT)) {
            return ancillaryCost(booking.getJourneys().get(0), booking)
                    .add(ancillaryCost(booking.getJourneys().get(1), booking));
        }
        return null;
    }

    public static BigDecimal ancillaryCost(Journey jrny, Booking bking) {
        List<Fee> allApplicablePaxFee =
                bking.getPassengers().values().stream()
                        .filter(pax -> pax.getFees() != null)
                        .flatMap(pax -> pax.getFees().stream())
                        .filter(paxFee -> !"INFT".equals(paxFee.getCode()))
                        .filter(CostUtils::isAncillaryFee)// blnk ssrcode are fees
                        .filter(paxFee -> isPaxFeeBelongToJrny(paxFee, jrny))
                        .collect(Collectors.toList());

        BigDecimal cost = allApplicablePaxFee.stream()
                .filter(paxFee -> paxFee.getServiceCharges() != null)
                .flatMap(paxFee -> paxFee.getServiceCharges().stream())
                .filter(serviceCharge -> serviceCharge.getType() != null && !serviceCharge.getType().name().contains("Included"))// exclude includedtax service charges
                .map(serviceCharge -> {
                    double amount = serviceCharge.getAmount();
                    if(serviceCharge.getType().equals(Discount)){
                        amount = amount *-1;
                    }
                    return BigDecimal.valueOf(amount);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return cost;
    }


    public Pair<BigDecimal, BigDecimal> computeSingleInfFare(Booking booking, Journey journey, String requiredCur) {
        BigDecimal base = BigDecimal.ZERO;
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Segment seg : journey.getSegments()) {
            String from = seg.getDesignator().getOrigin();
            String to = seg.getDesignator().getDestination();
            for (Passengers passenger : booking.getPassengers().values()) {
                if (passenger.getInfant() != null) {
                    for (Fee passengerFee : passenger.getInfant().getFees()) {
                        String[] oAndD = passengerFee.getFlightReference().split(" ");
                        if ("INFT".equalsIgnoreCase(passengerFee.getCode()) &&
                                oAndD[oAndD.length - 1].equalsIgnoreCase(from + to)) {
                            String currencyCode = "";
                            for (com.mmt.flights.dotrez.pnr.pnrresponse.ServiceCharge bookingServiceCharge : passengerFee.getServiceCharges()) {
                                BigDecimal serviceCharge = BigDecimal.valueOf(bookingServiceCharge.getAmount());
                                if (bookingServiceCharge.getType().equals(ServiceChargeType.ServiceCharge)) {
                                    total = total.add(serviceCharge);
                                } else {
                                    tax = tax.add(serviceCharge);
                                }
                                currencyCode = bookingServiceCharge.getCurrencyCode();
                            }
                            base = total.subtract(tax);
                            BigDecimal rateOfConversion  = getRateOfConversion(requiredCur,currencyCode);
                            base = base.multiply(rateOfConversion);
                            tax = tax.multiply(rateOfConversion);
                            return new Pair<>(base, tax);
                        }
                    }
                }
            }
        }
        return new Pair<>(base, tax);
    }

    public Pair<BigDecimal, BigDecimal> computeSingleInfFare(String carrierCode, Journey journey, String currency) {
        int owJrnySegmentCount = journey.getSegments().size();
        String fromCountry = airportUtil.getAirportDetailsFromCityCode(
                journey.getSegments().get(0).getDesignator().getOrigin()).getCountry();
        String toCountry = airportUtil.getAirportDetailsFromCityCode(
                journey.getSegments().get(owJrnySegmentCount - 1).getDesignator().getDestination()).getCountry();

        BigDecimal baseFare = null;
        try {
            Map<String, BigDecimal> infantFare = config.getInfantFare().get(currency.toUpperCase()).get(carrierCode);
            if (fromCountry.equalsIgnoreCase("India") && toCountry.equalsIgnoreCase("India")) {
                baseFare = infantFare.get("DOM");
            } else {
                baseFare = infantFare.get("INTL");
            }
        }
        catch (Exception ignored){}

        if(baseFare==null){
            throw new ServiceGeneralException("Infant fare not found for "+carrierCode+"-"+currency, PSCommonErrorEnum.EXT_CANT_DETERMINE_INFANT_FARE);
        }

        BigDecimal taxFare = BigDecimal.ZERO;

        return new Pair<>(baseFare, taxFare);
    }

    public Pair<BigDecimal, BigDecimal> computeSingleAdtFare(JourneyFare journeyFare, String requiredCurrency) {
        List<List<ServiceCharge>> segmentServiceCharges = new ArrayList<>();

        journeyFare.getFares().forEach(fare -> {
                    fare.getPassengerFares().forEach(passengerFare -> {
                        if (AdapterUtil.getSupplyPaxType(passengerFare.getPassengerType())== SupplyPaxType.ADULT) {
                            segmentServiceCharges.add(passengerFare.getServiceCharges());
                        }
                    });
                }
        );

        if (segmentServiceCharges.size() == 0) {
            throw new ServiceGeneralException(PSCommonErrorEnum.EXT_FARE_NOT_FOUND);
        }
        BigDecimal baseCharge = BigDecimal.ZERO;
        BigDecimal taxCharge = BigDecimal.ZERO;
        for (List<ServiceCharge> bookingServiceCharges : segmentServiceCharges) {
            for (ServiceCharge bookingServiceCharge : bookingServiceCharges) {
                String currencyCodeInXml = bookingServiceCharge.getCurrencyCode();
                BigDecimal amount = BigDecimal.valueOf(bookingServiceCharge.getAmount());
                BigDecimal roc = BigDecimal.ONE;
                roc = this.getRateOfConversion(requiredCurrency, currencyCodeInXml);
                if (bookingServiceCharge.getType().equals(Tax)) {
                    taxCharge = taxCharge.add(amount.multiply(roc));
                } else if (bookingServiceCharge.getType().equals(Discount) || bookingServiceCharge.getType().equals(PromotionDiscount)) {
                    baseCharge = baseCharge.subtract(amount.multiply(roc));
                } else {
                    baseCharge = baseCharge.add(amount.multiply(roc));
                }
            }
        }
        return new Pair<>(baseCharge, taxCharge);
    }

    public BigDecimal getRateOfConversion(String requiredCurrency, String currencyCodeInXml) {
        if (requiredCurrency.equalsIgnoreCase(currencyCodeInXml)) return BigDecimal.ONE;
        try {
            return BigDecimal.valueOf(currencyConverter.getConversionRate(currencyCodeInXml, requiredCurrency));
        } catch (Exception e) {
            MMTLogger.error(new LogParams.LogParamsBuilder()
                    .serviceName(MetricServices.CURRENCY_CONVERSION_ERROR.name())
                    .extraInfo("Error while getting Conversion rate "+currencyCodeInXml+" -> "+requiredCurrency)
                    .build());
            throw new ServiceGeneralException("Error while getting Conversion rate "+currencyCodeInXml+" -> "+requiredCurrency, ErrorEnum.CURRENCY_CONVERSION_ERROR);
        }
    }
    public BigDecimal getRefundableAncillaryCost(CancelType cancelType, Booking booking, String supplierName, AddOnsUtil addOnsUtil, SkipAncillaryConfig skipAncillaryConfig, String requiredCur){
        BigDecimal ancillaryCost = computeAncillaryCost(cancelType, booking);
        String logKey = booking.getRecordLocator() != null ? booking.getRecordLocator() : "";
        if (skipAncillaryConfig != null && skipAncillaryConfig.getSkipAncillary() != null && skipAncillaryConfig.getSkipAncillary().containsKey(supplierName)) {
            Map<SupplyAddonsTypeOuterClass.SupplyAddonsType, BigDecimal> individualAncillaryCostMap = getIndividualAncillaryCostMap(cancelType, booking, addOnsUtil, skipAncillaryConfig);
            ancillaryCost = getUpdatedAncillaryCost(ancillaryCost, skipAncillaryConfig, supplierName, individualAncillaryCostMap, logKey);
        }

        return ancillaryCost.multiply(getRateOfConversion(requiredCur, booking.getCurrencyCode()));
    }

    public static Map<SupplyAddonsTypeOuterClass.SupplyAddonsType, BigDecimal> getIndividualAncillaryCostMap(CancelType cancelType, Booking booking, AddOnsUtil addOnsUtil, SkipAncillaryConfig skipAncillaryConfig){
        Map<SupplyAddonsTypeOuterClass.SupplyAddonsType, BigDecimal> individualAncillaryCostMap = new HashMap<>();
        if (cancelType.equals(CancelType.OW) ||
                cancelType.equals(CancelType.ForwardJourneyOfRoundTrip)) {
            populateAncillaryCostMap(booking.getJourneys().get(0), booking, addOnsUtil, individualAncillaryCostMap, skipAncillaryConfig);
        } else if (cancelType.equals(CancelType.ReturnJourneyOfRoundTrip)) {
            populateAncillaryCostMap(booking.getJourneys().get(1), booking, addOnsUtil, individualAncillaryCostMap, skipAncillaryConfig);
        } else if (cancelType.equals(CancelType.BothLegOfRT)) {
            populateAncillaryCostMap(booking.getJourneys().get(0), booking, addOnsUtil, individualAncillaryCostMap, skipAncillaryConfig);
            populateAncillaryCostMap(booking.getJourneys().get(1), booking, addOnsUtil, individualAncillaryCostMap, skipAncillaryConfig);
        }
        return individualAncillaryCostMap;
    }

    public static void populateAncillaryCostMap(Journey jrny, Booking bking, AddOnsUtil addOnsUtil,  Map<SupplyAddonsTypeOuterClass.SupplyAddonsType, BigDecimal> individualAncillaryCostMap, SkipAncillaryConfig skipAncillaryConfig){
        List<Fee> allApplicablePaxFee =
                bking.getPassengers().values().stream()
                        .filter(pax -> pax.getFees() != null)
                        .flatMap(pax -> pax.getFees().stream())
                        .filter(paxFee -> !"INFT".equals(paxFee.getCode()))
                        .filter(CostUtils::isAncillaryFee)// blnk ssrcode are fees
                        .filter(paxFee -> isPaxFeeBelongToJrny(paxFee, jrny))
                        .collect(Collectors.toList());

        List<com.mmt.flights.dotrez.pnr.pnrresponse.ServiceCharge> includedServiceCharges = allApplicablePaxFee.stream()
                .filter(paxFee -> paxFee.getServiceCharges() != null)
                .flatMap(paxFee -> paxFee.getServiceCharges().stream())
                .filter(serviceCharge -> serviceCharge.getType() != null && !serviceCharge.getType().name().contains("Included"))
                .collect(Collectors.toList());


        for(com.mmt.flights.dotrez.pnr.pnrresponse.ServiceCharge serviceCharge : includedServiceCharges){
            SupplyAddonsTypeOuterClass.SupplyAddonsType addonsType = getAddonsType(serviceCharge.getCode(),bking,addOnsUtil,skipAncillaryConfig);
            double amount = serviceCharge.getAmount();
            if(serviceCharge.getType().equals(Discount)){
                amount*=-1;
            }
            BigDecimal value = individualAncillaryCostMap.getOrDefault(addonsType,new BigDecimal(0)).add(BigDecimal.valueOf(amount));
            individualAncillaryCostMap.put(addonsType,value);
        }
    }

    public static SupplyAddonsTypeOuterClass.SupplyAddonsType getAddonsType(String feecode, Booking booking, AddOnsUtil addOnsUtil, SkipAncillaryConfig skipAncillaryConfig) {
        Map<String, AddOnsDetails> addOnsMap = addOnsUtil.getSsrDetailsMap(SupplyAddonsTypeOuterClass.SupplyAddonsType.MEALS, booking.getInfo().getOwningCarrierCode());
        Set<String> mealsList = addOnsMap != null ? addOnsMap.keySet() : new HashSet<>();
        addOnsMap = addOnsUtil.getSsrDetailsMap(SupplyAddonsTypeOuterClass.SupplyAddonsType.BAGGAGE, booking.getInfo().getOwningCarrierCode());
        Set<String> baggageList = addOnsMap != null ? addOnsMap.keySet() : new HashSet<>();


        String carrierCode = getAirlineFromBooking(booking).get(0);
        Map<String,String> seatIdentifierMap = skipAncillaryConfig.getSeatIdentifier();
        for (Map.Entry<String,String> mapElement : seatIdentifierMap.entrySet()) {
            if(mapElement.getKey().equalsIgnoreCase(carrierCode) && feecode.equalsIgnoreCase(mapElement.getValue())){
                return SupplyAddonsTypeOuterClass.SupplyAddonsType.SEATS;
            }
        }
        if (mealsList.contains(feecode)) {
            return SupplyAddonsTypeOuterClass.SupplyAddonsType.MEALS;
        } else if (baggageList.contains(feecode)) {
            return SupplyAddonsTypeOuterClass.SupplyAddonsType.BAGGAGE;
        } else {
            return SupplyAddonsTypeOuterClass.SupplyAddonsType.OTHERS;
        }
    }

    public static List<String> getAirlineFromBooking(Booking booking) {
        return Collections.singletonList(booking.getJourneys().get(0).getSegments().get(0).getIdentifier().getCarrierCode());
    }

    public static BigDecimal getUpdatedAncillaryCost(BigDecimal ancillaryCost, SkipAncillaryConfig skipAncillaryConfig, String supplierName, Map<SupplyAddonsTypeOuterClass.SupplyAddonsType, BigDecimal> individualAncillaryCostMap, String logKey){
        for(String ancillaryType : skipAncillaryConfig.getSkipAncillary().get(supplierName)){
            if(individualAncillaryCostMap.containsKey(SupplyAddonsTypeOuterClass.SupplyAddonsType.valueOf(ancillaryType))){
                MMTLogger.info(logKey,"Skipping " + ancillaryType + " with cost: " + individualAncillaryCostMap.get(SupplyAddonsTypeOuterClass.SupplyAddonsType.valueOf(ancillaryType)) +  " for supplier: " + supplierName,  CostUtils.class.getName());
                ancillaryCost = ancillaryCost.subtract(individualAncillaryCostMap.get(SupplyAddonsTypeOuterClass.SupplyAddonsType.valueOf(ancillaryType)));
            }
        }
        return ancillaryCost;
    }
}
