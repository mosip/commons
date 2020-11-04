package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.TemplateTypeDto;
import io.mosip.kernel.masterdata.dto.TemplateTypeResponseDto;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.service.TemplateTypeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author Uday Kumar
 * @author Megha Tanga
 * @since 1.0.0
 * 
 */
@RestController
@Api(tags = { "TemplateType" })
public class TemplateTypeController {

	@Autowired
	TemplateTypeService templateTypeService;

	/**
	 * This method creates template type based on provided.
	 * 
	 * @param templateType
	 *            the request dto.
	 * @return {@link CodeAndLanguageCodeID}
	 */
	@ResponseFilter
	@PostMapping("/templatetypes")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Service to create template type", notes = "create TemplateType  and return  code and LangCode")
	@ApiResponses({ @ApiResponse(code = 201, message = " successfully created"),
			@ApiResponse(code = 400, message = " Request body passed  is null or invalid"),
			@ApiResponse(code = 500, message = " creating any error occured") })
	public ResponseWrapper<CodeAndLanguageCodeID> createTemplateType(
			@Valid @RequestBody RequestWrapper<TemplateTypeDto> templateType) {

		ResponseWrapper<CodeAndLanguageCodeID> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(templateTypeService.createTemplateType(templateType.getRequest()));
		return responseWrapper;

	}

	/**
	 * 
	 * Function to fetch template type detail based on given template type code and
	 * Language code.
	 * 
	 * @param templateTypeCode
	 *            type code pass template type code as String
	 * @param langCode
	 *            pass language code as String
	 * @return TemplateTypeResponseDto template type detail based on given template
	 *         type code and Language code {@link TemplateTypeResponseDto}
	 */
	@ResponseFilter
	@GetMapping(value = "/templatetypes/{code}/{langcode}")
	@PreAuthorize("hasAnyRole('ID_AUTHENTICATION','ZONAL_ADMIN','GLOBAL_ADMIN','PRE_REGISTRATION','RESIDENT','INDIVIDUAL','REGISTRATION_PROCESSOR','PARTNER','AUTH_PARTNER','PARTNER_ADMIN','DEVICE_PROVIDER','DEVICE_MANAGER')")
	@ApiOperation(value = "Retrieve all template type Details for given Languge Code", notes = "Retrieve all template type Detail for given Languge Code and code")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When template type Details retrieved from database for the given Languge Code and Code"),
			@ApiResponse(code = 404, message = "When No template type Details found for the given Languge Code and Code"),
			@ApiResponse(code = 500, message = "While retrieving template type Details any error occured") })
	public ResponseWrapper<TemplateTypeResponseDto> getTemplateTypeCodeandLangCode(
			@PathVariable("code") String templateTypeCode, @PathVariable("langcode") String langCode) {

		ResponseWrapper<TemplateTypeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(templateTypeService.getTemplateTypeCodeandLangCode(templateTypeCode, langCode));
		return responseWrapper;
	}

	/**
	 * 
	 * Function to fetch template type detail based on given Language code.
	 * 
	 * @param langCode
	 *            pass language code as String
	 * @return TemplateTypeResponseDto template type detail based on given Language
	 *         code {@link TemplateTypeResponseDto}
	 */
	@ResponseFilter
	@GetMapping(value = "/templatetypes/{langcode}")
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Retrieve all template type Details for given Languge Code", notes = "Retrieve all template type Detail for given Languge Code")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When template type Details retrieved from database for the given Languge Code"),
			@ApiResponse(code = 404, message = "When No template type Details found for the given Languge Code"),
			@ApiResponse(code = 500, message = "While retrieving template type Details any error occured") })
	public ResponseWrapper<TemplateTypeResponseDto> getTemplateFileFormatLangCode(
			@PathVariable("langcode") String langCode) {

		ResponseWrapper<TemplateTypeResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(templateTypeService.getTemplateTypeLangCode(langCode));
		return responseWrapper;
	}

}
