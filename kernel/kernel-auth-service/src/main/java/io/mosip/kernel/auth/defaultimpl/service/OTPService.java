/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

import java.util.List;

import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserTokenDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;

/**
 * @author Ramadurai Pandian
 *
 */

public interface OTPService {

	AuthNResponseDto sendOTP(MosipUserDto mosipUserDto, List<String> channel, String appId);

	MosipUserTokenDto validateOTP(MosipUserDto mosipUser, String otp,String appId);

	AuthNResponseDto sendOTPForUin(MosipUserDto mosipUserDto, OtpUser otpUser, String appId);

	AuthNResponseDto sendOTP(MosipUserDto mosipUser, OtpUser otpUser,String appId) throws Exception;

}
