/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateResponseDto;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;

/**
 * Calls the OTP manager to generate one-time passwords for a user.
 *
 * @author Ramadurai Pandian
 *
 */
public interface OTPGenerateService {

	/**
	 * Generates an OTP for a single default channel using an internal auth token.
	 *
	 * @param mosipUserDto user for whom OTP is generated
	 * @param token        internal service token for the OTP manager
	 * @return generate response including OTP or status
	 */
	OtpGenerateResponseDto generateOTP(MosipUserDto mosipUserDto, String token);

	/**
	 * Generates OTP(s) for the channels listed on {@code otpUser}.
	 *
	 * @param mosipUserDto user for whom OTP is generated
	 * @param otpUser      channels and context
	 * @param token        internal service token for the OTP manager
	 * @return generate response
	 */
	OtpGenerateResponseDto generateOTPMultipleChannels(MosipUserDto mosipUserDto, OtpUser otpUser, String token);

}
