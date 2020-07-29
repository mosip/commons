package io.mosip.kernel.masterdata.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.service.RegistrationCenterMachineService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@RestController
@RequestMapping("/registrationcentermachine")
@Api(tags = { "RegistrationCenterMachine" })
public class RegistrationCenterMachineController {
	@Autowired
	AuditUtil auditUtil;
	@Autowired
	private RegistrationCenterMachineService registrationCenterMachineService;



	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@ApiOperation(value = "Un-map center to machine map ")
	@PutMapping("/unmap/{regCenterId}/{machineId}")
	public ResponseWrapper<ResponseDto> unMapRegistrationCenterMachine(
			@ApiParam("Registration center id") @PathVariable String regCenterId,
			@ApiParam("MachineId id ") @PathVariable String machineId) {
		auditUtil.auditRequest(
				MasterDataConstant.UNMAP_API_IS_CALLED + RegistrationCenterMachineDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.UNMAP_API_IS_CALLED + RegistrationCenterMachineDto.class.getCanonicalName(),
				"ADM-743");
		ResponseWrapper<ResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper
				.setResponse(registrationCenterMachineService.unMapCenterToMachineMapping(regCenterId, machineId));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_UNMAP,
						RegistrationCenterMachineDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM, String.format(MasterDataConstant.SUCCESSFUL_UNMAP,
						RegistrationCenterMachineDto.class.getCanonicalName()),
				"ADM-744");
		return responseWrapper;
	}

	@PreAuthorize("hasAnyRole('ZONAL_ADMIN','GLOBAL_ADMIN')")
	@ResponseFilter
	@ApiOperation(value = "map center to machine map ")
	@PutMapping("/map/{regCenterId}/{machineId}")
	public ResponseWrapper<ResponseDto> mapRegistrationCenterMachine(
			@ApiParam("Registration center id") @PathVariable String regCenterId,
			@ApiParam("MachineId id ") @PathVariable String machineId) {
		auditUtil.auditRequest(
				MasterDataConstant.MAP_API_IS_CALLED + RegistrationCenterMachineDto.class.getCanonicalName(),
				MasterDataConstant.AUDIT_SYSTEM,
				MasterDataConstant.MAP_API_IS_CALLED + RegistrationCenterMachineDto.class.getCanonicalName(),
				"ADM-741");
		ResponseWrapper<ResponseDto> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponse(registrationCenterMachineService.mapCenterToMachineMapping(regCenterId, machineId));
		auditUtil.auditRequest(
				String.format(MasterDataConstant.SUCCESSFUL_MAP, RegistrationCenterMachineDto.class.getCanonicalName()),
				MasterDataConstant.AUDIT_SYSTEM,
				String.format(MasterDataConstant.SUCCESSFUL_MAP, RegistrationCenterMachineDto.class.getCanonicalName()),
				"ADM-742");
		return responseWrapper;
	}

}
