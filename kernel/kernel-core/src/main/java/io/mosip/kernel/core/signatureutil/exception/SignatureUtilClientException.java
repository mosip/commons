package io.mosip.kernel.core.signatureutil.exception;

import java.util.List;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * Unchecked exception carrying keymanager {@link ServiceError} entries from a failed signature call.
 * <p>
 * Contract: {@link #getList()} is never null after construction. Raised when
 * the remote service returns MOSIP errors instead of a signature.
 * </p>
 *
 * @author Srinivasan
 */
public class SignatureUtilClientException extends BaseUncheckedException {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 8152409863253682472L;

	/**
	 * MOSIP service errors from the keymanager response; never null after construction.
	 */
	private final List<ServiceError> list;

	/**
	 * Constructs the exception with the remote error list.
	 *
	 * @param list never-null list of {@link ServiceError}; may be empty
	 */
	public SignatureUtilClientException(List<ServiceError> list) {
		this.list = list;
	}

	/**
	 * Returns the remote MOSIP errors.
	 *
	 * @return never-null error list supplied at construction
	 */
	public List<ServiceError> getList() {
		return list;
	}
}
