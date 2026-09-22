/**
 * 
 */
package io.mosip.kernel.openid.bridge.api.service;


import io.mosip.kernel.core.authmanager.model.*;

/**
 * Authentication (AuthN) SPI for MOSIP auth manager against Keycloak.
 * <p>
 * Implementations obtain OIDC tokens via the Keycloak token endpoint using
 * resource-owner password, OTP, or client-credentials grants, and return
 * {@link AuthNResponseDto} with access token, refresh token, and expiry.
 *
 * @author Ramadurai Pandian
 *
 */
public interface AuthNService {

	/**
	 * Authenticates a user with username and password using the module's default
	 * OIDC client id and secret (resource-owner password grant).
	 *
	 * @param loginUser username, password, and MOSIP application id used to resolve
	 *                  the Keycloak realm
	 * @return tokens and status for the authenticated user
	 * @throws Exception if Keycloak rejects the credentials or the token call fails
	 * @deprecated use {@link #authenticateUser(LoginUserWithClientId)} instead,
	 *             which accepts an explicit client id and secret
	 */
	@Deprecated //instead use authenticateUser(LoginUserWithClientId loginUser)
	AuthNResponseDto authenticateUser(LoginUser loginUser) throws Exception;

	/**
	 * Sends an OTP to the user over the requested channel (email/SMS) without
	 * issuing tokens yet.
	 * <p>
	 * For UIN-based users, details are loaded from UIN services; for userid-based
	 * users, the user may be registered in Keycloak first.
	 *
	 * @param otpUser user id, application id, OTP channel, and user-id type
	 *                ({@code UIN} or userid)
	 * @return status and message for the OTP send operation
	 * @throws Exception if the user type is invalid or OTP delivery fails
	 */
	AuthNResponseDto authenticateWithOtp(OtpUser otpUser) throws Exception;

	/**
	 * Completes OTP login by validating the OTP against Keycloak (or UIN services
	 * for IDA) and returning access/refresh tokens.
	 *
	 * @param loginUser user id, OTP value, and application id
	 * @return tokens and status when the OTP is valid
	 * @throws Exception if the user cannot be resolved or OTP validation fails
	 */
	AuthNResponseDto authenticateUserWithOtp(UserOtp loginUser) throws Exception;

	/**
	 * Authenticates a confidential client with client id and secret (OIDC
	 * {@code client_credentials} grant) against the realm for the given app id.
	 *
	 * @param clientSecret client id, secret, and MOSIP application id
	 * @return client access token, refresh token, and expiry
	 * @throws Exception if the token endpoint call fails
	 */
	AuthNResponseDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception;

	/**
	 * Authenticates a user with username and password using the client id and
	 * secret supplied on {@link LoginUserWithClientId} (resource-owner password
	 * grant). Preferred over {@link #authenticateUser(LoginUser)}.
	 *
	 * @param loginUser username, password, client id, client secret, and
	 *                  application id
	 * @return tokens and status for the authenticated user
	 * @throws Exception if Keycloak rejects the credentials or the token call fails
	 */
	AuthNResponseDto authenticateUser(LoginUserWithClientId loginUser) throws Exception;

}
