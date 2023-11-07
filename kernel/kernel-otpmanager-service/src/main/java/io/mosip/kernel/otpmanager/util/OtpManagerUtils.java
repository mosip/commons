package io.mosip.kernel.otpmanager.util;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.HMACUtils2;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.otpmanager.constant.OtpErrorConstants;
import io.mosip.kernel.otpmanager.exception.OtpInvalidArgumentException;

/**
 * This utility class defines some of the utility methods used in OTP
 * validation.
 * 
 * @author Sagar Mahapatra
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@RefreshScope
@Component
public class OtpManagerUtils {

	@Value("${mosip.kernel.otp.min-key-length}")
	String keyMinLength;

	@Value("${mosip.kernel.otp.max-key-length}")
	String keyMaxLength;
	
	private static final String KEY_OTP_SEPARATOR = ":";

	/**
	 * This method returns the difference between two LocalDateTime objects in
	 * seconds.
	 * 
	 * @param fromDateTime The time from which the difference needs to be
	 *                     calculated.
	 * @param toDateTime   The time till which the difference needs to be
	 *                     calculated.
	 * @return The difference in seconds.
	 */
	public static int timeDifferenceInSeconds(LocalDateTime fromDateTime, LocalDateTime toDateTime) {
		return (int) fromDateTime.until(toDateTime, ChronoUnit.SECONDS);
	}

	/**
	 * This method returns the current LocalDateTime.
	 * 
	 * @return The current local date and time.
	 */
	public static LocalDateTime getCurrentLocalDateTime() {
		return LocalDateTime.now(ZoneId.of("UTC"));
	}

	/**
	 * This method validates the input arguments provided for validation.
	 * 
	 * @param key The key.
	 * @param otp The OTP to be validated against the given key.
	 */

	public void validateOtpRequestArguments(String key, String otp) {
		List<ServiceError> validationErrorsList = new ArrayList<>();
		if (key == null || key.isEmpty()) {
			validationErrorsList.add(new ServiceError(OtpErrorConstants.OTP_VAL_INVALID_KEY_INPUT.getErrorCode(),
					OtpErrorConstants.OTP_VAL_INVALID_KEY_INPUT.getErrorMessage()));
		} else {
			if ((key.length() < Integer.parseInt(keyMinLength)) || (key.length() > Integer.parseInt(keyMaxLength))) {

				validationErrorsList.add(new ServiceError(OtpErrorConstants.OTP_VAL_ILLEGAL_KEY_INPUT.getErrorCode(),
						OtpErrorConstants.OTP_VAL_ILLEGAL_KEY_INPUT.getErrorMessage()));
			}
		}
		if (otp == null || otp.isEmpty()) {
			validationErrorsList.add(new ServiceError(OtpErrorConstants.OTP_VAL_INVALID_OTP_INPUT.getErrorCode(),
					OtpErrorConstants.OTP_VAL_INVALID_OTP_INPUT.getErrorMessage()));
		}
		if ((otp != null) && (!StringUtils.isNumeric(otp))) {
			validationErrorsList.add(new ServiceError(OtpErrorConstants.OTP_VAL_ILLEGAL_OTP_INPUT.getErrorCode(),
					OtpErrorConstants.OTP_VAL_ILLEGAL_OTP_INPUT.getErrorMessage()));
		}
		if (!validationErrorsList.isEmpty()) {
			throw new OtpInvalidArgumentException(validationErrorsList);
		}
	}

	public static String getKeyOtpHash(String key, String otp) {
		return getHash(key + KEY_OTP_SEPARATOR + otp);
	}

	public static String getHash(String string) {
		try {
			return HMACUtils2.digestAsPlainText(string.getBytes());
		} catch (NoSuchAlgorithmException e) {
			throw new BaseUncheckedException(OtpErrorConstants.OTP_GEN_ALGO_FAILURE.getErrorCode(),
					OtpErrorConstants.OTP_GEN_ALGO_FAILURE.getErrorMessage(), e);
		}
	}
}
