package io.mosip.kernel.core.authmanager.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Unchecked exception thrown when authentication (AuthN) fails with one or more
 * MOSIP service errors.
 * <p>
 * Contract: constructed with a non-null error list from the auth provider.
 * Callers should inspect {@link #getList()} rather than relying on
 * {@link #getMessage()}. Does not perform I/O.
 * </p>
 *
 * @see ServiceError
 * @see AuthZException
 *
 * @author Srinivasan
 */
public class AuthNException extends BaseUncheckedException {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 8152409863253682472L;

	/**
	 * Service errors returned by the authentication provider; never mutated after
	 * construction.
	 */
	private final List<ServiceError> list;

	/**
	 * Constructs an authentication exception wrapping the given service errors.
	 *
	 * @param list never-null list of {@link ServiceError}; may be empty
	 */
	public AuthNException(List<ServiceError> list) {
		this.list = list;
	}

	/**
	 * Returns the authentication errors supplied at construction.
	 *
	 * @return the error list; never null, but may be empty; same instance as
	 *         passed to the constructor
	 */
	public List<ServiceError> getList() {
		return list;
	}
}
