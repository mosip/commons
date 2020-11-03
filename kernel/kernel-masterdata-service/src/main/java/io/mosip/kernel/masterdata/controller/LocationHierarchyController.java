package io.mosip.kernel.masterdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.LocationHierarchyLevelResponseDto;
import io.mosip.kernel.masterdata.dto.ModuleResponseDto;
import io.mosip.kernel.masterdata.service.LocationHierarchyService;
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
@Api(tags = { "Location Hierarchy level Details" })
public class LocationHierarchyController {

	@Autowired
	LocationHierarchyService locationHierarchyService;

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

	@ResponseFilter
	@GetMapping(value = "/locationHierarchyLevels/{level}/{langcode}")
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL','PRE_REGISTRATION','REGISTRATION_PROCESSOR')")
	@ApiOperation(value = "Retrieve all Module Details for given Languge Code", notes = "Retrieve all Location Hierarchy Level name for given Languge Code and level")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Location Hierarchy Level name retrieved from database for the given Languge Code and id"),
			@ApiResponse(code = 404, message = "When No Location Hierarchy Level name found for the given Languge Code and id"),
			@ApiResponse(code = 500, message = "While retrieving Location Hierarchy Level name any error occured") })
	public ResponseWrapper<LocationHierarchyLevelResponseDto> getLocationHierarchyLevelAndLangCode(
			@PathVariable("level") short level, @PathVariable("langcode") String langCode) {

		ResponseWrapper<LocationHierarchyLevelResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.getLocationHierarchyLevelAndLangCode(level, langCode));
		return responseWrapper;
	}

	/**
	 * 
	 * Function to fetch location Hierarchy Levels details based on the given
	 * language code.
	 * 
	 * @param langCode
	 *            pass language code as String
	 * @return LocationHierarchyLevelResponseDto Location Hierarchy Levels from DB
	 *         {@link LocationHierarchyLevelResponseDto}
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','INDIVIDUAL','PRE_REGISTRATION','REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@GetMapping(value = "/locationHierarchyLevels/{langcode}")
	@ApiOperation(value = "Retrieve all location Hierarchy Level details based on lanuage code ", notes = "Retrieve all location Hierarchy Levels ")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When location Hierarchy Levels details based on lanuage code retrieved"),
			@ApiResponse(code = 404, message = "When No location Hierarchy Levels found "),
			@ApiResponse(code = 500, message = "While retrieving location Hierarchy Levels any error occured") })
	public ResponseWrapper<LocationHierarchyLevelResponseDto> getModuleLangCode(
			@PathVariable("langcode") String langCode) {

		ResponseWrapper<LocationHierarchyLevelResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.getLocationHierarchyLangCode(langCode));
		return responseWrapper;
	}

}
