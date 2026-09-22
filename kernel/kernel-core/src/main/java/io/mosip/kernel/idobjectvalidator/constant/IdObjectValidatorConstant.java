package io.mosip.kernel.idobjectvalidator.constant;

/**
 * This enum provides all the constants for property source to be used.
 * 
 * @author Manoj SP
 * @author Swati Raj
 *
 */
/**
 * JSON-schema and MOSIP identity-object property names, formats, and messages.
 *
 * @author Manoj SP
 * @author Swati Raj
 */
public class IdObjectValidatorConstant {

	/** JSON report field for log level. */
	public static final String LEVEL = "level";
	/** JSON report field for message text. */
	public static final String MESSAGE = "message";
	/** JSON report level {@code warning}. */
	public static final String WARNING = "warning";
	/** JSON report field for the failing instance. */
	public static final String INSTANCE = "instance";
	/** JSON report field for the JSON pointer. */
	public static final String POINTER = "pointer";
	/** JSON report field for the failing keyword. */
	public static final String KEYWORD = "keyword";
	/** Schema keyword {@code validators}. */
	public static final String VALIDATORS = "validators";
	/** Separator used when appending a pointer to a message. */
	public static final String AT = " at ";
	/** JSON report level {@code error}. */
	public static final String ERROR = "error";
	/** JSON-pointer path separator. */
	public static final String PATH_SEPERATOR = "/";
	/** Legacy regex for a reference identity number (unused by current validators). */
	public static final String REFERENCE_IDENTITY_NUMBER_REGEX = "^ = [0-9]{10,30})$";
	/** Generic invalid-attribute label. */
	public static final String INVALID_ATTRIBUTE = "Invalid attribute";
	/** Root property name of the identity JSON. */
	public static final String ROOT_PATH = "identity";
	/** Array-item property holding a language-specific value. */
	public static final String IDENTITY_ARRAY_VALUE_FIELD = "value";
	/** Config key for application id. */
	public static final String APPLICATION_ID = "application.id";
	/** Format string for mandatory-attribute config keys. */
	public static final String FIELD_LIST = "mosip.kernel.idobjectvalidator.mandatory-attributes.%s.%s";
	/** Config key of the optional reference validator class. */
	public static final String REFERENCE_VALIDATOR = "mosip.kernel.idobjectvalidator.referenceValidator";
	
	/** MOSIP schema keyword {@code fieldType}. */
	public static final String ATTR_FIELDTYPE = "fieldType";
	/** MOSIP schema keyword {@code fieldCategory}. */
	public static final String ATTR_FIELD_CATEGORY = "fieldCategory";
	/** MOSIP schema keyword {@code validators}. */
	public static final String ATTR_VALIDATORS = "validators";
	/** MOSIP schema keyword {@code bioAttributes}. */
	public static final String ATTR_BIO = "bioAttributes";
	
	/** Custom format name requiring lowercase text. */
	public static final String FORMAT_LOWERCASED = "lowercased";
	/** Custom format name requiring uppercase text. */
	public static final String FORMAT_UPPERCASED = "uppercased";
	
	/** Message-bundle key for incorrect case. */
	public static final String INCORRECT_CASE_MSG_KEY = "incorrectCase";
	/** Message-bundle text for incorrect case. */
	public static final String INCORRECT_CASE_MSG_VALUE = "Input value is not in correct case";
	
	/** Message-bundle key for a failed custom validator. */
	public static final String INCORRECT_MATCH = "incorrectMatch";
	/** Message-bundle text for a failed custom validator. */
	public static final String INCORRECT_MATCH_MSG_VALUE = "Input value doesnot match validator";

}
