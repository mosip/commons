package io.mosip.kernel.emailnotification.exception;

import java.lang.reflect.Method;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.emailnotification.config.LoggerConfiguration;
import io.mosip.kernel.emailnotification.constant.MailNotifierArgumentErrorConstants;
import io.mosip.kernel.emailnotification.constant.MailNotifierConstants;
import io.mosip.kernel.emailnotification.constant.MailNotifierExceptionClassNameConstants;

/**
 * This class handles {@link MailSendException},
 * {@link MailAuthenticationException}, {@link MailException}.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
@Component
public class EmailNotificationAsyncHandler implements AsyncUncaughtExceptionHandler {

	/**
	 * MOSIP logger used to record asynchronous mail send failures.
	 */
	Logger mosipLogger = LoggerConfiguration.logConfig(EmailNotificationAsyncHandler.class);

	/**
	 * Logs uncaught exceptions from asynchronous mail send operations, mapping
	 * Spring Mail exception types to notifier error codes.
	 *
	 * @param ex     the uncaught throwable
	 * @param method the asynchronous method that threw
	 * @param params the method arguments
	 */
	@Override
	public void handleUncaughtException(Throwable ex, Method method, Object... params) {
		switch (ex.getClass().toString()) {
		case MailNotifierExceptionClassNameConstants.MAIL_AUTH_EXCEPTION_CLASS_NAME:
			mosipLogger.error(MailNotifierConstants.EMPTY_STRING.getValue(),
					MailNotifierConstants.ERROR_CODE.getValue(),
					MailNotifierArgumentErrorConstants.MAIL_AUTHENTICATION_EXCEPTION_CODE.getErrorCode(),
					ex.getMessage());
			break;
		case MailNotifierExceptionClassNameConstants.MAIL_SENDMAIL_SEND_EXCEPTION_CLASS_NAME:
			mosipLogger.error(MailNotifierConstants.EMPTY_STRING.getValue(),
					MailNotifierConstants.ERROR_CODE.getValue(),
					MailNotifierArgumentErrorConstants.MAIL_SEND_EXCEPTION_CODE.getErrorCode(), ex.getMessage());
			break;
		default:
			mosipLogger.error(MailNotifierConstants.EMPTY_STRING.getValue(),
					MailNotifierConstants.ERROR_CODE.getValue(),
					MailNotifierArgumentErrorConstants.MAIL_EXCEPTION_CODE.getErrorCode(), ex.getMessage());
			break;
		}
	}
}
