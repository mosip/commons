package io.mosip.kernel.auth.defaultimpl.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.appender.ConsoleAppender;
import io.mosip.kernel.logger.logback.factory.Logfactory;

/**
 * Console Logger Configuration for defaultimpl classes.
 * <p>
 * Obtains kernel-core SLF4J loggers via {@link Logfactory}; do not use a
 * separate logging artifact.
 * 
 * @author Bal Vikash Sharma
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
	 * @param clazz the class that will emit log events
	 * @return SLF4J {@link Logger} bound to {@code clazz}
	 */
	public static Logger logConfig(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}
}
