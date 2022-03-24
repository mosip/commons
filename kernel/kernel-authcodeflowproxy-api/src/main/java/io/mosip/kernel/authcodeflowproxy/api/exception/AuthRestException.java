package io.mosip.kernel.authcodeflowproxy.api.exception;
import java.util.List;

import org.springframework.http.HttpStatus;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ServiceError;
import lombok.Getter;

public class AuthRestException extends BaseUncheckedException {
	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 8152409863253682472L;

	@Getter
	private HttpStatus httpStatus;
	/**
	 * This variable holds the MosipErrors list.
	 */
	private final List<ServiceError> list;

	/**
	 * @param list The error list.
	 * @param httpStatus 
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