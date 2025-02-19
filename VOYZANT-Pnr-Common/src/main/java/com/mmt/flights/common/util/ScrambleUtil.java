package com.mmt.flights.common.util;

import com.mmt.flights.common.logging.LogParams;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.common.logging.metric.MetricType;
import com.mmt.flights.pii.clients.ScramblerClient;
import com.mmt.flights.pii.exception.ScramblerClientException;
import com.mmt.flights.pii.model.EncodeRequest;
import com.mmt.flights.pii.model.EncodeResponse;
import org.apache.commons.lang3.StringUtils;


public class ScrambleUtil {

    private static ScramblerClient scramblerClient;

    static {
        try {
            scramblerClient = ScramblerClient.getInstance();
        } catch (Exception e) {
            MMTLogger.error("", "Error while initializing scrambler client", ScrambleUtil.class.getName(), e);
        }
    }

    public static String getEncodedString(String stringToEncode, String salt) {
        if (StringUtils.isBlank(stringToEncode)) {
            return "";
        }
        try {
            if (StringUtils.isNotEmpty(stringToEncode) && StringUtils.isNotEmpty(salt)) {
                EncodeRequest encodeRequest = new EncodeRequest();
                encodeRequest.setSaltString(salt);
                encodeRequest.setStringToEncode(stringToEncode);
                EncodeResponse encode = scramblerClient.encode(encodeRequest);
                return encode.getEncodedString();
            }
        } catch (Exception e) {
            MMTLogger.error(new LogParams.LogParamsBuilder()
                            .correlationId(salt)
                            .extraInfo("Error while encoding data " + stringToEncode)
                            .throwable(e)
                            .build(),
                    MetricType.LOG_FILE);
        }
        return stringToEncode;
    }

    public static String getStandardEncodedString(String data, String logKey) throws ScramblerClientException {
        if (StringUtils.isNotBlank(data))
            return scramblerClient.encrypt(data, logKey);
        return data;
    }

    public static String getDecryptedString(String data, String logKey) throws ScramblerClientException {
        if (StringUtils.isNotBlank(data))
            return scramblerClient.decrypt(data, logKey);
        return data;
    }

    public static void encodeUserData(String response, String logKey) {
    }
}
