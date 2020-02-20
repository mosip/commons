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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.OrderEnum;
import io.mosip.kernel.masterdata.dto.MachineSpecificationDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.MachineSpecificationExtnDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.dto.request.FilterValueDto;
import io.mosip.kernel.masterdata.dto.request.SearchDto;
import io.mosip.kernel.masterdata.dto.response.FilterResponseDto;
import io.mosip.kernel.masterdata.dto.response.PageResponseDto;
import io.mosip.kernel.masterdata.entity.id.IdAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.MachineSpecificationService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * This class provide services to do CRUD operations on MachineSpecification.
 * 
 * @author Megha Tanga
 * @author Ayush Saxena
 * @since 1.0.0
 *
 */
@RestController
@Api(tags = { "MachineSpecification" })
public class MachineSpecificationController {

	@Autowired
	private AuditUtil auditUtil;

	/**
	 * Reference to MachineSpecificationService.
	 */
	@Autowired
	MachineSpecificationService machineSpecificationService;

	/**
	 * Post API to insert a new row of Machine Specification data
	 * 
	 * @param machineSpecification input Machine specification DTO from user
	 * @return ResponseEntity Machine Specification ID which is successfully
	 *         inserted
	 */
	@ResponseFilter
	@PostMapping("/machinespecifications")
	@PreAuthorize("hasRole('GLOBAL_ADMIN')")
	@ApiOperation(value = "Service to save Machine Specification", notes = "Saves Machine Spacification and return Machine Spacification ID ")
	@ApiResponses({ @ApiResponse(code = 201, message = "When Machine Specification successfully created"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = "While creating Machine Specification any error occured") })
	public ResponseWrapper<IdAndLanguageCodeID> createMachineSpecification(
			@Valid @RequestBody RequestWrapper<MachineSpecificationDto> machineSpecification) {
		auditUtil.auditRequest(
				MasterDataConstant.CREATE_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.CREATE_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(), "ADM-568");
		ResponseWrapper<IdAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(machineSpecificationService.createMachineSpecification(machineSpecification.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_CREATE, MachineSpecificationDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_CREATE_DESC,
						MachineSpecificationDto.class.getCanonicalName()),
				"ADM-659");
		return responseWrapper;
	}

