package io.mosip.kernel.core.idobjectvalidator.spi;

import io.mosip.kernel.core.idobjectvalidator.constant.IdObjectValidatorSupportedOperations;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;

/**
 * Interface JSON validation against the schema.
 * 
 * 
 * 
 * @author Manoj SP
 * @author Swati Raj
 * @since 1.0.0
 * 
 */
public interface IdObjectValidator {

	/**
	 * Validates a JSON object passed as string with the schema provided.
	 *
	 * @param identityObject the identity object
	 * @param operation      the operation
	 * @return true, if successful
	 * @throws IdObjectValidationFailedException the id object validation processing
	 *                                           exception
	 * @throws IdObjectIOException               the id object IO exception
	 */

	public boolean validateIdObject(Object identityObject, IdObjectValidatorSupportedOperations operation)
			throws IdObjectValidationFailedException, IdObjectIOException;
	
	/**
	 * Validates a JSON object passed as string with the identity schema provided.
	 * 
	 * @param identitySchema
	 * @param identityObject
	 * @param operation
	 * @return
	 * @throws IdObjectValidationFailedException
	 * @throws IdObjectIOException
	 */
	public boolean validateIdObject(String identitySchema, Object identityObject, IdObjectValidatorSupportedOperations operation)
			throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException;

}
