package io.mosip.kernel.syncdata.test.controller;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.signatureutil.model.SignatureResponse;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.syncdata.constant.MasterDataErrorCode;
import io.mosip.kernel.syncdata.dto.ApplicationDto;
import io.mosip.kernel.syncdata.dto.HolidayDto;
import io.mosip.kernel.syncdata.dto.MachineDto;
import io.mosip.kernel.syncdata.dto.MachineSpecificationDto;
import io.mosip.kernel.syncdata.dto.MachineTypeDto;
import io.mosip.kernel.syncdata.dto.PublicKeyResponse;
import io.mosip.kernel.syncdata.dto.SyncUserDetailDto;
import io.mosip.kernel.syncdata.dto.UploadPublicKeyResponseDto;
import io.mosip.kernel.syncdata.dto.UserDetailMapDto;
import io.mosip.kernel.syncdata.dto.response.MasterDataResponseDto;
import io.mosip.kernel.syncdata.dto.response.RolesResponseDto;
import io.mosip.kernel.syncdata.exception.RequestException;
import io.mosip.kernel.syncdata.exception.SyncDataServiceException;
import io.mosip.kernel.syncdata.repository.UserDetailsRepository;
import io.mosip.kernel.syncdata.service.SyncConfigDetailsService;
import io.mosip.kernel.syncdata.service.SyncMasterDataService;
import io.mosip.kernel.syncdata.service.SyncRolesService;
import io.mosip.kernel.syncdata.service.SyncUserDetailsService;
import io.mosip.kernel.syncdata.test.TestBootApplication;
import net.minidev.json.JSONObject;

