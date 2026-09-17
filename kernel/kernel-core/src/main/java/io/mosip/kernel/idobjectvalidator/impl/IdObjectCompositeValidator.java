package io.mosip.kernel.idobjectvalidator.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;
import io.mosip.kernel.core.idobjectvalidator.spi.IdObjectValidator;

/**
 * The Class IdObjectCompositeValidator.
 *
 * @author Manoj SP
 */
@Component
@Primary
public class IdObjectCompositeValidator implements IdObjectValidator {

	/** The schema validator. */
	@Autowired
	private IdObjectSchemaValidator schemaValidator;

	/** The reference validator. */
	@Autowired(required = false)
	@Qualifier("referenceValidator")
	@Lazy
	private IdObjectValidator referenceValidator;

	/**
	 * Runs schema validation then the optional reference validator.
	 *
	 * @param idSchema       identity JSON schema
	 * @param identityObject identity JSON to validate
	 * @param requiredFields mandatory field names (or pipe-separated alternatives)
	 * @return {@code true} if both validators succeed
	 * @throws IdObjectValidationFailedException if schema or reference validation fails
	 * @throws IdObjectIOException               if the identity cannot be read
	 * @throws InvalidIdSchemaException          if the schema is invalid
	 */
	@Override
	public boolean validateIdObject(String idSchema, Object identityObject, List<String> requiredFields)
			throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		schemaValidator.validateIdObject(idSchema, identityObject, requiredFields);
		referenceValidator.validateIdObject(idSchema, identityObject, requiredFields);
		return true;
	}

}
