/**
 * 
 */
package io.mosip.kernel.openid.bridge.api.service;

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
 * Combined MOSIP auth-manager SPI: authentication ({@link AuthNService}),
 * token authorization ({@link AuthZService}), Keycloak user/role administration,
 * and OIDC authorization-code helpers.
 * <p>
 * Default implementation talks to Keycloak Admin and token/userinfo/logout
 * endpoints. Auth-code login helpers mirror {@link LoginService} for admin UI
 * flows.
 *
 * @author Ramadurai Pandian
 *
 */
public interface AuthService extends AuthZService, AuthNService {

	/**
	 * Exchanges a Keycloak refresh token for a new access token (OIDC refresh-token
	 * grant) using the client on {@code refreshTokenRequest}.
	 * <p>
	 * Callers pass the MOSIP application id as the first argument (used to resolve
	 * the realm); the parameter name {@code refereshToken} is historical.
	 *
	 * @param refereshToken       MOSIP application id used to resolve the Keycloak
	 *                            realm
	 * @param refreshToken        OIDC refresh token previously issued by Keycloak
	 * @param refreshTokenRequest client id/secret (and related fields) for the
	 *                            token request
	 * @return new access token, refresh token, and related response fields
	 * @throws Exception if the refresh grant fails or the response cannot be parsed
	 */
	public RefreshTokenResponse refreshToken(String refereshToken, String refreshToken,
			RefreshTokenRequest refreshTokenRequest) throws Exception;

	/**
	 * Invalidates the given access token at the IAM (Keycloak logout / token
	 * revoke as implemented by the service).
	 *
	 * @param token access token to invalidate
	 * @return status and message of the invalidation
	 * @throws Exception if the IAM call fails
	 */
	public AuthNResponse invalidateToken(String token) throws Exception;

	/**
	 * Lists Keycloak realm roles for the MOSIP application (mapped to a realm).
	 *
	 * @param appId MOSIP application id used to resolve the Keycloak realm
	 * @return roles available in that realm
	 */
	public RolesListDto getAllRoles(String appId);

	/**
	 * Fetches Keycloak user profiles for the given user ids in the realm of
	 * {@code appId}.
	 *
	 * @param userDetails user identifiers to look up
	 * @param appId       MOSIP application id used to resolve the realm
	 * @return list of MOSIP user DTOs
	 * @throws Exception if the admin API call fails
	 */
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception;

	/**
	 * Fetches user details including password salts for the given user ids.
	 *
	 * @param userDetails user identifiers to look up
	 * @param appId       MOSIP application id used to resolve the realm
	 * @return users with salt material as returned by Keycloak
	 * @throws Exception if the admin API call fails
	 */
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception;

	/**
	 * Resolves the MOSIP RID (registration id) stored on the Keycloak user for
	 * {@code userId}.
	 *
	 * @param userId Keycloak / MOSIP user id
	 * @param appId  MOSIP application id used to resolve the realm
	 * @return RID DTO for the user
	 * @throws Exception if the user is missing or the admin API call fails
	 */
	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception;

	/**
	 * Registers a user in the Keycloak realm derived from the request application
	 * id.
	 *
	 * @param userCreationRequestDto username and other registration fields
	 * @return created MOSIP user as stored in Keycloak
	 */
	MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto);

	/**
	 * Validates an access token via Keycloak userinfo and maps JWT claims to a
	 * {@link MosipUserDto}.
	 * <p>
	 * The method name retains the historical spelling {@code valdiateToken}.
	 *
	 * @param token Bearer access token
	 * @return MOSIP user built from token claims when userinfo succeeds
	 */
	public MosipUserDto valdiateToken(String token);

	/**
	 * Logs the user out of Keycloak using RP-initiated logout
	 * ({@code id_token_hint}).
	 *
	 * @param token ID token or access token used as logout hint
	 * @return logout status and message
	 */
	public AuthResponseDto logoutUser(String token);

	/**
	 * Completes the authorization-code callback: checks CSRF {@code state}, then
	 * exchanges {@code code} at the Keycloak token endpoint.
	 *
	 * @param state         {@code state} query parameter from the IdP
	 * @param sessionState  OIDC {@code session_state} from the callback, if present
	 * @param code          authorization code
	 * @param stateCookie   CSRF state previously stored in a cookie
	 * @param redirectURI   application callback path appended to the configured
	 *                      redirect URI
	 * @return access token and expiry from Keycloak
	 */
	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI);

	/**
	 * Builds the Keycloak authorization endpoint URL for the authorization-code
	 * flow (client id, redirect, state, response type, scope).
	 *
	 * @param redirectURI application callback path appended to the configured
	 *                    redirect URI
	 * @param state       opaque CSRF value
	 * @return fully expanded Keycloak authorization URL
	 */
	public String getKeycloakURI(String redirectURI, String state);

	/**
	 * Resolves the MOSIP individual id linked to {@code userId} in the realm of
	 * {@code appId}.
	 *
	 * @param userId Keycloak / MOSIP user id
	 * @param appId  MOSIP application id used to resolve the realm
	 * @return individual id DTO
	 */
	public IndividualIdDto getIndividualIdBasedOnUserID(String userId, String appId);

	/**
	 * Searches Keycloak users with optional role, pagination, and attribute
	 * filters.
	 * <p>
	 * {@code realmId} is treated as a MOSIP application id and mapped to a
	 * Keycloak realm by the default implementation.
	 *
	 * @param realmId   MOSIP application id (mapped to a Keycloak realm)
	 * @param roleName  optional realm role to filter users
	 * @param pageStart first result index for Keycloak admin paging
	 * @param pageFetch maximum number of users to return
	 * @param email     optional email filter
	 * @param firstName optional first-name filter
	 * @param lastName  optional last-name filter
	 * @param username  optional username filter
	 * @param search    optional free-text search string for the admin API
	 * @return matching MOSIP user list
	 */
	public MosipUserListDto getListOfUsersDetails(String realmId, String roleName, int pageStart, int pageFetch,
			String email, String firstName, String lastName, String username, String search);
}
