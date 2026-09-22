package io.mosip.kernel.openid.bridge.api.authmanager.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Unchecked authentication (AuthN) failure that carries MOSIP
 * {@link ServiceError} entries from auth-manager login or token issuance.
 * 
 * @author Srinivasan
 *
 */
public class AuthNException extends BaseUncheckedException {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 8152409863253682472L;

	/**
	 * This variable holds the MosipErrors list.
	 */
	private final List<ServiceError> list;

	/**
	 * Creates an AuthN failure from MOSIP service errors (typically parsed from an
	 * auth-manager error response).
	 *
	 * @param list The error list.
	 */
	public AuthNException(List<ServiceError> list) {
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
