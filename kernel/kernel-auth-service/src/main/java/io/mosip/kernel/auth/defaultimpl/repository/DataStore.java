/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.repository;

import java.util.List;

import io.mosip.kernel.core.authmanager.model.AuthZResponseDto;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.authmanager.model.LoginUser;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserSaltListDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.authmanager.model.PasswordDto;
import io.mosip.kernel.core.authmanager.model.RIdDto;
import io.mosip.kernel.core.authmanager.model.RolesListDto;
import io.mosip.kernel.core.authmanager.model.UserDetailsResponseDto;
import io.mosip.kernel.core.authmanager.model.UserNameDto;
import io.mosip.kernel.core.authmanager.model.UserOtp;
import io.mosip.kernel.core.authmanager.model.UserPasswordRequestDto;
import io.mosip.kernel.core.authmanager.model.UserPasswordResponseDto;
import io.mosip.kernel.core.authmanager.model.UserRegistrationRequestDto;
import io.mosip.kernel.core.authmanager.model.ValidationResponseDto;

/**
 * @author Ramadurai Pandian
 *
 */
public interface DataStore {

	public RolesListDto getAllRoles(String appId);

	public MosipUserListDto getListOfUsersDetails(List<String> userDetails,String appId) throws Exception;

	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails,String appId) throws Exception;

	public RIdDto getRidFromUserId(String userId,String appId) throws Exception;

	public AuthZResponseDto unBlockAccount(String userId) throws Exception;

	public MosipUserDto registerUser(UserRegistrationRequestDto userId);

	public UserPasswordResponseDto addPassword(UserPasswordRequestDto userPasswordRequestDto);

	public AuthZResponseDto changePassword(PasswordDto passwordDto) throws Exception;

	public AuthZResponseDto resetPassword(PasswordDto passwordDto) throws Exception;

	public UserNameDto getUserNameBasedOnMobileNumber(String mobileNumber) throws Exception;

	public MosipUserDto authenticateUser(LoginUser loginUser) throws Exception;

	public MosipUserDto authenticateWithOtp(OtpUser otpUser) throws Exception;

	public MosipUserDto authenticateUserWithOtp(UserOtp loginUser) throws Exception;

	public MosipUserDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception;

	public MosipUserDto getUserRoleByUserId(String username) throws Exception;

	public MosipUserDto getUserDetailBasedonMobileNumber(String mobileNumber) throws Exception;

	public ValidationResponseDto validateUserName(String userId);

	public UserDetailsResponseDto getUserDetailBasedOnUid(List<String> userIds);

}
