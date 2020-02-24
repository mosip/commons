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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.OrderEnum;
import io.mosip.kernel.masterdata.dto.LocationCreateDto;
import io.mosip.kernel.masterdata.dto.LocationDto;
import io.mosip.kernel.masterdata.dto.LocationLevelResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationHierarchyResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.LocationResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.StatusResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.BlacklistedWordsExtnDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.LocationExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.CodeResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.LocationPostResponseDto;
import io.mosip.kernel.masterdata.dto.response.LocationPutResponseDto;
import io.mosip.kernel.masterdata.dto.response.LocationSearchDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.service.LocationService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * Class handles REST calls with appropriate URLs.Service class
 * {@link LocationService} is called wherein the business logics are handled.
 * 
 * @author Srinivasan
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@RestController
@Api(tags = { "Location" })
@RequestMapping(value = "/locations")
public class LocationController {

	/**
	 * Creates an instance of {@link LocationService}
	 */
	@Autowired
	private LocationService locationHierarchyService;

	@Autowired
	private AuditUtil auditUtil;

	/**
	 * This API fetches all location hierachy details irrespective of the arguments.
	 * 
	 * @param langcode language code
	 * @return list of location hierarchies
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','ID_AUTHENTICATION','REGISTRATION_ADMIN', 'REGISTRATION_SUPERVISOR', 'REGISTRATION_OFFICER','REGISTRATION_PROCESSOR','ZONAL_ADMIN','ZONAL_APPROVER','RESIDENT')")
	@ResponseFilter
	@GetMapping(value = "/{langcode}")
	public ResponseWrapper<LocationHierarchyResponseDto> getLocationHierarchyDetails(@PathVariable String langcode) {
		ResponseWrapper<LocationHierarchyResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.getLocationDetails(langcode));
		return responseWrapper;
	}

	/**
	 * 
	 * @param RequestWrapper<LocationDto>
	 * 
	 * @return ResponseWrapper<LocationPostResponseDto>
	 */

	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	@ResponseFilter
	@PostMapping
	public ResponseWrapper<LocationPostResponseDto> createLocationHierarchyDetails(
			@RequestBody @Valid RequestWrapper<LocationCreateDto> locationRequestDto) {
		auditUtil.auditRequest(MasterDataConstant.CREATE_API_IS_CALLED + LocationCreateDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.CREATE_API_IS_CALLED + LocationCreateDto.class.getSimpleName(), "ADM-568");
		ResponseWrapper<LocationPostResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.createLocation(locationRequestDto.getRequest()));
		return responseWrapper;
	}

	/**
	 * 
	 * @param locationCode location code
	 * @param langCode     language code
	 * @return list of location hierarchies
	 */
	@ResponseFilter
	@GetMapping(value = "/{locationcode}/{langcode}")
	public ResponseWrapper<LocationResponseDto> getLocationHierarchyByLangCode(
			@PathVariable("locationcode") String locationCode, @PathVariable("langcode") String langCode) {

		ResponseWrapper<LocationResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.getLocationHierarchyByLangCode(locationCode, langCode));
		return responseWrapper;
	}

	/**
	 * @param hierarchyName hierarchy Name
	 * @return list of location hierarchies
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','ID_AUTHENTICATION','REGISTRATION_ADMIN', 'REGISTRATION_SUPERVISOR', 'REGISTRATION_OFFICER','REGISTRATION_PROCESSOR','RESIDENT')")
	@ResponseFilter
	@GetMapping(value = "/locationhierarchy/{hierarchyname}")
	public ResponseWrapper<LocationResponseDto> getLocationDataByHierarchyName(
			@PathVariable(value = "hierarchyname") String hierarchyName) {

		ResponseWrapper<LocationResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.getLocationDataByHierarchyName(hierarchyName));
		return responseWrapper;

	}

	/**
	 * 
	 * @param RequestWrapper<LocationDto>
	 * 
	 * @return ResponseWrapper<LocationPutResponseDto>
	 */
	@ResponseFilter
	@PutMapping
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	public ResponseWrapper<LocationPutResponseDto> updateLocationHierarchyDetails(
			@Valid @RequestBody RequestWrapper<LocationDto> locationRequestDto) {
		auditUtil.auditRequest(MasterDataConstant.UPDATE_API_IS_CALLED + LocationDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.UPDATE_API_IS_CALLED + LocationDto.class.getSimpleName(), "ADM-569");
		ResponseWrapper<LocationPutResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.updateLocationDetails(locationRequestDto.getRequest()));
		return responseWrapper;
	}

	/**
	 * This API call would update isDeleted to true when called.
	 * 
	 * @param locationCode -location code
	 * @return CodeResponseDto
	 */
	@ResponseFilter
	@DeleteMapping(value = "/{locationcode}")
	public ResponseWrapper<CodeResponseDto> deleteLocationHierarchyDetails(
			@PathVariable(value = "locationcode") String locationCode) {
		ResponseWrapper<CodeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.deleteLocationDetials(locationCode));
		return responseWrapper;
	}

	/**
	 * 
	 * @param locationCode location code
	 * @param langCode     language code
	 * @return list of location hierarchies
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','ZONAL_ADMIN')")
	@ResponseFilter
	@GetMapping(value = "immediatechildren/{locationcode}/{langcode}")
	public ResponseWrapper<LocationResponseDto> getImmediateChildrenByLocCodeAndLangCode(
			@PathVariable("locationcode") String locationCode, @PathVariable("langcode") String langCode) {

		ResponseWrapper<LocationResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(locationHierarchyService.getImmediateChildrenByLocCodeAndLangCode(locationCode, langCode));
		return responseWrapper;
	}

	/**
	 * checks whether the given location name is valid or not
	 * 
	 * @param locationName
	 * @return StatusResponseCode
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@GetMapping(value = "/validate/{locationname}")
	public ResponseWrapper<StatusResponseDto> validateLocationName(@PathVariable("locationname") String locationName) {
		ResponseWrapper<StatusResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.validateLocationName(locationName));
		return responseWrapper;

	}

	/**
	 * This controller method provides with all locations.
	 * 
	 * @param pageNumber the page number
	 * @param pageSize   the size of each page
	 * @param sortBy     the attributes by which it should be ordered
	 * @param orderBy    the order to be used
	 * 
	 * @return the response i.e. pages containing the locations.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','CENTRAL_ADMIN')")
	@ResponseFilter
	@GetMapping("/all")
	@ApiOperation(value = "Retrieve all the location with additional metadata", notes = "Retrieve all the location with the additional metadata")
	@ApiResponses({ @ApiResponse(code = 200, message = "list of location"),
			@ApiResponse(code = 500, message = "Error occured while retrieving location") })
	public ResponseWrapper<PageDto<LocationExtnDto>> getLocations(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page no for the requested data", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size for the requested data", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdDateTime") @ApiParam(value = "sort the requested data based on param value", defaultValue = "createdDateTime") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "order the requested data based on param", defaultValue = "desc") OrderEnum orderBy) {
		ResponseWrapper<PageDto<LocationExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(locationHierarchyService.getLocations(pageNumber, pageSize, sortBy, orderBy.name()));
		return responseWrapper;
	}

	/**
	 * POST API to search location
	 * 
	 * @param request input from user
	 * @return location values
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	@ResponseFilter
	@PostMapping("/search")
	public ResponseWrapper<PageResponseDto<LocationSearchDto>> searchLocation(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		auditUtil.auditRequest(MasterDataConstant.SEARCH_API_IS_CALLED + LocationSearchDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.SEARCH_API_IS_CALLED + LocationSearchDto.class.getSimpleName(), "ADM-570");
		ResponseWrapper<PageResponseDto<LocationSearchDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.searchLocation(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH, LocationSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH_DESC, LocationSearchDto.class.getSimpleName()),
				"ADM-571");
		return responseWrapper;
	}

	/**
	 * POST API to filter location
	 * 
	 * @param request input from user
	 * @return column values corresponding to entered dto
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN')")
	@ResponseFilter
	@PostMapping("/filtervalues")
	public ResponseWrapper<FilterResponseDto> locationFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		auditUtil.auditRequest(MasterDataConstant.FILTER_API_IS_CALLED + LocationDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.FILTER_API_IS_CALLED + LocationDto.class.getSimpleName(), "ADM-572");
		ResponseWrapper<FilterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.locationFilterValues(request.getRequest()));
		auditUtil.auditRequest(MasterDataConstant.SUCCESSFUL_FILTER + LocationDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.SUCCESSFUL_FILTER_DESC + LocationDto.class.getSimpleName(), "ADM-573");
		return responseWrapper;
	}

	/**
	 * This method returns a list of holidays containing a particular language code
	 * for the given country level
	 * 
	 * @param langCode input parameter language code
	 * @return {@link LocationLevelResponseDto}
	 */
	@ResponseFilter
	@GetMapping("level/{langcode}")
	public ResponseWrapper<LocationLevelResponseDto> getLocationCodeByLangCode(
			@PathVariable("langcode") String langCode) {
		ResponseWrapper<LocationLevelResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(locationHierarchyService.getLocationCodeByLangCode(langCode));
		return responseWrapper;
	}

}
