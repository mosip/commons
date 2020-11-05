package io.mosip.kernel.biosdk.provider.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;


public class BioSDKProviderLoggerFactory {
	
	/**
	 * Instantiates a new bio sdk logger.
	 */
	private BioSDKProviderLoggerFactory() {
	}

	/**
	 * Gets the logger.
	 *
	 * @param clazz the clazz
	 * @return the logger
	 */
	public static Logger getLogger(Class<?> clazz) {
		return Logfactory.getSlf4jLogger(clazz);
	}

}
