package com.mmt.flights.odc.util;

import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.pii.clients.ScramblerClient;
import com.mmt.flights.pii.exception.ScramblerClientException;
import com.mmt.flights.pii.model.DecodeRequest;
import com.mmt.flights.pii.model.DecodeResponse;
import com.mmt.flights.pii.model.EncodeRequest;
import com.mmt.flights.pii.model.EncodeResponse;
import java.util.Arrays;


public class ScramblerUtil {

    private static ScramblerClient scramblerClient;

    static {
        try {
            scramblerClient = ScramblerClient.getInstance();
        } catch (ScramblerClientException e) {
            e.printStackTrace();
        }
    }



    public ScramblerUtil() throws ScramblerClientException {
    }


    public static String encode(String stringToEncode,String saltValue){

            try {
                final EncodeRequest encodeRequest = new EncodeRequest();
                encodeRequest.setStringToEncode(stringToEncode);
                encodeRequest.setSaltString(saltValue);
                EncodeResponse encodeResponse = scramblerClient.encode(encodeRequest);
                return encodeResponse.getEncodedString();
            } catch (Exception e) {
                MMTLogger.error("","Error while encoding: "+e+" Trace: "+ Arrays.toString(e.getStackTrace()),"ScramblerUtil",e);
                return stringToEncode;
            }
    }

    public static String decode(String stringToDecode,String saltValue){
        try {
            final DecodeRequest request = new DecodeRequest();
            request.setStringToDecode(stringToDecode);
            request.setSaltString(saltValue);
            DecodeResponse decode = scramblerClient.decode(request);
            return decode.getDecodedString();
        } catch (Exception e) {
            MMTLogger.error("","Error while decoding: "+e+" Trace: "+ Arrays.toString(e.getStackTrace()),"ScramblerUtil",e);
            return stringToDecode;
        }
    }
}
