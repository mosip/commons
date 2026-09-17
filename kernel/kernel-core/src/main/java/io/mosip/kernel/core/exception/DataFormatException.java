package io.mosip.kernel.core.exception;

/**
 * Checked exception thrown when zip or stream data is not in the expected
 * format.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.ZipUtils} when bytes
 * are not a valid zip archive. Extends {@link IOException}.
 * </p>
 *
 * @author Megha Tanga
 * @see io.mosip.kernel.core.util.constant.ZipUtilConstants
 */
public class DataFormatException extends IOException {

	private static final long serialVersionUID = -1762806620894866489L;

	/**
	 * Constructs a data-format exception with MOSIP error code, message, and
	 * cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public DataFormatException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Constructs a data-format exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public DataFormatException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

}
