/**
 * 
 */
package io.mosip.kernel.bioextractor.exception;

import io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants;
import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * @author Loganathan Sekar
 *
 */
public class BiometricExtractionException extends BaseCheckedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -527809804505218573L;

	/**
	 * 
	 */
	public BiometricExtractionException() {
	}

	/**
	 * @param errorMessage
	 */
	public BiometricExtractionException(String errorMessage) {
		super(errorMessage);
	}

	/**
	 * @param errorCode
	 * @param errorMessage
	 */
	public BiometricExtractionException(BiometricExtractionErrorConstants errConst) {
		this(errConst.getErrorCode(), errConst.getErrorMessage());
	}

	/**
	 * @param errorCode
	 * @param errorMessage
	 * @param rootCause
	 */
	public BiometricExtractionException(BiometricExtractionErrorConstants errConst, Throwable rootCause) {
		super(errConst.getErrorCode(), errConst.getErrorMessage(), rootCause);
	}
	
	/**
	 * @param errorCode
	 * @param errorMessage
	 */
	public BiometricExtractionException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * @param errorCode
	 * @param errorMessage
	 * @param rootCause
	 */
	public BiometricExtractionException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
