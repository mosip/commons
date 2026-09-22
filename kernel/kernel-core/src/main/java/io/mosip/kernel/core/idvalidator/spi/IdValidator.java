package io.mosip.kernel.core.idvalidator.spi;

/**
 * Validates a MOSIP identifier of type {@code T} against format and checksum
 * rules.
 * <p>
 * Contract: implementations do not perform HTTP; they apply local checksum and
 * length rules. {@code id} must be non-null. Invalid ids typically throw
 * {@link io.mosip.kernel.core.idvalidator.exception.InvalidIDException}
 * rather than returning {@code false} (implementation-defined).
 * </p>
 *
 * @param <T> identifier type, typically {@link String}
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public interface IdValidator<T> {

	/**
	 * Validates the given identifier.
	 *
	 * @param id never-null identifier to validate
	 * @return {@code true} if the identifier is valid
	 * @throws io.mosip.kernel.core.idvalidator.exception.InvalidIDException when
	 *                                                                       the
	 *                                                                       identifier
	 *                                                                       is null
	 *                                                                       or
	 *                                                                       malformed
	 */
	boolean validateId(T id);
}
