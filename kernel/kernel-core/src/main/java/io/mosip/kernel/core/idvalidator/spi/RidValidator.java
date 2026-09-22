package io.mosip.kernel.core.idvalidator.spi;

/**
 * Validates a MOSIP Registration ID (RID).
 * <p>
 * Contract: implementations check length, center id, machine id, sequence, and
 * timestamp fields locally (no HTTP). String arguments must be non-null;
 * length arguments must be positive and match the corresponding field widths.
 * </p>
 *
 * @param <T> RID / field type, typically {@link String}
 * @author Ritesh Sinha
 * @author Abhishek Kumar
 * @see io.mosip.kernel.core.idvalidator.exception.InvalidIDException
 */
public interface RidValidator<T> {
	/**
	 * Validates a RID against the given center and machine identifiers using
	 * configured field lengths.
	 *
	 * @param id        never-null RID
	 * @param centerId  never-null registration-center id expected in the RID
	 * @param machineId never-null machine id expected in the RID
	 * @return {@code true} if the RID matches the expected structure
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the RID
	 *                                                                       is null
	 *                                                                       or
	 *                                                                       malformed
	 */
	boolean validateId(T id, T centerId, T machineId);

	/**
	 * Validates a RID using configured default field lengths.
	 *
	 * @param id never-null RID
	 * @return {@code true} if the RID is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the RID
	 *                                                                       is null
	 *                                                                       or
	 *                                                                       malformed
	 */
	boolean validateId(T id);

	/**
	 * Validates a RID against center, machine, and explicit field lengths.
	 *
	 * @param id              never-null RID
	 * @param centerId        never-null registration-center id
	 * @param machineId       never-null machine id
	 * @param centerIdLength  expected center-id length; must be positive
	 * @param machineIdLength expected machine-id length; must be positive
	 * @param sequenceLength  expected sequence length; must be positive
	 * @param timeStampLength expected timestamp length; must be positive
	 * @return {@code true} if the RID is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the RID
	 *                                                                       is null
	 *                                                                       or
	 *                                                                       malformed
	 */
	boolean validateId(T id, T centerId, T machineId, int centerIdLength, int machineIdLength, int sequenceLength,
			int timeStampLength);

	/**
	 * Validates a RID using explicit field lengths without checking center or
	 * machine values.
	 *
	 * @param id              never-null RID
	 * @param centerIdLength  expected center-id length; must be positive
	 * @param machineIdLength expected machine-id length; must be positive
	 * @param sequenceLength  expected sequence length; must be positive
	 * @param timeStampLength expected timestamp length; must be positive
	 * @return {@code true} if the RID is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the RID
	 *                                                                       is null
	 *                                                                       or
	 *                                                                       malformed
	 */
	public boolean validateId(T id, int centerIdLength, int machineIdLength, int sequenceLength, int timeStampLength);
}
