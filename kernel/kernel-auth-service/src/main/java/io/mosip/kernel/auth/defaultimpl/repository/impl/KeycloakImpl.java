package io.mosip.kernel.auth.defaultimpl.repository.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.apache.directory.api.ldap.model.password.PasswordDetails;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.dto.KeycloakPasswordDTO;
import io.mosip.kernel.auth.defaultimpl.dto.KeycloakRequestDto;
import io.mosip.kernel.auth.defaultimpl.dto.Roles;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.repository.DataStore;
import io.mosip.kernel.auth.defaultimpl.util.AuthUtil;
import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import io.mosip.kernel.core.authmanager.model.AuthZResponseDto;
import io.mosip.kernel.core.authmanager.model.ClientSecret;
import io.mosip.kernel.core.authmanager.model.LoginUser;
import io.mosip.kernel.core.authmanager.model.MosipUserDto;
import io.mosip.kernel.core.authmanager.model.MosipUserListDto;
import io.mosip.kernel.core.authmanager.model.MosipUserSalt;
import io.mosip.kernel.core.authmanager.model.MosipUserSaltListDto;
import io.mosip.kernel.core.authmanager.model.OtpUser;
import io.mosip.kernel.core.authmanager.model.PasswordDto;
import io.mosip.kernel.core.authmanager.model.RIdDto;
import io.mosip.kernel.core.authmanager.model.Role;
import io.mosip.kernel.core.authmanager.model.RolesListDto;
import io.mosip.kernel.core.authmanager.model.UserDetailsResponseDto;
import io.mosip.kernel.core.authmanager.model.UserNameDto;
import io.mosip.kernel.core.authmanager.model.UserOtp;
import io.mosip.kernel.core.authmanager.model.UserPasswordRequestDto;
import io.mosip.kernel.core.authmanager.model.UserPasswordResponseDto;
import io.mosip.kernel.core.authmanager.model.UserRegistrationRequestDto;
import io.mosip.kernel.core.authmanager.model.ValidationResponseDto;
import io.mosip.kernel.core.authmanager.model.IndividualIdDto;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.StringUtils;


@Component
public class KeycloakImpl implements DataStore {

	private static final String INDIVIDUAL = "INDIVIDUAL";

	@Value("${mosip.iam.realm.operations.base-url}")
	private String keycloakBaseUrl;

	@Value("${mosip.iam.admin-url}")
	private String keycloakAdminUrl;

	@Value("${mosip.iam.admin-realm-id}")
	private String adminRealmId;

	// @Value("${mosip.iam.default.realm-id}")
	// private String realmId;

	@Autowired
	private AuthUtil authUtil;

	@Value("${mosip.iam.roles-extn-url}")
	private String roles;

	@Value("${mosip.iam.users-extn-url}")
	private String users;

	@Value("${mosip.iam.role-user-mapping-url}")
	private String roleUserMappingurl;

	@Qualifier(value = "keycloakRestTemplate")
	@Autowired
	private RestTemplate restTemplate;

	@Value("${db_3_DS.keycloak.ipaddress}")
	private String keycloakHost;

	@Value("${db_3_DS.keycloak.port}")
	private String keycloakPort;

	@Value("${db_3_DS.keycloak.username}")
	private String keycloakUsername;

	@Value("${db_3_DS.keycloak.password}")
	private String keycloakPassword;

	@Value("${db_3_DS.keycloak.driverClassName}")
	private String keycloakDriver;

	@Value("${mosip.iam.pre-reg_user_password}")
	private String preRegUserPassword;
	
	@Value("${mosip.iam.role-based-user-url}")
	private String roleBasedUsersurl;

