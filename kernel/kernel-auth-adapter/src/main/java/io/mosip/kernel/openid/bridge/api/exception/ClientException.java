
package io.mosip.kernel.openid.bridge.api.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception for client-side failures in the OIDC authorization-code
 * flow (for example CSRF {@code state} mismatch).
 * <p>
 * Error codes typically come from {@link io.mosip.kernel.openid.bridge.api.constants.Errors}.
 * Arguments are passed to {@link BaseUncheckedException} as
 * {@code (errorMessage, errorCode)} — message first, unlike
 * {@link ServiceException}.
 */
public class ClientException extends BaseUncheckedException {

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
	public ClientException(String errorCode, String errorMessage) {
		super(errorMessage, errorCode);
	}

	/**
	 * Creates an exception that wraps the underlying cause.
	 *
	 * @param errorCode    MOSIP error code (for example {@code KER-ACP-007})
	 * @param errorMessage human-readable message
	 * @param cause        original failure
	 */
	public ClientException(String errorCode, String errorMessage, Throwable cause) {
		super(errorMessage, errorCode, cause);
	}
}
