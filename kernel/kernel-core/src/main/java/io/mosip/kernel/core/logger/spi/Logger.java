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
 * MOSIP structured logger used by kernel and consuming modules.
 * <p>
 * Contract: implementations write to Logback or similar; they do not throw
 * MOSIP exceptions on log failure. Session / id arguments may be empty
 * strings when unused. SLF4J-style {@code {}} placeholders are supported on
 * the overloaded methods. Call from application code instead of using SLF4J
 * directly when MOSIP correlation fields are required.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public interface Logger {

	/**
	 * Logs at DEBUG with MOSIP correlation fields.
	 *
	 * @param sessionId   session or request id; may be empty
	 * @param idType      type of {@code id} such as UIN; may be empty
	 * @param id          identifier value; may be empty
	 * @param description log text; never null
	 */
	void debug(String sessionId, String idType, String id, String description);

	/**
	 * Logs at WARN with MOSIP correlation fields.
	 *
	 * @param sessionId   session or request id; may be empty
	 * @param idType      type of {@code id}; may be empty
	 * @param id          identifier value; may be empty
	 * @param description log text; never null
	 */
	void warn(String sessionId, String idType, String id, String description);

	/**
	 * Logs at ERROR with MOSIP correlation fields.
	 *
	 * @param sessionId   session or request id; may be empty
	 * @param idType      type of {@code id}; may be empty
	 * @param id          identifier value; may be empty
	 * @param description log text; never null
	 */
	void error(String sessionId, String idType, String id, String description);

	/**
	 * Logs at INFO with MOSIP correlation fields.
	 *
	 * @param sessionId   session or request id; may be empty
	 * @param idType      type of {@code id}; may be empty
	 * @param id          identifier value; may be empty
	 * @param description log text; never null
	 */
	void info(String sessionId, String idType, String id, String description);

	/**
	 * Logs at TRACE with MOSIP correlation fields.
	 *
	 * @param sessionId   session or request id; may be empty
	 * @param idType      type of {@code id}; may be empty
	 * @param id          identifier value; may be empty
	 * @param description log text; never null
	 */
	void trace(String sessionId, String idType, String id, String description);

	/**
	 * Logs a DEBUG message without correlation fields.
	 *
	 * @param message never-null log text
	 */
	void debug(String message);

	/**
	 * Logs a DEBUG message with SLF4J-style format arguments.
	 *
	 * @param message never-null format string with {@code {}} placeholders
	 * @param args    format arguments; may be empty
	 */
	void debug(String message, Object... args);

	/**
	 * Logs an INFO message without correlation fields.
	 *
	 * @param message never-null log text
	 */
	void info(String message);

	/**
	 * Logs an INFO message with SLF4J-style format arguments.
	 *
	 * @param message never-null format string with {@code {}} placeholders
	 * @param args    format arguments; may be empty
	 */
	void info(String message, Object... args);

	/**
	 * Logs a WARN message without correlation fields.
	 *
	 * @param message never-null log text
	 */
	void warn(String message);

	/**
	 * Logs a WARN message with SLF4J-style format arguments.
	 *
	 * @param message never-null format string with {@code {}} placeholders
	 * @param args    format arguments; may be empty
	 */
	void warn(String message, Object... args);

	/**
	 * Logs a TRACE message without correlation fields.
	 *
	 * @param message never-null log text
	 */
	void trace(String message);

	/**
	 * Logs a TRACE message with SLF4J-style format arguments.
	 *
	 * @param message never-null format string with {@code {}} placeholders
	 * @param args    format arguments; may be empty
	 */
	void trace(String message, Object... args);

	/**
	 * Logs an ERROR message without correlation fields.
	 *
	 * @param message never-null log text
	 */
	void error(String message);

	/**
	 * Logs an ERROR message with SLF4J-style format arguments.
	 *
	 * @param message never-null format string with {@code {}} placeholders
	 * @param args    format arguments; may be empty
	 */
	void error(String message, Object... args);

	/**
	 * Logs an ERROR message and the throwable stack.
	 *
	 * @param message   never-null log text
	 * @param throwable never-null cause to attach
	 */
	void error(String message, Throwable throwable);
}
