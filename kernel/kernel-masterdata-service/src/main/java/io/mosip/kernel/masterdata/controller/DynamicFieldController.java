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
import io.mosip.kernel.masterdata.constant.OrderEnum;
import io.mosip.kernel.masterdata.dto.DynamicFieldDto;
import io.mosip.kernel.masterdata.dto.DynamicFieldValueDto;
import io.mosip.kernel.masterdata.dto.getresponse.DynamicFieldResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.service.DynamicFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping(value = "/dynamicfields")
@Api(tags = { "Dynamic Field" })
public class DynamicFieldController {
	
	@Autowired
	private DynamicFieldService dynamicFieldService;
	
	@ResponseFilter
	@GetMapping
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ApiOperation(value = "Service to fetch all dynamic fields")
	public ResponseWrapper<PageDto<DynamicFieldResponseDto>> getAllDynamicFields(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page number", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "cr_dtimes") @ApiParam(value = "sort on field name", defaultValue = "cr_dtimes") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "sort order", defaultValue = "desc") OrderEnum orderBy,
			@RequestParam(name = "langCode", required = false) @ApiParam(value = "Lang Code", required = false) String langCode) {
		ResponseWrapper<PageDto<DynamicFieldResponseDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dynamicFieldService.getAllDynamicField(pageNumber, pageSize, sortBy, orderBy.name(), langCode));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PostMapping
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ApiOperation(value = "Service to create dynamic field")
	public ResponseWrapper<DynamicFieldResponseDto> createDynamicField (@Valid @RequestBody RequestWrapper<DynamicFieldDto> dynamicFieldDto) {
		ResponseWrapper<DynamicFieldResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dynamicFieldService.createDynamicField(dynamicFieldDto.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PutMapping
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ApiOperation(value = "Service to update dynamic field")
	public ResponseWrapper<DynamicFieldResponseDto> updateDynamicField (
			@RequestParam(name = "id") @ApiParam(value = "field id") String id,
			@Valid @RequestBody RequestWrapper<DynamicFieldDto> dynamicFieldDto) {
		ResponseWrapper<DynamicFieldResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dynamicFieldService.updateDynamicField(id, dynamicFieldDto.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PutMapping("values")
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ApiOperation(value = "Service to update dynamic field")
	public ResponseWrapper<String> updateFieldValue (
			@RequestParam(name = "id") @ApiParam(value = "field name") String fieldName,
			@Valid @RequestBody RequestWrapper<DynamicFieldValueDto> dynamicFieldValueDto) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(dynamicFieldService.updateFieldValue(fieldName, dynamicFieldValueDto.getRequest()));
		return responseWrapper;
	}

}
