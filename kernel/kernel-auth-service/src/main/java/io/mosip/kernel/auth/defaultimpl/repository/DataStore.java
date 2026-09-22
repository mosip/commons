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
import io.mosip.kernel.core.authmanager.model.IndividualIdDto;

/**
 * User-store operations for authmanager. The production implementation is
 * {@link io.mosip.kernel.auth.defaultimpl.repository.impl.KeycloakImpl} talking
 * to Keycloak IAM.
 *
 * @author Ramadurai Pandian
 *
 */
public interface DataStore {

	/**
	 * Lists roles available in the realm mapped to {@code appId}.
	 *
	 * @param appId MOSIP application identifier
	 * @return roles for the realm
	 */
	public RolesListDto getAllRoles(String appId);

	/**
	 * Loads user details for the given user ids in the realm mapped to {@code appId}.
	 *
	 * @param userDetails user ids to load
	 * @param appId       MOSIP application identifier
	 * @return list of MOSIP users
	 * @throws Exception if the IAM call fails
	 */
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails,String appId) throws Exception;

	/**
	 * Loads users with salt metadata for the given user ids.
	 *
	 * @param userDetails user ids to load
	 * @param appId       MOSIP application identifier
	 * @return users with salts
	 * @throws Exception if the IAM call fails
	 */
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails,String appId) throws Exception;

	/**
	 * Resolves registration id (RID) from a user id.
	 *
	 * @param userId Keycloak / MOSIP user id
	 * @param appId  MOSIP application identifier
	 * @return RID wrapper
	 * @throws Exception if the user or attribute is missing
	 */
	public RIdDto getRidFromUserId(String userId,String appId) throws Exception;

	/**
	 * Unlocks a previously locked user account.
	 *
	 * @param userId user to unlock
	 * @return authorization response
	 * @throws Exception if IAM rejects the operation
	 */
	public AuthZResponseDto unBlockAccount(String userId) throws Exception;

	/**
	 * Registers a new user in IAM.
	 *
	 * @param userId registration request
	 * @return created user
	 */
	public MosipUserDto registerUser(UserRegistrationRequestDto userId);

	/**
	 * Sets the password for a user.
	 *
	 * @param userPasswordRequestDto password payload
	 * @return password operation result
	 */
	public UserPasswordResponseDto addPassword(UserPasswordRequestDto userPasswordRequestDto);

	/**
	 * Changes password after verifying the old password.
	 *
	 * @param passwordDto old and new password
	 * @return authorization response
	 * @throws Exception if the old password is wrong or policy fails
	 */
	public AuthZResponseDto changePassword(PasswordDto passwordDto) throws Exception;

	/**
	 * Resets a user password without the old password (admin/reset flow).
	 *
	 * @param passwordDto new password payload
	 * @return authorization response
	 * @throws Exception if IAM rejects the operation
	 */
	public AuthZResponseDto resetPassword(PasswordDto passwordDto) throws Exception;

	/**
	 * Looks up username by mobile number.
	 *
	 * @param mobileNumber mobile attribute value
	 * @return username wrapper
	 * @throws Exception if no user matches
	 */
	public UserNameDto getUserNameBasedOnMobileNumber(String mobileNumber) throws Exception;

	/**
	 * Authenticates username and password against IAM.
	 *
	 * @param loginUser credentials
	 * @return authenticated user
	 * @throws Exception if credentials are invalid
	 */
	public MosipUserDto authenticateUser(LoginUser loginUser) throws Exception;

	/**
	 * Sends or prepares OTP authentication for the given OTP user.
	 *
	 * @param otpUser OTP send request
	 * @return user context for OTP
	 * @throws Exception if send/validation setup fails
	 */
	public MosipUserDto authenticateWithOtp(OtpUser otpUser) throws Exception;

	/**
	 * Validates a user-submitted OTP.
	 *
	 * @param loginUser user id and OTP
	 * @return authenticated user
	 * @throws Exception if the OTP is invalid
	 */
	public MosipUserDto authenticateUserWithOtp(UserOtp loginUser) throws Exception;

	/**
	 * Authenticates an OAuth client id and secret.
	 *
	 * @param clientSecret client credentials
	 * @return authenticated client as a MOSIP user
	 * @throws Exception if the secret is rejected
	 */
	public MosipUserDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception;

	/**
	 * Loads roles for a username.
	 *
	 * @param username user id
	 * @return user with roles
	 * @throws Exception if the user is missing
	 */
	public MosipUserDto getUserRoleByUserId(String username) throws Exception;

	/**
	 * Loads user details by mobile number.
	 *
	 * @param mobileNumber mobile attribute value
	 * @return user details
	 * @throws Exception if no user matches
	 */
	public MosipUserDto getUserDetailBasedonMobileNumber(String mobileNumber) throws Exception;

	/**
	 * Checks whether a user id exists / is valid in IAM.
	 *
	 * @param userId user id
	 * @return validation result
	 */
	public ValidationResponseDto validateUserName(String userId);

	/**
	 * Loads user details for a list of UIDs.
	 *
	 * @param userIds user ids
	 * @return user details response
	 */
	public UserDetailsResponseDto getUserDetailBasedOnUid(List<String> userIds);
	
	/**
	 * Resolves individual id from user id in the given realm.
	 *
	 * @param userId  Keycloak username/id
	 * @param realmID Keycloak realm
	 * @return individual id wrapper
	 */
	public IndividualIdDto getIndividualIdFromUserId(String userId,String realmID);
	
	/**
	 * Searches users in a realm with optional role, pagination, and attribute filters.
	 *
	 * @param realmId   Keycloak realm
	 * @param roleName  optional role filter
	 * @param pageStart pagination offset
	 * @param pageFetch page size
	 * @param email     optional email filter
	 * @param firstName optional first name filter
	 * @param lastName  optional last name filter
	 * @param username  optional username filter
	 * @param search    optional free-text search
	 * @return matching users
	 */
	public MosipUserListDto getListOfUsersDetails(String realmId,String roleName,int pageStart, int pageFetch, String email, String firstName, String lastName, String username,String search);
}
