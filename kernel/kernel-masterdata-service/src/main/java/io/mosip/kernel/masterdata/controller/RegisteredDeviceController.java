package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.dto.DeRegisterDevicePostDto;
import io.mosip.kernel.masterdata.dto.DeviceRegisterResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.dto.registerdevice.RegisteredDevicePostDto;
import io.mosip.kernel.masterdata.service.RegisteredDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller for CURD operation on Registered Device Details
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
@RestController
@RequestMapping(value = "/registereddevices")
@Api(tags = { "Registered Device" })
@Validated
public class RegisteredDeviceController {

	@Autowired
	RegisteredDeviceService registeredDeviceService;

	/**
	 * Api to Register Device.
	 * 
	 * Digitally signed device
	 * 
	 * @param registeredDevicePostDto
	 * @return ResponseWrapper<String>
	 * @throws Exception
	 */
	@ResponseFilter
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@PostMapping
	public ResponseWrapper<String> signedRegisteredDevice(
			@Valid @RequestBody RequestWrapper<RegisteredDevicePostDto> registeredDevicePostDto) throws Exception {
		ResponseWrapper<String> response = new ResponseWrapper<>();
		response.setResponse(registeredDeviceService.signedRegisteredDevice(registeredDevicePostDto.getRequest()));
		return response;
	}

	/**
	 * Api to de-register Device.
	 * 
	 * @param request
	 *            the request DTO.
	 * @return the {@link DeviceRegisterResponseDto}.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "DeRegister Device")
	@PostMapping("/deregister")
	@ResponseFilter
	public ResponseWrapper<String> deRegisterDevice(@Valid @RequestBody RequestWrapper<DeRegisterDevicePostDto>
							deRegisterDevicePostDto) {
		ResponseWrapper<String> response = new ResponseWrapper<>();
		response.setResponse(registeredDeviceService.deRegisterDevice(deRegisterDevicePostDto.getRequest()));
		return response;
	}

	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Update status of the devive")
	@PutMapping("/update/status")
	public ResponseEntity<ResponseDto> deRegisterDevice(
			@NotBlank @RequestParam(name = "deviceCode") String deviceCode,
			@NotBlank @RequestParam(name = "statusCode") String statusCode) {
		return new ResponseEntity<>(registeredDeviceService.updateStatus(deviceCode, statusCode), HttpStatus.OK);
	}

}
