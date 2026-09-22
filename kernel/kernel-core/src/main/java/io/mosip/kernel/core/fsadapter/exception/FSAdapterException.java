package io.mosip.kernel.core.fsadapter.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when DFS / HDFS packet storage fails.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter}
 * implementations on connectivity, permission, or I/O failures.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public class FSAdapterException extends BaseUncheckedException {

	/**
	 * Generated serialVersionUID
	 */
	private static final long serialVersionUID = 5074628123959874252L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public FSAdapterException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public FSAdapterException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
