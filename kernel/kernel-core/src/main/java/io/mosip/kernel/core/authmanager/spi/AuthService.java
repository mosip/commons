/**
 * Combined authentication, authorization, and user-administration SPI.
 */
package io.mosip.kernel.core.authmanager.spi;

import java.util.List;

import io.mosip.kernel.core.authmanager.model.AccessTokenResponseDTO;
import io.mosip.kernel.core.authmanager.model.AuthNResponse;
import io.mosip.kernel.core.authmanager.model.AuthResponseDto;
import io.mosip.kernel.core.authmanager.model.AuthZResponseDto;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserSaltListDto;
import io.mosip.kernel.core.authmanager.model.PasswordDto;
import io.mosip.kernel.core.authmanager.model.RIdDto;
import io.mosip.kernel.core.authmanager.model.RefreshTokenRequest;
import io.mosip.kernel.core.authmanager.model.RefreshTokenResponse;
import io.mosip.kernel.core.authmanager.model.RolesListDto;
import io.mosip.kernel.core.authmanager.model.UserDetailsResponseDto;
import io.mosip.kernel.core.authmanager.model.UserNameDto;
import io.mosip.kernel.core.authmanager.model.UserPasswordRequestDto;
import io.mosip.kernel.core.authmanager.model.UserPasswordResponseDto;
import io.mosip.kernel.core.authmanager.model.UserRegistrationRequestDto;
import io.mosip.kernel.core.authmanager.model.UserRoleDto;
import io.mosip.kernel.core.authmanager.model.ValidationResponseDto;
import io.mosip.kernel.core.authmanager.model.IndividualIdDto;

/**
 * Extends AuthN and AuthZ with token lifecycle, user administration, and
 * OpenID login helpers.
 * <p>
 * Contract: implementations perform HTTP to the identity provider (typically
 * Keycloak). Call from kernel-authmanager or service adapters. Unused imports
 * in this interface are part of the historical SPI surface kept for
 * downstream MOSIP modules.
 * </p>
 *
 * @author Ramadurai Pandian
 * @see AuthNService
 * @see AuthZService
 */
public interface AuthService extends AuthZService, AuthNService {

	/**
	 * Exchanges a refresh token for a new access-token pair.
	 * <p>
	 * Contract: performs HTTP to the token endpoint. Both token arguments must
	 * be non-blank; {@code refreshTokenRequest} must carry client credentials.
	 * </p>
	 *
	 * @param refereshToken       refresh token value (historical spelling of
	 *                            the first argument); never null or empty
	 * @param refreshToken        refresh token value; never null or empty
	 * @param refreshTokenRequest never-null client id and secret
	 * @return never-null refresh result; token fields may be null on failure
	 * @throws Exception when the token endpoint call fails
	 */
	public RefreshTokenResponse refreshToken(String refereshToken, String refreshToken,
			RefreshTokenRequest refreshTokenRequest) throws Exception;

	/**
	 * Invalidates the given access token at the identity provider.
	 * <p>
	 * Contract: performs HTTP logout / revoke. {@code token} must be non-blank.
	 * </p>
	 *
	 * @param token never-null, never-blank access token to revoke
	 * @return never-null status and message
	 * @throws Exception when the identity provider call fails
	 */
	public AuthNResponse invalidateToken(String token) throws Exception;

	/**
	 * Lists all roles defined for the given MOSIP application.
	 * <p>
	 * Contract: performs HTTP to the identity provider. {@code appId} must be
	 * non-blank.
	 * </p>
	 *
	 * @param appId never-null, never-blank MOSIP application identifier
	 * @return never-null wrapper; the inner role list may be empty
	 */
	public RolesListDto getAllRoles(String appId);

	/**
	 * Fetches user profiles for the given user identifiers.
	 * <p>
	 * Contract: performs HTTP to the identity provider.
	 * </p>
	 *
	 * @param userDetails never-null list of user ids; may be empty
	 * @param appId       never-null, never-blank MOSIP application identifier
	 * @return never-null wrapper; the inner user list may be empty
	 * @throws Exception when the identity provider call fails
	 */
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception;

