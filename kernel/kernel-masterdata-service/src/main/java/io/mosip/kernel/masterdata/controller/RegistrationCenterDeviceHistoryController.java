package io.mosip.kernel.masterdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.getresponse.RegistrationCenterDeviceHistoryResponseDto;
import io.mosip.kernel.masterdata.service.RegistrationCenterDeviceHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author Uday Kumar
 * @since 1.0.0
 */
@RestController
@RequestMapping("/registrationcenterdevicehistory")
@Api(tags = { "RegistrationCenterDevicesHistory" })
public class RegistrationCenterDeviceHistoryController {

	/**
	 * 
	 * Reference to Registration Center device history Service.
	 */
	@Autowired
	private RegistrationCenterDeviceHistoryService registrationCenterDeviceHistoryService;

	/**
	 * Get API to fetch a Registration Center Device History based on given
	 * registration center id, device id and effective date time
	 * 
	 * @param regCenterId    input Registration Center Id from User
	 * @param deviceId       input Device Id from user
	 * @param effectiveTimes input effective date and time from user
	 * 
	 * @return RegistrationCenterDeviceHistoryResponseDto returning registration
	 *         center device history based on given regCenterId, deviceId and
	 *         effective date time
	 */
	@PreAuthorize("hasAnyRole('REGISTRATION_PROCESSOR')")
	@ResponseFilter
	@GetMapping(value = "/{regcenterid}/{deviceid}/{effdatetimes}")
	@ApiOperation(value = "Retrieve Registration Center Device History Details for the given Registration Center Id, Device Id and Effective date time")
	@ApiResponses({
			@ApiResponse(code = 200, message = "When Registration Center Device History Details retrieved from Registration Center Id, Device Id and Effective date time"),
			@ApiResponse(code = 404, message = "When No Registration Center Device History Details retrieved from Registration Center Id, Device Id and Effective date time"),
			@ApiResponse(code = 500, message = "While retrieving Registration Center Device History Details any error occured") })
	public ResponseWrapper<RegistrationCenterDeviceHistoryResponseDto> getRegCentDevHistByregCentIdDevIdEffTime(
			@PathVariable("regcenterid") String regCenterId, @PathVariable("deviceid") String deviceId,
			@PathVariable("effdatetimes") String effectiveTimes) {

		ResponseWrapper<RegistrationCenterDeviceHistoryResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterDeviceHistoryService
				.getRegCenterDeviceHisByregCenterIdDevIdEffDTime(regCenterId, deviceId, effectiveTimes));
		return responseWrapper;
	}
}
