package io.mosip.kernel.core.exception;

/**
 * Checked exception thrown when biometric CBEFF signature validation fails.
 * <p>
 * Contract: raised when a BIR signature block cannot be verified. Callers
 * should treat the biometric payload as untrusted.
 * </p>
 *
 * @author Satish Gohil
 */
public class BiometricSignatureValidationException extends BaseCheckedException {

	private static final long serialVersionUID = 9190616446912282298L;

	/**
	 * Constructs the exception with a detail message only.
	 *
	 * @param message never-null human-readable description
	 */
	public BiometricSignatureValidationException(String message) {
		super(message);
	}
	
	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public BiometricSignatureValidationException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