	/**
	 * Fetches user identifiers with their password salts.
	 * <p>
	 * Contract: performs HTTP to the identity provider. Treat salts as
	 * sensitive.
	 * </p>
	 *
	 * @param userDetails never-null list of user ids; may be empty
	 * @param appId       never-null, never-blank MOSIP application identifier
	 * @return never-null wrapper; the inner salt list may be empty
	 * @throws Exception when the identity provider call fails
	 */
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception;

	/**
	 * Resolves the registration ID for a user in the given application.
	 * <p>
	 * Contract: performs HTTP or DB lookup depending on the implementation.
	 * </p>
	 *
	 * @param userId never-null, never-blank user identifier
	 * @param appId  never-null, never-blank MOSIP application identifier
	 * @return never-null DTO; {@code rId} may be null if unmapped
	 * @throws Exception when the lookup fails
	 */
	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception;

	/**
	 * Registers a new user in the identity store.
	 * <p>
	 * Contract: performs HTTP to the identity provider. {@code userCreationRequestDto}
	 * must be non-null and satisfy its bean-validation constraints.
	 * </p>
	 *
	 * @param userCreationRequestDto never-null registration payload
	 * @return never-null created user profile
	 */
	MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto);

	/**
	 * Validates an access token and returns the bound user profile.
	 * <p>
	 * Contract: performs HTTP or local JWT verification. {@code token} must be
	 * non-blank. Historical spelling of {@code validate}.
	 * </p>
	 *
	 * @param token never-null, never-blank access token
	 * @return never-null user profile; fields may be null if invalid
	 */
	public MosipUserDto valdiateToken(String token);

	/**
	 * Logs the user out and invalidates the given token.
	 * <p>
	 * Contract: performs HTTP logout. {@code token} must be non-blank.
	 * </p>
	 *
	 * @param token never-null, never-blank access token
	 * @return never-null status and message
	 */
	public AuthResponseDto logoutUser(String token);

	/**
	 * Completes an OpenID Connect authorization-code login redirect.
	 * <p>
	 * Contract: performs HTTP to the token endpoint. All arguments must be
	 * non-null; empty strings are invalid except where the provider allows
	 * them.
	 * </p>
	 *
	 * @param state         OIDC state query parameter from the callback
	 * @param sessionState  OIDC session_state query parameter; may be empty
	 * @param code          authorization code; never empty
	 * @param stateCookie   state value stored in the browser cookie
	 * @param redirectURI   registered redirect URI used in the token request
	 * @return never-null access-token response; token may be null on failure
	 */
	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI);

	/**
	 * Builds the Keycloak / identity-provider authorization URL.
	 * <p>
	 * Contract: does not perform HTTP; returns a URL the browser should open.
	 * </p>
	 *
	 * @param redirectURI never-null registered redirect URI
	 * @param state       never-null CSRF state value
	 * @return never-null absolute authorization URL
	 */
	public String getKeycloakURI(String redirectURI, String state);

	/**
	 * Resolves the individual identifier (UIN or VID) for a user.
	 * <p>
	 * Contract: performs HTTP or DB lookup depending on the implementation.
	 * </p>
	 *
	 * @param userId never-null, never-blank user identifier
	 * @param appId  never-null, never-blank MOSIP application identifier
	 * @return never-null DTO; {@code individualId} may be null if unmapped
	 */
	public IndividualIdDto getIndividualIdBasedOnUserID(String userId, String appId);

	/**
	 * Searches users in a realm with optional filters and paging.
	 * <p>
	 * Contract: performs HTTP to the identity provider. {@code realmId} must
	 * be non-blank. Filter strings may be null to skip that criterion.
	 * {@code pageStart} is 0-based; {@code pageFetch} is the page size.
	 * </p>
	 *
	 * @param realmId   never-null, never-blank identity-provider realm
	 * @param roleName  optional role filter; null to skip
	 * @param pageStart 0-based page index; must be non-negative
	 * @param pageFetch page size; must be positive
	 * @param email     optional email filter; null to skip
	 * @param firstName optional given-name filter; null to skip
	 * @param lastName  optional family-name filter; null to skip
	 * @param username  optional login-name filter; null to skip
	 * @param search    optional free-text search; null to skip
	 * @return never-null wrapper; the inner user list may be empty
	 */
	public MosipUserListDto getListOfUsersDetails(String realmId, String roleName, int pageStart, int pageFetch,
			String email, String firstName, String lastName, String username, String search);
}
