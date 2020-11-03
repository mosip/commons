package io.mosip.kernel.syncdata.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.validation.Valid;

import io.mosip.kernel.syncdata.dto.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.syncdata.dto.ConfigDto;
import io.mosip.kernel.syncdata.dto.IdSchemaDto;
import io.mosip.kernel.syncdata.dto.PublicKeyResponse;
import io.mosip.kernel.syncdata.dto.SyncUserDetailDto;
import io.mosip.kernel.syncdata.dto.SyncUserSaltDto;
import io.mosip.kernel.syncdata.dto.UploadPublicKeyRequestDto;
import io.mosip.kernel.syncdata.dto.UploadPublicKeyResponseDto;
import io.mosip.kernel.syncdata.service.SyncConfigDetailsService;
import io.mosip.kernel.syncdata.service.SyncMasterDataService;
import io.mosip.kernel.syncdata.service.SyncRolesService;
import io.mosip.kernel.syncdata.service.SyncUserDetailsService;
import io.mosip.kernel.syncdata.utils.LocalDateTimeUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.minidev.json.JSONObject;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Sync Handler Controller
 * 
 * 
 * @author Srinivasan
 * @author Abhishek Kumar
 * @author Bal Vikash Sharma
 * @author Megha Tanga
 * @author Urvil Joshi
 * @since 1.0.0
 */
@RestController
public class SyncDataController {
	/**
	 * Service instance {@link SyncMasterDataService}
	 */
	@Autowired
	private SyncMasterDataService masterDataService;

	/**
	 * Service instance {@link SyncConfigDetailsService}
	 */
	@Autowired
	SyncConfigDetailsService syncConfigDetailsService;

	/**
	 * Service instnace {@link SyncRolesService}
	 */
	@Autowired
	SyncRolesService syncRolesService;

	@Autowired
	SyncUserDetailsService syncUserDetailsService;

	@Autowired
	LocalDateTimeUtil localDateTimeUtil;

