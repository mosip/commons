package io.mosip.kernel.openid.bridge.api.exception;
import java.util.List;

import org.springframework.http.HttpStatus;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import lombok.Getter;

/**
 * Unchecked exception that carries MOSIP {@link ServiceError} entries and the
 * HTTP status from a failed auth-manager or token-validation REST call.
 * <p>
 * Used when the auth-code proxy validates a token online and the auth manager
 * returns a MOSIP error list in the body.
 */
public class AuthRestException extends BaseUncheckedException {
	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 8152409863253682472L;

	/**
	 * HTTP status of the failed downstream response (for example 401 Unauthorized).
	 */
	@Getter
	private HttpStatus httpStatus;
	/**
	 * This variable holds the MosipErrors list.
	 */
	private final List<ServiceError> list;

	/**
	 * Creates an exception from MOSIP service errors and the HTTP status of the
	 * failed call.
	 *
	 * @param list The error list.
	 * @param httpStatus HTTP status associated with {@code list}
	 */
	public AuthRestException(List<ServiceError> list, HttpStatus httpStatus) {
		this.list = list;
		this.httpStatus = httpStatus;
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
