package io.mosip.kernel.core.exception;

import lombok.Data;

/**
 * Single MOSIP service error carried in HTTP {@link ErrorResponse} and
 * {@link io.mosip.kernel.core.http.ResponseWrapper} bodies.
 * <p>
 * Contract: {@code errorCode} should be a MOSIP code; {@code message} is
 * human-readable. Either field may be null on partial DTOs. Does not perform
 * I/O.
 * </p>
 *
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@Data
public class ServiceError {

	/**
	 * MOSIP error code such as {@code KER-ATH-401}; may be null.
	 */
	private String errorCode;
	/**
	 * Human-readable error description; may be null or empty.
	 */
	private String message;

	/**
	 * Constructs a service error with code and message.
	 *
	 * @param errorCode    MOSIP error code; may be null
	 * @param errorMessage human-readable description; may be null
	 */
	public ServiceError(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.message = errorMessage;
	}

	/**
	 * Constructs an empty service error for JSON deserialization.
	 */
	public ServiceError() {

	}

}
