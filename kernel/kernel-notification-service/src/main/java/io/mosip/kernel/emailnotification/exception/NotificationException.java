package io.mosip.kernel.emailnotification.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception wrapping mail MIME or attachment failures during email
 * send.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public class NotificationException extends BaseUncheckedException {

	/**
	 * Generated serial version.
	 */
	private static final long serialVersionUID = 3949838534862481500L;

	/**
	 * Instantiates the exception from a wrapped mail or attachment failure.
	 * 
	 * @param notificationException the cause whose localized message and message
	 *                              are copied onto this exception
	 */
	public NotificationException(Throwable notificationException) {
		super(notificationException.getLocalizedMessage(), notificationException.getMessage());
	}
}
