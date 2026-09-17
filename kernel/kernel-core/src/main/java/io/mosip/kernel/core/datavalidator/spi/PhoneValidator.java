/**
 * Phone-number validation SPI.
 */
package io.mosip.kernel.core.datavalidator.spi;

/**
 * Validates a phone number against MOSIP format rules.
 * <p>
 * Contract: implementations apply regex locally (no HTTP). {@code phoneNum}
 * must be non-null. Invalid numbers typically throw
 * {@link io.mosip.kernel.core.datavalidator.exception.InvalidPhoneNumberException}.
 * </p>
 *
 * @param <T> phone type, typically {@link String}
 * @author Megha Tanga
 * @since 1.0.0
 */
public interface PhoneValidator<T> {

	/**
	 * Validates the given phone number.
	 *
	 * @param phoneNum never-null phone number
	 * @return {@code true} if the number is valid
	 * @throws io.mosip.kernel.core.datavalidator.exception.InvalidPhoneNumberException
	 *                                                                                  when
	 *                                                                                  the
	 *                                                                                  number
	 *                                                                                  is
	 *                                                                                  null
	 *                                                                                  or
	 *                                                                                  malformed
	 */
	boolean validatePhone(T phoneNum);

}
