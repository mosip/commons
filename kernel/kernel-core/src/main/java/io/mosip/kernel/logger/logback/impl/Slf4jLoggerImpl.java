package io.mosip.kernel.logger.logback.impl;

import io.mosip.kernel.core.logger.spi.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Logger} that delegates to SLF4J {@link org.slf4j.LoggerFactory}.
 * <p>
 * Session-aware methods format four fields as {@code sessionId - idType - id - description}.
 * </p>
 */
public class Slf4jLoggerImpl  implements Logger  {

    private org.slf4j.Logger logger;
    private static final String LOGDISPLAY = "{} - {} - {} - {}";

    /**
     * Creates a logger named after {@code clazz}.
     *
     * @param clazz class used as the SLF4J logger name
     */
    public Slf4jLoggerImpl(Class<?> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    /**
     * Logs at DEBUG with MOSIP session fields.
     *
     * @param sessionId   session identifier
     * @param idType      identity type (for example UIN)
     * @param id          identity value
     * @param description message
     */
    @Override
    public void debug(String sessionId, String idType, String id, String description) {
        logger.debug(LOGDISPLAY, sessionId, idType, id, description);
    }

    /**
     * Logs at WARN with MOSIP session fields.
     *
     * @param sessionId   session identifier
     * @param idType      identity type (for example UIN)
     * @param id          identity value
     * @param description message
     */
    @Override
    public void warn(String sessionId, String idType, String id, String description) {
        logger.warn(LOGDISPLAY, sessionId, idType, id, description);
    }

    /**
     * Logs at ERROR with MOSIP session fields.
     *
     * @param sessionId   session identifier
     * @param idType      identity type (for example UIN)
     * @param id          identity value
     * @param description message
     */
    @Override
    public void error(String sessionId, String idType, String id, String description) {
        logger.error(LOGDISPLAY, sessionId, idType, id, description);
    }

    /**
     * Logs at INFO with MOSIP session fields.
     *
     * @param sessionId   session identifier
     * @param idType      identity type (for example UIN)
     * @param id          identity value
     * @param description message
     */
    @Override
    public void info(String sessionId, String idType, String id, String description) {
        logger.info(LOGDISPLAY, sessionId, idType, id, description);
    }

    /**
     * Logs at TRACE with MOSIP session fields.
     *
     * @param sessionId   session identifier
     * @param idType      identity type (for example UIN)
     * @param id          identity value
     * @param description message
     */
    @Override
    public void trace(String sessionId, String idType, String id, String description) {
        logger.trace(LOGDISPLAY, sessionId, idType, id, description);
    }

    /**
     * Logs a DEBUG message.
     *
     * @param message text to log
     */
    @Override
    public void debug(String message) {
        logger.debug(message);
    }

    /**
     * Logs a DEBUG message with SLF4J placeholders.
     *
     * @param message pattern with {@code {}} placeholders
     * @param args    placeholder values
     */
    @Override
    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    /**
     * Logs an INFO message.
     *
     * @param message text to log
     */
    @Override
    public void info(String message) {
        logger.info(message);
    }

    /**
     * Logs an INFO message with SLF4J placeholders.
     *
     * @param message pattern with {@code {}} placeholders
     * @param args    placeholder values
     */
    @Override
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    /**
     * Logs a WARN message.
     *
     * @param message text to log
     */
    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    /**
     * Logs a WARN message with SLF4J placeholders.
     *
     * @param message pattern with {@code {}} placeholders
     * @param args    placeholder values
     */
    @Override
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    /**
     * Logs a TRACE message.
     *
     * @param message text to log
     */
    @Override
    public void trace(String message) {
        logger.trace(message);
    }

    /**
     * Logs a TRACE message with SLF4J placeholders.
     *
     * @param message pattern with {@code {}} placeholders
     * @param args    placeholder values
     */
    @Override
    public void trace(String message, Object... args) {
        logger.trace(message, args);
    }

    /**
     * Logs an ERROR message.
     *
     * @param message text to log
     */
    @Override
    public void error(String message) {
        logger.error(message);
    }

    /**
     * Logs an ERROR message with SLF4J placeholders.
     *
     * @param message pattern with {@code {}} placeholders
     * @param args    placeholder values
     */
    @Override
    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    /**
     * Logs an ERROR message with a stack trace.
     *
     * @param message   text to log
     * @param throwable cause to attach
     */
    @Override
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }
}
