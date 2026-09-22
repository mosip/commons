/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;

/**
 * OTP send and validate operations used by authmanager against notification
 * channels (email/SMS) and the OTP manager.
 *
 * @author Ramadurai Pandian
 *
 */

public interface OTPService {


	/**
	 * Validates an OTP for the given user and application, returning a token DTO
	 * on success.
	 *
	 * @param mosipUser authenticated user context
	 * @param otp       OTP value to verify
	 * @param appId     MOSIP application identifier
	 * @return user and token after successful OTP validation
	 */
	MosipUserTokenDto validateOTP(MosipUserDto mosipUser, String otp,String appId);

	/**
	 * Sends OTP for a UIN-based identity.
	 *
	 * @param mosipUserDto user loaded from ID repository
	 * @param otpUser      channel and context for the OTP
	 * @param appId        MOSIP application identifier
	 * @return send status and message
	 */
	AuthNResponseDto sendOTPForUin(MosipUserDto mosipUserDto, OtpUser otpUser, String appId);

	/**
	 * Sends OTP for a userid-based identity.
	 *
	 * @param mosipUser user details including notification addresses
	 * @param otpUser   channel and context for the OTP
	 * @param appId     MOSIP application identifier
	 * @return send status and message
	 * @throws Exception if generation or notification fails
	 */
	AuthNResponseDto sendOTP(MosipUserDto mosipUser, OtpUser otpUser,String appId) throws Exception;

}
