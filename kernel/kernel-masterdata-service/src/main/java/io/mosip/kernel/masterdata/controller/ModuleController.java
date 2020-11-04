package io.mosip.kernel.masterdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.ModuleResponseDto;
import io.mosip.kernel.masterdata.service.ModuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author Megha Tanga
 * @since 1.0.0
 * 
 */
@RestController
@Api(tags = { "Module Details" })
public class ModuleController {

	@Autowired
	ModuleService moduleService;

	/**
	 * 
	 * Function to fetch Module detail based on given Module id and Language code.
	 * 
	 * @param id
	 *            type code pass Module id as String
	 * @param langCode
	 *            pass language code as String
	 * @return ModuleResponseDto Module detail based on given module id and Language
	 *         code {@link ModuleResponseDto}
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@GetMapping(value = "/modules/{id}/{langcode}")
	@ApiOperation(value = "Retrieve all Module Details for given Languge Code", notes = "Retrieve all Module Detail for given Languge Code and id")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Module Details retrieved from database for the given Languge Code and id"),
			@ApiResponse(code = 404, message = "When No Module Details found for the given Languge Code and id"),
			@ApiResponse(code = 500, message = "While retrieving Module Details any error occured") })
	public ResponseWrapper<ModuleResponseDto> getModuleIdandLangCode(@PathVariable("id") String id,
			@PathVariable("langcode") String langCode) {

		ResponseWrapper<ModuleResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(moduleService.getModuleIdandLangCode(id, langCode));
		return responseWrapper;
	}

	/**
	 * 
	 * Function to fetch Module detail based on given Language code.
	 * 
	 * @param langCode
	 *            pass language code as String
	 * @return ModuleResponseDto Module detail based on given Language code
	 *         {@link ModuleResponseDto}
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@GetMapping(value = "/modules/{langcode}")
	@ApiOperation(value = "Retrieve all Module Details for given Languge Code", notes = "Retrieve all Module Detail for given Languge Code")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Module Details retrieved from database for the given Languge Code"),
			@ApiResponse(code = 404, message = "When No Module Details found for the given Languge Code"),
			@ApiResponse(code = 500, message = "While retrieving Module  Details any error occured") })
	public ResponseWrapper<ModuleResponseDto> getModuleLangCode(@PathVariable("langcode") String langCode) {

		ResponseWrapper<ModuleResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(moduleService.getModuleLangCode(langCode));
		return responseWrapper;
	}

}
