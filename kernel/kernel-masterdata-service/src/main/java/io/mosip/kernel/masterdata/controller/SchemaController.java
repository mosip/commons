package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import io.mosip.kernel.masterdata.dto.IdSchemaPublishDto;
import io.mosip.kernel.masterdata.dto.IdentitySchemaDto;
import io.mosip.kernel.masterdata.dto.getresponse.IdSchemaResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.PageDto;
import io.mosip.kernel.masterdata.service.IdentitySchemaService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * @author anusha
 *
 */

@RestController
@RequestMapping(value = "/idschema")
@Api(tags = { "Identity Schema" })
public class SchemaController {
	
	@Autowired
	private IdentitySchemaService identitySchemaService;
	
	@ResponseFilter
	@PostMapping
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to create identity schema")
	public ResponseWrapper<IdSchemaResponseDto> createSchema(@Valid @RequestBody RequestWrapper<IdentitySchemaDto> schema) {
		ResponseWrapper<IdSchemaResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(identitySchemaService.createSchema(schema.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PutMapping
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to update identity schema in draft status")
	public ResponseWrapper<IdSchemaResponseDto> updateSchema(@RequestParam(name = "id") @ApiParam(value = "schema id") String id,
			@Valid @RequestBody RequestWrapper<IdentitySchemaDto> schema) {
		ResponseWrapper<IdSchemaResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(identitySchemaService.updateSchema(id, schema.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@PutMapping("/publish")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to publish identity schema in draft status")
	public ResponseWrapper<String> publishSchema(@Valid @RequestBody RequestWrapper<IdSchemaPublishDto> idSchemaPublishDto) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(identitySchemaService.publishSchema(idSchemaPublishDto.getRequest()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@DeleteMapping
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to delete identity schema in draft status")
	public ResponseWrapper<String> deleteSchema(
			@RequestParam(name = "id") @ApiParam(value = "schema id") String id) {
		ResponseWrapper<String> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(identitySchemaService.deleteSchema(id));
		return responseWrapper;
	}
	
	@ResponseFilter
	@GetMapping("/all")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to fetch all identity schema")
	public ResponseWrapper<PageDto<IdSchemaResponseDto>> getAllSchema(
			@RequestParam(name = "pageNumber", defaultValue = "0") @ApiParam(value = "page number", defaultValue = "0") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") @ApiParam(value = "page size", defaultValue = "10") int pageSize,
			@RequestParam(name = "sortBy", defaultValue = "cr_dtimes") @ApiParam(value = "sort on field name", defaultValue = "cr_dtimes") String sortBy,
			@RequestParam(name = "orderBy", defaultValue = "desc") @ApiParam(value = "sort order", defaultValue = "desc") OrderEnum orderBy) {
		ResponseWrapper<PageDto<IdSchemaResponseDto>> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(identitySchemaService.getAllSchema(pageNumber, pageSize, sortBy, orderBy.name()));
		return responseWrapper;
	}
	
	@ResponseFilter
	@GetMapping("/latest")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN','REGISTRATION_CLIENT','REGISTRATION_SUPERVISOR','REGISTRATION_OFFICER','REGISTRATION_PROCESSOR','RESIDENT')")
	@ApiOperation(value = "Service to fetch latest published identity schema")
	public ResponseWrapper<IdSchemaResponseDto> getLatestPublishedSchema(
			@RequestParam(name = "schemaVersion", defaultValue = "0", required = false) @ApiParam(value = "schema version", defaultValue = "0") double schemaVersion) {
		ResponseWrapper<IdSchemaResponseDto> responseWrapper = new ResponseWrapper<>();
		if(schemaVersion <= 0)
			responseWrapper.setResponse(identitySchemaService.getLatestSchema());
		else
			responseWrapper.setResponse(identitySchemaService.getIdentitySchema(schemaVersion));
		return responseWrapper;
	}

}
