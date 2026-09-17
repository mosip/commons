package io.mosip.kernel.core.saltgenerator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.saltgenerator.constant.SaltGeneratorErrorConstants;

/**
 * Unchecked exception thrown when the salt-generator job cannot populate salt rows.
 * <p>
 * Contract: {@link #getOperation()} is optional context for the failing step.
 * Raised instead of continuing the job when records already exist or insert fails.
 * </p>
 *
 * @author Manoj SP
 */
public class SaltGeneratorException extends BaseUncheckedException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6748760277721155095L;

	/** Optional name of the failing salt-generator operation; may be null. */
	private String operation;

	/**
	 * Constructs an empty exception with no error code.
	 */
	public SaltGeneratorException() {
		super();
	}

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public SaltGeneratorException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

	/**
	 * Constructs the exception with MOSIP error code, message, and cause.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 */
	public SaltGeneratorException(String errorCode, String errorMessage, Throwable rootCause) {
		super(errorCode, errorMessage, rootCause);
	}

	/**
	 * Constructs the exception from a {@link SaltGeneratorErrorConstants} value.
	 *
	 * @param exceptionConstant never-null error constant
	 */
	public SaltGeneratorException(SaltGeneratorErrorConstants exceptionConstant) {
		this(exceptionConstant.getErrorCode(), exceptionConstant.getErrorMessage());
	}

	/**
	 * Constructs the exception from a constant and cause.
	 *
	 * @param exceptionConstant never-null error constant
	 * @param rootCause         underlying cause; may be null
	 */
	public SaltGeneratorException(SaltGeneratorErrorConstants exceptionConstant, Throwable rootCause) {
		this(exceptionConstant.getErrorCode(), exceptionConstant.getErrorMessage(), rootCause);
	}

	/**
	 * Constructs the exception with an operation label.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param operation    failing step name; may be null
	 */
	public SaltGeneratorException(String errorCode, String errorMessage, String operation) {
		super(errorCode, errorMessage);
		this.operation = operation;
	}

	/**
	 * Constructs the exception with cause and an operation label.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 * @param rootCause    underlying cause; may be null
	 * @param operation    failing step name; may be null
	 */
	public SaltGeneratorException(String errorCode, String errorMessage, Throwable rootCause, String operation) {
		super(errorCode, errorMessage, rootCause);
		this.operation = operation;
	}

	/**
	 * Constructs the exception from a constant and an operation label.
	 *
	 * @param exceptionConstant never-null error constant
	 * @param operation         failing step name; may be null
	 */
	public SaltGeneratorException(SaltGeneratorErrorConstants exceptionConstant, String operation) {
		this(exceptionConstant.getErrorCode(), exceptionConstant.getErrorMessage());
		this.operation = operation;
	}

	/**
	 * Constructs the exception from a constant, cause, and operation label.
	 *
	 * @param exceptionConstant never-null error constant
	 * @param rootCause         underlying cause; may be null
	 * @param operation         failing step name; may be null
	 */
	public SaltGeneratorException(SaltGeneratorErrorConstants exceptionConstant, Throwable rootCause,
			String operation) {
		this(exceptionConstant.getErrorCode(), exceptionConstant.getErrorMessage(), rootCause);
		this.operation = operation;
	}

	/**
	 * Returns the optional failing-step label.
	 *
	 * @return operation name; may be null
	 */
	public String getOperation() {
		return operation;
	}

}
