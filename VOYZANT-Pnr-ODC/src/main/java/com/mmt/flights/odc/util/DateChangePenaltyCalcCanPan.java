package com.mmt.flights.odc.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricServices;
import com.mmt.flights.config.FareRuleConfig;
import com.mmt.flights.helper.CurrencyConverter;
import com.mmt.flights.odc.common.AbstractDateChangeRequest;
import com.mmt.flights.v1.rule.pojo.CPPostSalesReq;
import com.mmt.flights.v1.rule.pojo.cppostsaleres.CPPostSaleRes;
import com.mmt.flights.v1.rule.pojo.cpreqres.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

import static com.mmt.flights.v1.rule.common.PaxType.ADULT;


@Component
public class DateChangePenaltyCalcCanPan {

    //private final String targetUrl = "http://flights-rules.ecs.mmt/flights-rules/v1/postSaleRules?subsystem=odc";
    @Autowired
    RestTemplate resTemplate;
    @Autowired
    CurrencyConverter currencyConverter;
    @Autowired
    ObjectMapper mapper;

    @Autowired
    private FareRuleConfig fareRuleConfig;

    public BigDecimal getDateChangePenalty(Calendar jrnyStartTime,
                                           String fromArportCode, String toArportCode,
                                           AbstractDateChangeRequest odcReq) {
        try {
            String src = odcReq.getSrc();

            CPPostSalesReq req = new CPPostSalesReq(odcReq.getMmtId(), odcReq.getLob(), src);

            ResponseEntity<CPPostSaleRes> cpRes = resTemplate.postForEntity(fareRuleConfig.getFareRuleUrl(), req, CPPostSaleRes.class);

            BigDecimal dcpenalty = new BigDecimal(-1);
            if (cpRes.getBody() != null && (cpRes.getBody().getStatus() == null || cpRes.getBody().getStatus().equals(Status.SUCCCES))) {
                String requiredCurrency = odcReq.getCurrency();
                dcpenalty = calculateSingleAdtDCF(cpRes.getBody().getPostSale(), fromArportCode, toArportCode, jrnyStartTime);
                String currencyInCanPen = dcpenalty.doubleValue() != -1 ? cpRes.getBody().getPostSale().getMeta().getCurrency() : "";
                if (dcpenalty.doubleValue() == -1) {
                    try {

                        MMTLogger.error(new LogParams.LogParamsBuilder()
                                .extraInfo("Failed to find penalty "+(mapper).writeValueAsString(cpRes.getBody()))
                                .correlationId(odcReq.getPnr())
                                .serviceName(MetricServices.ODC_PENALTY_NOT_FOUND.name())
                                .build());

                    } catch (JsonProcessingException e) {
                        MMTLogger.error(new LogParams.LogParamsBuilder()
                                .extraInfo("Failed to find penalty ")
                                .correlationId(odcReq.getPnr())
                                .serviceName(MetricServices.ODC_PENALTY_NOT_FOUND.name())
                                .build());
                    }
                }
                if (dcpenalty.doubleValue() > 0) {
                    double roc = currencyConverter.getConversionRate(currencyInCanPen, requiredCurrency);
                    dcpenalty = dcpenalty.multiply(BigDecimal.valueOf(roc));
                }
            }
            return dcpenalty;
        } catch (Exception e) {
            MMTLogger.error(new LogParams.LogParamsBuilder()
                    .extraInfo("Exception while fetching date change penalty")
                    .throwable(e)
                    .correlationId(odcReq.getPnr())
                    .serviceName(MetricServices.ODC_PENALTY_NOT_FOUND.name())
                    .build());
            return new BigDecimal(-1);
        }

    }

    private BigDecimal calculateSingleAdtDCF(CPReqRes cPRes, String fromArportCode, String toArportCode, Calendar oldJrnyStartTime) {
        try {
            Recom recom = cPRes.getRcomGrps().getRecoms().get(0);
            int pnrGrpNum = getPnrGrpNum(cPRes, fromArportCode, toArportCode);

            List<JrnyWiseDCPenalty> dateChangePenalty = recom.getFrInfo().getPnrGrpdFrInfo().get(pnrGrpNum).getPnrFareInfos().get(0).getDateChangePenalty();
            Optional<JrnyWiseDCPenalty> firstPenalty = dateChangePenalty.stream()
                    .filter(jrnyDCPenalty -> jrnyDCPenalty.getJrnyKey().startsWith(fromArportCode))
                    .findFirst();
            if (firstPenalty.isPresent()) {
                JrnyWiseDCPenalty jrnyDateChangePenalty = firstPenalty
                        .get();
                List<DCTimeWindow> timeWindows = jrnyDateChangePenalty.getPaxWiseTW().get(ADULT).getTimeWindows();
                double oldJryHourRemaining = (oldJrnyStartTime.getTimeInMillis()-System.currentTimeMillis()) / (1000 * 60 * 60d);

                Optional<DCTimeWindow> timeWindowToUse = timeWindows.stream()
                        .filter(tw -> isUserInTW(tw, oldJryHourRemaining))
                        .findFirst();
                if (timeWindowToUse.isPresent()) {
                    return BigDecimal.valueOf(timeWindowToUse.get().getAirlinePenalty().getCancellationPenalty());
                }
            }
        } catch (Exception e) {
            MMTLogger.error(new LogParams.LogParamsBuilder()
                    .extraInfo("Error while fetching adult penalty")
                    .build());
        }

        return new BigDecimal(-1);
    }

    private boolean isUserInTW(DCTimeWindow tw, double oldJryHourRemaining) {
        return oldJryHourRemaining >= tw.getFromTimeWindow()
                && oldJryHourRemaining < tw.getToTimeWindow();
    }

    private int getPnrGrpNum(CPReqRes cPRes, String fromArportCode, String toArportCode) {

        Optional<String> firstFlight = cPRes.getFlightLookUpList().entrySet().stream()
                .filter(ent -> isSameFromCity(ent.getValue(), fromArportCode))
                .map(Map.Entry::getKey)
                .findFirst();

        if (firstFlight.isPresent()) {
            String fltLookUpKey = firstFlight.get();
            Recom recom = cPRes.getRcomGrps().getRecoms().get(0);

            Optional<Integer> firstPnrGroup = recom.getItineraryJrnyList().stream()
                    .flatMap(flightPnrLookupInfo -> flightPnrLookupInfo.getFlightPnrLookupInfo().stream())
                    .filter(flightPnrLookup -> Objects.equals(flightPnrLookup.getFltLookUpKey(), fltLookUpKey))
                    .map(ItneraryFlight::getPnrGroupNum)
                    .findFirst();
            if (firstPnrGroup.isPresent())
                return firstPnrGroup.get();
        }
        return 0;
    }

    private boolean isSameFromCity(Flight flt, String fromArportCode) {
        return flt.getDepartureInfo().getArpCd().equals(fromArportCode);
    }

}

