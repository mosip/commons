package io.mosip.kernel.auth.defaultimpl.repository.impl;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import jakarta.annotation.PostConstruct;
import javax.xml.bind.DatatypeConverter;

import org.apache.directory.api.ldap.model.password.PasswordDetails;
import org.apache.directory.api.ldap.model.password.PasswordUtil;
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

/**
 * Keycloak IAM {@link DataStore}: admin REST (roles, users, registration) plus
 * JDBC to the Keycloak DB for password salts. Uses pooled
 * {@code keycloakRestTemplate} and {@link UriComponentsBuilder#fromUriString}.
 */
@Component
public class KeycloakImpl implements DataStore {

	/**
	 * Default individual role name used when registering users.
	 */
	private static final String INDIVIDUAL = "INDIVIDUAL";

	/**
	 * Keycloak realm operations base URL template.
	 */
	@Value("${mosip.iam.realm.operations.base-url}")
	private String keycloakBaseUrl;

	/**
	 * Keycloak admin API base URL.
	 */
	@Value("${mosip.iam.admin-url}")
	private String keycloakAdminUrl;

	/**
	 * Admin realm used for privileged operations.
	 */
	@Value("${mosip.iam.admin-realm-id}")
	private String adminRealmId;

	// @Value("${mosip.iam.default.realm-id}")
	// private String realmId;

	/**
	 * App-id to realm mapper.
	 */
	@Autowired
	private AuthUtil authUtil;

	/**
	 * Admin API path suffix for listing roles.
	 */
	@Value("${mosip.iam.roles-extn-url}")
	private String roles;

	/**
	 * Admin API path suffix for users.
	 */
	@Value("${mosip.iam.users-extn-url}")
	private String users;

	/**
	 * Admin API path suffix for role-user mapping.
	 */
	@Value("${mosip.iam.role-user-mapping-url}")
	private String roleUserMappingurl;

	/**
	 * Pooled RestTemplate for Keycloak HTTP.
	 */
	@Autowired
	@Qualifier("keycloakRestTemplate")
	private RestTemplate restTemplate;

	/**
	 * JDBC URL/host for the Keycloak database.
	 */
	@Value("${db_3_DS.keycloak.ipaddress}")
	private String keycloakHost;

	/**
	 * Keycloak DB port (bound from config; JDBC URL uses {@link #keycloakHost}).
	 */
	@Value("${db_3_DS.keycloak.port}")
	private String keycloakPort;

	/**
	 * Keycloak DB username.
	 */
	@Value("${db_3_DS.keycloak.username}")
	private String keycloakUsername;

	/**
	 * Keycloak DB password.
	 */
	@Value("${db_3_DS.keycloak.password}")
	private String keycloakPassword;

	/**
	 * JDBC driver class for the Keycloak DB.
	 */
	@Value("${db_3_DS.keycloak.driverClassName}")
	private String keycloakDriver;

	/**
	 * Default password assigned to pre-registration users.
	 */
	@Value("${mosip.iam.pre-reg_user_password}")
	private String preRegUserPassword;

	/**
	 * Admin API path for users-by-role search.
	 */
	@Value("${mosip.iam.role-based-user-url}")
	private String roleBasedUsersurl;

	/**
	 * Hikari maximum pool size.
	 */
	@Value("${hikari.maximumPoolSize:25}")
	private int maximumPoolSize;
	/**
	 * Hikari validation timeout in milliseconds.
	 */
	@Value("${hikari.validationTimeout:3000}")
	private int validationTimeout;
	/**
	 * Hikari connection timeout in milliseconds.
	 */
	@Value("${hikari.connectionTimeout:60000}")
	private int connectionTimeout;
	/**
	 * Hikari idle timeout in milliseconds.
	 */
	@Value("${hikari.idleTimeout:200000}")
	private int idleTimeout;
	/**
	 * Hikari minimum idle connections.
	 */
	@Value("${hikari.minimumIdle:0}")
	private int minimumIdle;

	/**
	 * Max users returned from Keycloak list APIs ({@code max} query param).
	 */
	@Value("${mosip.keycloak.max-no-of-users:100}")
	private String maxUsers;

