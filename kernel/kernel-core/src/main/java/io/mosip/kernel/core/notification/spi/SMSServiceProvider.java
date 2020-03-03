package io.mosip.kernel.core.notification.spi;

import io.mosip.kernel.core.notification.model.SMSResponseDto;

/**
 * This interface is responsible to provide SMS as a service for SMS
 * notification API. SMS notification service can connect to different
 * implementation of service providers and vendors by injecting this Interface.
 * 
 * Addition of a new vendor in MOSIP platform will be done by implementing this
 * interface and giving a implementation based on vendors requirements.
 *
 * 
 * @author Urvil Joshi
 * @since 1.0.7
 * 
 */
public interface SMSServiceProvider {

	/**
	 * Method responsible for sending SMS.
	 * 
	 * @param contactNumber Contact number to sent SMS to.
	 * @param message       Message to send
	 * @return acknowledgement
	 */
	public SMSResponseDto sendSms(String contactNumber, String message);

}
