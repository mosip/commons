package io.mosip.kernel.openid.bridge.api.authmanager.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Unchecked authorization (AuthZ) failure that carries MOSIP
 * {@link ServiceError} entries from token validation or access-denied paths.
 * 
 * @author Srinivasan
 *
 */
public class AuthZException extends BaseUncheckedException {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 8152409863253682472L;

	/**
	 * This variable holds the MosipErrors list.
	 */
	private final List<ServiceError> list;

	/**
	 * Creates an AuthZ failure from MOSIP service errors (typically parsed from a
	 * token-validation or access-denied response).
	 *
	 * @param list The error list.
	 */
	public AuthZException(List<ServiceError> list) {
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
