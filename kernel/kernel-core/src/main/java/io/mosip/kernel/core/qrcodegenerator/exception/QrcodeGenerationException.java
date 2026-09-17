package io.mosip.kernel.core.qrcodegenerator.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when QR-code encoding fails.
 * <p>
 * Contract: wraps Writer-framework failures from
 * {@link io.mosip.kernel.core.qrcodegenerator.spi.QrCodeGenerator}. Callers
 * must handle or declare this exception.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class QrcodeGenerationException extends BaseCheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 473719335574042491L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying Writer / I/O cause; may be null
	 */
	public QrcodeGenerationException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
