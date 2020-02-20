package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.DeviceAndRegCenterMappingResponseDto;
import io.mosip.kernel.masterdata.dto.MOSIPDeviceServiceDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.masterdata.dto.ResponseRegistrationCenterDeviceDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterDeviceID;
import io.mosip.kernel.masterdata.service.RegistrationCenterDeviceService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @author Megha Tanga
 * @since 1.0.0
 */
@RestController
@RequestMapping("/registrationcenterdevice")
@Api(value = "Operation related to mapping of registration center and devices", tags = { "RegistrationCenterDevice" })
public class RegistrationCenterDeviceController {
	@Autowired
	AuditUtil auditUtil;
	@Autowired
	private RegistrationCenterDeviceService registrationCenterDeviceService;

	@ResponseFilter
	@PostMapping
	@ApiOperation(value = "Map provided registration center and device", notes = "Map provided registration center id and device id")
	@ApiResponses({ @ApiResponse(code = 201, message = "When registration center and device mapped"),
			@ApiResponse(code = 400, message = "When Request body passed  is invalid"),
			@ApiResponse(code = 500, message = "While mapping registration center and device") })
	public ResponseWrapper<ResponseRegistrationCenterDeviceDto> createRegistrationCenterAndDevice(
			@Valid @RequestBody RequestWrapper<RegistrationCenterDeviceDto> requestDto) {

		ResponseWrapper<ResponseRegistrationCenterDeviceDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				registrationCenterDeviceService.createRegistrationCenterAndDevice(requestDto.getRequest()));
		return responseWrapper;
	}

	/**
	 * Delete the mapping of registration center and device
	 * 
	 * @param regCenterId Registration center id to be deleted
	 * @param deviceId    DeviceId id to be deleted
	 * @return {@link RegistrationCenterDeviceID}
	 */
	@ResponseFilter
	@ApiOperation(value = "Delete the mapping of registration center and device")
	@DeleteMapping("/{regCenterId}/{deviceId}")
	public ResponseWrapper<RegistrationCenterDeviceID> deleteRegistrationCenterDeviceMapping(
			@ApiParam("Registration center id to be deleted") @PathVariable String regCenterId,
			@ApiParam("DeviceId id to be deleted") @PathVariable String deviceId) {

		ResponseWrapper<RegistrationCenterDeviceID> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(
				registrationCenterDeviceService.deleteRegistrationCenterDeviceMapping(regCenterId, deviceId));
		return responseWrapper;
	}

	/**
	 * Api to un-map Device from a Registration Center .
	 * 
	 * @param deviceId    the Device ID.
	 * @param regCenterId the Registration Center ID.
	 * @return the DeviceAndRegCenterMappingResponseDto.
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@PutMapping("/unmap/{deviceid}/{regcenterid}")
	public ResponseWrapper<DeviceAndRegCenterMappingResponseDto> unmapDeviceRegCenter(
			@PathVariable("deviceid") @NotBlank @Size(min = 1, max = 36) String deviceId,
			@PathVariable("regcenterid") @NotBlank @Size(min = 1, max = 10) String regCenterId) {
		auditUtil.auditRequest(
				MasterDataConstant.UNMAP_API_IS_CALLED + RegistrationCenterDeviceDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.UNMAP_API_IS_CALLED + RegistrationCenterDeviceDto.class.getCanonicalName(),
				"ADM-729");
		ResponseWrapper<DeviceAndRegCenterMappingResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterDeviceService.unmapDeviceRegCenter(deviceId, regCenterId));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_UNMAP,
						RegistrationCenterDeviceDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UNMAP,
						RegistrationCenterDeviceDto.class.getCanonicalName()),
				"ADM-730");
		return responseWrapper;
	}

	/**
	 * Map registration center device.
	 *
	 * @param regCenterId the reg center id
	 * @param deviceId    the device id
	 * @return {@link ResponseDto}
	 */
	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@ApiOperation(value = "map registration center with device")
	@GetMapping("map/{regCenterId}/{deviceId}")
	public ResponseWrapper<ResponseDto> mapRegistrationCenterDevice(
			@ApiParam("Registration center id ") @PathVariable String regCenterId,
			@ApiParam("DeviceId id ") @PathVariable String deviceId) {
		auditUtil.auditRequest(
				MasterDataConstant.MAP_API_IS_CALLED + RegistrationCenterDeviceDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.MAP_API_IS_CALLED + RegistrationCenterDeviceDto.class.getCanonicalName(), "ADM-727");
		ResponseWrapper<ResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(registrationCenterDeviceService.mapRegistrationCenterWithDevice(regCenterId, deviceId));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_MAP, RegistrationCenterDeviceDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_MAP, RegistrationCenterDeviceDto.class.getCanonicalName()),
				"ADM-728");
		return responseWrapper;
	}
}
