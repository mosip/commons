/*
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.core.logger.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a logger file name is null or empty.
 * <p>
 * Contract: raised during MOSIP logger configuration when the rolling-file
 * name is missing.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class FileNameNotProvided extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = 105555532L;

	/**
	 * Constructs the exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public FileNameNotProvided(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}

}
