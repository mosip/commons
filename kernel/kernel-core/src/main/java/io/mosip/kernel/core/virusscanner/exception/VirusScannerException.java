/**
 * Virus-scanner exceptions.
 */
package io.mosip.kernel.core.virusscanner.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when the antivirus service is unreachable or
 * scanning fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.virusscanner.spi.VirusScanner}
 * implementations on daemon, HTTP, or I/O failures. Callers should treat the
 * scanned artifact as not yet trusted.
 * </p>
 *
 * @author Mukul Puspam
 */
public class VirusScannerException extends BaseUncheckedException {
	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a virus-scanner exception with no detail.
	 */
	public VirusScannerException() {
		super();
	}

	/**
	 * Constructs a virus-scanner exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public VirusScannerException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs a virus-scanner exception with a root cause.
	 *
	 * @param errorCode never-null MOSIP error code
	 * @param message   never-null human-readable description
	 * @param cause     underlying cause; may be null
	 */
	public VirusScannerException(String errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