	/**
	 * JDBC template over the Keycloak database (salts/credentials).
	 */
	private NamedParameterJdbcTemplate jdbcTemplate;

	/**
	 * SQL to load {@code userPassword} attributes for usernames.
	 */
	private static final String FETCH_ALL_SALTS = "select ue.username,ua.value from public.user_entity ue, public.user_attribute ua where ue.id=ua.user_id and ua.name='userPassword' and ue.username IN(:username)";

	/**
	 * SQL to load credential secret data for a username.
	 */
	private static final String FETCH_PASS_QUERY = "select cr.value from public.credential cr, public.user_entity ue where cr.user_id=ue.id and ue.username=:username";
	/**
	 * JSON mapper for Keycloak admin API bodies.
	 */
	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Cached Keycloak id of the INDIVIDUAL role (set when mapping roles).
	 */
	private String individualRoleID;
	/**
	 * Logger for admin API and JDBC errors.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KeycloakImpl.class);

	/**
	 * Initializes the Keycloak JDBC pool after properties are injected.
	 */
	@PostConstruct
	private void setup() {
		setUpConnection();
	}

	/**
	 * Builds a Hikari datasource to the Keycloak DB and wraps it as
	 * {@link NamedParameterJdbcTemplate}.
	 */
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

	/**
	 * Lists roles in the given realm via Keycloak admin API.
	 *
	 * @param appId Keycloak realm id
	 * @return roles list
	 */
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
				String name = jsonNode.get(AuthConstant.NAME).textValue();
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

