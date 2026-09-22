package io.mosip.kernel.auth.keycloak.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.defaultimpl.constant.AuthConstant;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultimpl.repository.impl.KeycloakImpl;
import io.mosip.kernel.auth.defaultimpl.util.AuthUtil;
import io.mosip.kernel.auth.test.AuthTestBootApplication;
import io.mosip.kernel.core.authmanager.model.IndividualIdDto;
import io.mosip.kernel.core.authmanager.model.MosipUserListDto;
import io.mosip.kernel.core.authmanager.model.RIdDto;
import io.mosip.kernel.core.authmanager.model.RolesListDto;

/**
 * Tests {@link KeycloakImpl} role listing, user listing/search, RID, and
 * individual-id lookup against a mocked {@code keycloakRestTemplate}.
 * <p>
 * Uses Boot 4 {@link AutoConfigureMockMvc} from
 * {@code org.springframework.boot.webmvc.test.autoconfigure} and
 * {@link MockitoBean} for the Keycloak RestTemplate.
 */
@SpringBootTest(classes = { AuthTestBootApplication.class })
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class KeycloakImplTest {

	/** Keycloak RestTemplate replaced with a Mockito bean. */
	@MockitoBean
	@Qualifier("keycloakRestTemplate")
	private RestTemplate restTemplate;

	/** Auth util from the test context. */
	@Autowired
	private AuthUtil authUtil;

	/** JSON mapper from the test context. */
	@Autowired
	private ObjectMapper objectMapper;

	/** Keycloak IAM repository under test. */
	@Autowired
	private KeycloakImpl keycloakImpl;

	/** Keycloak roles extension URL from test properties. */
	@Value("${mosip.iam.roles-extn-url}")
	private String roles;

	/** Keycloak users extension URL from test properties. */
	@Value("${mosip.iam.users-extn-url}")
	private String users;

	/** Keycloak role-user mapping URL from test properties. */
	@Value("${mosip.iam.role-user-mapping-url}")
	private String roleUserMappingurl;

	/** Keycloak realm operations base URL from test properties. */
	@Value("${mosip.iam.realm.operations.base-url}")
	private String keycloakBaseUrl;

	/** Keycloak admin URL from test properties. */
	@Value("${mosip.iam.admin-url}")
	private String keycloakAdminUrl;

	/** Keycloak admin realm id from test properties. */
	@Value("${mosip.iam.admin-realm-id}")
	private String adminRealmId;

	/** Max users fetched from Keycloak. */
	@Value("${mosip.keycloak.max-no-of-users:100}")
	private String maxUsers;

	/** Keycloak role-based user URL from test properties. */
	@Value("${mosip.iam.role-based-user-url}")
	private String roleBasedUsersurl;

//	restTemplate.exchange(url, httpMethod, requestEntity, String.class);

	/**
	 * Injects a mocked JDBC template into {@link KeycloakImpl}.
	 */
	@Before
	public void init() {
		NamedParameterJdbcTemplate jdbcTemplate = Mockito.mock(NamedParameterJdbcTemplate.class);
		ReflectionTestUtils.setField(keycloakImpl, "jdbcTemplate", jdbcTemplate);
	}

	/**
	 * Asserts getAllRoles parses the Keycloak roles JSON into {@link RolesListDto}.
	 *
	 * @throws Exception if the call fails
	 */
	@Test
	public void getAllRolesTest() throws Exception {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + roles);
		String userIDResp = "[\r\n" + "  {\r\n" + "    \"name\": \"PROCESSOR\"  }\r\n" + "]";
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		RolesListDto rolesListDto = keycloakImpl.getAllRoles("ida");
		assertThat(rolesListDto.getRoles().get(0).getRoleName(), is("PROCESSOR"));
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getAllRoles raises AuthManagerException on malformed JSON.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getAllRolesIOExceptionTest() throws Exception {

		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + roles);
		String userIDResp = "[\r\n" + "  \r\n" + "    \"name\": \"PROCESSOR\"  }\r\n" + "]";
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		RolesListDto rolesListDto = keycloakImpl.getAllRoles("ida");
		assertThat(rolesListDto.getRoles().get(0).getRoleName(), is("PROCESSOR"));
	}

	@Test
	/**
	 * Asserts getListOfUsersDetails returns mapped users from Keycloak.
	 *
	 * @throws Exception if the call fails
	 */
	public void getListOfUsersDetailsTest() throws Exception {

		// arrange
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");

		// Single-user exact endpoint your code builds now
		UriComponentsBuilder ucb = UriComponentsBuilder.fromUriString(keycloakAdminUrl + users)
				.queryParam("username", "mock-user")
				.queryParam("exact", true)
				.queryParam("briefRepresentation", true)
				.queryParam("max", 1);

		String userIDResp =
				"[{\"username\":\"mock-user\",\"email\":\"mock@mosip.io\",\"firstName\":\"fname\",\"lastName\":\"lname\",\"id\":\"829329\"," +
						"\"attributes\":{\"mobile\":[\"8291930201\"],\"rid\":[\"728391\"],\"name\":[\"mock-name\"]}}]";

		when(restTemplate.exchange(
				Mockito.eq(ucb.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET),
				Mockito.any(),
				Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));

		// role mapping (unchanged)
		Map<String, String> rolePathParams = new HashMap<>();
		rolePathParams.put(AuthConstant.REALM_ID, "ida");
		rolePathParams.put("userId", "829329");

		UriComponentsBuilder roleUcb = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + roleUserMappingurl);

		String roleResp = "[{\"name\":\"PROCESSOR\"}]";
		when(restTemplate.exchange(
				Mockito.eq(roleUcb.buildAndExpand(rolePathParams).toString()),
				Mockito.eq(HttpMethod.GET),
				Mockito.any(),
				Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(roleResp));

		// act
		List<String> userd = java.util.Arrays.asList("mock-user");
		MosipUserListDto dto = keycloakImpl.getListOfUsersDetails(userd, "ida");

		// assert
		assertThat(dto.getMosipUserDtoList().get(0).getName(), is("mock-name"));
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getListOfUsersDetails raises AuthManagerException on malformed JSON.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getListOfUsersDetailsIOExceptionTest() throws Exception {

		// Arrange: realm path
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");

		// The NEW URL your code builds for single-user exact lookup
		UriComponentsBuilder ucb = UriComponentsBuilder.fromUriString(keycloakAdminUrl + users)
				.queryParam("username", "mock-user")
				.queryParam("exact", true)
				.queryParam("briefRepresentation", true)
				.queryParam("max", 1);

		// Malformed JSON to force Jackson IOException
		String badJson = "[{\"username\":\"mock-user\""; // truncated JSON

		when(restTemplate.exchange(
				Mockito.eq(ucb.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET),
				Mockito.any(),                 // HttpEntity
				Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(badJson));

		// Act: should throw AuthManagerException before any roles call
		keycloakImpl.getListOfUsersDetails(java.util.Collections.singletonList("mock-user"), "ida");

		// No asserts needed; expected exception ends the test
	}

	@Test
	/**
	 * Asserts getRidFromUserId returns the RID from Keycloak user attributes.
	 *
	 * @throws Exception if the call fails
	 */
	public void getRidFromUserIdTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user\",\"attributes\":{\"rid\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		RIdDto rolesListDto = keycloakImpl.getRidFromUserId("mock-user", "ida");
		assertThat(rolesListDto.getRId(), is("8291930201"));
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getRidFromUserId raises AuthManagerException when the response is null.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getRidFromUserIdNullRespTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user\",\"attributes\":{\"rid\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(null));
		RIdDto rolesListDto = keycloakImpl.getRidFromUserId("mock-user", "ida");
		assertThat(rolesListDto.getRId(), is("8291930201"));
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getRidFromUserId raises AuthManagerException when the user is missing.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getRidFromUserIdUserNotFoundTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user1\",\"attributes\":{\"rid\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		RIdDto rolesListDto = keycloakImpl.getRidFromUserId("mock-user", "ida");
		assertThat(rolesListDto.getRId(), is("8291930201"));
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getRidFromUserId raises AuthManagerException on malformed JSON.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getRidFromUserIdIOExpTest() throws Exception {
		String userIDResp = "[\"username\": \"mock-user\",\"attributes\":{\"rid\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		RIdDto rolesListDto = keycloakImpl.getRidFromUserId("mock-user", "ida");
		assertThat(rolesListDto.getRId(), is("8291930201"));
	}

	/*
	 * @Test public void registerUserUserNotPresentTest() throws Exception { //is
	 * user already present Map<String, String> registerPathParams = new
	 * HashMap<>(); registerPathParams.put(AuthConstant.REALM_ID,
	 * "preregistration"); UriComponentsBuilder uriComponentsBuilder =
	 * UriComponentsBuilder
	 * .fromUriString(keycloakBaseUrl.concat("/users?username=").concat("112211"));
	 * when(restTemplate.exchange(
	 * Mockito.eq(uriComponentsBuilder.buildAndExpand(registerPathParams).toString()
	 * ), Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
	 * .thenReturn(null);
	 *
	 * // get useridfrom id String userIDResp = "[\r\n" + "  {\r\n" +
	 * "    \"username\": \"112211\",\r\n" + "    \"id\": \"8282828282\"\r\n" +
	 * "  }\r\n" + "]"; UriComponentsBuilder getUserIDUriComponentsBuilder =
	 * UriComponentsBuilder
	 * .fromUriString(keycloakBaseUrl.concat("/users?username=").concat("112211"));
	 * when(restTemplate.exchange(
	 * Mockito.eq(getUserIDUriComponentsBuilder.buildAndExpand(registerPathParams).
	 * toString()), Mockito.eq(HttpMethod.GET), Mockito.any(),
	 * Mockito.eq(String.class))) .thenReturn(ResponseEntity.ok(userIDResp));
	 *
	 * // get role ID
	 *
	 * Map<String, String> roleIDPathParams = new HashMap<>();
	 * roleIDPathParams.put(AuthConstant.REALM_ID, "preregistration");
	 * roleIDPathParams.put("roleName", "INDIVIDUAL"); UriComponentsBuilder
	 * rolIDUriComponentsBuilder =
	 * UriComponentsBuilder.fromUriString(keycloakBaseUrl + "/roles/" +
	 * "INDIVIDUAL"); // get useridfromid String roleIDResp = "  {\r\n" +
	 * "    \"id\": \"8282828282\"\r\n" + "  }"; when(restTemplate.exchange(
	 * Mockito.eq(rolIDUriComponentsBuilder.buildAndExpand(roleIDPathParams).
	 * toString()), Mockito.eq(HttpMethod.GET), Mockito.any(),
	 * Mockito.eq(String.class))) .thenReturn(ResponseEntity.ok(roleIDResp));
	 *
	 * // map roles UriComponentsBuilder roleMapperUriComponentsBuilder
	 * =UriComponentsBuilder.fromUriString(keycloakBaseUrl.concat(
	 * "/users/{userID}/role-mappings/realm")); registerPathParams.put("userID",
	 * "112211");
	 * when(restTemplate.exchange(Mockito.eq(roleMapperUriComponentsBuilder.
	 * buildAndExpand(registerPathParams).toString()), Mockito.eq(HttpMethod.POST),
	 * Mockito.any(), Mockito.eq(String.class)))
	 * .thenReturn(ResponseEntity.ok("{}"));
	 *
	 * UserRegistrationRequestDto userRegistrationRequestDto = new
	 * UserRegistrationRequestDto(); userRegistrationRequestDto.setAppId("prereg");
	 * userRegistrationRequestDto.setUserName("112211");
	 *
	 * MosipUserDto rolesListDto =
	 * keycloakImpl.registerUser(userRegistrationRequestDto);
	 * assertThat(rolesListDto.getUserId(), is("112211"));
	 *
	 * }
	 */

	@Test
	/**
	 * Asserts getIndividualIdFromUserId returns the individual id attribute.
	 *
	 * @throws Exception if the call fails
	 */
	public void getIndividualIdFromUserIdTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user\",\"attributes\":{\"individualId\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		IndividualIdDto rolesListDto = keycloakImpl.getIndividualIdFromUserId("mock-user", "ida");
		assertThat(rolesListDto.getIndividualId(), is("8291930201"));
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getIndividualIdFromUserId raises AuthManagerException when the user is missing.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getIndividualIdFromUserIdAuthManagerExceptionTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user1\",\"attributes\":{\"individualId\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		IndividualIdDto rolesListDto = keycloakImpl.getIndividualIdFromUserId("mock-user", "ida");
		rolesListDto.getIndividualId();
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getIndividualIdFromUserId raises AuthManagerException on malformed JSON.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getIndividualIdFromUserIdIOTest() throws Exception {
		String userIDResp = "[\"username\": \"mock-user\",\"attributes\":{\"individualId\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));
		IndividualIdDto rolesListDto = keycloakImpl.getIndividualIdFromUserId("mock-user", "ida");
		rolesListDto.getIndividualId();
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts getIndividualIdFromUserId raises AuthManagerException when the response is null.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getIndividualIdFromUserIdNullRespTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user1\",\"attributes\":{\"individualId\":[\"8291930201\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + "?username=" + "mock-user");
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(null));
		IndividualIdDto rolesListDto = keycloakImpl.getIndividualIdFromUserId("mock-user", "ida");
		rolesListDto.getIndividualId();
	}

	@Test
	/**
	 * Asserts user search by text returns matching Keycloak users.
	 *
	 * @throws Exception if the call fails
	 */
	public void getListOfUsersDetailsSearchTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user\",\"email\": \"mock@mosip.io\",\"firstName\": \"fname\",\"lastName\": \"lname\",\"id\": \"829329\",\"attributes\":{\"mobile\":[\"8291930201\"],\"rid\":[\"728391\"],\"name\":[\"mock-name\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.REALM_ID, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(keycloakAdminUrl + users);
		uriComponentsBuilder.queryParam("email", "mock@mosip.io");
		uriComponentsBuilder.queryParam("firstName", "fname");
		uriComponentsBuilder.queryParam("lastName", "lname");
		uriComponentsBuilder.queryParam("username", "username");
		uriComponentsBuilder.queryParam("search", "search");
		uriComponentsBuilder.queryParam("first", 0);
		uriComponentsBuilder.queryParam("max", 10);
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));

		// role
		Map<String, String> rolePathParams = new HashMap<>();
		rolePathParams.put(AuthConstant.REALM_ID, "ida");
		rolePathParams.put("userId", "829329");

		UriComponentsBuilder rolePathParamsUriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + roleUserMappingurl);
		String roleResp = "[\r\n" + "  {\r\n" + "    \"name\": \"PROCESSOR\"  }\r\n" + "]";
		String roleUrl = rolePathParamsUriComponentsBuilder.buildAndExpand(rolePathParams).toString();
		when(restTemplate.exchange(Mockito.eq(roleUrl), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(String.class))).thenReturn(ResponseEntity.ok(roleResp));
		MosipUserListDto rolesListDto = keycloakImpl.getListOfUsersDetails("ida", null, 0, 10, "mock@mosip.io", "fname",
				"lname", "username", "search");
		assertThat(rolesListDto.getMosipUserDtoList().get(0).getName(), is("mock-name"));
	}

	@Test
	/**
	 * Asserts user search by role returns matching Keycloak users.
	 *
	 * @throws Exception if the call fails
	 */
	public void getListOfUsersDetailsRoleSearchTest() throws Exception {
		String userIDResp = "[{\"username\": \"mock-user\",\"email\": \"mock@mosip.io\",\"firstName\": \"fname\",\"lastName\": \"lname\",\"id\": \"829329\",\"attributes\":{\"mobile\":[\"8291930201\"],\"rid\":[\"728391\"],\"name\":[\"mock-name\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.ROLE_NAME, "PROCESSOR");
		pathParams.put(AuthConstant.REALM, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + roleBasedUsersurl);
		uriComponentsBuilder.queryParam("first", 0);
		uriComponentsBuilder.queryParam("max", 10);
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));

		// role
		Map<String, String> rolePathParams = new HashMap<>();
		rolePathParams.put(AuthConstant.REALM_ID, "ida");
		rolePathParams.put("userId", "829329");

		UriComponentsBuilder rolePathParamsUriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + roleUserMappingurl);
		String roleResp = "[\r\n" + "  {\r\n" + "    \"name\": \"PROCESSOR\"  }\r\n" + "]";
		String roleUrl = rolePathParamsUriComponentsBuilder.buildAndExpand(rolePathParams).toString();
		when(restTemplate.exchange(Mockito.eq(roleUrl), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(String.class))).thenReturn(ResponseEntity.ok(roleResp));
		MosipUserListDto rolesListDto = keycloakImpl.getListOfUsersDetails("ida", "PROCESSOR", 0, 10, "mock@mosip.io", "fname",
				"lname", "username", "search");
		assertThat(rolesListDto.getMosipUserDtoList().get(0).getName(), is("mock-name"));
	}

	@Test(expected = AuthManagerException.class)
	/**
	 * Asserts role-based user search raises AuthManagerException on malformed JSON.
	 *
	 * @throws Exception if the call fails unexpectedly
	 */
	public void getListOfUsersDetailsRoleSearchIoExceptionTest() throws Exception {
		String userIDResp = "[\"username\": \"mock-user\",\"email\": \"mock@mosip.io\",\"firstName\": \"fname\",\"lastName\": \"lname\",\"id\": \"829329\",\"attributes\":{\"mobile\":[\"8291930201\"],\"rid\":[\"728391\"],\"name\":[\"mock-name\"]} }]";
		Map<String, String> pathParams = new HashMap<>();
		pathParams.put(AuthConstant.ROLE_NAME, "PROCESSOR");
		pathParams.put(AuthConstant.REALM, "ida");
		UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + roleBasedUsersurl);
		uriComponentsBuilder.queryParam("first", 0);
		uriComponentsBuilder.queryParam("max", 10);
		when(restTemplate.exchange(Mockito.eq(uriComponentsBuilder.buildAndExpand(pathParams).toString()),
				Mockito.eq(HttpMethod.GET), Mockito.any(), Mockito.eq(String.class)))
				.thenReturn(ResponseEntity.ok(userIDResp));

		// role
		Map<String, String> rolePathParams = new HashMap<>();
		rolePathParams.put(AuthConstant.REALM_ID, "ida");
		rolePathParams.put("userId", "829329");

		UriComponentsBuilder rolePathParamsUriComponentsBuilder = UriComponentsBuilder
				.fromUriString(keycloakAdminUrl + users + roleUserMappingurl);
		String roleResp = "[\r\n" + "  {\r\n" + "    \"name\": \"PROCESSOR\"  }\r\n" + "]";
		String roleUrl = rolePathParamsUriComponentsBuilder.buildAndExpand(rolePathParams).toString();
		when(restTemplate.exchange(Mockito.eq(roleUrl), Mockito.eq(HttpMethod.GET), Mockito.any(),
				Mockito.eq(String.class))).thenReturn(ResponseEntity.ok(roleResp));
		MosipUserListDto rolesListDto = keycloakImpl.getListOfUsersDetails("ida", "PROCESSOR", 0, 10, "mock@mosip.io", "fname",
				"lname", "username", "search");
		assertThat(rolesListDto.getMosipUserDtoList().get(0).getName(), is("mock-name"));
	}

}
