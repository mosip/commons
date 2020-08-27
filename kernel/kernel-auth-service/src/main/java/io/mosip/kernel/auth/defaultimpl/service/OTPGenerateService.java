/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import io.mosip.kernel.auth.defaultimpl.dto.otp.OtpGenerateResponseDto;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;

/**
 * @author Ramadurai Pandian
 *
 */
public interface OTPGenerateService {

	OtpGenerateResponseDto generateOTP(MosipUserDto mosipUserDto, String token);

	OtpGenerateResponseDto generateOTPMultipleChannels(MosipUserDto mosipUserDto, OtpUser otpUser, String token);

}