	/**
	 * Put API to update a new row of Machine Specification data
	 * 
	 * @param machineSpecification input Machine specification DTO from user
	 * @return ResponseEntity Machine Specification ID which is successfully updated
	 */
	@ResponseFilter
	@PutMapping("/machinespecifications")
	@PreAuthorize("hasRole('GLOBAL_ADMIN')")
	@ApiOperation(value = "Service to update Machine Specification", notes = "update Machine Spacification and return Machine Spacification ID ")
	@ApiResponses({ @ApiResponse(code = 200, message = "When Machine Specification successfully updated"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Machine Specification found"),
			@ApiResponse(code = 500, message = "While updating Machine Specification any error occured") })
	public ResponseWrapper<IdAndLanguageCodeID> updateMachineSpecification(
			@Valid @RequestBody RequestWrapper<MachineSpecificationDto> machineSpecification) {
		auditUtil.auditRequest(
				MasterDataConstant.UPDATE_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.UPDATE_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(), "ADM-660");
		ResponseWrapper<IdAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(machineSpecificationService.updateMachineSpecification(machineSpecification.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_UPDATE, MachineSpecificationDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UPDATE_DESC,
						MachineSpecificationDto.class.getCanonicalName()),
				"ADM-666");
		return responseWrapper;
	}

	/**
	 * Put API to delete a new row of Machine Specification data
	 * 
	 * @param id input Machine specification id
	 * @return ResponseEntity Machine Specification ID which is successfully deleted
	 */
	@ResponseFilter
	@DeleteMapping("/machinespecifications/{id}")
	@ApiOperation(value = "Service to delete Machine Specification", notes = "Delete Machine Spacification and return Machine Spacification ID ")
	@ApiResponses({ @ApiResponse(code = 200, message = "When Machine Specification successfully deleted"),
			@ApiResponse(code = 400, message = "When Request body passed  is null or invalid"),
			@ApiResponse(code = 404, message = "When No Machine Specification found"),
			@ApiResponse(code = 500, message = "While deleting Machine Specification any error occured") })
	public ResponseWrapper<IdResponseDto> deleteMachineSpecification(@Valid @PathVariable("id") String id) {
		auditUtil.auditRequest(
				String.format(MasterDataConstant.DECOMMISION_API_CALLED,
						MachineSpecificationDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.DECOMMISION_API_CALLED,
						MachineSpecificationDto.class.getCanonicalName()),
				"ADM-667");

		ResponseWrapper<IdResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(machineSpecificationService.deleteMachineSpecification(id));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.DECOMMISSION_SUCCESS,
						MachineSpecificationDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.DECOMMISSION_SUCCESS,
						MachineSpecificationDto.class.getCanonicalName()),
				"ADM-668");
		return responseWrapper;
	}

	/**
	 * This controller method provides with all machine specifications.
	 * 
	 * @param pageNumber the page number
	 * @param pageSize   the size of each page
	 * @param sortBy     the attributes by which it should be ordered
	 * @param orderBy    the order to be used
	 * @return the response i.e. pages containing the machine specifications.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','CENTRAL_ADMIN')")
	@ResponseFilter
	@GetMapping("/machinespecifications/all")
	@ApiOperation(value = "Retrieve all the machine specification with additional metadata", notes = "Retrieve all the machine specification with the additional metadata")
	@ApiResponses({ @ApiResponse(code = 200, message = "list of machine specification"),
			@ApiResponse(code = 500, message = "Error occured while retrieving machine specification") })
	public ResponseWrapper<PageDto<MachineSpecificationExtnDto>> getAllMachineSpecifications(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page no for the requested data", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size for the requested data", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "createdDateTime") @ApiParam(value = "sort the requested data based on param value", defaultValue = "createdDateTime") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "order the requested data based on param", defaultValue = "desc") OrderEnum orderBy) {
		ResponseWrapper<PageDto<MachineSpecificationExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				machineSpecificationService.getAllMachineSpecfication(pageNumber, pageSize, sortBy, orderBy.name()));
		return responseWrapper;
	}

	/**
	 * Api to search machine specification based on filters provided.
	 * 
	 * @param request the request DTO.
	 * @return the pages of {@link MachineSpecificationExtnDto}.
	 */
	@ResponseFilter
	@PreAuthorize("hasRole('GLOBAL_ADMIN')")
	@PostMapping(value = "/machinespecifications/search")
	@ApiOperation(value = "Retrieve all machine specifications for the given Filter parameters", notes = "Retrieve all machine specifications for the given Filter parameters")
	public ResponseWrapper<PageResponseDto<MachineSpecificationExtnDto>> searchMachineSpecification(
			@Valid @RequestBody RequestWrapper<SearchDto> request) {
		auditUtil.auditRequest(
				MasterDataConstant.SEARCH_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.SEARCH_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(), "ADM-669");
		ResponseWrapper<PageResponseDto<MachineSpecificationExtnDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(machineSpecificationService.searchMachineSpecification(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_SEARCH, MachineSpecificationDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_SEARCH_DESC,
						MachineSpecificationDto.class.getCanonicalName()),
				"ADM-670");
		return responseWrapper;
	}

	/**
	 * Api to filter machine specification based on column and type provided.
	 * 
	 * @param request the request DTO.
	 * @return the {@link FilterResponseDto}.
	 */
	@ResponseFilter
	@PreAuthorize("hasRole('GLOBAL_ADMIN')")
	@PostMapping("/machinespecifications/filtervalues")
	public ResponseWrapper<FilterResponseDto> machineSpecificationFilterValues(
			@RequestBody @Valid RequestWrapper<FilterValueDto> request) {
		auditUtil.auditRequest(
				MasterDataConstant.FILTER_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.FILTER_API_IS_CALLED + MachineSpecificationDto.class.getCanonicalName(), "ADM-671");
		ResponseWrapper<FilterResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(machineSpecificationService.machineSpecificationFilterValues(request.getRequest()));
		auditUtil.auditRequest(MasterDataConstant.SUCCESSFUL_FILTER + MachineSpecificationDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.SUCCESSFUL_FILTER_DESC + MachineSpecificationDto.class.getCanonicalName(),
				"ADM-672");
		return responseWrapper;
	}

}
