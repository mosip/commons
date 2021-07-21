package io.mosip.kernel.zkcryptoservice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.zkcryptoservice.dto.ReEncryptRandomKeyResponseDto;
import io.mosip.kernel.zkcryptoservice.dto.ZKCryptoRequestDto;
import io.mosip.kernel.zkcryptoservice.dto.ZKCryptoResponseDto;
import io.mosip.kernel.zkcryptoservice.service.spi.ZKCryptoManagerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * Rest Controller for Zero Knowledge Crypto-Manager-Service
 * 
 * @author Mahammed Taheer
 *
 * @since 1.1.2
 */

@CrossOrigin
@RestController
@Api(value = "Operation related to Zero Knowledge Encryption and Decryption", tags = { "zkcryptomanager" })
public class ZKCryptoManagerController {
	
	/**
	 * Instance for KeymanagerService
	 */
	@Autowired
	ZKCryptoManagerService zkCryptoManagerService;
	
    /**
	 * Controller for Encrypt the data
	 * 
	 * @param zkCryptoRequestDto {@link ZKCryptoRequestDto} request
	 * @return {@link ZKCryptoResponseDto} encrypted Data
	 */
	//@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL','ID_AUTHENTICATION','TEST', 'REGISTRATION_ADMIN', 'REGISTRATION_SUPERVISOR', 'REGISTRATION_OFFICER', 'REGISTRATION_PROCESSOR','PRE_REGISTRATION_ADMIN','RESIDENT')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostzkencrypt())")
	@PostMapping(value = "/zkEncrypt", produces = "application/json")
	public ResponseWrapper<ZKCryptoResponseDto> zkEncrypt(
			@ApiParam("List of ZK Data Attributes to Encrypt.") @RequestBody @Valid RequestWrapper<ZKCryptoRequestDto> zkCryptoRequestDto) {

		ResponseWrapper<ZKCryptoResponseDto> response = new ResponseWrapper<>();
		response.setResponse(zkCryptoManagerService.zkEncrypt(zkCryptoRequestDto.getRequest()));
		return response;
	}

	/**
	 * Controller for Decrypt the data
	 * 
	 * @param zkCryptoRequestDto {@link ZKCryptoRequestDto} request
	 * @return {@link ZKCryptoResponseDto} decrypted Data
	 */
	//@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL','ID_AUTHENTICATION', 'TEST', 'REGISTRATION_ADMIN', 'REGISTRATION_SUPERVISOR', 'REGISTRATION_OFFICER', 'REGISTRATION_PROCESSOR','PRE_REGISTRATION_ADMIN','RESIDENT')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostzkdecrypt())")
	@PostMapping(value = "/zkDecrypt", produces = "application/json")
	public ResponseWrapper<ZKCryptoResponseDto> zkDecrypt(
			@ApiParam("List of ZK Data Attributes to Decrypt.") @RequestBody @Valid RequestWrapper<ZKCryptoRequestDto> zkCryptoRequestDto) {
		ResponseWrapper<ZKCryptoResponseDto> response = new ResponseWrapper<>();
		response.setResponse(zkCryptoManagerService.zkDecrypt(zkCryptoRequestDto.getRequest()));
		return response;
    }
    

    /**
	 * Controller for Decrypt the data
	 * 
	 * @param cryptomanagerRequestDto {@link CryptomanagerRequestDto} request
	 * @return {@link CryptomanagerResponseDto} decrypted Data
	 */
	//@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL','ID_AUTHENTICATION', 'TEST', 'REGISTRATION_ADMIN', 'REGISTRATION_SUPERVISOR', 'REGISTRATION_OFFICER', 'REGISTRATION_PROCESSOR','PRE_REGISTRATION_ADMIN','RESIDENT')")
	@ResponseFilter
	@PreAuthorize("hasAnyRole(@authorizedRoles.getPostzkreencryptrandomkey())")
	@PostMapping(value = "/zkReEncryptRandomKey", produces = "application/json")
	public ResponseWrapper<ReEncryptRandomKeyResponseDto> zkReEncryptRandomKey(
		@ApiParam("Random key to re-encrypt") @RequestParam("encryptedKey") String encryptedKey) {

		ResponseWrapper<ReEncryptRandomKeyResponseDto> response = new ResponseWrapper<>();
		response.setResponse(zkCryptoManagerService.zkReEncryptRandomKey(encryptedKey));
		return response;
	}
}