package io.mosip.commons.packet.util;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.appender.RollingFileAppender;
import io.mosip.kernel.logger.logback.factory.Logfactory;

public class PacketManagerLogger {

    public static final String SESSIONID = "SESSION_ID";
    public static final String REGISTRATIONID = "REGISTRATION_ID";

    /**
     * The mosip rolling file appender.
     */
    private static RollingFileAppender mosipRollingFileAppender;

    static {
        mosipRollingFileAppender = new RollingFileAppender();
        mosipRollingFileAppender.setAppend(true);
        mosipRollingFileAppender.setAppenderName("fileappender");
        mosipRollingFileAppender.setFileName("logs/packetutility.log");
        mosipRollingFileAppender.setFileNamePattern("logs/packetutility-%d{yyyy-MM-dd}-%i.log");
        mosipRollingFileAppender.setImmediateFlush(true);
        mosipRollingFileAppender.setMaxFileSize("1mb");
        mosipRollingFileAppender.setMaxHistory(3);
        mosipRollingFileAppender.setPrudent(false);
        mosipRollingFileAppender.setTotalCap("10mb");
    }

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
        return Logfactory.getDefaultRollingFileLogger(mosipRollingFileAppender, clazz);
    }
}
