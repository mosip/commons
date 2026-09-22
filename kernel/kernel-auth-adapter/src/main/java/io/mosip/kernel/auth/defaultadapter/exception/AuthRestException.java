package io.mosip.kernel.auth.defaultadapter.exception;
import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Unchecked exception wrapping MOSIP {@link ServiceError} entries parsed from
 * an OIDC or auth-service HTTP response body.
 * <p>
 * Thrown by {@link io.mosip.kernel.auth.defaultadapter.helper.TokenHelper}
 * when the token endpoint returns a MOSIP error envelope.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
public class AuthRestException extends BaseUncheckedException {
	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 8152409863253682472L;

	/**
	 * This variable holds the MosipErrors list.
	 */
	private final List<ServiceError> list;

	/**
	 * Creates an exception holding the given service errors.
	 *
	 * @param list The error list.
	 */
	public AuthRestException(List<ServiceError> list) {
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
