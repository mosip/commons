package io.mosip.kernel.core.idvalidator.spi;

/**
 * Validates a MOSIP Pre-Registration ID (PRID).
 * <p>
 * Contract: implementations apply length, sequence, repeating-digit, and
 * repeating-block filter rules locally (no HTTP). {@code id} must be
 * non-null and non-blank.
 * </p>
 *
 * @param <T> PRID type, typically {@link String}
 * @author Abhishek Kumar
 * @since 1.0.0
 * @see io.mosip.kernel.core.idvalidator.exception.InvalidIDException
 */
public interface PridValidator<T> {
	/**
	 * Validates the given PRID using configured default filter limits.
	 *
	 * @param id never-null PRID string
	 * @return {@code true} if the PRID is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the
	 *                                                                       PRID is
	 *                                                                       null or
	 *                                                                       malformed
	 */
	boolean validateId(T id);

	/**
	 * Validates the given PRID using caller-supplied filter limits.
	 *
	 * @param id             never-null PRID string
	 * @param pridLength     expected character length; must be positive
	 * @param sequenceLimit  maximum allowed sequential digits; must be
	 *                       non-negative
	 * @param repeatingLimit maximum allowed repeating digits; must be
	 *                       non-negative
	 * @param blockLimit     maximum allowed repeating block length; must be
	 *                       non-negative
	 * @return {@code true} if the PRID is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the
	 *                                                                       PRID is
	 *                                                                       null or
	 *                                                                       malformed
	 */
	boolean validateId(String id, int pridLength, int sequenceLimit, int repeatingLimit, int blockLimit);
}
