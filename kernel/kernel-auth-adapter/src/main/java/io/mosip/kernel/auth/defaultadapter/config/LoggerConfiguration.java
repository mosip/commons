package io.mosip.kernel.auth.defaultadapter.config;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

/**
 * Factory for SLF4J loggers used by this adapter.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath. Logging
 * goes through {@code kernel-core}'s {@link Logfactory} rather than a
 * service-local appender.
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 */
public class LoggerConfiguration {
	/**
	 * Private constructor to prevent instantiation.
	 */
	private LoggerConfiguration() {
	}

	/**
	 * Returns an SLF4J {@link Logger} bound to the given class.
	 *
	 * @param clazz the class that will emit log statements
	 * @return the logger for {@code clazz}
	 */
	public static Logger logConfig(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}
}
