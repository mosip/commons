package io.mosip.kernel.emailnotification.service;

import io.mosip.kernel.core.notification.model.SMSResponseDto;

/**
 * Service contract for sending SMS notifications from the notifier HTTP API
 * ({@code POST /sms/send}).
 *
 * Implementations validate the destination number and message, then delegate to
 * an SMS service provider unless the service is running in proxy or local
 * profile mode.
 * 
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */

public interface SmsNotification {

	/**
	 * Sends an SMS with the given message to the given contact number.
	 * 
	 * @param contactNumber  the destination MSISDN; must not be null or blank
	 * @param contentMessage the SMS body; must not be null or blank
	 * @return the SMS response containing status and message
	 */
	public SMSResponseDto sendSmsNotification(String contactNumber, String contentMessage);

}
