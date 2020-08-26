package io.mosip.kernel.auth.defaultadapter.config;

import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.core.logger.spi.Logger;
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
	 * This method sets the logger target, and returns appender.
	 * 
	 * @param clazz the class.
	 * @return the appender.
	 */
	public static Logger logConfig(Class<?> clazz) {
		ConsoleAppender appender = new ConsoleAppender();
		appender.setTarget(AuthAdapterConstant.LOGGER_TARGET);
		return Logfactory.getDefaultConsoleLogger(appender, clazz);
	}
}
