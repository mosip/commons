package io.mosip.kernel.core.notification.spi;

import io.mosip.kernel.core.notification.model.SMSResponseDto;

/**
 * Sends MOSIP SMS notifications via a pluggable vendor provider.
 * <p>
 * Contract: implementations perform HTTP to an SMS gateway (for example
 * MSG91). Call from {@code kernel-notification-service}.
 * {@code contactNumber} must be a non-blank E.164 or local number;
 * {@code message} must be non-null. New vendors implement this SPI.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.7
 * @see SMSResponseDto
 */
public interface SMSServiceProvider {

	/**
	 * Sends an SMS to the given contact number.
	 * <p>
	 * Contract: performs HTTP to the SMS vendor. Invalid numbers typically
	 * throw {@link io.mosip.kernel.core.notification.exception.InvalidNumberException}.
	 * </p>
	 *
	 * @param contactNumber never-null, never-blank destination number
	 * @param message       never-null message body; may be empty
	 * @return never-null vendor acknowledgement with status and message
	 */
	public SMSResponseDto sendSms(String contactNumber, String message);

}