@SpringBootTest(classes = TestBootApplication.class)
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class SyncDataControllerTest {
	private MasterDataResponseDto masterDataResponseDto;
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SyncMasterDataService masterDataService;

	@MockBean
	private SyncConfigDetailsService syncConfigDetailsService;

	@MockBean
	private SyncUserDetailsService syncUserDetailsService;

	@MockBean
	private SyncRolesService syncRolesService;

	@MockBean
	private UserDetailsRepository userRepository;

	private SignatureResponse signResponse;

	@MockBean
	private SignatureUtil signingUtil;

	JSONObject globalConfigMap = null;
	JSONObject regCentreConfigMap = null;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${mosip.kernel.syncdata-service-idschema-url}")
	private String idSchemaUrl;
	
	@Autowired
	RestTemplate restTemplate;
	
	private MockRestServiceServer mockRestServer;

	@Before
	public void setup() {

		configDetialsSyncSetup();
		syncMasterDataSetup();
		getUsersBasedOnRegCenterSetUp();
		signResponse = new SignatureResponse();
		signResponse.setData("asdasdsadf4e");
		signResponse.setTimestamp(LocalDateTime.now(ZoneOffset.UTC));
		
		mockRestServer = MockRestServiceServer.bindTo(restTemplate).build();
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(idSchemaUrl);
		mockRestServer.expect(MockRestRequestMatchers.requestTo(builder.build().toString()))
		.andRespond(withSuccess().body(
				"{ \"id\": null, \"version\": null, \"responsetime\": \"2019-04-24T09:07:42.017Z\", \"metadata\": null, "
				+ "\"response\": { \"lastSyncTime\": \"2019-04-24T09:07:41.771Z\", "
				+ "\"idVersion\": 1.0, \"schema\": [{\"id\":\"IDSchemaVersion\",\"inputRequired\":false,\"type\":\"number\",\"minimum\":0,"
				+ "\"maximum\":0,\"description\":\"\",\"label\":[{\"value\":\"IDSchemaVersion\",\"language\":\"eng\"}],\"controlType\":\"none\",\"fieldType\":\"default\","
				+ "\"format\":\"none\",\"validators\":[],\"fieldCategory\":\"Pvt\",\"required\":false},{\"id\":\"UIN\",\"inputRequired\":false,"
				+ "\"type\":\"integer\",\"minimum\":0,\"maximum\":0,\"description\":\"\",\"label\":[{\"value\":\"UIN\",\"language\":\"eng\"}],\"controlType\":\"none\","
				+ "\"fieldType\":\"default\",\"format\":\"none\",\"validators\":[],\"fieldCategory\":\"Pvt\",\"required\":false},{\"id\":\"fullName\","
				+ "\"inputRequired\":true,\"type\":\"simpleType\",\"minimum\":0,\"maximum\":0,\"description\":\"\",\"label\":[{\"value\":\"Full Name\",\"language\":\"eng\"}],"
				+ "\"controlType\":\"textbox\",\"fieldType\":\"default\",\"format\":\"none\",\"validators\":[],\"fieldCategory\":\"Pvt\",\"required\":true}], "
				+ "\"schemaJson\": \"{}\", \"effectiveFrom\" : \"2019-04-24T09:07:42.017Z\"}, \"errors\": null }"));

	}

	SyncUserDetailDto syncUserDetailDto;
	List<UserDetailMapDto> users;
	UserDetailMapDto userDetailMapDto;

	public void getUsersBasedOnRegCenterSetUp() {
		List<String> roles = new ArrayList<>();
		roles.add("admin");
		roles.add("superAdmin");
		syncUserDetailDto = new SyncUserDetailDto();
		users = new ArrayList<>();
		userDetailMapDto = new UserDetailMapDto();
		userDetailMapDto.setMail("mosip@gmail.com");
		userDetailMapDto.setMobile("9988866600");
		userDetailMapDto.setName("100022");
		userDetailMapDto.setUserName("individula");
		userDetailMapDto.setRoles(roles);
		users.add(userDetailMapDto);
		syncUserDetailDto.setUserDetails(users);
	}

	public void configDetialsSyncSetup() {
		globalConfigMap = new JSONObject();
		globalConfigMap.put("archivalPolicy", "arc_policy_2");
		globalConfigMap.put("otpTimeOutInMinutes", 2);
		globalConfigMap.put("numberOfWrongAttemptsForOtp", 5);
		globalConfigMap.put("uinLength", 24);

		regCentreConfigMap = new JSONObject();

		regCentreConfigMap.put("fingerprintQualityThreshold", 120);
		regCentreConfigMap.put("irisQualityThreshold", 25);
		regCentreConfigMap.put("irisRetryAttempts", 10);
		regCentreConfigMap.put("faceQualityThreshold", 25);
		regCentreConfigMap.put("faceRetry", 12);
		regCentreConfigMap.put("supervisorVerificationRequiredForExceptions", true);
		regCentreConfigMap.put("operatorRegSubmissionMode", "fingerprint");

	}

	public void syncMasterDataSetup() {
		masterDataResponseDto = new MasterDataResponseDto();
		List<ApplicationDto> applications = new ArrayList<>();
		applications.add(new ApplicationDto("01", "REG FORM", "REG Form"));
		masterDataResponseDto.setApplications(applications);
		List<HolidayDto> holidays = new ArrayList<>();
		holidays.add(new HolidayDto("1", "2018-01-01", "01", "01", "2018", "NEW YEAR", "LOC01"));
		masterDataResponseDto.setHolidays(holidays);
		List<MachineDto> machines = new ArrayList<>();
		machines.add(new MachineDto("1001", "Laptop", "QWE23456", "1223:23:31:23", "172.12.128.1", "1",
				LocalDateTime.parse("2018-01-01T01:01:01"), null, null));
		masterDataResponseDto.setMachineDetails(machines);
		List<MachineSpecificationDto> machineSpecifications = new ArrayList<>();
		machineSpecifications
				.add(new MachineSpecificationDto("1", "lenovo Thinkpad", "Lenovo", "T480", "1", "1.0.1", "Thinkpad"));
		masterDataResponseDto.setMachineSpecification(machineSpecifications);
		List<MachineTypeDto> machineTypes = new ArrayList<>();
		machineTypes.add(new MachineTypeDto("1", "ENG", "Laptop"));
		masterDataResponseDto.setMachineType(machineTypes);
	}

	/*
	 * @Test
	 * 
	 * @WithUserDetails(value = "reg-officer") public void syncMasterDataSuccess()
	 * throws Exception { when(masterDataService.syncData(Mockito.anyString(),
	 * Mockito.isNull(), Mockito.any())) .thenReturn(masterDataResponseDto);
	 * mockMvc.perform(get("/v1.0/masterdata/{machineId}",
	 * "1001")).andExpect(status().isOk()); }
	 * 
	 * @Test
	 * 
	 * @WithUserDetails(value = "reg-officer") public void
	 * syncMasterDataWithlastUpdatedTimestampSuccess() throws Exception {
	 * when(masterDataService.syncData(Mockito.anyString(), Mockito.any(),
	 * Mockito.any())) .thenReturn(masterDataResponseDto); mockMvc.perform(get(
	 * "/v1.0/masterdata/{machineId}?lastUpdated=2018-01-01T01:01:01.021Z", "1001"))
	 * .andExpect(status().isOk()); }
	 * 
	 * @Test
	 * 
	 * @WithUserDetails(value = "reg-officer") public void
	 * syncMasterDataWithlastUpdatedTimestampfailure() throws Exception {
	 * mockMvc.perform(get(
	 * "/v1.0/masterdata/{machineId}?lastUpdated=2018-01-016501:01:01", "1001"))
	 * .andExpect(status().isOk()); }
	 */

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncGlobalConfigDetailsSuccess() throws Exception {

		when(signingUtil.sign(Mockito.anyString())).thenReturn(signResponse);
		when(syncConfigDetailsService.getGlobalConfigDetails()).thenReturn(globalConfigMap);
		mockMvc.perform(get("/globalconfigs")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncRegistrationConfigDetailsSuccess() throws Exception {
		when(signingUtil.sign(Mockito.anyString())).thenReturn(signResponse);
		when(syncConfigDetailsService.getRegistrationCenterConfigDetails(Mockito.anyString()))
				.thenReturn(globalConfigMap);
		mockMvc.perform(get("/registrationcenterconfig/1")).andExpect(status().isOk());
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void syncGlobalConfigDetailsFailure() throws Exception {
		when(syncConfigDetailsService.getGlobalConfigDetails())
				.thenThrow(new SyncDataServiceException("KER-SYNC-127", "Error occured in service"));
		mockMvc.perform(get("/globalconfigs")).andExpect(status().isInternalServerError());
	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void getUsersBasedOnRegCenter() throws Exception {
		String regId = "110044";
		when(signingUtil.sign(Mockito.anyString())).thenReturn(signResponse);
		when(syncUserDetailsService.getAllUserDetail(regId)).thenReturn(syncUserDetailDto);
		mockMvc.perform(get("/userdetails/{regid}", "110044")).andExpect(status().isOk());

	}

	@Test
	@WithUserDetails(value = "reg-officer")
	public void getUsersBasedOnRegCenterFailure() throws Exception {
		String regId = "110044";
		when(syncUserDetailsService.getAllUserDetail(regId))
				.thenThrow(new SyncDataServiceException("KER-SYNC-301", "Error occured while fetching User Details"));
		mockMvc.perform(get("/userdetails/{regid}", "110044")).andExpect(status().isInternalServerError());

	}

	// -----------------------public key-------------------------------------//

	@Test
	@WithUserDetails(value = "reg-officer")
	public void getPublicKey() throws Exception {
		PublicKeyResponse<String> publicKeyResponse = new PublicKeyResponse<>();
		publicKeyResponse.setPublicKey("aasfdsfsadfdsaf");
		when(signingUtil.sign(Mockito.anyString())).thenReturn(signResponse);
		Mockito.when(syncConfigDetailsService.getPublicKey(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
				.thenReturn(publicKeyResponse);
		mockMvc.perform(get("/publickey/REGISTRATION").param("timeStamp", "2019-09-09T09%3A00%3A00.000Z"))
				.andExpect(status().isOk());
	}

	// -----------------AllRoles-------------------------------//

	@WithUserDetails(value = "reg-officer")
	@Test
	public void getAllRoles() throws Exception {
		when(signingUtil.sign(Mockito.anyString())).thenReturn(signResponse);
		RolesResponseDto rolesResponseDto = new RolesResponseDto();
		rolesResponseDto.setLastSyncTime("2019-09-09T09:09:09.000Z");
		Mockito.when(syncRolesService.getAllRoles()).thenReturn(rolesResponseDto);
		mockMvc.perform(get("/roles")).andExpect(status().isOk());
	}
	
	
	// -----------------AllRoles-------------------------------//

	@WithUserDetails(value = "reg-officer")
	@Test
	public void validateMachineMapping() throws Exception {
		UploadPublicKeyResponseDto resp = new UploadPublicKeyResponseDto("keyindex");
		when(masterDataService.validateKeyMachineMapping(Mockito.any())).thenReturn(resp);		
		mockMvc.perform(post("/tpm/publickey/verify").contentType(MediaType.APPLICATION_JSON)
				.content("{\"machineName\":\"name\",\"publicKey\":\"key\"}")).andExpect(status().isOk());
	}
	
	@WithUserDetails(value = "reg-officer")
	@Test
	public void validateMachineMappingWithInvalidInput() throws Exception {
		MvcResult result = mockMvc.perform(post("/tpm/publickey/verify").contentType(MediaType.APPLICATION_JSON)
		.content("{\"machineName\":\"name\",\"publicKey\":null}")).andExpect(status().isOk()).andReturn();
		
		ResponseWrapper<UploadPublicKeyResponseDto> responseWrapper = objectMapper.readValue(result.getResponse().getContentAsString(),
				new TypeReference<ResponseWrapper<UploadPublicKeyResponseDto>>() {});
		
		assertEquals(MasterDataErrorCode.INVALID_INPUT_REQUEST.getErrorCode(), responseWrapper.getErrors().get(0).getErrorCode());
	}
	
	@WithUserDetails(value = "reg-officer")
	@Test
	public void validateMachineMappingException() throws Exception {
		when(masterDataService.validateKeyMachineMapping(Mockito.any())).thenThrow(RequestException.class);
		mockMvc.perform(post("/tpm/publickey/verify").contentType(MediaType.APPLICATION_JSON)
				.content("{\"machineName\":\"name\",\"publicKey\":\"key\"}")).andExpect(status().isOk());
	}
	
	@WithUserDetails(value = "reg-officer")
	@Test
	public void validateLatestSchemaSync() throws Exception {
		when(signingUtil.sign(Mockito.anyString())).thenReturn(signResponse);
		mockMvc.perform(get("/latestidschema").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}
	
}