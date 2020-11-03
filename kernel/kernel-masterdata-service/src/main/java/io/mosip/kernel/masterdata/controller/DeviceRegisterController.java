package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.masterdata.dto.DeviceRegisterResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.service.DeviceRegisterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * Controller class for Device registration and de registration.
 * 
 * @author Ritesh Sinha
 * @author Ramadurai Pandian
 * @since 1.0.0
 */
@RestController
@RequestMapping(value = "/device")
@Api(tags = { "DeviceRegister-Decommissioned" })
public class DeviceRegisterController {
	/**
	 * Reference to {@link DeviceRegisterService}.
	 */
	@Autowired
	private DeviceRegisterService deviceRegisterService;

	/**
	 * Api to de register Device.
	 * 
	 * @param request the request DTO.
	 * @return the {@link DeviceRegisterResponseDto}.
	 */
	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "DeRegister Device")
	@DeleteMapping("/deregister/{deviceCode}")
	public ResponseEntity<DeviceRegisterResponseDto> deRegisterDevice(@Valid @PathVariable String deviceCode) {
		return new ResponseEntity<>(deviceRegisterService.deRegisterDevice(deviceCode), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole('GLOBAL_ADMIN','ZONAL_ADMIN')")
	@ApiOperation(value = "Update status of the devive")
	@PutMapping("/update/status")
	public ResponseEntity<ResponseDto> deRegisterDevice(
			@NotBlank @RequestParam(value = "devicecode", required = true) String deviceCode,
			@NotBlank @RequestParam(value = "statuscode", required = true) String statusCode) {
		return new ResponseEntity<>(deviceRegisterService.updateStatus(deviceCode, statusCode), HttpStatus.OK);
	}
}
