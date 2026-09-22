package io.mosip.kernel.core.pdfgenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when PDF generation, merge, or signing fails.
 * <p>
 * Contract: wraps iText / Flying Saucer failures from
 * {@link io.mosip.kernel.core.pdfgenerator.spi.PDFGenerator}. Callers should
 * treat the PDF as not produced.
 * </p>
 *
 * @author M1046571
 * @since 1.0.0
 */
public class PDFGeneratorException extends BaseUncheckedException {

	private static final long serialVersionUID = -6138841548758442351L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public PDFGeneratorException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public PDFGeneratorException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
