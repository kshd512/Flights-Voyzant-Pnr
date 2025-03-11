package com.mmt.flights.odc.service.tasks;


import com.mmt.flights.dotrez.pnr.pnrresponse.Booking;
import com.mmt.flights.dotrez.pnr.pnrresponse.Journey;
import com.mmt.flights.helper.CurrencyConverter;
import com.mmt.flights.odc.common.CancelType;
import com.mmt.flights.odc.search.DateChangeSearchRequest;
import com.mmt.flights.odc.util.DateChangePenaltyCalcCanPan;
import com.mmt.flights.pnr.util.AdapterUtil;
import com.mmt.flights.pnr.util.DateTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Calendar;


@Component
public class DateChangePenaltyUtil {

    @Autowired
    DateChangePenaltyCalcCanPan dateChangePenaltyCalcCanPan;
    @Autowired
    CurrencyConverter currencyConverter;
    @Autowired
    private RecommendationBuilderUtil recomUtil;

    public BigDecimal getTotalDateChangePenaltyCanPen(CancelType cancelType, Booking booking,
                                                      DateChangeSearchRequest odcreq, PaxCount paxCount){
        Journey owJrny = booking.getJourneys().get(0);
        int owJrnySegmentCount = owJrny.getSegments().size();
        String fromAirportCode = owJrny.getSegments().get(0).getDesignator().getOrigin();
        String toAirportCode = owJrny.getSegments().get(owJrnySegmentCount - 1).getDesignator().getDestination();
        long adultCount = paxCount.getAdult();
        long childCount = paxCount.getChild();
        if (cancelType.equals(CancelType.OW) || cancelType.equals(CancelType.ForwardJourneyOfRoundTrip) || cancelType.equals(CancelType.ReturnJourneyOfRoundTrip)) {
            return getPenalty(odcreq, owJrny, fromAirportCode, toAirportCode, BigDecimal.valueOf(adultCount + childCount));
        } else {// both
            return getPenalty(odcreq, booking.getJourneys().get(0), fromAirportCode, toAirportCode, BigDecimal.valueOf((adultCount + childCount) * 2));
        }
    }

    public BigDecimal getTotalDateChangePenaltyCanPen(CancelType cancelType, Booking booking,
                                                      DateChangeSearchRequest odcreq) {
        PaxCount paxCount = AdapterUtil.getPaxCount(booking);
       return getTotalDateChangePenaltyCanPen(cancelType,booking,odcreq,paxCount);
    }

    private BigDecimal getPenalty(DateChangeSearchRequest odcreq,  Journey owJrny, String fromAirportCode, String toAirportCode,  BigDecimal totalAdtAndChd) {
        Calendar jrnyStartTime = Calendar.getInstance();
        jrnyStartTime.setTime(DateTimeUtil.getStandardDate(owJrny.getSegments().get(0).getDesignator().getDeparture()));
        BigDecimal singleAdtPenalty = dateChangePenaltyCalcCanPan.getDateChangePenalty( jrnyStartTime, fromAirportCode.trim(), toAirportCode.trim(),odcreq);
        return singleAdtPenalty.multiply(totalAdtAndChd);
    }
}

