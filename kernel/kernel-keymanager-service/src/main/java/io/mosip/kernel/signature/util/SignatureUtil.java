package io.mosip.kernel.signature.util;

import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.signature.constant.SignatureConstant;

/**
 * Utility class for Signature Service
 * 
 * @author Mahammed Taheer
  * @since 1.2.0-rc1
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

    public static boolean isCertificateDatesValid(X509Certificate x509Cert) {
        
        try {
            Date currentDate = Date.from(DateUtils.getUTCCurrentDateTime().atZone(ZoneId.systemDefault()).toInstant());
            x509Cert.checkValidity(currentDate);
            return true;
        } catch(CertificateExpiredException | CertificateNotYetValidException exp) {
            LOGGER.info(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN,
                SignatureConstant.BLANK,
                    "Exception thrown when certificate dates are not valid.");
        }
        try {
            // Checking both system default timezone & UTC Offset timezone. Issue found in reg-client during trust validation. 
            x509Cert.checkValidity();
            return true;
        } catch(CertificateExpiredException | CertificateNotYetValidException exp) {
            LOGGER.info(SignatureConstant.SESSIONID, SignatureConstant.JWT_SIGN,
            SignatureConstant.BLANK,
                    "Exception thrown when certificate dates are not valid.");
        }
        return false;
    }

}