	/**
	 * This API method would fetch all synced global config details from server
	 * 
	 * @return JSONObject - global config response
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@ApiOperation(value = "API to sync global config details")
	@GetMapping(value = "/configs")
	public ResponseWrapper<ConfigDto> getConfigDetails() {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		ConfigDto syncConfigResponse = syncConfigDetailsService.getConfigDetails();
		syncConfigResponse.setLastSyncTime(currentTimeStamp);
		ResponseWrapper<ConfigDto> response = new ResponseWrapper<>();
		response.setResponse(syncConfigResponse);
		return response;
	}

	/**
	 * This API method would fetch all synced global config details from server
	 * 
	 * @return JSONObject - global config response
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN')")
	@ResponseFilter
	@ApiIgnore
	@ApiOperation(value = "API to sync global config details")
	@GetMapping(value = "/globalconfigs")
	public ResponseWrapper<JSONObject> getGlobalConfigDetails() {
		ResponseWrapper<JSONObject> response = new ResponseWrapper<>();
		response.setResponse(syncConfigDetailsService.getGlobalConfigDetails());
		return response;
	}

	/**
	 * * This API method would fetch all synced registration center config details
	 * from server
	 * 
	 * @param regId registration Id
	 * @return JSONObject
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@ApiIgnore
	@ApiOperation(value = "Api to get registration center configuration")
	@GetMapping(value = "/registrationcenterconfig/{registrationcenterid}")
	public ResponseWrapper<JSONObject> getRegistrationCentreConfig(
			@PathVariable(value = "registrationcenterid") String regId) {
		ResponseWrapper<JSONObject> response = new ResponseWrapper<>();
		response.setResponse(syncConfigDetailsService.getRegistrationCenterConfigDetails(regId));
		return response;
	}

	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@ApiIgnore
	@GetMapping("/configuration/{registrationCenterId}")
	public ResponseWrapper<ConfigDto> getConfiguration(
			@PathVariable("registrationCenterId") String registrationCenterId) {
		ResponseWrapper<ConfigDto> response = new ResponseWrapper<>();
		response.setResponse(syncConfigDetailsService.getConfiguration(registrationCenterId));
		return response;
	}
	
	/**
	 * 
	 * @param keyIndex     - keyIndex mapped to machine
	 * @param lastUpdated  - last sync updated time stamp
	 * @return {@link SyncDataResponseDto}
	 * @throws InterruptedException - this method will throw interrupted Exception
	 * @throws ExecutionException   - this method will throw exeution exception
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@GetMapping("/clientsettings")
	public ResponseWrapper<SyncDataResponseDto> syncClientSettings(
			@RequestParam(value = "keyindex", required = true) String keyIndex,
			@RequestParam(value = "lastupdated", required = false) String lastUpdated)
			throws InterruptedException, ExecutionException {

		LocalDateTime currentTimeStamp = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime timestamp = localDateTimeUtil.getLocalDateTimeFromTimeStamp(currentTimeStamp, lastUpdated);
		
		SyncDataResponseDto syncDataResponseDto = masterDataService.syncClientSettings(null, keyIndex,
				timestamp, currentTimeStamp);

		syncDataResponseDto.setLastSyncTime(DateUtils.formatToISOString(currentTimeStamp));

		ResponseWrapper<SyncDataResponseDto> response = new ResponseWrapper<>();
		response.setResponse(syncDataResponseDto);
		return response;
	}
	
	/**
	 * 
	 * @param keyIndex     - keyIndex mapped to machine
	 * @param regCenterId  - reg Center Id
	 * @param lastUpdated  - last sync updated time stamp
	 * @return {@link SyncDataResponseDto}
	 * @throws InterruptedException - this method will throw interrupted Exception
	 * @throws ExecutionException   - this method will throw exeution exception
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@GetMapping("/clientsettings/{regcenterid}")
	public ResponseWrapper<SyncDataResponseDto> syncClientSettingsWithRegCenterId(
			@PathVariable("regcenterid") String regCenterId,
			@RequestParam(value = "lastupdated", required = false) String lastUpdated,
			@RequestParam(value = "keyindex", required = true) String keyIndex)
			throws InterruptedException, ExecutionException {

		LocalDateTime currentTimeStamp = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime timestamp = localDateTimeUtil.getLocalDateTimeFromTimeStamp(currentTimeStamp, lastUpdated);
		
		SyncDataResponseDto syncDataResponseDto = masterDataService.syncClientSettings(regCenterId, keyIndex,
				timestamp, currentTimeStamp);

		syncDataResponseDto.setLastSyncTime(DateUtils.formatToISOString(currentTimeStamp));

		ResponseWrapper<SyncDataResponseDto> response = new ResponseWrapper<>();
		response.setResponse(syncDataResponseDto);
		return response;
	}
	

	/**
	 * API will fetch all roles from Auth server
	 * 
	 * @return RolesResponseDto
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@GetMapping("/roles")
	public ResponseWrapper<RolesResponseDto> getAllRoles() {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		RolesResponseDto rolesResponseDto = syncRolesService.getAllRoles();
		rolesResponseDto.setLastSyncTime(currentTimeStamp);
		ResponseWrapper<RolesResponseDto> response = new ResponseWrapper<>();
		response.setResponse(rolesResponseDto);
		return response;
	}

	/**
	 * API will all the userDetails from LDAP server
	 * 
	 * @param regId - registration center Id
	 * 
	 * @return UserDetailResponseDto - user detail response
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@GetMapping("/userdetails/{regid}")
	public ResponseWrapper<SyncUserDetailDto> getUserDetails(@PathVariable("regid") String regId) {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		SyncUserDetailDto syncUserDetailDto = syncUserDetailsService.getAllUserDetail(regId);
		syncUserDetailDto.setLastSyncTime(currentTimeStamp);
		ResponseWrapper<SyncUserDetailDto> response = new ResponseWrapper<>();
		response.setResponse(syncUserDetailDto);
		return response;
	}

	/**
	 * API will all the userDetails from LDAP server
	 * 
	 * @param regId - registration center Id
	 * 
	 * @return UserDetailResponseDto - user detail response
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@GetMapping("/usersalt/{regid}")
	public ResponseWrapper<SyncUserSaltDto> getUserSalts(@PathVariable("regid") String regId) {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		SyncUserSaltDto syncUserDetailDto = syncUserDetailsService.getUserSalts(regId);
		syncUserDetailDto.setLastSyncTime(currentTimeStamp);
		ResponseWrapper<SyncUserSaltDto> response = new ResponseWrapper<>();
		response.setResponse(syncUserDetailDto);
		return response;
	}

	/**
	 * Request mapping to get Public Key
	 * 
	 * @param applicationId Application id of the application requesting publicKey
	 * @param timeStamp     Timestamp of the request
	 * @param referenceId   Reference id of the application requesting publicKey
	 * @return {@link PublicKeyResponse} instance
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@GetMapping(value = "/publickey/{applicationId}")
	public ResponseWrapper<PublicKeyResponse<String>> getPublicKey(
			@ApiParam("Id of application") @PathVariable("applicationId") String applicationId,
			@ApiParam("Timestamp as metadata") @RequestParam(value = "timeStamp", required = false) String timeStamp,
			@ApiParam("Refrence Id as metadata") @RequestParam(value = "referenceId", required = false) String referenceId) {

		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		PublicKeyResponse<String> publicKeyResponse = syncConfigDetailsService.getPublicKey(applicationId, timeStamp,
				referenceId);
		publicKeyResponse.setLastSyncTime(currentTimeStamp);

		ResponseWrapper<PublicKeyResponse<String>> response = new ResponseWrapper<>();
		response.setResponse(publicKeyResponse);
		return response;
	}


	/**
	 * Verifies mapping of input public key with any machine.
	 * if valid returns corresponding keyIndex.
	 * 
	 * @param uploadPublicKeyRequestDto
	 * @return
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@PostMapping(value = "/tpm/publickey/verify", produces = "application/json")
	public ResponseWrapper<UploadPublicKeyResponseDto> validateKeyMachineMapping(
			@ApiParam("public key in BASE64 encoded") @RequestBody @Valid RequestWrapper<UploadPublicKeyRequestDto> uploadPublicKeyRequestDto) {
		ResponseWrapper<UploadPublicKeyResponseDto> response = new ResponseWrapper<>();
		response.setResponse(masterDataService.validateKeyMachineMapping(uploadPublicKeyRequestDto.getRequest()));
		return response;
	}
	
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','REGISTRATION_PROCESSOR','ID_AUTHENTICATION','RESIDENT','INDIVIDUAL','Default')")
	@ResponseFilter
	@GetMapping(value = "/latestidschema", produces = "application/json")
	public ResponseWrapper<IdSchemaDto> getLatestPublishedIdSchema(
			@RequestParam(value = "lastupdated", required = false) String lastUpdated,
			@RequestParam(value = "schemaVersion", defaultValue = "0", required = false) double schemaVersion) {
		LocalDateTime currentTimeStamp = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime timestamp = localDateTimeUtil.getLocalDateTimeFromTimeStamp(currentTimeStamp, lastUpdated);
		
		ResponseWrapper<IdSchemaDto> response = new ResponseWrapper<>();
		response.setResponse(masterDataService.getLatestPublishedIdSchema(timestamp, schemaVersion));
		return response;
	}

	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@GetMapping(value = "/getCertificate")
	public ResponseWrapper<KeyPairGenerateResponseDto> getCertificate(
			@ApiParam("Id of application") @RequestParam("applicationId") String applicationId,
			@ApiParam("Refrence Id as metadata") @RequestParam("referenceId") Optional<String> referenceId) {

		ResponseWrapper<KeyPairGenerateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(masterDataService.getCertificate(applicationId, referenceId));
		return response;
	}

	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@GetMapping(value = "/tpm/publickey/{machineId}", produces = "application/json")
	public ResponseWrapper<ClientPublicKeyResponseDto> getClientPublicKey(
			@ApiParam("Machine id") @PathVariable("machineId") String machineId) {
		ResponseWrapper<ClientPublicKeyResponseDto> response = new ResponseWrapper<>();
		response.setResponse(masterDataService.getClientPublicKey(machineId));
		return response;
	}

	/**
	 * This API method would fetch all synced global config details from server
	 *
	 * @return JSONObject - global config response
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_ADMIN','Default')")
	@ResponseFilter
	@ApiOperation(value = "API to sync global config details")
	@GetMapping(value = "/configs/{machineName}")
	public ResponseWrapper<ConfigDto> getMachineConfigDetails(@PathVariable(value = "machineName") String machineName) {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		ConfigDto syncConfigResponse = syncConfigDetailsService.getConfigDetails(machineName);
		syncConfigResponse.setLastSyncTime(currentTimeStamp);
		ResponseWrapper<ConfigDto> response = new ResponseWrapper<>();
		response.setResponse(syncConfigResponse);
		return response;
	}

}
