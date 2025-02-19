package com.mmt.flights.util;

import com.mmt.flights.asm.core.AmazonSecretsManager;
import com.mmt.flights.asm.core.AmazonSecretsManagerConfig;
import com.mmt.flights.common.enums.ErrorEnum;
import com.mmt.flights.common.logging.MMTLogger;
import com.mmt.flights.logger.helper.JsonConvertor;
import com.mmt.flights.postsales.error.PSErrorException;

public class SecretManagerUtil {
    public static LoginCredentials getLoginCredentials(String secretName) {
        try {
            AmazonSecretsManagerConfig secretsManagerConfig = new AmazonSecretsManagerConfig();
            String secretCredentialsJson = AmazonSecretsManager.getInstance(secretsManagerConfig).getSecret(secretName);
            return JsonConvertor.convertJsonToObject(secretCredentialsJson, LoginCredentials.class);
        } catch (Exception e) {
            MMTLogger.error("SecretManagerUtilError", "Error while retrieving loginCredentials", SecretManagerUtil.class.getName(), e);
            throw new PSErrorException("Error while retrieving loginCredentials" + e, com.mmt.flights.postsales.error.PSCommonErrorEnum.FLT_UNKNOWN_ERROR);
        }
    }
}