package io.mosip.kernel.core.idobjectvalidator.spi;

import java.util.List;

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
	 * Validates a identityObject passed with the identity schema provided.
	 * 
	 * @param identitySchema
	 * @param identityObject
	 * @return
	 * @throws IdObjectValidationFailedException
	 * @throws IdObjectIOException
	 * @throws InvalidIdSchemaException
	 */
	default boolean validateIdObject(String identitySchema, Object identityObject)
			throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		return validateIdObject(identitySchema, identityObject, null);
	}
	
	/**
	 * Validates a identityObject passed with the identity schema provided.
	 * if validation errors are found then filters error list to ignore missing field errors.
	 *  
	 * @param identitySchema
	 * @param identityObject
	 * @param ignorableRequiredFields
	 * @return
	 * @throws IdObjectValidationFailedException
	 * @throws IdObjectIOException
	 * @throws InvalidIdSchemaException
	 */
	public boolean validateIdObject(String identitySchema, Object identityObject, List<String> requiredFields)
			throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException;

}
