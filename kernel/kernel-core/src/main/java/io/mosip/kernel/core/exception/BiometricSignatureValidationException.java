package io.mosip.kernel.core.exception;

/**
 * @author Satish Gohil
 *
 */
public class BiometricSignatureValidationException extends BaseCheckedException {

	private static final long serialVersionUID = 9190616446912282298L;

	public BiometricSignatureValidationException(String message) {
		super(message);
	}
	
	public BiometricSignatureValidationException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
