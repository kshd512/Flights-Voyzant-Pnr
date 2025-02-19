package com.mmt.flights.common.util;

import com.mmt.api.rxflow.FlowState;
import com.mmt.flights.common.constants.CMSConstants;
import com.mmt.flights.common.constants.FlowStateKey;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.entity.cms.CMSMapHolder;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class EncryptionUtil {

    public static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    public static String decrypt(String cipherText, FlowState state) {
        String logKey = state.getValue(FlowStateKey.LOG_KEY);
        if(StringUtils.isBlank(cipherText)){
            MMTLogger.info(logKey, "String to decrypt is null", EncryptionUtil.class.getName());
            return "";
        }

        CMSMapHolder cmsMapHolder = state.getValue(FlowStateKey.CMS_MAP);
        Map<String, String> cmsMap = cmsMapHolder.getCmsMap();

        String key = "";
        if(cmsMap.containsKey(CMSConstants.ENCRYTION_KEY)){
            key = cmsMap.get(CMSConstants.ENCRYTION_KEY);
        }else{
            MMTLogger.info(logKey,"Missing encryption key in CMS map",EncryptionUtil.class.getName());
        }

        if (cipherText == null || cipherText.trim().isEmpty()) {
            return cipherText;
        }
        try {
            byte[] cipherBytes = Base64.getDecoder().decode(cipherText);
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 16) {
                throw new IllegalArgumentException("Invalid key size.");
            }
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
            byte[] original = cipher.doFinal(cipherBytes);
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            //fail
            return cipherText;
        }
    }

    public static String encrypt(String plaintext) {

        String key = "66F128A5561C96CBDF1B12E8A4D755E9";

        if (plaintext == null || plaintext.trim().isEmpty()) {
            return plaintext;
        }
        try {
            byte[] raw = key.getBytes(StandardCharsets.UTF_8);
            if (raw.length < 16) {
                throw new IllegalArgumentException("Invalid key size.");
            }
            SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(new byte[16]));
            byte[] cipherBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(cipherBytes);
        } catch (Exception e) {
            // fail
            return plaintext;
        }
    }
}