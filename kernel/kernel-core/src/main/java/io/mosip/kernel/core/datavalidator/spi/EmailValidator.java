/**
 * Email validation SPI.
 */
package io.mosip.kernel.core.datavalidator.spi;

/**
 * Validates an email address against MOSIP format rules.
 * <p>
 * Contract: implementations apply regex locally (no HTTP). {@code email} must
 * be non-null. Invalid addresses typically throw
 * {@link io.mosip.kernel.core.datavalidator.exception.InvalideEmailException}.
 * </p>
 *
 * @param <T> email type, typically {@link String}
 * @author Megha Tanga
 * @since 1.0.0
 */
public interface EmailValidator<T> {

	/**
	 * Validates the given email address.
	 *
	 * @param email never-null email string
	 * @return {@code true} if the email is valid
	 * @throws io.mosip.kernel.core.datavalidator.exception.InvalideEmailException
	 *                                                                             when
	 *                                                                             the
	 *                                                                             email
	 *                                                                             is
	 *                                                                             null
	 *                                                                             or
	 *                                                                             malformed
	 */
	boolean validateEmail(T email);

}
