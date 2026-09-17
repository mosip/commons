package io.mosip.kernel.core.exception;

/**
 * Checked MOSIP wrapper when a character encoding is not supported.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.FileUtils}. Extends
 * {@link IOException}.
 * </p>
 *
 * @author Priya Soni
 * @see io.mosip.kernel.core.util.constant.FileUtilConstants
 */
public class UnsupportedEncodingException extends IOException {

	private static final long serialVersionUID = -8185171240584538662L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public UnsupportedEncodingException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public UnsupportedEncodingException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
