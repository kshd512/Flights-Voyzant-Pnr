package com.mmt.flights.odc.service.tasks;

import com.mmt.flights.odc.common.AbstractDateChangeRequest;
import org.apache.commons.lang3.StringUtils;

public class LobSrcUtil {
    public static String getRequiredCurrency(AbstractDateChangeRequest odcReq) {
        if(StringUtils.isNotEmpty(odcReq.getCurrency())) return odcReq.getCurrency();
        String src = odcReq.getSrc();
        if("MMTUSA".equals(src)){
            return "USD";
        }
        if("MMTUAE".equals(src)){
            return "AED";
        }
        return "INR";
    }

    public static String getSrcByCurrency(String currency) {
        if("USD".equals(currency)){
            return "MMTUSA";
        }
        if("AED".equals(currency)){
            return "MMTUAE";
        }
        return "MMT";
    }
}
