package io.mosip.kernel.core.idobjectvalidator.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

public class InvalidIdSchemaException extends BaseCheckedException  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8957950467794649169L;

	/**
	 * Constructor for Invalid IdSchema Exception class.
	 * 
	 * @param errorCode    the error code of the exception.
	 * @param errorMessage the error message associated with the exception.
	 */
	public InvalidIdSchemaException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
