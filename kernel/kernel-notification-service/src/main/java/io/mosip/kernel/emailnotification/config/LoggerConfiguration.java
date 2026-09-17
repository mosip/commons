package io.mosip.kernel.emailnotification.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.emailnotification.constant.MailNotifierConstants;
import io.mosip.kernel.logger.logback.appender.ConsoleAppender;
import io.mosip.kernel.logger.logback.factory.Logfactory;

/**
 * Console Logger Configuration.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
public class LoggerConfiguration {
	/**
	 * Private Constructor to prevent instantiation.
	 */
	private LoggerConfiguration() {
	}

	/**
	 * Returns a MOSIP SLF4J {@link Logger} for the given class.
	 * 
	 * @param clazz the class requesting the logger
	 * @return the logger instance
	 */
	public static Logger logConfig(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}
}
