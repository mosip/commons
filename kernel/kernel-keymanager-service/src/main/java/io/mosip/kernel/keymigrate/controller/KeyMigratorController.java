package io.mosip.kernel.keymigrate.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyRequestDto;
import io.mosip.kernel.keymigrate.dto.KeyMigrateBaseKeyResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateCertficateResponseDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateRequestDto;
import io.mosip.kernel.keymigrate.dto.ZKKeyMigrateResponseDto;
import io.mosip.kernel.keymigrate.service.spi.KeyMigratorService;
import io.swagger.annotations.Api;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.swagger.annotations.ApiParam;

/**
 * Rest Controller for Key Migration from one HSM to another HSM.
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.6
 */

@Lazy
@CrossOrigin
@RestController
@Api(value = "Operation related to Key Migration from one HSM to another HSM.", tags = { "keymigrator" })
public class KeyMigratorController {
    
    /**
	 * Instance for KeyMigratorService
	 */
	@Autowired
	KeyMigratorService keyMigratorService;
	
    /**
	 * Controller for migrating base key.
	 * 
	 * @param migrateBaseKeyRequestDto {@link KeyMigrateBaseKeyRequestDto} request
	 * @return {@link KeyMigrateBaseKeyAddResponseDto} migrate response
	 */
	@PreAuthorize("hasAnyRole('KEY_MIGRATION_ADMIN')")
	@ResponseFilter
	@PostMapping(value = "/migrateBaseKey", produces = "application/json")
	public ResponseWrapper<KeyMigrateBaseKeyResponseDto> migrateBaseKey(
			@ApiParam("Base Key Migrate Attributes.") @RequestBody @Valid RequestWrapper<KeyMigrateBaseKeyRequestDto> migrateBaseKeyRequestDto) {

		ResponseWrapper<KeyMigrateBaseKeyResponseDto> response = new ResponseWrapper<>();
		response.setResponse(keyMigratorService.migrateBaseKey(migrateBaseKeyRequestDto.getRequest()));
		return response;
	}

	/**
	 * Controller to get the certificate for migrating ZK keys.
	 * 
	 * @param migrateBaseKeyRequestDto {@link KeyMigrateBaseKeyRequestDto} request
	 * @return {@link KeyMigrateBaseKeyAddResponseDto} migrate response
	 */
	@PreAuthorize("hasAnyRole('KEY_MIGRATION_ADMIN')")
	@ResponseFilter
	@GetMapping(value = "/getZKTempCertificate", produces = "application/json")
	public ResponseWrapper<ZKKeyMigrateCertficateResponseDto> getZKTempCertificate() {

		ResponseWrapper<ZKKeyMigrateCertficateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(keyMigratorService.getZKTempCertificate());
		return response;
	}

	/**
	 * Controller for migrating ZK keys.
	 * 
	 * @param migrateZKKeysRequestDto {@link ZKKeyMigrateRequestDto} request
	 * @return {@link ZKKeyMigrateResponseDto} migrate response
	 */
	@PreAuthorize("hasAnyRole('KEY_MIGRATION_ADMIN')")
	@ResponseFilter
	@PostMapping(value = "/migrateZKKeys", produces = "application/json")
	public ResponseWrapper<ZKKeyMigrateResponseDto> migrateZKKeys(
			@ApiParam("ZK Keys Migrate Attributes.") @RequestBody @Valid RequestWrapper<ZKKeyMigrateRequestDto> migrateZKKeysRequestDto) {

		ResponseWrapper<ZKKeyMigrateResponseDto> response = new ResponseWrapper<>();
		response.setResponse(keyMigratorService.migrateZKKeys(migrateZKKeysRequestDto.getRequest()));
		return response;
	}
}
