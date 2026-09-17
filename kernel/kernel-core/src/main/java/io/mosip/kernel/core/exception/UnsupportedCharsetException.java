package io.mosip.kernel.core.exception;

/**
 * Unchecked MOSIP wrapper when a requested charset is not supported.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.FileUtils} when a
 * charset name cannot be resolved.
 * </p>
 *
 * @author Priya Soni
 * @see io.mosip.kernel.core.util.constant.FileUtilConstants
 */
public class UnsupportedCharsetException extends BaseUncheckedException {

	private static final long serialVersionUID = -6711647152648795666L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public UnsupportedCharsetException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

}