	/**
	 * Loads user details: empty list, single-user exact search, or slow multi-user
	 * scan limited by {@link #maxUsers}.
	 *
	 * @param userDetails usernames to load
	 * @param realmId     Keycloak realm
	 * @return matching users
	 * @throws Exception if the admin API fails
	 */
	@Override
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String realmId) throws Exception {
		if (userDetails == null || userDetails.isEmpty()) {
			MosipUserListDto out = new MosipUserListDto();
			out.setMosipUserDtoList(java.util.Collections.emptyList());
			return out;
		}
		if (userDetails.size() == 1) {
			return getSingleUserDetails(userDetails.get(0), realmId);
		}
		// fallback to existing multi-user path (see next fixes)
		return getListOfUsersDetailsSlowPath(userDetails, realmId);
	}

	/**
	 * Exact username search ({@code max=1}) including roles and attributes.
	 *
	 * @param username user to load
	 * @param realmId  Keycloak realm
	 * @return list with at most one user
	 */
	private MosipUserListDto getSingleUserDetails(String username, String realmId) {
		Map<String, String> path = Map.of(AuthConstant.REALM_ID, realmId);
		var uri = UriComponentsBuilder.fromUriString(keycloakAdminUrl + users)
				.queryParam(AuthConstant.USER_NAME, username)
				.queryParam(AuthConstant.EXACT, true)
				.queryParam(AuthConstant.BRIEF_REPRESENTATION, true)
				.queryParam(AuthConstant.MAX, 1)
				.buildAndExpand(path)
				.toString();

		String response = callKeycloakService(uri, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()));
		List<MosipUserDto> list = new java.util.ArrayList<>(1);

		try {
			JsonNode node = objectMapper.readTree(response);
			for (JsonNode json : node) {
				if (username.equalsIgnoreCase(json.path(AuthConstant.USER_NAME).asText())) {
					MosipUserDto dto = new MosipUserDto();
					dto.setUserId(username);
					dto.setMail(json.hasNonNull(AuthConstant.EMAIL) ? json.get(AuthConstant.EMAIL).asText() : null);
					dto.setName(String.format("%s %s",
							json.path(AuthConstant.FIRST_NAME).asText(""),
							json.path(AuthConstant.LAST_NAME).asText("")));
					// If you really need roles for single-user:
					String roles = getRolesAsString(json.get(AuthConstant.ID).asText(), realmId); // consider caching this
					dto.setRole(roles);

					if (json.has(AuthConstant.ATTRIBUTES)) {
						JsonNode attrs = json.get(AuthConstant.ATTRIBUTES);
						if (attrs.has(AuthConstant.MOBILE) && attrs.get(AuthConstant.MOBILE).has(0)) dto.setMobile(attrs.get(AuthConstant.MOBILE).get(0).asText());
						if (attrs.has(AuthConstant.RID) && attrs.get(AuthConstant.RID).has(0))       dto.setRId(attrs.get(AuthConstant.RID).get(0).asText());
						if (attrs.has(AuthConstant.NAME) && attrs.get(AuthConstant.NAME).has(0))     dto.setName(attrs.get(AuthConstant.NAME).get(0).asText());
					}
					dto.setUserPassword(null);
					list.add(dto);
					break;
				}
			}
		} catch (IOException e) {
			LOGGER.error("Parsing single user details", e);
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}

		MosipUserListDto out = new MosipUserListDto();
		out.setMosipUserDtoList(list);
		return out;
	}

	/**
	 * Lists up to {@link #maxUsers} users and filters to {@code userDetails}.
	 *
	 * @param userDetails usernames wanted
	 * @param realmId     Keycloak realm
	 * @return matching users
	 * @throws Exception if the admin API fails
	 */
	private MosipUserListDto getListOfUsersDetailsSlowPath(List<String> userDetails, String realmId) throws Exception {
		Set<String> wanted = new HashSet<>(userDetails.size());
		for (String u : userDetails) if (u != null) wanted.add(u.toLowerCase(Locale.ROOT));

		Map<String,String> path = Map.of(AuthConstant.REALM_ID, realmId);
		var uri = UriComponentsBuilder.fromUriString(keycloakAdminUrl + users)
				.queryParam(AuthConstant.MAX, maxUsers)
				.queryParam(AuthConstant.BRIEF_REPRESENTATION, true)
				.buildAndExpand(path)
				.toString();

		String response = callKeycloakService(uri, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()));
		List<MosipUserDto> mosipUserDtos = new ArrayList<>();

		try {
			JsonNode node = objectMapper.readTree(response);
			for (JsonNode json : node) {
				String username = json.path(AuthConstant.USER_NAME).asText();
				if (!wanted.contains(username.toLowerCase(Locale.ROOT))) continue;

				MosipUserDto dto = new MosipUserDto();
				dto.setUserId(username);
				dto.setMail(json.hasNonNull(AuthConstant.EMAIL) ? json.get(AuthConstant.EMAIL).asText() : null);
				dto.setName(String.format("%s %s", json.path(AuthConstant.FIRST_NAME).asText(""), json.path(AuthConstant.LAST_NAME).asText("")));
				// Only call roles if you absolutely must:
				String roles = getRolesAsString(json.get("id").asText(), realmId);
				dto.setRole(roles);

				if (json.has(AuthConstant.ATTRIBUTES)) {
					JsonNode attrs = json.get(AuthConstant.ATTRIBUTES);
					if (attrs.has(AuthConstant.MOBILE) && attrs.get(AuthConstant.MOBILE).has(0)) dto.setMobile(attrs.get(AuthConstant.MOBILE).get(0).asText());
					if (attrs.has(AuthConstant.RID) && attrs.get(AuthConstant.RID).has(0))       dto.setRId(attrs.get(AuthConstant.RID).get(0).asText());
					if (attrs.has(AuthConstant.NAME) && attrs.get(AuthConstant.NAME).has(0))     dto.setName(attrs.get(AuthConstant.NAME).get(0).asText());
				}
				dto.setUserPassword(null);
				mosipUserDtos.add(dto);
			}
		} catch (IOException e) {
			LOGGER.error("Error in getListOfUsersDetails", e);
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}

		MosipUserListDto out = new MosipUserListDto();
		out.setMosipUserDtoList(mosipUserDtos);
		return out;
	}

	/*@Override
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
			mosipUserDtos = mapUsersToUserDetailDto(node, userDetails, realmId);
		} catch (IOException e) {
			LOGGER.error("Error in getListOfUsersDetails", e);
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		MosipUserListDto mosipUserListDto = new MosipUserListDto();
		mosipUserListDto.setMosipUserDtoList(mosipUserDtos);
		return mosipUserListDto;
	}*/

	/**
	 * Loads password salts from the Keycloak DB for the given usernames.
	 *
	 * @param userDetails usernames
	 * @param appId       unused realm argument on this JDBC path
	 * @return salts per user
	 * @throws Exception if JDBC fails
	 */
	@Override
	public MosipUserSaltListDto getAllUserDetailsWithSalt(List<String> userDetails, String appId) throws Exception {

		return jdbcTemplate.query(FETCH_ALL_SALTS, new MapSqlParameterSource(AuthConstant.USER_NAME, userDetails),
				new ResultSetExtractor<MosipUserSaltListDto>() {

	/**
	 * Maps JDBC rows to {@link MosipUserSalt} (username + decoded salt).
	 *
	 * @param rs result set of username and userPassword attribute
	 * @return salt list DTO
	 * @throws SQLException        if a column cannot be read
	 * @throws DataAccessException if Spring JDBC fails
	 */
					@Override
					public MosipUserSaltListDto extractData(ResultSet rs) throws SQLException, DataAccessException {
						MosipUserSaltListDto mosipUserSaltListDto = new MosipUserSaltListDto();
						List<MosipUserSalt> mosipUserSaltList = new ArrayList<>();
						while (rs.next()) {
							MosipUserSalt mosipUserSalt = new MosipUserSalt();
							mosipUserSalt.setUserId(rs.getString(AuthConstant.USER_NAME));
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

	/**
	 * Reads RID user attribute from Keycloak admin user search.
	 *
	 * @param userId username
	 * @param appId  realm id
	 * @return RID wrapper
	 * @throws Exception if the user or RID attribute is missing
	 */
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
					JsonNode attriNode = jsonNode.get(AuthConstant.ATTRIBUTES);
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

	/**
	 * Not implemented.
	 *
	 * @param userId unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public AuthZResponseDto unBlockAccount(String userId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Creates the user in Keycloak if missing and maps INDIVIDUAL role when present.
	 *
	 * @param userId registration request including app id
	 * @return DTO with username only
	 */
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

	/**
	 * POSTs realm role mapping for INDIVIDUAL onto the user.
	 *
	 * @param userID  Keycloak internal user id
	 * @param realmId realm
	 */
	private void roleMapper(String userID, String realmId) {
		Map<String, String> pathParams = new HashMap<>();

		pathParams.put(AuthConstant.REALM_ID, realmId);
		pathParams.put("userID", userID);
		try {
			if (individualRoleID == null || individualRoleID.isEmpty())
				individualRoleID = getRoleId(INDIVIDUAL, realmId);
		} catch (Exception ex) {
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

	/**
	 * Resolves Keycloak internal id from username.
	 *
	 * @param userName username
	 * @param realmId  realm
	 * @return user id, or {@code null} if missing
	 */
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
				if (userName.equals(jsonNode.get(AuthConstant.USER_NAME).asText())) {
					return jsonNode.get("id").asText();
				}
			}

		}
		return null;
	}

	/**
	 * Checks if is user already present.
	 *
	 * @param userName the user name
	 * @param realmId  Keycloak realm
	 * @return true, if successful
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
				if (userName.equals(jsonNode.get(AuthConstant.USER_NAME).asText())) {
					return true;
				}
			}

		}
		return false;
	}

	/**
	 * Maps a MOSIP registration request to a Keycloak create-user body (roles,
	 * credentials, mobile/gender attributes).
	 *
	 * @param userRegDto registration request
	 * @return Keycloak payload
	 */
	private KeycloakRequestDto mapUserRequestToKeycloakRequestDto(UserRegistrationRequestDto userRegDto) {
		KeycloakRequestDto keycloakRequestDto = new KeycloakRequestDto();
		List<String> roles = new ArrayList<>();
		List<KeycloakPasswordDTO> credentialObject = new ArrayList<>();
		KeycloakPasswordDTO dto = null;
		if (userRegDto.getAppId().equalsIgnoreCase("prereg")) {
			roles.add(INDIVIDUAL);
			dto = new KeycloakPasswordDTO();
			dto.setType(AuthConstant.PASSWORDCONSTANT);
			dto.setValue(preRegUserPassword);
		} else if (userRegDto.getAppId().equalsIgnoreCase("registrationclient")) {
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
		attributes.put(AuthConstant.MOBILE, contactNoList);

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

	/**
	 * Unused stub retained for historical mapping.
	 *
	 * @param userId unused registration request
	 */
	private void KeycloakRequestDtomapUserRequestToKeycloakRequestDto(UserRegistrationRequestDto userId) {
		// TODO Auto-generated method stub

	}

	/**
	 * Not implemented.
	 *
	 * @param userPasswordRequestDto unused
	 * @return {@code null}
	 */
	@Override
	public UserPasswordResponseDto addPassword(UserPasswordRequestDto userPasswordRequestDto) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param passwordDto unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public AuthZResponseDto changePassword(PasswordDto passwordDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param passwordDto unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public AuthZResponseDto resetPassword(PasswordDto passwordDto) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param mobileNumber unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public UserNameDto getUserNameBasedOnMobileNumber(String mobileNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented (authentication is in {@code AuthServiceImpl}).
	 *
	 * @param loginUser unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public MosipUserDto authenticateUser(LoginUser loginUser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param otpUser unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public MosipUserDto authenticateWithOtp(OtpUser otpUser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param loginUser unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public MosipUserDto authenticateUserWithOtp(UserOtp loginUser) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param clientSecret unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public MosipUserDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param username unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public MosipUserDto getUserRoleByUserId(String username) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param mobileNumber unused
	 * @return {@code null}
	 * @throws Exception never thrown
	 */
	@Override
	public MosipUserDto getUserDetailBasedonMobileNumber(String mobileNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param userId unused
	 * @return {@code null}
	 */
	@Override
	public ValidationResponseDto validateUserName(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Not implemented.
	 *
	 * @param userIds unused
	 * @return {@code null}
	 */
	@Override
	public UserDetailsResponseDto getUserDetailBasedOnUid(List<String> userIds) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Call keycloak service.
	 *
	 * @param url           the url
	 * @param httpMethod    the http method
	 * @param requestEntity the request entity
	 * @return the string
	 */
	private String callKeycloakService(String url, HttpMethod httpMethod, HttpEntity<?> requestEntity) {
		ResponseEntity<String> responseEntity = null;
		String response = null;
		try {

			responseEntity = restTemplate.exchange(url, httpMethod, requestEntity, String.class);
		} catch (HttpServerErrorException | HttpClientErrorException ex) {
			List<ServiceError> validationErrorsList = ExceptionUtils.getServiceErrorList(ex.getResponseBodyAsString());

			if (ex.getStatusCode().value() == 401) {
				if (!validationErrorsList.isEmpty()) {
					throw new AuthNException(validationErrorsList);
				} else {
					throw new BadCredentialsException("Authentication failed from AuthManager");
				}
			}
			if (ex.getStatusCode().value() == 403) {
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
	 * @param node        the node
	 * @param userDetails
	 * @return the list
	 */
	private List<MosipUserDto> mapUsersToUserDetailDto(JsonNode node, List<String> userDetails, String realmId) {
		List<MosipUserDto> mosipUserDtos = new ArrayList<>();
		if (node == null) {
			LOGGER.error("response from openid is null >>");
			return mosipUserDtos;
		}

		for (JsonNode jsonNode : node) {
			MosipUserDto mosipUserDto = new MosipUserDto();
			String username = jsonNode.get(AuthConstant.USER_NAME).textValue();
			if (userDetails.stream().anyMatch(user -> user.equalsIgnoreCase(username))) {
				mosipUserDto.setUserId(username);
				mosipUserDto.setMail(jsonNode.hasNonNull(AuthConstant.EMAIL) ? jsonNode.get(AuthConstant.EMAIL).textValue() : null);
				mosipUserDto.setName(String.format("%s %s",
						(jsonNode.hasNonNull(AuthConstant.FIRST_NAME) ? jsonNode.get(AuthConstant.FIRST_NAME).textValue() : ""),
						(jsonNode.hasNonNull(AuthConstant.LAST_NAME) ? jsonNode.get(AuthConstant.LAST_NAME).textValue() : "")));
				try {
					String roles = getRolesAsString(jsonNode.get("id").textValue(), realmId);
					mosipUserDto.setRole(roles);
				} catch (IOException e) {
					LOGGER.error("getRolesAsString >>", e);
					throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
							AuthErrorCode.IO_EXCEPTION.getErrorMessage());
				}

				if (jsonNode.hasNonNull(AuthConstant.ATTRIBUTES)) {
					JsonNode attributeNodes = jsonNode.get(AuthConstant.ATTRIBUTES);
					if (attributeNodes.hasNonNull(AuthConstant.MOBILE) && attributeNodes.get(AuthConstant.MOBILE).hasNonNull(0)) {
						mosipUserDto.setMobile(attributeNodes.get(AuthConstant.MOBILE).get(0).textValue());
					}
					if (attributeNodes.hasNonNull(AuthConstant.RID) && attributeNodes.get(AuthConstant.RID).hasNonNull(0)) {
						mosipUserDto.setRId(attributeNodes.get(AuthConstant.RID).get(0).textValue());
					}
					if (attributeNodes.hasNonNull(AuthConstant.NAME) && attributeNodes.get(AuthConstant.NAME).hasNonNull(0)) {
						mosipUserDto.setName(attributeNodes.get(AuthConstant.NAME).get(0).textValue());
					}
				}
				mosipUserDto.setUserPassword(null);
				mosipUserDtos.add(mosipUserDto);
			}
		}
		return mosipUserDtos;
	}


	/**
	 * Gets the roles as string.
	 *
	 * @param userId the id generated by keycloak for that user not username or
	 *               userid
	 * @return role as string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String getRolesAsString(String userId, String realmId) throws IOException {
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
			String role = node.get(AuthConstant.NAME).textValue();
			Objects.nonNull(role);
			roleBuilder.append(role).append(AuthConstant.COMMA);
		}
		return roleBuilder.length() > 0 ? roleBuilder.substring(0, roleBuilder.length() - 1) : "";
	}

	/**
	 * Gets the role details given a role name.
	 *
	 * @param roleName the id generated by keycloak for that user not username or
	 *                 userid
	 * @return roleid as string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private String getRoleId(String roleName, String realmId) throws IOException {
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

	/**
	 * Reads individualId (or individualid) attribute from Keycloak user search.
	 *
	 * @param userId  username
	 * @param realmID realm
	 * @return individual id wrapper
	 */
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
					JsonNode attriNode = jsonNode.get(AuthConstant.ATTRIBUTES);
					String individualId = null;
					if (attriNode.has(AuthConstant.INDIVIDUAL_ID))
						individualId = attriNode.get(AuthConstant.INDIVIDUAL_ID).get(0).textValue();
					if (attriNode.has(AuthConstant.INDIVIDUALID))
						individualId = attriNode.get(AuthConstant.INDIVIDUALID).get(0).textValue();
						
					if (Objects.nonNull(individualId)) {
						LOGGER.info("Found Individual Id for the input user: " + userId + ", Id: " + individualId);
						individualIdDto.setIndividualId(individualId);
						break;
					}
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

	/**
	 * Searches users by role or by email/name/username/search with pagination.
	 *
	 * @param realmId   Keycloak realm
	 * @param roleName  optional role; when set, uses role-based user URL
	 * @param pageStart {@code first} offset
	 * @param pageFetch page size; {@code 0} uses {@link #maxUsers}
	 * @param email     optional email filter
	 * @param firstName optional first name
	 * @param lastName  optional last name
	 * @param username  optional username
	 * @param search    optional free-text search
	 * @return matching users
	 */
	@Override
	public MosipUserListDto getListOfUsersDetails(String realmId, String roleName, int pageStart, int pageFetch,
			String email, String firstName, String lastName, String username, String search) {
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
			if (StringUtils.isNotBlank(email)) {
				uriComponentsBuilder.queryParam(AuthConstant.EMAIL, email);
			}
			if (StringUtils.isNotBlank(firstName)) {
				uriComponentsBuilder.queryParam(AuthConstant.FIRST_NAME, firstName);
			}
			if (StringUtils.isNotBlank(lastName)) {
				uriComponentsBuilder.queryParam(AuthConstant.LAST_NAME, lastName);
			}
			if (StringUtils.isNotBlank(username)) {
				uriComponentsBuilder.queryParam(AuthConstant.USER_NAME, username);
			}
			if (StringUtils.isNotBlank(search)) {
				uriComponentsBuilder.queryParam(AuthConstant.SEARCH, search);
			}
		}
		uriComponentsBuilder.queryParam(AuthConstant.FIRST, pageStart);
		uriComponentsBuilder.queryParam(AuthConstant.MAX, pageFetch == 0 ? maxUsers : pageFetch);
		String response = callKeycloakService(uriComponentsBuilder.buildAndExpand(pathParams).toString(),
				HttpMethod.GET, httpEntity);
		List<MosipUserDto> mosipUserDtos = null;
		try {
			JsonNode node = objectMapper.readTree(response);
			mosipUserDtos = mapUsersToUserDetailDto(node, realmId, isRoleBasedSearch, roleName);
		} catch (IOException e) {
			LOGGER.error("Error in getListOfUsersDetails", e);
			throw new AuthManagerException(AuthErrorCode.IO_EXCEPTION.getErrorCode(),
					AuthErrorCode.IO_EXCEPTION.getErrorMessage());
		}
		MosipUserListDto mosipUserListDto = new MosipUserListDto();
		mosipUserListDto.setMosipUserDtoList(mosipUserDtos);
		return mosipUserListDto;
	}

	/**
	 * Maps Keycloak user JSON to MOSIP users; loads roles unless this is a
	 * role-based search (then {@code roleName} is used).
	 *
	 * @param node              admin API JSON array
	 * @param realmId           realm for role lookup
	 * @param isRoleBasedSearch {@code true} to skip per-user role fetch
	 * @param roleName          role to set when role-based
	 * @return mapped users
	 */
	private List<MosipUserDto> mapUsersToUserDetailDto(JsonNode node, String realmId, boolean isRoleBasedSearch,
			String roleName) {
		List<MosipUserDto> mosipUserDtos = new ArrayList<>();
		if (node == null) {
			LOGGER.error("response from openid is null >>");
			return mosipUserDtos;
		}

		for (JsonNode jsonNode : node) {
			MosipUserDto mosipUserDto = new MosipUserDto();
			String username = jsonNode.get(AuthConstant.USER_NAME).textValue();
			mosipUserDto.setUserId(username);
			mosipUserDto.setMail(jsonNode.hasNonNull(AuthConstant.EMAIL) ? jsonNode.get(AuthConstant.EMAIL).textValue() : null);
			mosipUserDto.setName(String.format("%s %s",
					(jsonNode.hasNonNull(AuthConstant.FIRST_NAME) ? jsonNode.get(AuthConstant.FIRST_NAME).textValue() : ""),
					(jsonNode.hasNonNull(AuthConstant.LAST_NAME) ? jsonNode.get(AuthConstant.LAST_NAME).textValue() : "")));
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

			if (jsonNode.hasNonNull(AuthConstant.ATTRIBUTES)) {
				JsonNode attributeNodes = jsonNode.get(AuthConstant.ATTRIBUTES);
				if (attributeNodes.hasNonNull(AuthConstant.MOBILE) && attributeNodes.get(AuthConstant.MOBILE).hasNonNull(0)) {
					mosipUserDto.setMobile(attributeNodes.get(AuthConstant.MOBILE).get(0).textValue());
				}
				if (attributeNodes.hasNonNull(AuthConstant.RID) && attributeNodes.get(AuthConstant.RID).hasNonNull(0)) {
					mosipUserDto.setRId(attributeNodes.get(AuthConstant.RID).get(0).textValue());
				}
				if (attributeNodes.hasNonNull(AuthConstant.NAME) && attributeNodes.get(AuthConstant.NAME).hasNonNull(0)) {
					mosipUserDto.setName(attributeNodes.get(AuthConstant.NAME).get(0).textValue());
				}
			}
			mosipUserDto.setUserPassword(null);
			mosipUserDtos.add(mosipUserDto);
		}

		return mosipUserDtos;
	}
}
