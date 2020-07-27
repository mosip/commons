package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;

/**
 * The RegistrationCenterMachineService interface provides method to perform
 * operation on Registration center and Machine. It performs mapping in database
 * for registration center id and machine id.
 * 
 * @author Bal Vikash Sharma
 * @author Megha Tanga
 * @since 1.0.0
 */
public interface RegistrationCenterMachineService {



	/**
	 * Un map center to machine mapping.
	 *
	 * @param regCenterId the reg center id
	 * @param machineId   the machine id
	 * @return the response dto
	 */
	public ResponseDto unMapCenterToMachineMapping(String regCenterId, String machineId);

	/**
	 * Map center to machine mapping.
	 *
	 * @param regCenterId the reg center id
	 * @param machineId   the machine id
	 * @return the response dto
	 */
	public ResponseDto mapCenterToMachineMapping(String regCenterId, String machineId);

}
