package io.mosip.kernel.core.util.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when {@link io.mosip.kernel.core.util.HashUtils} fails.
 * <p>
 * Contract: raised for invalid hash parameters rather than returning a
 * default digest. Callers must handle or declare this exception.
 * </p>
 *
 * @version 1.0 14 August 2018
 * @author Jyoti Prakash Nayak
 */
public class HashUtilException extends BaseCheckedException {
	/** Serializable version Id. */
	private static final long serialVersionUID = 924722202110630628L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param arg0 never-null MOSIP error code
	 * @param arg1 never-null human-readable description
	 */
	public HashUtilException(String arg0, String arg1) {
		super(arg0, arg1);

	}
}
