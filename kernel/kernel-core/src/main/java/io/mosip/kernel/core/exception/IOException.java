package io.mosip.kernel.core.exception;

/**
 * Checked MOSIP wrapper for failed or interrupted I/O.
 * <p>
 * Contract: superclass of {@link FileNotFoundException},
 * {@link FileExistsException}, {@link DataFormatException}, and
 * {@link UnsupportedEncodingException}. Raised by file, zip, and JSON
 * utilities.
 * </p>
 *
 * @author Priya Soni
 * @author Sidhant Agarwal
 */
public class IOException extends BaseCheckedException {

	private static final long serialVersionUID = 7464354823823721387L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public IOException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public IOException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

}
