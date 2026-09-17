/**
 * PIN validation SPI.
 */
package io.mosip.kernel.core.pinvalidator.spi;

/**
 * Validates a MOSIP PIN (personal identification number).
 * <p>
 * Contract: implementations apply length and digit-only rules locally (no
 * HTTP). {@code pin} must be non-null. Invalid pins typically throw
 * {@link io.mosip.kernel.core.pinvalidator.exception.InvalidPinException}.
 * </p>
 *
 * @param <T> PIN type, typically {@link String}
 * @author Uday Kumar
 * @since 1.0.0
 */
public interface PinValidator<T> {

	/**
	 * Validates the given PIN.
	 *
	 * @param pin never-null PIN
	 * @return {@code true} if the PIN is valid
	 * @throws io.mosip.kernel.core.pinvalidator.exception.InvalidPinException when
	 *                                                                         the
	 *                                                                         PIN
	 *                                                                         is
	 *                                                                         null
	 *                                                                         or
	 *                                                                         malformed
	 */
	boolean validatePin(T pin);
}
