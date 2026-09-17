package io.mosip.kernel.logger.logback.constant;

/**
 * Selects the MOSIP logger implementation used by {@link io.mosip.kernel.logger.logback.factory.Logfactory}.
 */
public enum LoggerMethod {
	/** Logback-backed {@link io.mosip.kernel.logger.logback.impl.LoggerImpl}. */
	MOSIPLOGBACK;
}
