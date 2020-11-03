package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.LanguageDto;
import io.mosip.kernel.masterdata.dto.getresponse.LanguageResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.service.LanguageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This class provide services to system to do CRUD operations on Languages.
 * 
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@RestController
@RequestMapping("/languages")
@Api(tags = { "Language" })
public class LanguageController {

	/**
	 * Service provide CRUD operation over Languages.
	 */
	@Autowired
	private LanguageService languageService;

	@PreAuthorize("hasAnyRole('INDIVIDUAL','ID_AUTHENTICATION', 'REGISTRATION_SUPERVISOR', 'REGISTRATION_OFFICER', 'REGISTRATION_PROCESSOR','ZONAL_ADMIN','PRE_REGISTRATION','RESIDENT','GLOBAL_ADMIN','PARTNER','AUTH_PARTNER','PARTNER_ADMIN','DEVICE_PROVIDER','DEVICE_MANAGER')")
	@ResponseFilter
	@GetMapping
	@ApiOperation(value = "Retrieve all Languages", notes = "Retrieve all Languages")
	@ApiResponses({ @ApiResponse(code = 200, message = "When all Language retrieved from database"),
			@ApiResponse(code = 404, message = "When No Language found"),
			@ApiResponse(code = 500, message = "While retrieving Language any error occured") })
	public ResponseWrapper<LanguageResponseDto> getAllLaguages() {
		ResponseWrapper<LanguageResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(languageService.getAllLaguages());
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@PostMapping
	@ApiOperation(value = "Service to save Language", notes = "Saves Language and return Language code")
	@ApiResponses({ @ApiResponse(code = 201, message = "When Language successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating Language any error occured") })
	public ResponseWrapper<CodeResponseDto> saveLanguage(@Valid @RequestBody RequestWrapper<LanguageDto> language) {
		ResponseWrapper<CodeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(languageService.saveLanguage(language.getRequest()));
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@PutMapping
	@ApiOperation(value = "Service to update Language", notes = "Update Language and return Language code")
	@ApiResponses({ @ApiResponse(code = 200, message = "When Language successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Language found"),
			@ApiResponse(code = 500, message = "While updating Language any error occured") })
	public ResponseWrapper<CodeResponseDto> updateLanguage(@Valid @RequestBody RequestWrapper<LanguageDto> language) {
		ResponseWrapper<CodeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(languageService.updateLanguage(language.getRequest()));
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@DeleteMapping("/{code}")
	@ApiOperation(value = "Service to delete Language", notes = "Delete Language and return Language code")
	@ApiResponses({ @ApiResponse(code = 200, message = "When Language successfully deleted"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Language found"),
			@ApiResponse(code = 500, message = "While deleting Language any error occured") })
	public ResponseWrapper<CodeResponseDto> deleteLanguage(@PathVariable("code") String code) {
		ResponseWrapper<CodeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(languageService.deleteLanguage(code));
		return responseWrapper;
	}

}
