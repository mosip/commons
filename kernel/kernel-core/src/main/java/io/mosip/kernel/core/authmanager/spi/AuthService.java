/**
 * 
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
 * @author Ramadurai Pandian
 *
 */
public interface AuthService extends AuthZService, AuthNService {

	public RefreshTokenResponse refreshToken(String refereshToken, String refreshToken,
			RefreshTokenRequest refreshTokenRequest) throws Exception;

	public AuthNResponse invalidateToken(String token) throws Exception;

	public RolesListDto getAllRoles(String appId);

	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception;

	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception;

	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception;

	MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto);

	public MosipUserDto valdiateToken(String token);

	public AuthResponseDto logoutUser(String token);

	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI);

	public String getKeycloakURI(String redirectURI, String state);

	public IndividualIdDto getIndividualIdBasedOnUserID(String userId, String appId);

	public MosipUserListDto getListOfUsersDetails(String realmId, String roleName, int pageStart, int pageFetch,
			String email, String firstName, String lastName, String username, String search);
}
