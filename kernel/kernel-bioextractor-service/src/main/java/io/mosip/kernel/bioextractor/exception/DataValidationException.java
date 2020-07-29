package io.mosip.kernel.bioextractor.exception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.mosip.kernel.bioextractor.config.constant.BiometricExtractionErrorConstants;
import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * The Class DataValidationException - Thrown when any Data validation error
 * occurs.
 *
 * @author Loganathan Sekar
 */
public class DataValidationException extends BiometricExtractionException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8736814351095768205L;
	
	/** The Constant args. */
	final private transient List<Object[]> args = new ArrayList<>();


	/**
	 * Instantiates a new ID data validation exception.
	 */
	public DataValidationException() {
		super();
	}

	/**
	 * Instantiates a new ID data validation exception.
	 *
	 * @param exceptionConstant the exception constant
	 * @param args              the args
	 */
	public DataValidationException(BiometricExtractionErrorConstants exceptionConstant, Object... args) {
		super(exceptionConstant);
		if (args.length != 0) {
			this.args.add(args);
		} else {
			this.args.add(null);
		}
	}

	/**
	 * Instantiates a new ID data validation exception.
	 *
	 * @param exceptionConstant the exception constant
	 * @param rootCause         the root cause
	 * @param args              the args
	 */
	public DataValidationException(BiometricExtractionErrorConstants exceptionConstant, Throwable rootCause,
			Object... args) {
		super(exceptionConstant, rootCause);
		if (args.length != 0) {
			this.args.add(args);
		} else {
			this.args.add(null);
		}
	}

	/**
	 * Instantiates a new ID data validation exception.
	 *
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 * @param args         the args
	 */
	public DataValidationException(String errorCode, String errorMessage, Object... args) {
		super(errorCode, errorMessage);
		if (args.length != 0) {
			this.args.add(args);
		} else {
			this.args.add(null);
		}
	}

	/**
	 * Constructs exception for the given error code, error message and
	 * {@code Throwable}.
	 *
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 * @param cause        the cause
	 * @param args         the args
	 * @see BaseUncheckedException#BaseUncheckedException(String, String, Throwable)
	 */
	public DataValidationException(String errorCode, String errorMessage, Throwable cause, Object... args) {
		super(errorCode, errorMessage, cause);
		if (args.length != 0) {
			this.args.add(args);
		} else {
			this.args.add(null);
		}
	}

	/**
	 * Adds the info.
	 *
	 * @param errorCode    the error code
	 * @param errorMessage the error message
	 * @param actionMesgsage the action mesgsage
	 * @param args         the args
	 */
	public void addInfo(String errorCode, String errorMessage, Object... args) {
		String msg = Optional.ofNullable(errorMessage).orElseGet(() -> "");
		super.addInfo(errorCode, msg);
		this.args.add(args);
	}

	/**
	 * Clear agrs.
	 */
	public void clearArgs() {
		this.args.clear();
	}

	/**
	 * Gets the args.
	 *
	 * @return the args
	 */
	public List<Object[]> getArgs() {
		return Collections.unmodifiableList(args);
	}

}
