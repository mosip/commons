package io.mosip.kernel.masterdata.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.deviceprovidermanager.spi.DeviceProviderService;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.DeviceProviderDto;
import io.mosip.kernel.masterdata.dto.DeviceProviderPutDto;
import io.mosip.kernel.masterdata.dto.ValidateDeviceDto;
import io.mosip.kernel.masterdata.dto.ValidateDeviceHistoryDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.DeviceProviderExtnDto;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;

/**
 * Device provider management controller
 * 
 * @author Srinivasan
 *
 */
@RestController
@RequestMapping(value = "/deviceprovidermanagement")
@Api(tags = { "DeviceProvider Management" })
public class DeviceProviderManagementController {

	@Autowired
	private AuditUtil auditUtil;

	@Autowired
	private DeviceProviderService<ResponseDto, ValidateDeviceDto, ValidateDeviceHistoryDto, DeviceProviderDto, DeviceProviderExtnDto, DeviceProviderPutDto> deviceProviderService;

	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','ID_AUTHENTICATION','REGISTRATION_PROCESSOR','RESIDENT')")
	@PostMapping("/validate")
	@ResponseFilter
	public ResponseWrapper<ResponseDto> validateDeviceProvider(
			@RequestBody @Valid RequestWrapper<ValidateDeviceDto> request) {
		ResponseWrapper<ResponseDto> responseWrapper = new ResponseWrapper<>();
		auditUtil.auditRequest(
				MasterDataConstant.DEVICE_VALIDATION_API_CALLED + ValidateDeviceDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.DEVICE_VALIDATION_API_CALLED + ValidateDeviceDto.class.getSimpleName(), "ADM-600");
		responseWrapper.setResponse(deviceProviderService.validateDeviceProviders(request.getRequest()));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.DEVICE_VALIDATION_SUCCESS, ValidateDeviceDto.class.getSimpleName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.DEVICE_VALIDATION_HISTORY_SUCCESS_DESC,
						ValidateDeviceDto.class.getSimpleName()),
				"ADM-601");
		return responseWrapper;

	}

	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','ID_AUTHENTICATION','REGISTRATION_PROCESSOR')")
	@PostMapping("/validate/history")
	@ResponseFilter
	public ResponseWrapper<ResponseDto> validateDeviceProviderHistory(
			@RequestBody @Valid RequestWrapper<ValidateDeviceHistoryDto> request) {
		auditUtil.auditRequest(
				MasterDataConstant.DEVICE_VALIDATION_HISTORY_API_CALLED
						+ ValidateDeviceHistoryDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM, MasterDataConstant.DEVICE_VALIDATION_HISTORY_API_CALLED
						+ ValidateDeviceHistoryDto.class.getSimpleName(),
				"ADM-602");

		ResponseWrapper<ResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(deviceProviderService.validateDeviceProviderHistory(request.getRequest()));
		auditUtil.auditRequest(
				MasterDataConstant.DEVICE_VALIDATION_HISTORY_SUCCESS + ValidateDeviceHistoryDto.class.getSimpleName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.DEVICE_VALIDATION_HISTORY_SUCCESS_DESC + ValidateDeviceDto.class.getSimpleName(),
				"ADM-604");

		return responseWrapper;
	}

}
