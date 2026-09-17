package io.mosip.kernel.core.idvalidator.spi;

/**
 * Validates a MOSIP Unique Identification Number (UIN).
 * <p>
 * Contract: implementations apply length, checksum, and filter rules locally
 * (no HTTP). {@code id} must be a non-null, non-blank numeric string.
 * </p>
 *
 * @param <T> UIN type, typically {@link String}
 * @author Abhishek Kumar
 * @since 1.0.0
 * @see io.mosip.kernel.core.idvalidator.exception.InvalidIDException
 */
public interface UinValidator<T> {
	/**
	 * Validates the given UIN.
	 *
	 * @param id never-null UIN string
	 * @return {@code true} if the UIN is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the UIN
	 *                                                                       is null
	 *                                                                       or
	 *                                                                       malformed
	 */
	boolean validateId(T id);
}
