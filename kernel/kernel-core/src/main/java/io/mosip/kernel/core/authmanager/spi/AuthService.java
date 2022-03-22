/**
 * 
 */
package io.mosip.kernel.core.authmanager.spi;

import java.util.List;

import io.mosip.kernel.core.authmanager.model.*;

/**
 * @author Ramadurai Pandian
 *
 */
public interface AuthService extends AuthZService, AuthNService {

	public RefreshTokenResponse refreshToken(String refereshToken,String refreshToken, RefreshTokenRequest refreshTokenRequest) throws Exception;

	public AuthNResponse invalidateToken(String token) throws Exception;

	public RolesListDto getAllRoles(String appId);

	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception;

	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception;

	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception;

	public AuthZResponseDto unBlockUser(String userId, String appId) throws Exception;

	public AuthZResponseDto changePassword(String appId, PasswordDto passwordDto) throws Exception;

	public AuthZResponseDto resetPassword(String appId, PasswordDto passwordDto) throws Exception;

	public UserNameDto getUserNameBasedOnMobileNumber(String appId, String mobileNumber) throws Exception;

	MosipUserDto registerUser(UserRegistrationRequestDto userCreationRequestDto);

	@Deprecated(forRemoval = true, since = "1.1.4")
	UserPasswordResponseDto addUserPassword(UserPasswordRequestDto userPasswordRequestDto);

	public UserRoleDto getUserRole(String appId, String userId) throws Exception;

	public MosipUserDto getUserDetailBasedonMobileNumber(String appId, String mobileNumber) throws Exception;

	public ValidationResponseDto validateUserName(String appId, String userName);

	public UserDetailsResponseDto getUserDetailBasedOnUserId(String appId, List<String> userIds);

	public MosipUserDto valdiateToken(String token);

	public AuthResponseDto logoutUser(String token);

	public AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI);

	public String getKeycloakURI(String redirectURI, String state);

	public IndividualIdDto getIndividualIdBasedOnUserID(String userId, String appId);

}
