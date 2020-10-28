package io.mosip.kernel.logger.logback.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jLoggerImpl  implements Logger  {

    private org.slf4j.Logger logger;
    private static final String LOGDISPLAY = "{} - {} - {} - {}";

    public Slf4jLoggerImpl(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void debug(String sessionId, String idType, String id, String description) {
        logger.debug(LOGDISPLAY, sessionId, idType, id, description);
    }

    @Override
    public void warn(String sessionId, String idType, String id, String description) {
        logger.warn(LOGDISPLAY, sessionId, idType, id, description);
    }

    @Override
    public void error(String sessionId, String idType, String id, String description) {
        logger.error(LOGDISPLAY, sessionId, idType, id, description);
    }

    @Override
    public void info(String sessionId, String idType, String id, String description) {
        logger.info(LOGDISPLAY, sessionId, idType, id, description);
    }

    @Override
    public void trace(String sessionId, String idType, String id, String description) {
        logger.trace(LOGDISPLAY, sessionId, idType, id, description);
    }
}
