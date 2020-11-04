package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
import io.mosip.kernel.masterdata.dto.IndividualTypeDto;
import io.mosip.kernel.masterdata.dto.getresponse.IndividualTypeResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.IndividualTypeExtnDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.service.IndividualTypeService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * This controller class provides crud operation on individual type.
 * 
 * @author Bal Vikash Sharma
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@RestController
@RequestMapping(value = "/individualtypes")
@Api(tags = { "IndividualType" })
public class IndividualTypeController {

	@Autowired
	private IndividualTypeService individualTypeService;

	@Autowired
	private AuditUtil auditUtil;

	/**
	 * @return the all active individual type.
	 */
	@PreAuthorize("hasAnyRole('INDIVIDUAL','ZONAL_ADMIN','GLOBAL_ADMIN','REGISTRATION_PROCESSOR','PRE_REGISTRATION','PARTNER','AUTH_PARTNER','PARTNER_ADMIN','DEVICE_PROVIDER','DEVICE_MANAGER')")
	@ResponseFilter
	@GetMapping
	@ApiOperation(value = "get value from Caretory for the given id", notes = "get value from Category for the given id")
	public ResponseWrapper<IndividualTypeResponseDto> getAllIndividualTypes() {
		ResponseWrapper<IndividualTypeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(individualTypeService.getAllIndividualTypes());
		return responseWrapper;
	}

	/**
	 * This controller method provides with all individual types.
	 * 
	 * @param pageNumber the page number
	 * @param pageSize   the size of each page
	 * @param sortBy     the attributes by which it should be ordered
	 * @param orderBy    the order to be used
	 * 
	 * @return the response i.e. pages containing the individual types.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN','CENTRAL_ADMIN','REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@GetMapping("/all")
	@ApiOperation(value = "Retrieve all the individual types with additional metadata", notes = "Retrieve all the individual types with the additional metadata")
	@ApiResponses({ @ApiResponse(code = 200, message = "list of individual types"),
			@ApiResponse(code = 500, message = "Error occured while retrieving individual types") })
	public ResponseWrapper<PageDto<IndividualTypeExtnDto>> getIndividualTypes(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page no for the requested data", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size for the requested data", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdDateTime") @ApiParam(value = "sort the requested data based on param value", defaultValue = "createdDateTime") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "order the requested data based on param", defaultValue = "desc") OrderEnum orderBy) {
		ResponseWrapper<PageDto<IndividualTypeExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(individualTypeService.getIndividualTypes(pageNumber, pageSize, sortBy, orderBy.name()));
		return responseWrapper;
	}

	/**
	 * API to search Individual Types.
	 * 
	 * @param request the request DTO {@link SearchDto} wrapped in
	 *                {@link RequestWrapper}.
	 * @return the response i.e. multiple entities based on the search values
	 *         required.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@PostMapping("/search")
	public ResponseWrapper<PageResponseDto<IndividualTypeExtnDto>> searchIndividuals(
			@RequestBody @Valid RequestWrapper<SearchDto> request) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SEARCH_API_IS_CALLED, IndividualTypeDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SEARCH_API_IS_CALLED, IndividualTypeDto.class.getSimpleName()),
				"ADM-589");
		ResponseWrapper<PageResponseDto<IndividualTypeExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(individualTypeService.searchIndividuals(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH, IndividualTypeDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH_DESC, IndividualTypeDto.class.getSimpleName()),
				"ADM-590");
		return responseWrapper;
	}

	/**
	 * API that returns the values required for the column filter columns.
	 * 
	 * @param request the request DTO {@link FilterResponseDto} wrapper in
	 *                {@link RequestWrapper}.
	 * @return the response i.e. the list of values for the specific filter column
	 *         name and type.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@PostMapping("/filtervalues")
	public ResponseWrapper<FilterResponseDto> individualsFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> requestWrapper) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.FILTER_API_IS_CALLED, IndividualTypeDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.FILTER_API_IS_CALLED, IndividualTypeDto.class.getSimpleName()),
				"ADM-591");
		ResponseWrapper<FilterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(individualTypeService.individualsFilterValues(requestWrapper.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_FILTER, IndividualTypeDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_FILTER_DESC, IndividualTypeDto.class.getSimpleName()),
				"ADM-592");
		return responseWrapper;
	}
	
	/**
	 * create Individual Type
	 * 
	 * 
	 * @param individualType
	 * @return
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@PostMapping
	@ApiOperation(value = "Service to create Individual Type", notes = "create Individual Type  and return  code and LangCode")
	@ApiResponses({ @ApiResponse(code = 201, message = " successfully created"),
			@ApiResponse(code = 400, message = " Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = " creating any error occured") })
	public ResponseWrapper<IndividualTypeExtnDto> createIndividualType(
			@Valid @RequestBody RequestWrapper<IndividualTypeDto> individualType) {

		ResponseWrapper<IndividualTypeExtnDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(individualTypeService.createIndividualsTypes(individualType.getRequest()));
		return responseWrapper;
	}
	/**
	 * This method updates IndividualType by Admin.
	 * 
	 * @param individualTypeDto the request DTO for updating machine.
	 * @return the response i.e. the updated machine.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@PutMapping()
	@ApiOperation(value = "Service to upadte IndividualType", notes = "Update IndividualType Detail and return updated IndividualType")
	@ApiResponses({ @ApiResponse(code = 201, message = "When IndividualType successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No IndividualType found"),
			@ApiResponse(code = 500, message = "While updating IndividualType any error occured") })
	public ResponseWrapper<IndividualTypeExtnDto> updateIndividualType(
			@RequestBody @Valid RequestWrapper<IndividualTypeDto> individualTypeDto) {
		ResponseWrapper<IndividualTypeExtnDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(individualTypeService.updateIndividualsTypes(individualTypeDto.getRequest()));
		return responseWrapper;
	}
}
