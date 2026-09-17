package io.mosip.kernel.core.idobjectvalidator.exception;

import io.mosip.kernel.core.exception.BaseCheckedException;

/**
 * Checked exception thrown when an identity-object JSON schema is missing or
 * malformed.
 * <p>
 * Contract: raised by
 * {@link io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator} when
 * the schema cannot be loaded or parsed. Callers should treat this as a
 * configuration error, not an applicant-data error.
 * </p>
 *
 * @see io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator
 */
public class InvalidIdSchemaException extends BaseCheckedException  {

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = -8957950467794649169L;

	/**
	 * Constructs an invalid-schema exception with MOSIP error code and message.
	 *
	 * @param errorCode    never-null MOSIP error code
	 * @param errorMessage never-null human-readable description
	 */
	public InvalidIdSchemaException(String errorCode, String errorMessage) {
		super(errorCode, errorMessage);
	}
}
