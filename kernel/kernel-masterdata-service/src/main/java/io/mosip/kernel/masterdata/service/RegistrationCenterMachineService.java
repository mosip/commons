package io.mosip.kernel.masterdata.service;

import io.mosip.kernel.masterdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.masterdata.dto.ResponseRrgistrationCenterMachineDto;
import io.mosip.kernel.masterdata.dto.getresponse.ResponseDto;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterMachineID;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;

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
	 * This method used to create mapping between registration center and machine.
	 * 
	 * @param requestDto contains {@link RegistrationCenterMachineDto} which must
	 *                   contain registration center id and machine id.
	 * @return response object which contains registration center id and machine id.
	 * @throws MasterDataServiceException if any error occurs while mapping
	 *                                    registration center id and machine id.
	 *                                    Like if registration center id or machine
	 *                                    id is not valid or not present in
	 *                                    database.
	 */
	public ResponseRrgistrationCenterMachineDto createRegistrationCenterAndMachine(
			RegistrationCenterMachineDto requestDto);

	/**
	 * Delete the mapping of registration center and machine
	 * 
	 * @param regCenterId Registration center id to be deleted
	 * @param machineId   MachineId id to be deleted
	 * @return {@link RegistrationCenterMachineID}
	 */
	public RegistrationCenterMachineID deleteRegistrationCenterMachineMapping(String regCenterId, String machineId);

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
