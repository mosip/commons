package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
import io.mosip.kernel.masterdata.dto.MachineTypeDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.MachineTypeExtnDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseCodeDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.MachineTypeService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * This controller class to save Machine type details.
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@RestController
@Api(tags = { "MachineType" })
public class MachineTypeController {

	/**
	 * Reference to MachineType Service.
	 */
	@Autowired
	private MachineTypeService machinetypeService;

	@Autowired
	private AuditUtil auditUtil;

	/**
	 * Post API to insert a new row of Machine Type data
	 * 
	 * @param machineType input Machine Type DTO from user
	 * 
	 * @return ResponseEntity Machine Type Code and Language Code which is
	 *         successfully inserted
	 * 
	 */
	@ResponseFilter
	@PostMapping("/machinetypes")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to save Machine Type", notes = "Saves MachineType and return  code and Languge Code")
	@ApiResponses({ @ApiResponse(code = 201, message = "When Machine Type successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating Machine Type any error occured") })
	public ResponseWrapper<CodeAndLanguageCodeID> createMachineType(
			@Valid @RequestBody RequestWrapper<MachineTypeDto> machineType) {
		auditUtil.auditRequest(MasterDataConstant.CREATE_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.CREATE_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(), "ADM-651");
		ResponseWrapper<CodeAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(machinetypeService.createMachineType(machineType.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_CREATE, MachineTypeDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC, MachineTypeDto.class.getCanonicalName()),
				"ADM-652");
		return responseWrapper;
	}
	/**
	 * Put API to update a new row of Machine Type data
	 * 
	 * @param machineType input Machine Type DTO from user
	 * 
	 * @return ResponseEntity Machine Type Code and Language Code which is
	 *         successfully inserted
	 * 
	 */
	@ResponseFilter
	@PutMapping("/machinetypes")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to save Machine Type", notes = "Saves MachineType and return  code and Languge Code")
	@ApiResponses({ @ApiResponse(code = 201, message = "When Machine Type successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating Machine Type any error occured") })
	public ResponseWrapper<CodeAndLanguageCodeID> updateMachineType(
			@Valid @RequestBody RequestWrapper<MachineTypeDto> machineType) {
		auditUtil.auditRequest(MasterDataConstant.CREATE_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.CREATE_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(), "ADM-651");
		ResponseWrapper<CodeAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(machinetypeService.updateMachineType(machineType.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_CREATE, MachineTypeDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC, MachineTypeDto.class.getCanonicalName()),
				"ADM-652");
		return responseWrapper;
	}

	/**
	 * This controller method provides with all machine types.
	 * 
	 * @param pageNumber the page number
	 * @param pageSize   the size of each page
	 * @param sortBy     the attributes by which it should be ordered
	 * @param orderBy    the order to be used
	 * @return the response i.e. pages containing the machine types.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@GetMapping("/machinetypes/all")
	@ApiOperation(value = "Retrieve all the machine types with additional metadata", notes = "Retrieve all the machine types with the additional metadata")
	@ApiResponses({ @ApiResponse(code = 200, message = "list of machine types"),
			@ApiResponse(code = 500, message = "Error occured while retrieving machine types") })
	public ResponseWrapper<PageDto<MachineTypeExtnDto>> getAllMachineTypes(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page no for the requested data", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size for the requested data", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdDateTime") @ApiParam(value = "sort the requested data based on param value", defaultValue = "createdDateTime") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "order the requested data based on param", defaultValue = "desc") OrderEnum orderBy) {
		ResponseWrapper<PageDto<MachineTypeExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(machinetypeService.getAllMachineTypes(pageNumber, pageSize, sortBy, orderBy.name()));
		return responseWrapper;
	}

	/**
	 * Api to search Machine Types.
	 * 
	 * @param request the request DTO.
	 * @return the {@link MachineTypeExtnDto}.
	 */
	@ResponseFilter
	@PostMapping("/machinetypes/search")
	@ApiOperation(value = "Api to search Machine Types")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	public ResponseWrapper<PageResponseDto<MachineTypeExtnDto>> searchMachineType(
			@ApiParam(value = "Request DTO to search Machine Types") @RequestBody @Valid RequestWrapper<SearchDto> request) {
		auditUtil.auditRequest(MasterDataConstant.SEARCH_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.SEARCH_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(), "ADM-653");
		ResponseWrapper<PageResponseDto<MachineTypeExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(machinetypeService.searchMachineType(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH, MachineTypeDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH_DESC, MachineTypeDto.class.getCanonicalName()),
				"ADM-654");
		return responseWrapper;
	}

	/**
	 * Api to filter Machine Types based on column and type provided.
	 * 
	 * @param request the request DTO.
	 * @return the {@link FilterResponseDto}.
	 */
	@ResponseFilter
	@PostMapping("/machinetypes/filtervalues")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	public ResponseWrapper<FilterResponseCodeDto> machineTypesFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		auditUtil.auditRequest(MasterDataConstant.FILTER_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.FILTER_API_IS_CALLED + MachineTypeDto.class.getCanonicalName(), "ADM-655");
		ResponseWrapper<FilterResponseCodeDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(machinetypeService.machineTypesFilterValues(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_FILTER, MachineTypeDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_FILTER_DESC, MachineTypeDto.class.getCanonicalName()),
				"ADM-656");
		return responseWrapper;
	}
}
