package io.mosip.kernel.core.idgenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a generated or requested UIN is invalid.
 * <p>
 * Contract: raised when a UIN fails checksum, length, or filter rules during
 * generation or assignment from the pool.
 * </p>
 *
 * @author M1043226
 * @since 1.0.0
 */
public class InValidUinException extends BaseUncheckedException {
	/**
	 * The generated serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an invalid-UIN exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InValidUinException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
