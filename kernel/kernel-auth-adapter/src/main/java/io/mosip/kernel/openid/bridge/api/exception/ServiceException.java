
package io.mosip.kernel.openid.bridge.api.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception for downstream IAM or MOSIP service failures (token
 * endpoint, keymanager sign, HTTP parse errors).
 * <p>
 * Error codes typically come from {@link io.mosip.kernel.openid.bridge.api.constants.Errors}.
 * Arguments are passed to {@link BaseUncheckedException} as
 * {@code (errorCode, errorMessage)} — code first, unlike {@link ClientException}.
 */
public class ServiceException extends BaseUncheckedException {

	/**
	 * Serialization identifier.
	 */
	private static final long serialVersionUID = 4060346018688709387L;

	/**
	 * Constructor the initialize Handler exception
	 * 
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public ServiceException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Creates an exception that wraps the underlying cause.
	 *
	 * @param errorCode    MOSIP error code (for example {@code KER-ACP-006})
	 * @param errorMessage human-readable message
	 * @param cause        original failure (HTTP or parse error)
	 */
	public ServiceException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}
}
