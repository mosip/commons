package io.mosip.kernel.core.keymanager.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a keystore alias does not exist.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.keymanager.spi.KeyStore}
 * getters when {@code alias} is unknown.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public class NoSuchAliasException extends BaseUncheckedException {
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
	public NoSuchAliasException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
