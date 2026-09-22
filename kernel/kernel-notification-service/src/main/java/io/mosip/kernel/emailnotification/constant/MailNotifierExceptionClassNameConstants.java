package io.mosip.kernel.emailnotification.constant;

import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;

/**
 * Class that provides with the constants for mail notifier exceptions that are
 * not handled by controller advice.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public final class MailNotifierExceptionClassNameConstants {
	/**
	 * Private constructor for {@link MailNotifierExceptionClassNameConstants}
	 */
	private MailNotifierExceptionClassNameConstants() {
	}

	/**
	 * Fully qualified class name string of {@link MailSendException}, used by the
	 * async handler switch.
	 */
	public static final String MAIL_SENDMAIL_SEND_EXCEPTION_CLASS_NAME = "class org.springframework.mail.MailSendException";
	/**
	 * Fully qualified class name string of {@link MailAuthenticationException},
	 * used by the async handler switch.
	 */
	public static final String MAIL_AUTH_EXCEPTION_CLASS_NAME = "class org.springframework.mail.MailAuthenticationException";
}
