/**
 * 
 */
package io.mosip.kernel.core.authmanager.spi;


import io.mosip.kernel.core.authmanager.model.AuthNResponseDto;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.authmanager.model.LoginUser;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.authmanager.model.UserOtp;

/**
 * @author Ramadurai Pandian
 *
 */
public interface AuthNService {

	AuthNResponseDto authenticateUser(LoginUser loginUser) throws Exception;

	AuthNResponseDto authenticateWithOtp(OtpUser otpUser) throws Exception;

	AuthNResponseDto authenticateUserWithOtp(UserOtp loginUser) throws Exception;

	AuthNResponseDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception;

}
