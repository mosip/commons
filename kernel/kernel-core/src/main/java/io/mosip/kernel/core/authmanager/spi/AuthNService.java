/**
 * Authentication (AuthN) SPI for MOSIP identity providers.
 */
package io.mosip.kernel.core.authmanager.spi;


import io.mosip.kernel.core.authmanager.model.*;

/**
 * Authenticates MOSIP users via password, OTP, or client secret.
 * <p>
 * Contract: implementations perform HTTP to the identity provider (typically
 * Keycloak or kernel-authmanager). Request DTOs must be non-null and
 * populated; returned DTOs are never null but token fields may be. Call these
 * methods from login controllers only.
 * </p>
 *
 * @author Ramadurai Pandian
 */
public interface AuthNService {

	/**
	 * Authenticates with username and password without an OAuth client id.
	 * <p>
	 * Contract: deprecated; new callers must use
	 * {@link #authenticateUser(LoginUserWithClientId)}. Performs HTTP to the
	 * identity provider.
	 * </p>
	 *
	 * @param loginUser never-null username, password, and app id
	 * @return never-null authentication result; tokens may be null on failure
	 * @throws Exception when the identity provider call fails
	 * @deprecated use {@link #authenticateUser(LoginUserWithClientId)} instead
	 */
	@Deprecated //instead use authenticateUser(LoginUserWithClientId loginUser)
	AuthNResponseDto authenticateUser(LoginUser loginUser) throws Exception;

	/**
	 * Requests an OTP for the given user and delivery channels.
	 * <p>
	 * Contract: performs HTTP to the OTP / identity provider. Does not verify
	 * the OTP; use {@link #authenticateUserWithOtp(UserOtp)} after the user
	 * submits it.
	 * </p>
	 *
	 * @param otpUser never-null OTP request with user id, channels, and app id
	 * @return never-null result describing whether the OTP was sent
	 * @throws Exception when the identity provider or OTP channel fails
	 */
	AuthNResponseDto authenticateWithOtp(OtpUser otpUser) throws Exception;

	/**
	 * Verifies a previously issued OTP and issues tokens on success.
	 * <p>
	 * Contract: performs HTTP to the identity provider.
	 * </p>
	 *
	 * @param loginUser never-null user id, OTP, and app id
	 * @return never-null authentication result; tokens may be null on failure
	 * @throws Exception when the identity provider call fails
	 */
	AuthNResponseDto authenticateUserWithOtp(UserOtp loginUser) throws Exception;

	/**
	 * Authenticates a confidential OAuth client with id and secret.
	 * <p>
	 * Contract: performs HTTP to the token endpoint.
	 * </p>
	 *
	 * @param clientSecret never-null client id, secret, and app id
	 * @return never-null authentication result; tokens may be null on failure
	 * @throws Exception when the token endpoint call fails
	 */
	AuthNResponseDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception;

	/**
	 * Authenticates with username, password, and OAuth client credentials.
	 * <p>
	 * Contract: preferred password-login entry point. Performs HTTP to the
	 * identity provider.
	 * </p>
	 *
	 * @param loginUser never-null login request including client id and secret
	 * @return never-null authentication result; tokens may be null on failure
	 * @throws Exception when the identity provider call fails
	 */
	AuthNResponseDto authenticateUser(LoginUserWithClientId loginUser) throws Exception;

}
