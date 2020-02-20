package io.mosip.kernel.masterdata.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.getresponse.ZoneNameResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.ZoneExtnDto;
import io.mosip.kernel.masterdata.service.ZoneService;
import io.mosip.kernel.masterdata.validator.ValidLangCode;
import io.swagger.annotations.Api;

/**
 * Controller to handle api request for the zones
 * 
 * @author Abhishek Kumar
 * @since 1.0.0
 */
@RestController
@RequestMapping("/zones")
@Validated
@Api(tags = { "Zone" })
public class ZoneController {

	@Autowired
	private ZoneService zoneService;

	/**
	 * api to fetch the logged-in user zone hierarchy
	 * 
	 * @param langCode input language code
	 * @return {@link List} of {@link ZoneExtnDto}
	 */
	@PreAuthorize("hasRole('ZONAL_ADMIN')")
	@GetMapping("/hierarchy/{langCode}")
	public ResponseWrapper<List<ZoneExtnDto>> getZoneHierarchy(
			@PathVariable("langCode") @ValidLangCode(message = "Language Code is Invalid") String langCode) {
		ResponseWrapper<List<ZoneExtnDto>> response = new ResponseWrapper<>();
		response.setResponse(zoneService.getUserZoneHierarchy(langCode));
		return response;
	}

	/**
	 * api to fetch the logged-in user zone hierarchy leaf zones
	 * 
	 * @param langCode input language code
	 * @return {@link List} of {@link ZoneExtnDto}
	 */
	@PreAuthorize("hasRole('ZONAL_ADMIN')")
	@GetMapping("/leafs/{langCode}")
	public ResponseWrapper<List<ZoneExtnDto>> getLeafZones(
			@PathVariable("langCode") @ValidLangCode(message = "Language Code is Invalid") String langCode) {
		ResponseWrapper<List<ZoneExtnDto>> response = new ResponseWrapper<>();
		response.setResponse(zoneService.getUserLeafZone(langCode));
		return response;
	}

	@GetMapping("/zonename")
	public ResponseWrapper<ZoneNameResponseDto> getZoneNameBasedOnUserIDAndLangCode(
			@RequestParam("userID") String userID,
			@ValidLangCode(message = "Language Code is Invalid") @RequestParam("langCode") String langCode) {
		ResponseWrapper<ZoneNameResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(zoneService.getZoneNameBasedOnLangCodeAndUserID(userID, langCode));
		return responseWrapper;
	}

	@GetMapping("/authorize")
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','REGISTRATION_ADMIN')")
	@ResponseFilter
	public ResponseWrapper<Boolean> authorizeZone(@NotBlank @RequestParam("rid") String rId) {
		ResponseWrapper<Boolean> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(zoneService.authorizeZone(rId));
		return responseWrapper;
	}

}
