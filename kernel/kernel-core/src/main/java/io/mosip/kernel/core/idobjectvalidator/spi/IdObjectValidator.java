package io.mosip.kernel.core.idobjectvalidator.spi;

import java.util.List;

import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectIOException;
import io.mosip.kernel.core.idobjectvalidator.exception.IdObjectValidationFailedException;
import io.mosip.kernel.core.idobjectvalidator.exception.InvalidIdSchemaException;

/**
 * Validates a MOSIP identity JSON object against an identity schema.
 * <p>
 * Contract: implementations parse schema and identity JSON locally and may
 * load masterdata over HTTP. {@code identitySchema} and
 * {@code identityObject} must be non-null. Call from registration or IDA
 * before persisting an identity.
 * </p>
 *
 * @author Manoj SP
 * @author Swati Raj
 * @since 1.0.0
 */
public interface IdObjectValidator {
	
	/**
	 * Validates {@code identityObject} against {@code identitySchema} with no
	 * ignorable required fields.
	 *
	 * @param identitySchema never-null JSON schema text
	 * @param identityObject never-null identity JSON (string, map, or tree)
	 * @return {@code true} if valid
	 * @throws IdObjectValidationFailedException when the identity fails schema
	 *                                           rules
	 * @throws IdObjectIOException               when the identity cannot be read
	 * @throws InvalidIdSchemaException          when the schema is malformed
	 */
	default boolean validateIdObject(String identitySchema, Object identityObject)
			throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException {
		return validateIdObject(identitySchema, identityObject, null);
	}
	
	/**
	 * Validates {@code identityObject} against {@code identitySchema}, ignoring
	 * missing-field errors for names in {@code requiredFields} when that list is
	 * used as an ignore set by the implementation (historical parameter name).
	 *
	 * @param identitySchema never-null JSON schema text
	 * @param identityObject never-null identity JSON (string, map, or tree)
	 * @param requiredFields optional field names whose missing-required errors
	 *                       may be ignored; null means ignore none
	 * @return {@code true} if valid after applying ignore rules
	 * @throws IdObjectValidationFailedException when remaining errors exist
	 * @throws IdObjectIOException               when the identity cannot be read
	 * @throws InvalidIdSchemaException          when the schema is malformed
	 */
	public boolean validateIdObject(String identitySchema, Object identityObject, List<String> requiredFields)
			throws IdObjectValidationFailedException, IdObjectIOException, InvalidIdSchemaException;

}
