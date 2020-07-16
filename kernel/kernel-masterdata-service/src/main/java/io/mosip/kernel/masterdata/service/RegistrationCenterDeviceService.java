package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.DeviceAndRegCenterMappingResponseDto;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.masterdata.dto.ResponseRegistrationCenterDeviceDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterDeviceID;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;

/**
 * RegistrationCenterDeviceService interface provide methods used to create
 * mapping between registration center id and device id.
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */

public interface RegistrationCenterDeviceService {

	/**
	 * Map registration center with device.
	 *
	 * @param regCenterId the reg center id
	 * @param deviceId    the device id
	 * @return {@link ResponseDto} response dto
	 */
	public ResponseDto mapRegistrationCenterWithDevice(String regCenterId, String deviceId);

	/**
	 * This method to un-map a Device from a Registration Center .
	 * 
	 * @param deviceId    the device Id.
	 * @param regCenterId the registration center Id.
	 * @return {@link DeviceAndRegCenterMappingResponseDto}.
	 */
	public DeviceAndRegCenterMappingResponseDto unmapDeviceRegCenter(String deviceId, String regCenterId);

}
