/**
 * 
 */
package io.mosip.kernel.auth.defaultadapter.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.AuthenticationException;

import io.mosip.kernel.core.exception.ServiceError;

/**
 * Authentication failure thrown when token validation or related auth-manager
 * checks fail.
 * <p>
 * {@link io.mosip.kernel.auth.defaultadapter.filter.AuthFilter} maps this to an
 * HTTP 401 MOSIP {@code ResponseWrapper} of {@link ServiceError}s.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author M1049825
 */
public class AuthManagerException extends AuthenticationException {

	/**
	 * MOSIP error code associated with this exception.
	 */
	private String errorCode;
	/**
	 * Serialization identifier.
	 */
	private static final long serialVersionUID = 4060346018688709387L;

	/**
	 * Creates an exception with an error code and message.
	 *
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 */
	public AuthManagerException(String errorCode, String errorMessage) {
		super(errorMessage);
		this.errorCode = errorCode;
	}

	/**
	 * Creates an exception with an error code, message, and root cause.
	 *
	 * @param errorCode    The error code for this exception
	 * @param errorMessage The error message for this exception
	 * @param rootCause    the underlying cause
	 */
	public AuthManagerException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorMessage, rootCause);
		this.errorCode = errorCode;
	}

	/**
	 * MOSIP {@code KER-ATH-*} code for this failure.
	 *
	 * @return error code, or {@code null} when only the list constructor was used
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * This variable holds the MosipErrors list.
	 */
	private List<ServiceError> list = new ArrayList<>();

	/**
	 * Creates an exception whose message is {@code errorCode} and whose
	 * {@link #list} is the given service errors.
	 *
	 * @param errorCode used as the exception message
	 * @param list      The error list.
	 */
	public AuthManagerException(String errorCode, List<ServiceError> list) {
		super(errorCode);
		this.list = list;
	}

	/**
	 * Getter for error list.
	 *
	 * @return The error list.
	 */
	public List<ServiceError> getList() {
		return list;
	}
}
