package io.mosip.kernel.idobjectvalidator.constant;

/**
 * This enum provides all the constants for property source to be used.
 * 
 * @author Manoj SP
 * @author Swati Raj
 *
 */
public class IdObjectValidatorConstant {

	public static final String LEVEL = "level";
	public static final String MESSAGE = "message";
	public static final String WARNING = "warning";
	public static final String INSTANCE = "instance";
	public static final String POINTER = "pointer";
	public static final String KEYWORD = "keyword";
	public static final String VALIDATORS = "validators";
	public static final String AT = " at ";
	public static final String ERROR = "error";
	public static final String PATH_SEPERATOR = "/";
	public static final String REFERENCE_IDENTITY_NUMBER_REGEX = "^ = [0-9]{10,30})$";
	public static final String INVALID_ATTRIBUTE = "Invalid attribute";
	public static final String ROOT_PATH = "identity";
	public static final String IDENTITY_ARRAY_VALUE_FIELD = "value";
	public static final String APPLICATION_ID = "application.id";
	public static final String FIELD_LIST = "mosip.kernel.idobjectvalidator.mandatory-attributes.%s.%s";
	public static final String REFERENCE_VALIDATOR = "mosip.kernel.idobjectvalidator.referenceValidator";
	
	//New attributes & formats added to Json Schema
	public static final String ATTR_FIELDTYPE = "fieldType";
	public static final String ATTR_FIELD_CATEGORY = "fieldCategory";
	public static final String ATTR_VALIDATORS = "validators";
	public static final String ATTR_BIO = "bioAttributes";
	
	public static final String FORMAT_LOWERCASED = "lowercased";
	public static final String FORMAT_UPPERCASED = "uppercased";
	
	public static final String INCORRECT_CASE_MSG_KEY = "incorrectCase";
	public static final String INCORRECT_CASE_MSG_VALUE = "Input value is not in correct case";
	
	public static final String INCORRECT_MATCH = "incorrectMatch";
	public static final String INCORRECT_MATCH_MSG_VALUE = "Input value doesnot match validator";

}
