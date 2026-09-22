package io.mosip.kernel.emailnotification.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Exception class to handle invalid email arguments. Carries a list of
 * {@link ServiceError} entries for the failed request.
 * 
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
public class InvalidArgumentsException extends BaseUncheckedException {
	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = -1416474253520030879L;
	/**
	 * Validation errors collected for the failed mail request.
	 */
	private final List<ServiceError> list;

	/**
	 * Instantiates the exception with the given validation errors.
	 *
	 * @param list the error list; must not be {@code null}
	 */
	public InvalidArgumentsException(List<ServiceError> list) {
		this.list = list;
	}

	/**
	 * Returns the validation error list.
	 * 
	 * @return the error list
	 */
	public List<ServiceError> getList() {
		return list;
	}
}
