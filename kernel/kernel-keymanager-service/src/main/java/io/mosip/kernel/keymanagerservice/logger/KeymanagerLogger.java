package io.mosip.kernel.keymanagerservice.logger;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

/**
 * Keymanager logger.
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public final class KeymanagerLogger {

	/**
	 * Instantiates a new logger.
	 */
	private KeymanagerLogger() {
	}

	/**
	 * Method to get the rolling file logger for the class provided.
	 *
	 * @param clazz the clazz
	 * @return the logger
	 */
	public static Logger getLogger(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}
}