	@Value("${hikari.maximumPoolSize:25}")
	private int maximumPoolSize;
	@Value("${hikari.validationTimeout:3000}")
	private int validationTimeout;
	@Value("${hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	@Value("${hikari.idleTimeout:200000}")
	private int idleTimeout;
	@Value("${hikari.minimumIdle:0}")
	private int minimumIdle;

	@Value("${mosip.keycloak.max-no-of-users:100}")
	private String maxUsers;

	private NamedParameterJdbcTemplate jdbcTemplate;

	private static final String FETCH_ALL_SALTS = "select ue.username,ua.value from public.user_entity ue, public.user_attribute ua where ue.id=ua.user_id and ua.name='userPassword' and ue.username IN(:username)";

	private static final String FETCH_PASSWORD = "select cr.value from public.credential cr, public.user_entity ue where cr.user_id=ue.id and ue.username=:username";
	@Autowired
	private ObjectMapper objectMapper;

	private String individualRoleID;
	private static final Logger LOGGER= LoggerFactory.getLogger(KeycloakImpl.class);

	@PostConstruct
	private void setup() {
		setUpConnection();
	}

	private void setUpConnection() {
		HikariConfig hikariConfig = new HikariConfig();
		hikariConfig.setDriverClassName(keycloakDriver);
		hikariConfig.setJdbcUrl(keycloakHost);
		hikariConfig.setUsername(keycloakUsername);
		hikariConfig.setPassword(keycloakPassword);
		hikariConfig.setMaximumPoolSize(maximumPoolSize);
		hikariConfig.setValidationTimeout(validationTimeout);
		hikariConfig.setConnectionTimeout(connectionTimeout);
		hikariConfig.setIdleTimeout(idleTimeout);
		hikariConfig.setMinimumIdle(minimumIdle);
		HikariDataSource dataSource = new HikariDataSource(hikariConfig);
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	@Override
	public RolesListDto getAllRoles(String appId) {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, appId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + roles);
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		List<Role> rolesList = new ArrayList<>();
		try {
			JsonNode node = objectMapper.readTree(response);
			for (JsonNode jsonNode : node) {
				Role role = new Role();
				String name = jsonNode.get("name").textValue();
				role.setRoleId(name);
				role.setRoleName(name);
				rolesList.add(role);
			}
		} catch (IOException e) {
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		RolesListDto rolesListDto = new RolesListDto();
		rolesListDto.setRoles(rolesList);
		return rolesListDto;
	}

	@Override
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String realmId) throws Exception {
		List<MosipUserDto> mosipUserDtos = null;
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		HttpEntity<String> httpEntity = new HttpEntity<>(null, new HttpHeaders());
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + users);
		uriComponentsBuilder.queryParam("max", maxUsers);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		try {
			JsonNode node = objectMapper.readTree(response);
			mosipUserDtos = mapUsersToUserDetailDto(node, userDetails,realmId);
		} catch (IOException e) {
			LOGGER.error("Error in getListOfUsersDetails", e);
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		MosipUserListDto mosipUserListDto = new MosipUserListDto();
		mosipUserListDto.setMosipUserDtoList(mosipUserDtos);
		return mosipUserListDto;
	}

	@Override
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception {

		return jdbcTemplate.query(FETCH_ALL_SALTS, new MapSqlParameterSource("username", userDetails),
				new ResultSetExtractor<MosipUserSaltListDto>() {

					@Override
					public MosipUserSaltListDto extractData(ResultSet rs) throws SQLException, DataAccessException {
						MosipUserSaltListDto mosipUserSaltListDto = new MosipUserSaltListDto();
						List<MosipUserSalt> mosipUserSaltList = new ArrayList<>();
						while (rs.next()) {
							MosipUserSalt mosipUserSalt = new MosipUserSalt();
							mosipUserSalt.setUserId(rs.getString("username"));
							PasswordDetails password = PasswordUtil
									.splitCredentials(CryptoUtil.decodeBase64(rs.getString("value")));
							mosipUserSalt.setSalt(CryptoUtil.encodeBase64String(password.getSalt()));
							mosipUserSaltList.add(mosipUserSalt);
						}
						mosipUserSaltListDto.setMosipUserSaltList(mosipUserSaltList);
						return mosipUserSaltListDto;
					}

				});
	}

	@Override
	public RIdDto getRidFromUserId(String userId, String appId) throws Exception {
		RIdDto rIdDto = new RIdDto();
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, appId);
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + userId);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		if (response == null || response.isEmpty()) {
			throw new AuthManagerException(AuthErrorCode.USER_NOT_FOUND.getErrorCode(),
					AuthErrorCode.USER_NOT_FOUND.getErrorMessage());
		}
		try {
			JsonNode node = objectMapper.readTree(response);
			for (JsonNode jsonNode : node) {
				if (jsonNode.get(AuthConstant.USER_NAME).textValue().equals(userId)) {
					JsonNode attriNode = jsonNode.get("attributes");
					String rid = attriNode.get(AuthConstant.RID).get(0).textValue();
					rIdDto.setRId(rid);
					break;
				}
			}
			if (rIdDto.getRId() == null) {
				throw new AuthManagerException(AuthErrorCode.USER_NOT_FOUND.getErrorCode(),
						AuthErrorCode.USER_NOT_FOUND.getErrorMessage());
			}

		} catch (IOException e) {
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}

