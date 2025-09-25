package io.mosip.kernel.websub.api.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;

/**
 * Generic unchecked exception for the MOSIP WebSub API.
 * <p>
 * Extends {@link BaseUncheckedException} to handle errors in WebSub operations (e.g., topic registration,
 * subscription, intent verification) using error codes from {@link WebSubClientErrorCode}. Thrown by
 * components like {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl} and
 * {@link io.mosip.kernel.websub.api.filter.IntentVerificationFilter}. Supports constructors for
 * error code/message pairs and wrapping other exceptions.
 * </p>
 * <p>
 * Example Usage:
 * <pre>
 * throw new WebSubClientException(WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorCode(),
 *                                WebSubClientErrorCode.SUBSCRIBE_ERROR.getErrorMessage());
 * </pre>
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see WebSubClientErrorCode
 */
public class WebSubClientException extends BaseUncheckedException {

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
	public WebSubClientException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs an exception with an error code, message, and cause.
	 *
	 * @param errorCode    the error code (e.g., KER-WSC-101)
	 * @param errorMessage the error message
	 * @param rootCause    the cause of the error
	 */
	public WebSubClientException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs an exception wrapping another throwable with a default internal error code.
	 *
	 * @param rootCause the cause of the error
	 */
	public WebSubClientException(Throwable rootCause) {
		super(WebSubClientErrorCode.INTERNAL_ERROR.getErrorCode(),
				WebSubClientErrorCode.INTERNAL_ERROR.getErrorMessage(),
				rootCause);
	}
}