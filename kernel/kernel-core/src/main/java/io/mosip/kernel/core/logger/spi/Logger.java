/*
 *
 * 
 * 
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.logger.spi;

/**
 * Logging interface for Mosip
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public interface Logger {

	/**
	 * Logs at Debug logging level
	 * 
	 * @param sessionId   session id
	 * @param idType      type of id
	 * @param id          id value
	 * @param description description of log
	 */
	void debug(String sessionId, String idType, String id, String description);

	/**
	 * Logs at Warn logging level
	 * 
	 * @param sessionId   session id
	 * @param idType      type of id
	 * @param id          id value
	 * @param description description of log
	 */
	void warn(String sessionId, String idType, String id, String description);

	/**
	 * Logs at Error logging level
	 * 
	 * @param sessionId   session id
	 * @param idType      type of id
	 * @param id          id value
	 * @param description description of log
	 */
	void error(String sessionId, String idType, String id, String description);

	/**
	 * Logs at Info logging level
	 * 
	 * @param sessionId   session id
	 * @param idType      type of id
	 * @param id          id value
	 * @param description description of log
	 */
	void info(String sessionId, String idType, String id, String description);

	/**
	 * Logs at Trace logging level
	 * 
	 * @param sessionId   session id
	 * @param idType      type of id
	 * @param id          id value
	 * @param description description of log
	 */
	void trace(String sessionId, String idType, String id, String description);

	/**
	 * Default debug logging
	 */
	void debug(String message);

	/**
	 * Default debug logging with format and arguments
	 */
	void debug(String message, Object... args);

	/**
	 * Default info logging
	 */
	void info(String message);

	/**
	 * Default info logging with format and arguments
	 */
	void info(String message, Object... args);

	/**
	 * Default warn logging
	 */
	void warn(String message);

	/**
	 * Default warn logging with format and arguments
	 */
	void warn(String message, Object... args);

	/**
	 * Default trace logging
	 */
	void trace(String message);

	/**
	 * Default trace logging with format and arguments
	 */
	void trace(String message, Object... args);

	/**
	 * Default error logging
	 */
	void error(String message);

	/**
	 * Default error logging with format and arguments
	 */
	void error(String message, Object... args);

	/**
	 * Default error logging with stacktrace logging
	 */
	void error(String message, Throwable throwable);
}