		return rIdDto;

	}

	@Override
	public AuthZResponseDto unBlockAccount(String userId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MosipUserDto registerUser(UserRegistrationRequestDto userId) {
		Map<String, String> pathParams = new HashMap<>();
		KeycloakRequestDto keycloakRequestDto = mapUserRequestToKeycloakRequestDto(userId);
		String realm = authUtil.getRealmIdFromAppId(userId.getAppId());
		if (userId.getAppId().equalsIgnoreCase(AuthConstant.PRE_REGISTRATION)) {
			realm = userId.getAppId();
		}
		pathParams.put(AuthConstant.REALM_ID, realm);
		HttpEntity<KeycloakRequestDto> httpEntity = new HttpEntity<>(keycloakRequestDto);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users"));
		if (!isUserAlreadyPresent(userId.getUserName(), realm)) {
			callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(), HttpMethod.POST,
					httpEntity);
			if (keycloakRequestDto.getRealmRoles().contains(INDIVIDUAL)) {
				String userID = getIDfromUserID(userId.getUserName(), realm);
				roleMapper(userID, realm);
			}
		}

		MosipUserDto mosipUserDTO = new MosipUserDto();
		mosipUserDTO.setUserId(userId.getUserName());
		return mosipUserDTO;

	}

	private void roleMapper(String userID, String realmId) {
		Map<String, String> pathParams = new HashMap<>();

		pathParams.put(AuthConstant.REALM_ID, realmId);
		pathParams.put("userID", userID);
		try {
			if(Strings.isNullOrEmpty(individualRoleID))
				individualRoleID = getRoleId(INDIVIDUAL,realmId);
		}
		catch(Exception ex){
			LOGGER.error("Role " + INDIVIDUAL + " not found in " + realmId + " for user " + userID);
		}
		
		Roles role = new Roles(individualRoleID, INDIVIDUAL);
		List<Roles> roles = new ArrayList<>();
		roles.add(role);
		pathParams.put(AuthConstant.REALM_ID, realmId);
		HttpEntity<List<Roles>> httpEntity = new HttpEntity<>(roles);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users/{userID}/role-mappings/realm"));
		callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(), HttpMethod.POST, httpEntity);
	}

	private String getIDfromUserID(String userName, String realmId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users?username=").concat(userName));
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, null);
		JsonNode jsonNodes;
		try {
			if (response == null) {
				return null;
			}
			jsonNodes = objectMapper.readTree(response);
		} catch (IOException e) {
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		if (jsonNodes.size() > 0) {
			for (JsonNode jsonNode : jsonNodes) {
				if (userName.equals(jsonNode.get("username").asText())) {
					return jsonNode.get("id").asText();
				}
			}

		}
		return null;
	}

	/**
	 * Checks if is user already present.
	 *
	 * @param userName
	 *            the user name
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean isUserAlreadyPresent(String userName, String realmId) {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl.concat("/users?username=").concat(userName));
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, null);
		JsonNode jsonNodes;
		try {
			if (response == null) {
				return false;
			}
			jsonNodes = objectMapper.readTree(response);
		} catch (IOException e) {
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		if (jsonNodes.size() > 0) {
			for (JsonNode jsonNode : jsonNodes) {
				if (userName.equals(jsonNode.get("username").asText())) {
					return true;
				}
			}

		}
		return false;
	}

	private KeycloakRequestDto mapUserRequestToKeycloakRequestDto(UserRegistrationRequestDto userRegDto) {
		KeycloakRequestDto keycloakRequestDto = new KeycloakRequestDto();
		List<String> roles = new ArrayList<>();
		List<KeycloakPasswordDTO> credentialObject = null;
		KeycloakPasswordDTO dto = null;
		if (userRegDto.getAppId().equalsIgnoreCase("prereg")) {
			roles.add(INDIVIDUAL);
			credentialObject = new ArrayList<>();
			dto = new KeycloakPasswordDTO();
			dto.setType(AuthConstant.PASSWORDCONSTANT);
			dto.setValue(preRegUserPassword);
		} else if (userRegDto.getAppId().equalsIgnoreCase("registrationclient")) {
			credentialObject = new ArrayList<>();
			dto = new KeycloakPasswordDTO();
			dto.setType(AuthConstant.PASSWORDCONSTANT);
			dto.setValue(userRegDto.getUserPassword());
		}
		credentialObject.add(dto);
		List<Object> contactNoList = new ArrayList<>();
		List<Object> genderList = new ArrayList<>();
		genderList.add(userRegDto.getGender());
		contactNoList.add(userRegDto.getContactNo());
		HashMap<String, List<Object>> attributes = new HashMap<>();
		attributes.put("mobile", contactNoList);

		attributes.put("gender", genderList);
		keycloakRequestDto.setUsername(userRegDto.getUserName());
		keycloakRequestDto.setFirstName(userRegDto.getFirstName());
		keycloakRequestDto.setEmail(userRegDto.getEmailID());
		keycloakRequestDto.setRealmRoles(roles);
		keycloakRequestDto.setAttributes(attributes);
		keycloakRequestDto.setEnabled(true);
		if (credentialObject != null) {
			keycloakRequestDto.setCredentials(credentialObject);
		}
		return keycloakRequestDto;
	}

	private void KeycloakRequestDtomapUserRequestToKeycloakRequestDto(UserRegistrationRequestDto userId) {
		// TODO Auto-generated method stub

	}

	@Override
	public UserPasswordResponseDto addPassword(UserPasswordRequestDto userPasswordRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthZResponseDto changePassword(PasswordDto passwordDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthZResponseDto resetPassword(PasswordDto passwordDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserNameDto getUserNameBasedOnMobileNumber(String mobileNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MosipUserDto authenticateUser(LoginUser loginUser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MosipUserDto authenticateWithOtp(OtpUser otpUser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MosipUserDto authenticateUserWithOtp(UserOtp loginUser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MosipUserDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MosipUserDto getUserRoleByUserId(String username) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MosipUserDto getUserDetailBasedonMobileNumber(String mobileNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValidationResponseDto validateUserName(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UserDetailsResponseDto getUserDetailBasedOnUid(List<String> userIds) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Call keycloak service.
	 *
	 * @param url
	 *            the url
	 * @param httpMethod
	 *            the http method
	 * @param requestEntity
	 *            the request entity
	 * @return the string
	 */
	private String callKeycloakService(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity) {
		ResponseEntity<String> responseEntity = null;
		String response = null;
		try {

			responseEntity = restTemplate.exchange(url, httpMethod, requestEntity, String.class);
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getRawStatusCode() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new BadCredentialsException("Authentication failed from AuthManager");
				}
			}
			if (ex.getRawStatusCode() == 403) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthZException(validationErrorsList);
				} else {
					throw new AccessDeniedException("Access denied from AuthManager");
				}
			}

			throw new AuthManagerException(AuthErrorCode.SERVER_ERROR.getErrorCode(),
					AuthErrorCode.SERVER_ERROR.getErrorMessage());

		}
		if (responseEntity != null && responseEntity.hasBody() && responseEntity.getStatusCode() == HttpStatus.OK) {
			response = responseEntity.getBody();
		}

		return response;
	}

	/**
	 * Map users to user detail dto.
	 *
	 * @param node
	 *            the node
	 * @param userDetails
	 * @return the list
	 */
	private List<MosipUserDto> mapUsersToUserDetailDto(JsonNode node, List<String> userDetails,String realmId) {
		List<MosipUserDto> mosipUserDtos = new ArrayList<>();
		if(node == null) {
			LOGGER.error("response from openid is null >>");
			return mosipUserDtos;
		}

		for(JsonNode jsonNode : node) {
			MosipUserDto mosipUserDto = new MosipUserDto();
			String username = jsonNode.get("username").textValue();
			if (userDetails.stream().anyMatch(user -> user.equalsIgnoreCase(username))) {
				mosipUserDto.setUserId(username);
				mosipUserDto.setMail(jsonNode.hasNonNull("email") ?
						jsonNode.get("email").textValue() : null);
				mosipUserDto.setName(String.format("%s %s", (jsonNode.hasNonNull("firstName") ?
						jsonNode.get("firstName").textValue() : ""), (jsonNode.hasNonNull("lastName") ?
						jsonNode.get("lastName").textValue() : "")));
				try {
					String roles = getRolesAsString(jsonNode.get("id").textValue(),realmId);
					mosipUserDto.setRole(roles);
				} catch (IOException e) {
					LOGGER.error("getRolesAsString >>", e);
					throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
							AuthErrorCode.IO_EXCEPTION.getErrorMessage());
				}

				if(jsonNode.hasNonNull("attributes")) {
					JsonNode attributeNodes = jsonNode.get("attributes");
					if(attributeNodes.hasNonNull("mobile") && attributeNodes.get("mobile").hasNonNull(0)) {
						mosipUserDto.setMobile(attributeNodes.get("mobile").get(0).textValue());
					}
					if(attributeNodes.hasNonNull("rid") && attributeNodes.get("rid").hasNonNull(0)) {
						mosipUserDto.setRId(attributeNodes.get("rid").get(0).textValue());
					}
					if(attributeNodes.hasNonNull("name") && attributeNodes.get("name").hasNonNull(0)) {
						mosipUserDto.setName(attributeNodes.get("name").get(0).textValue());
					}
				}
				mosipUserDto.setUserPassword(null);
				mosipUserDtos.add(mosipUserDto);
			}
		}
		return mosipUserDtos;
	}

	private String getPasswordFromDatabase(String userName) {
		return jdbcTemplate.query(FETCH_PASSWORD, new MapSqlParameterSource().addValue("username", userName),
				new ResultSetExtractor<String>() {

					@Override
					public String extractData(ResultSet rs) throws SQLException, DataAccessException {
						String pwd = null;
						while (rs.next()) {
							pwd = rs.getString("value");

						}
						return pwd;
					}
				});
	}

	/**
	 * Gets the roles as string.
	 *
	 * @param userId
	 *            the id generated by keycloak for that user not username or userid
	 * @return role as string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String getRolesAsString(String userId,String realmId) throws IOException {
		StringBuilder roleBuilder = new StringBuilder();
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);
		pathParams.put("userId", userId);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + roleUserMappingurl);
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		JsonNode jsonNode = objectMapper.readTree(response);
		for (JsonNode node : jsonNode) {
			String role = node.get("name").textValue();
			Objects.nonNull(role);
			roleBuilder.append(role).append(AuthConstant.COMMA);
		}
		return roleBuilder.length() > 0 ? roleBuilder.substring(0, roleBuilder.length() - 1) : "";
	}

	/**
	 * Gets the role details given a role name.
	 *
	 * @param roleName
	 *            the id generated by keycloak for that user not username or userid
	 * @return roleid as string
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private String getRoleId(String roleName,String realmId) throws IOException {
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmId);		
		pathParams.put("roleName", roleName);
// https://preprod.southindia.cloudapp.azure.com/keycloak/auth/admin/realms/preregistration/roles/INDIVIDUAL
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakBaseUrl + "/roles/" + roleName);
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		JsonNode jsonNode = objectMapper.readTree(response);
		String roleId = jsonNode.get("id").asText();
		return roleId;
		
	}

	@Override
	public IndividualIdDto getIndividualIdFromUserId(String userId, String realmID) {
		IndividualIdDto individualIdDto = new IndividualIdDto();
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, realmID);
		HttpHeaders httpHeaders = new HttpHeaders();
		HttpEntity<String> httpEntity = new HttpEntity<>(null, httpHeaders);
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + userId);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		if (response == null || response.isEmpty()) {
			throw new AuthManagerException(AuthErrorCode.USER_NOT_FOUND.getErrorCode(),
					AuthErrorCode.USER_NOT_FOUND.getErrorMessage());
		}
		try {
			JsonNode node = objectMapper.readTree(response);
			for (JsonNode jsonNode : node) {
				if (jsonNode.get(AuthConstant.USER_NAME).textValue().equals(userId)) {
					JsonNode attriNode = jsonNode.get("attributes");
					String individualId = attriNode.get(AuthConstant.INDIVIDUAL_ID).get(0).textValue();
					individualIdDto.setIndividualId(individualId);
 					break;
				}
			}
			if (individualIdDto.getIndividualId() == null) {
				throw new AuthManagerException(AuthErrorCode.INDIVIDUAL_ID_NOT_FOUND.getErrorCode(),
						AuthErrorCode.INDIVIDUAL_ID_NOT_FOUND.getErrorMessage());
			}

		} catch (IOException e) {
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}

		return individualIdDto;
	}

	@Override
	public MosipUserListDto getListOfUsersDetails(String realmId, String roleName, int pageStart, int pageFetch,
			String email, String firstName, String lastName, String username) {		
		Map<String, String> pathParams = new HashMap<>();		
		UriComponentsBuilder uriComponentsBuilder = null;
		boolean isRoleBasedSearch = false;
		HttpEntity<String> httpEntity = new HttpEntity<>(null, new HttpHeaders());
		if (roleName != null && !roleName.isBlank() && !roleName.isEmpty()) {			
			pathParams.put(AuthConstant.ROLE_NAME, roleName);
			pathParams.put(AuthConstant.REALM, realmId);
			uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + roleBasedUsersurl);
			isRoleBasedSearch = true;
		} else {
			pathParams.put(AuthConstant.REALM_ID, realmId);
			uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + users);
			if(StringUtils.isNotBlank(email)) {
				uriComponentsBuilder.queryParam("email", email);
			}
			if(StringUtils.isNotBlank(firstName)) {
				uriComponentsBuilder.queryParam("firstName", firstName);
			}
			if(StringUtils.isNotBlank(lastName)) {
				uriComponentsBuilder.queryParam("lastName", lastName);
			}
			if(StringUtils.isNotBlank(username)) {
				uriComponentsBuilder.queryParam("username", username);
			}
		}
		uriComponentsBuilder.queryParam("first", pageStart);
		uriComponentsBuilder.queryParam("max", pageFetch == 0 ? maxUsers:pageFetch);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		List<MosipUserDto> mosipUserDtos = null;
		try {
			JsonNode node = objectMapper.readTree(response);
			mosipUserDtos = mapUsersToUserDetailDto(node, realmId,isRoleBasedSearch,roleName);
		} catch (IOException e) {
			LOGGER.error("Error in getListOfUsersDetails", e);
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		MosipUserListDto mosipUserListDto = new MosipUserListDto();
		mosipUserListDto.setMosipUserDtoList(mosipUserDtos);
		return mosipUserListDto;
	}
	
	private List<MosipUserDto> mapUsersToUserDetailDto(JsonNode node, String realmId, boolean isRoleBasedSearch,
			String roleName) {
		List<MosipUserDto> mosipUserDtos = new ArrayList<>();
		if (node == null) {
			LOGGER.error("response from openid is null >>");
			return mosipUserDtos;
		}

		for (JsonNode jsonNode : node) {
			MosipUserDto mosipUserDto = new MosipUserDto();
			String username = jsonNode.get("username").textValue();
			mosipUserDto.setUserId(username);
			mosipUserDto.setMail(jsonNode.hasNonNull("email") ? jsonNode.get("email").textValue() : null);
			mosipUserDto.setName(String.format("%s %s",
					(jsonNode.hasNonNull("firstName") ? jsonNode.get("firstName").textValue() : ""),
					(jsonNode.hasNonNull("lastName") ? jsonNode.get("lastName").textValue() : "")));
			mosipUserDto.setRole(roleName);
			if (!isRoleBasedSearch) {
				try {
					String roles = getRolesAsString(jsonNode.get("id").textValue(), realmId);
					mosipUserDto.setRole(roles);
				} catch (IOException e) {
					LOGGER.error("getRolesAsString >>", e);
					throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
							AuthErrorCode.IO_EXCEPTION.getErrorMessage());
				}
			}

			if (jsonNode.hasNonNull("attributes")) {
				JsonNode attributeNodes = jsonNode.get("attributes");
				if (attributeNodes.hasNonNull("mobile") && attributeNodes.get("mobile").hasNonNull(0)) {
					mosipUserDto.setMobile(attributeNodes.get("mobile").get(0).textValue());
				}
				if (attributeNodes.hasNonNull("rid") && attributeNodes.get("rid").hasNonNull(0)) {
					mosipUserDto.setRId(attributeNodes.get("rid").get(0).textValue());
				}
				if (attributeNodes.hasNonNull("name") && attributeNodes.get("name").hasNonNull(0)) {
					mosipUserDto.setName(attributeNodes.get("name").get(0).textValue());
				}
			}
			mosipUserDto.setUserPassword(null);
			mosipUserDtos.add(mosipUserDto);
		}

		return mosipUserDtos;
	}	
}
