package io.mosip.kernel.core.keymanager.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a requested JCA security provider is
 * unavailable.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.keymanager.spi.KeyStore}
 * when the configured HSM / PKCS#11 provider cannot be loaded.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public class NoSuchSecurityProviderException extends BaseUncheckedException {
	/**
	 * The generated serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public NoSuchSecurityProviderException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public NoSuchSecurityProviderException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}
}
