/**
 * 
 */
package io.mosip.kernel.core.authmanager.spi;


import io.mosip.kernel.core.authmanager.model.*;

/**
 * @author Ramadurai Pandian
 *
 */
public interface AuthNService {

	@Deprecated //instead use authenticateUser(LoginUserWithClientId loginUser)
	AuthNResponseDto authenticateUser(LoginUser loginUser) throws Exception;

	AuthNResponseDto authenticateWithOtp(OtpUser otpUser) throws Exception;

	AuthNResponseDto authenticateUserWithOtp(UserOtp loginUser) throws Exception;

	AuthNResponseDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception;

	AuthNResponseDto authenticateUser(LoginUserWithClientId loginUser) throws Exception;

}
