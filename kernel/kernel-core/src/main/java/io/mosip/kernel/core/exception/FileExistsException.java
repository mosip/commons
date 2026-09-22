package io.mosip.kernel.core.exception;

/**
 * Checked exception thrown when a destination file already exists.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.FileUtils} when a copy
 * or create would overwrite an existing file. Extends {@link IOException}.
 * </p>
 *
 * @author Priya Soni
 * @see io.mosip.kernel.core.util.constant.FileUtilConstants
 */
public class FileExistsException extends IOException {

	private static final long serialVersionUID = 2842522173494167519L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public FileExistsException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);

	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public FileExistsException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);

	}

}
