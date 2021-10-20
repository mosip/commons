package io.mosip.kernel.signature.util;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.signature.constant.SignatureConstant;

/**
 * Utility class for Signature Service
 * 
 * @author Mahammed Taheer
  * @since 1.1.5.3
 *
 */

public class SignatureUtil {
    
    private static final Logger LOGGER = KeymanagerLogger.getLogger(SignatureUtil.class);

    
    public static boolean isDataValid(String anyData) {
        return anyData != null && !anyData.trim().isEmpty();
    }

    
    public static boolean isJsonValid(String jsonInString) {
        try {
           ObjectMapper mapper = new ObjectMapper();
           mapper.readTree(jsonInString);
           return true;
        } catch (IOException e) {
            LOGGER.error(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN, SignatureConstant.BLANK,
                        "Provided JSON Data to sign value is invalid.");
        }
        return false;
    }

    public static boolean isIncludeAttrsValid(Boolean includes) {
        if (Objects.isNull(includes)) {
            return SignatureConstant.DEFAULT_INCLUDES;
        }
        return includes;
    }

}
