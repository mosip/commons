package io.mosip.kernel.masterdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.getresponse.ApplicationConfigResponseDto;
import io.mosip.kernel.masterdata.service.ApplicationConfigService;
import io.swagger.annotations.Api;

@Api(tags = { "Application Configs" })
@RestController
@RequestMapping("/applicationconfigs")
public class ApplicationConfigController {

	@Autowired
	private ApplicationConfigService applicationService;
	
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ResponseFilter
	@GetMapping
	public ResponseWrapper<ApplicationConfigResponseDto> getAllApplication() {
		ResponseWrapper<ApplicationConfigResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(applicationService.getLanguageConfigDetails());
		return responseWrapper;
	}
}
