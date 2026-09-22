package io.mosip.kernel.pinvalidator.impl;

import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.pinvalidator.exception.InvalidPinException;
import io.mosip.kernel.core.pinvalidator.spi.PinValidator;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.pinvalidator.constant.PinExceptionConstant;

/**
 * Validates a static PIN as a digit string of configured length.
 * <p>
 * Length is {@code mosip.kernel.pin.length}. Empty, wrong-length, and
 * non-numeric values raise {@link InvalidPinException}.
 * </p>
 *
 * @author Uday Kumar
 * @since 1.0.0
 * @see PinValidator
 */
@Component
public class PinValidatorImpl implements PinValidator<String> {
	/**
	 * Required PIN length from {@code mosip.kernel.pin.length}.
	 */
	@Value("${mosip.kernel.pin.length}")
	private int pinLength;
	/**
	 * Digit-only pattern of length {@link #pinLength}, built in {@link #uinValidatorImplnumaricRegEx()}.
	 */
	private String numaricRegEx;

	/**
	 * Builds {@link #numaricRegEx} as {@code \\d{pinLength}}.
	 */
	@PostConstruct
	private void uinValidatorImplnumaricRegEx() {
		numaricRegEx = "\\d{" + pinLength + "}";
	}

	/**
	 * Returns {@code true} when {@code pin} is non-empty, has length {@link #pinLength}, and is digits only.
	 *
	 * @param pin PIN to validate
	 * @return {@code true} if the PIN is valid
	 * @throws InvalidPinException if the PIN is empty, the wrong length, or non-numeric
	 */
	@Override
	public boolean validatePin(String pin) {
		/**
		 * 
		 * Check Pin, It Shouldn't be Null or empty
		 * 
		 */
		if (StringUtils.isEmpty(pin)) {
			throw new InvalidPinException(PinExceptionConstant.PIN_INVALID_NULL.getErrorCode(),
					PinExceptionConstant.PIN_INVALID_NULL.getErrorMessage());
		}
		/**
		 * 
		 * Check the Length of the pin, It Should be specified number of digits
		 * 
		 */

		if (pin.length() != pinLength) {
			throw new InvalidPinException(PinExceptionConstant.PIN_INVALID_LENGTH.getErrorCode(),
					PinExceptionConstant.PIN_INVALID_LENGTH.getErrorMessage() + pinLength);
		}
		/**
		 * 
		 * Validate the pin, It should not contain any alphanumeric characters
		 * 
		 */
		if (!Pattern.matches(numaricRegEx, pin)) {
			throw new InvalidPinException(PinExceptionConstant.PIN_INVALID_CHAR.getErrorCode(),
					PinExceptionConstant.PIN_INVALID_CHAR.getErrorMessage());
		}

		return true;
	}

}
