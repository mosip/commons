/*
 * 
 * 
 */
package io.mosip.kernel.core.idvalidator.exception;

import io.mosip.kernel.core.exception.BaseUncheckedException;

/**
 * Unchecked exception thrown when a MOSIP identifier fails validation.
 * <p>
 * Contract: raised by UIN, VID, PRID, and RID validators for null, wrong
 * length, checksum, or filter-rule failures.
 * </p>
 *
 * @see io.mosip.kernel.core.idvalidator.spi.IdValidator
 * @author Megha Tanga
 * @since 1.0.0
 */
public class InvalidIDException extends BaseUncheckedException {

	/**
	 * Unique id for serialization
	 */
	private static final long serialVersionUID = -3556229489431119187L;

	/**
	 * Constructs an invalid-id exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalidIDException(String errorCode, String errorMessage) {

		super(errorCode, errorMessage);

	}
}
