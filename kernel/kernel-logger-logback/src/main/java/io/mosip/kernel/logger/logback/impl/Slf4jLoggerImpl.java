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

    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    @Override
    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    @Override
    public void trace(String message, Object... args) {
        logger.trace(message, args);
    }

    @Override
    public void error(String message) {
        logger.error(message);
    }

    @Override
    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
