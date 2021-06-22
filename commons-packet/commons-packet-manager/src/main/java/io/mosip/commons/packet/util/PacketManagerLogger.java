package io.mosip.commons.packet.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.factory.Logfactory;

public class PacketManagerLogger {

    public static final String SESSIONID = "SESSION_ID";
    public static final String REGISTRATIONID = "REGISTRATION_ID";
    public static final String REFERENCEID = "REFERENCE_ID";
   
    /**
     * Instantiates a new packet manager logger.
     */
    private PacketManagerLogger() {
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
