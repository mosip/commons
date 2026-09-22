package io.mosip.kernel.ridgenerator.service;

/**
 * This interface contains methods for RID generation.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 * @param <T> the requestDTO.
 */
public interface RidGeneratorService<T> {

	/**
	 * Generates a RID for the given registration center and machine.
	 *
	 * @param centerId  the center id
	 * @param machineId the machine id
	 * @return the generated RID payload
	 * @throws io.mosip.kernel.ridgenerator.exception.EmptyInputException when an id is empty
	 * @throws io.mosip.kernel.ridgenerator.exception.InputLengthException when an id length is invalid
	 * @throws io.mosip.kernel.ridgenerator.exception.RidException when sequence persistence fails
	 */
	public T generateRid(String centerId, String machineId);
}
