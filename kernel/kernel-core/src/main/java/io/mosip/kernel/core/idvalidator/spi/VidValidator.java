package io.mosip.kernel.core.idvalidator.spi;

/**
 * Validates a MOSIP Virtual ID (VID).
 * <p>
 * Contract: implementations apply length, checksum, and filter rules locally
 * (no HTTP). {@code id} must be a non-null, non-blank numeric string.
 * </p>
 *
 * @param <T> VID type, typically {@link String}
 * @author Abhishek Kumar
 * @since 1.0.0
 * @see io.mosip.kernel.core.idvalidator.exception.InvalidIDException
 */
public interface VidValidator<T> {
	/**
	 * Validates the given VID.
	 *
	 * @param id never-null VID string
	 * @return {@code true} if the VID is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the VID
	 *                                                                       is null
	 *                                                                       or
	 *                                                                       malformed
	 */
	boolean validateId(T id);
}
