package com.mmt.flights.odc.service.util;

import com.mmt.flights.odc.common.ErrorDetails;
import com.mmt.flights.postsales.error.PSErrorEnum;

/**
 * Utility class for ODC operations
 */
public class ODCUtil {

    /**
     * Creates ErrorDetails object from error enum and message
     * 
     * @param errorEnum the error enum
     * @param message the error message
     * @return ErrorDetails object
     */
    public static ErrorDetails getErrorDetails(PSErrorEnum errorEnum, String message) {
        ErrorDetails errorDetails = new ErrorDetails();
        errorDetails.setErrorMessage(message);
        errorDetails.setErrorCode(errorEnum.getCode());
        return errorDetails;
    }
}
