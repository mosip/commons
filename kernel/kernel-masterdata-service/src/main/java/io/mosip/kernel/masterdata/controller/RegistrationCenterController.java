package io.mosip.kernel.masterdata.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.OrderEnum;
import io.mosip.kernel.masterdata.dto.PageDto;
import io.mosip.kernel.masterdata.dto.RegCenterPostReqDto;
import io.mosip.kernel.masterdata.dto.RegCenterPutReqDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterHolidayDto;
import io.mosip.kernel.masterdata.dto.getresponse.RegistrationCenterResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResgistrationCenterStatusResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.RegistrationCenterExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.dto.response.RegistrationCenterSearchDto;
import io.mosip.kernel.masterdata.service.RegistrationCenterService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

/**
 * This controller class provides registration centers details based on user
 * provided data.
 * 
 * @author Dharmesh Khandelwal
 * @author Abhishek Kumar
 * @author Urvil Joshi
 * @author Ritesh Sinha
 * @author Sagar Mahapatra
 * @author Sidhant Agarwal
 * @author Srinivasan
 * @author Uday Kumar
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@RestController
@Api(tags = { "Registration Center" })
public class RegistrationCenterController {

	/**
	 * Reference to RegistrationCenterService.
	 */
	@Autowired
	RegistrationCenterService registrationCenterService;

	@Autowired
	private AuditUtil auditUtil;

	/**
	 * Function to fetch registration centers list using location code and language
	 * code.
	 * 
	 * @param langCode     language code for which the registration center needs to
	 *                     be searched.
	 * @param locationCode location code for which the registration center needs to
	 *                     be searched.
	 * @return {@link RegistrationCenterResponseDto} RegistrationCenterResponseDto
	 */
	@ResponseFilter
	@GetMapping("/getlocspecificregistrationcenters/{langcode}/{locationcode}")
	public ResponseWrapper<RegistrationCenterResponseDto> getRegistrationCenterDetailsByLocationCode(
			@PathVariable("langcode") String langCode, @PathVariable("locationcode") String locationCode) {

		ResponseWrapper<RegistrationCenterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				registrationCenterService.getRegistrationCentersByLocationCodeAndLanguageCode(locationCode, langCode));
		return responseWrapper;
	}

	/**
	 * Function to fetch specific registration center holidays by registration
	 * center id , year and language code.
	 * 
	 * @param langCode             langCode of required center.
	 * @param registrationCenterId centerId of required center
	 * @param year                 the year provided by user.
	 * @return {@link RegistrationCenterHolidayDto} RegistrationCenterHolidayDto
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','PRE_REGISTRATION_ADMIN','REGISTRATION_SUPERVISOR')")
	@ResponseFilter
	@GetMapping("/getregistrationcenterholidays/{langcode}/{registrationcenterid}/{year}")
	public ResponseWrapper<RegistrationCenterHolidayDto> getRegistrationCenterHolidays(
			@PathVariable("langcode") String langCode,
			@PathVariable("registrationcenterid") String registrationCenterId, @PathVariable("year") int year) {

		ResponseWrapper<RegistrationCenterHolidayDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				registrationCenterService.getRegistrationCenterHolidays(registrationCenterId, year, langCode));
		return responseWrapper;
	}

	/**
	 * Function to fetch nearby registration centers using coordinates
	 * 
	 * @param langCode          langCode of required centers.
	 * @param longitude         the longitude provided by user.
	 * @param latitude          the latitude provided by user.
	 * @param proximityDistance the proximity distance provided by user.
	 * @return {@link RegistrationCenterResponseDto} RegistrationCenterResponseDto
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@ResponseFilter
	@GetMapping("/getcoordinatespecificregistrationcenters/{langcode}/{longitude}/{latitude}/{proximitydistance}")
	public ResponseWrapper<RegistrationCenterResponseDto> getCoordinateSpecificRegistrationCenters(
			@PathVariable("langcode") String langCode, @PathVariable("longitude") double longitude,
			@PathVariable("latitude") double latitude, @PathVariable("proximitydistance") int proximityDistance) {

		ResponseWrapper<RegistrationCenterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService.getRegistrationCentersByCoordinates(longitude, latitude,
				proximityDistance, langCode));
		return responseWrapper;
	}

	/**
	 * Function to fetch registration center using centerId and language code.
	 * 
	 * @param registrationCenterId centerId of required center.
	 * @param langCode             langCode of required center.
	 * @return {@link RegistrationCenterResponseDto} RegistrationCenterResponseDto
	 */
	@ResponseFilter
	@GetMapping("/registrationcenters/{id}/{langcode}")
	public ResponseWrapper<RegistrationCenterResponseDto> getSpecificRegistrationCenterById(
			@PathVariable("id") String registrationCenterId, @PathVariable("langcode") String langCode) {
		ResponseWrapper<RegistrationCenterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				registrationCenterService.getRegistrationCentersByIDAndLangCode(registrationCenterId, langCode));
		return responseWrapper;
	}

	/**
	 * Function to fetch all registration centers.
	 * 
	 * @return {@link RegistrationCenterResponseDto} RegistrationCenterResponseDto
	 */
	@ResponseFilter
	@GetMapping("/registrationcenters")
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','ZONAL_APPROVER','INDIVIDUAL','PRE_REGISTRATION_ADMIN')")
	public ResponseWrapper<RegistrationCenterResponseDto> getAllRegistrationCentersDetails() {
		ResponseWrapper<RegistrationCenterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService.getAllRegistrationCenters());
		return responseWrapper;
	}

	/**
	 * Function to fetch list of registration centers based on hierarchy level,text
	 * and language code
	 * 
	 * @param langCode       input from user
	 * @param hierarchyLevel input from user
	 * @param name           input from user
	 * @return {@link RegistrationCenterResponseDto} RegistrationCenterResponseDto
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@ResponseFilter
	@GetMapping("/registrationcenters/{langcode}/{hierarchylevel}/{name}")
	public ResponseWrapper<RegistrationCenterResponseDto> getRegistrationCenterByHierarchyLevelAndTextAndlangCode(
			@PathVariable("langcode") String langCode, @PathVariable("hierarchylevel") Short hierarchyLevel,
			@PathVariable("name") String name) {

		ResponseWrapper<RegistrationCenterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService
				.findRegistrationCenterByHierarchyLevelandTextAndLanguageCode(langCode, hierarchyLevel, name));
		return responseWrapper;

	}

	/**
	 * Check whether the time stamp sent for the given registration center id is not
	 * a holiday and is in between working hours.
	 * 
	 * @param regId     - registration center id
	 * @param langCode  - language code
	 * @param timeStamp - timestamp based on the format YYYY-MM-ddTHH:mm:ss.SSSZ
	 * @return {@link ResgistrationCenterStatusResponseDto} -
	 *         RegistrationCenterStatusResponseDto
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@GetMapping("/registrationcenters/validate/{id}/{langCode}/{timestamp}")
	public ResponseWrapper<ResgistrationCenterStatusResponseDto> validateTimestamp(@PathVariable("id") String regId,
			@PathVariable("langCode") String langCode, @PathVariable("timestamp") String timeStamp) {

		ResponseWrapper<ResgistrationCenterStatusResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				registrationCenterService.validateTimeStampWithRegistrationCenter(regId, langCode, timeStamp));
		return responseWrapper;
	}

	@ResponseFilter
	@DeleteMapping("/registrationcenters/{registrationCenterId}")
	public ResponseWrapper<IdResponseDto> deleteRegistrationCenter(
			@PathVariable("registrationCenterId") String registrationCenterId) {

		ResponseWrapper<IdResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService.deleteRegistrationCenter(registrationCenterId));
		return responseWrapper;
	}

	/**
	 * Function to fetch list of registration centers based on hierarchy level,List
	 * of text and language code
	 * 
	 * @param langCode       input from user
	 * @param hierarchyLevel input from user
	 * @param names          input from user
	 * @return {@link RegistrationCenterResponseDto} RegistrationCenterResponseDto
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL')")
	@ResponseFilter
	@GetMapping("/registrationcenters/{langcode}/{hierarchylevel}/names")
	public ResponseWrapper<RegistrationCenterResponseDto> getRegistrationCenterByHierarchyLevelAndListTextAndlangCode(
			@PathVariable("langcode") String langCode, @PathVariable("hierarchylevel") Short hierarchyLevel,
			@RequestParam("name") List<String> names) {

		ResponseWrapper<RegistrationCenterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService
				.findRegistrationCenterByHierarchyLevelAndListTextAndlangCode(langCode, hierarchyLevel, names));
		return responseWrapper;
	}

	/**
	 * Function to fetch all registration centers.
	 * 
	 * @return {@link RegistrationCenterResponseDto} RegistrationCenterResponseDto
	 */
	@ResponseFilter
	@GetMapping("/registrationcenters/all")
	@PreAuthorize("hasRole('ZONAL_ADMIN')")
	public ResponseWrapper<PageDto<RegistrationCenterExtnDto>> getAllExistingRegistrationCentersDetails(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page no for the requested data", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size for the requested data", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdDateTime") @ApiParam(value = "sort the requested data based on param value", defaultValue = "createdDateTime") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "order the requested data based on param", defaultValue = "desc") OrderEnum orderBy) {
		ResponseWrapper<PageDto<RegistrationCenterExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService.getAllExistingRegistrationCenters(pageNumber, pageSize,
				sortBy, orderBy.name()));
		return responseWrapper;
	}

	/**
	 * Api to search the registration center based on the search input
	 * 
	 * @param request search input for registration center search
	 * @return list of registration center  
	 */
	@ResponseFilter
	@PostMapping("/registrationcenters/search")
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	public ResponseWrapper<PageResponseDto<RegistrationCenterSearchDto>> searchRegistrationCenter(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SEARCH_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SEARCH_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				"ADM-517");
		ResponseWrapper<PageResponseDto<RegistrationCenterSearchDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService.searchRegistrationCenter(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH, RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_SEARCH_DESC,
						RegistrationCenterSearchDto.class.getSimpleName()),
				"ADM-518");
		return responseWrapper;
	}

	/**
	 * Api to filter Registration Center based on column and type provided.
	 * 
	 * @param request the request DTO.
	 * @return the {@link FilterResponseDto}.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@PostMapping("/registrationcenters/filtervalues")
	public ResponseWrapper<FilterResponseDto> registrationCenterFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.FILTER_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.FILTER_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				"ADM-519");
		ResponseWrapper<FilterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService.registrationCenterFilterValues(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_FILTER, RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_FILTER_DESC,
						RegistrationCenterSearchDto.class.getSimpleName()),
				"ADM-520");
		return responseWrapper;
	}

	/**
	 * This method creates registration center by Admin.
	 * 
	 * @param reqRegistrationCenterDto the request DTO for creating registration
	 *                                 center.
	 * @return RegistrationCenterPostResponseDto return the created registration
	 *         center DTO.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@PostMapping("/registrationcenters")
	public ResponseWrapper<RegistrationCenterExtnDto> createRegistrationCenter(
			@RequestBody @Valid RequestWrapper<RegCenterPostReqDto> reqRegistrationCenterDto) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.CREATE_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.CREATE_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				"ADM-521");
		ResponseWrapper<RegistrationCenterExtnDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(registrationCenterService.createRegistrationCenter(reqRegistrationCenterDto.getRequest()));

		return responseWrapper;
	}

	/**
	 * This method updates registration center by Admin.
	 * 
	 * @param reqRegistrationCenterDto the request DTO for updating registration
	 *                                 center.
	 * @return the response i.e. the id of the registration center updated.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@PutMapping("/registrationcenters")
	public ResponseWrapper<RegistrationCenterExtnDto> updateRegistrationCenterAdmin(
			@RequestBody @Valid RequestWrapper<RegCenterPutReqDto> reqRegistrationCenterDto) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.UPDATE_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.UPDATE_API_IS_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				"ADM-522");
		ResponseWrapper<RegistrationCenterExtnDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(registrationCenterService.updateRegistrationCenter(reqRegistrationCenterDto.getRequest()));
		return responseWrapper;
	}

	/**
	 * API to decommission registration center based on ID.
	 * 
	 * @param regCenterID the center ID of the reg-center which needs to be
	 *                    decommissioned.
	 * @return ID Response : returns the ID in response, if the reg-center gets
	 *         decommissioned
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@PutMapping("/registrationcenters/decommission/{regCenterID}")
	public ResponseWrapper<IdResponseDto> decommissionRegCenter(@PathVariable("regCenterID") String regCenterID) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.DECOMMISION_API_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.DECOMMISION_API_CALLED,
						RegistrationCenterSearchDto.class.getSimpleName()),
				"ADM-523");
		ResponseWrapper<IdResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterService.decommissionRegCenter(regCenterID));
		return responseWrapper;
	}
}
