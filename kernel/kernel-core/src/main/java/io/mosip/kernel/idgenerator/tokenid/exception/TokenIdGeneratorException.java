package io.mosip.kernel.idgenerator.tokenid.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception when token-id seed or sequence persistence fails.
 */
public class TokenIdGeneratorException extends BaseUncheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 923629062510387031L;

	/**
	 * Creates an exception with code, message, and cause.
	 *
	 * @param errorCode    MOSIP error code
	 * @param errorMessage human-readable message
	 * @param rootCause    underlying cause
	 */
	public TokenIdGeneratorException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
