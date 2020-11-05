package io.mosip.kernel.packetmanager.logger;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

public class PacketUtilityLogger {

	/**
	 * Instantiates a new reg processor logger.
	 */
	private PacketUtilityLogger() {
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
