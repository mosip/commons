package io.mosip.kernel.zkcryptoservice.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Custom Exception Class in case of Random Key Decryption Exception.
 * 
 * @author Mahammed Taheer
 * @since 1.0.0
 *
 */
public class ZKRandomKeyDecryptionException extends BaseUncheckedException {

	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 8621530697947108810L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The errorcode for this exception
	 * @param errorMessage The error message for this exception
	 */
	public ZKRandomKeyDecryptionException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}