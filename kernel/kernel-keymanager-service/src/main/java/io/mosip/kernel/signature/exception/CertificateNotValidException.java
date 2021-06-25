package io.mosip.kernel.signature.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Class to handle exceptions for Signature verification certificate invalid.
 * 
 * @author Mahammed Taheer
 * @since 1.1.6
 *
 */
public class CertificateNotValidException extends BaseUncheckedException {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = -3069970234745966967L;

    /**
	 * Constructor for CryptoFailureException class.
	 * 
	 * @param errorCode    the error code.
	 * @param errorMessage the error message.
	 * @param rootCause    the cause.
	 */
	public CertificateNotValidException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructor for CryptoFailureException class.
	 * 
	 * @param errorCode    the error code.
	 * @param errorMessage the error message.
	 * @param rootCause    the cause.
	 */
	public CertificateNotValidException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
