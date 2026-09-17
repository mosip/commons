package io.mosip.kernel.core.exception;

/**
 * Unchecked exception thrown when a directory cannot be deleted because it is
 * not empty.
 * <p>
 * Contract: raised by {@link io.mosip.kernel.core.util.FileUtils} on
 * non-empty directory delete. Callers should empty the directory first.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class DirectoryNotEmptyException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -381238520404127950L;

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param cause        underlying cause; may be null
	 */
	public DirectoryNotEmptyException(String errorCode, String errorMessage, Throwable cause) {
		super(errorCode, errorMessage, cause);
	}

}
