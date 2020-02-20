package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.DeRegisterDeviceRequestDto;
import io.mosip.kernel.masterdata.dto.DeviceRegisterDto;
import io.mosip.kernel.masterdata.dto.DeviceRegisterResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;

/**
 * Service to register and de register Device.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 */
public interface DeviceRegisterService {

	/**
	 * Method to de register Device.
	 * 
	 * @param deviceCode the {@link DeRegisterDeviceRequestDto}.
	 * @return the {@link DeviceRegisterResponseDto}.
	 */
	public DeviceRegisterResponseDto deRegisterDevice(String deviceCode);

	/**
	 * Update status.
	 *
	 * @param deviceCode the device code
	 * @param statusCode the status code
	 * @return the response dto
	 */
	public ResponseDto updateStatus(String deviceCode, String statusCode);
}
