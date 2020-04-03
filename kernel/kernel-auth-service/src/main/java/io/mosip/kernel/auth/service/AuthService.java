/**
 * 
 */
package io.mosip.kernel.auth.service;

import java.util.List;

import io.mosip.kernel.auth.dto.AccessTokenResponseDTO;
import io.mosip.kernel.auth.dto.AuthNResponse;
import io.mosip.kernel.auth.dto.AuthResponseDto;
import io.mosip.kernel.auth.dto.AuthZResponseDto;
import io.mosip.kernel.auth.dto.MosipUserDto;
import io.mosip.kernel.auth.dto.MosipUserListDto;
import io.mosip.kernel.auth.dto.MosipUserSaltListDto;
import io.mosip.kernel.auth.dto.PasswordDto;
import io.mosip.kernel.auth.dto.RIdDto;
import io.mosip.kernel.auth.dto.RefreshTokenRequest;
import io.mosip.kernel.auth.dto.RefreshTokenResponse;
import io.mosip.kernel.auth.dto.RolesListDto;
import io.mosip.kernel.auth.dto.UserDetailsResponseDto;
import io.mosip.kernel.auth.dto.UserNameDto;
import io.mosip.kernel.auth.dto.UserPasswordRequestDto;
import io.mosip.kernel.auth.dto.UserPasswordResponseDto;
import io.mosip.kernel.auth.dto.UserRegistrationRequestDto;
import io.mosip.kernel.auth.dto.UserRoleDto;
import io.mosip.kernel.auth.dto.ValidationResponseDto;

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

	UserPasswordResponseDto addUserPassword(UserPasswordRequestDto userPasswordRequestDto);

	public UserRoleDto getUserRole(String appId, String userId) throws Exception;

	public MosipUserDto getUserDetailBasedonMobileNumber(String appId, String mobileNumber) throws Exception;

	public ValidationResponseDto validateUserName(String appId, String userName);

	public UserDetailsResponseDto getUserDetailBasedOnUserId(String appId, List<String> userIds);

	public MosipUserDto valdiateToken(String token);

	public AuthResponseDto logoutUser(String token);

	AccessTokenResponseDTO loginRedirect(String state, String sessionState, String code, String stateCookie,
			String redirectURI);

	String getKeycloakURI(String redirectURI, String state);

}
