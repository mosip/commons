/*
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

package io.mosip.kernel.logger.logback.constant;

/**
 * {@link Enum} for exception constants
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public enum LogExeptionCodeConstant {
	/** Error code: logger class name is empty. */
	CLASSNAMENOTFOUNDEXEPTION("KER-LOG-001"),
	/** Error code: rolling file-name pattern is empty or null. */
	EMPTYPATTERNEXCEPTION("KER-LOG-004"),
	/** Error code: log file name is empty or null. */
	FILENAMENOTPROVIDED("KER-LOG-006"),
	/** Error code: file-size string is not a valid Logback size. */
	MOSIPILLEGALARGUMENTEXCEPTION("KER-LOG-007"),
	/** Error code: log file location is not writable. */
	MOSIPILLEGALFILEACCESS("KER-LOG-008"),
	/** Error code: appender XML cannot be unmarshalled. */
	MOSIPCONFIGURATIONXMLPARSE("KER-LOG-009"),
	/** Error code: file-name pattern has no valid date token. */
	MOSIPILLEGALSTATEEXCEPTION("KER-LOG-003"),
	/** Error code: requested {@link LoggerMethod} is not implemented. */
	IMPLEMENTATIONNOTFOUND("KER-LOG-002"),
	/** Error code: rolling file-name pattern is syntactically invalid. */
	PATTERNSYNTAXEXCEPTION("KER-LOG-005"),
	/** Message for {@link #CLASSNAMENOTFOUNDEXEPTION}. */
	CLASSNAMENOTFOUNDEXEPTIONMESSAGE("Class name is empty"),
	/** Message for empty {@link #EMPTYPATTERNEXCEPTION}. */
	EMPTYPATTERNEXCEPTIONMESSAGEEMPTY("File name pattern is empty"),
	/** Message for null {@link #EMPTYPATTERNEXCEPTION}. */
	EMPTYPATTERNEXCEPTIONMESSAGENULL("File name pattern is null"),
	/** Message for {@link #MOSIPILLEGALSTATEEXCEPTION}. */
	MOSIPILLEGALSTATEEXCEPTIONMESSAGE("FileNamePattern does not contain a valid DateToken"),
	/** Message for {@link #MOSIPILLEGALARGUMENTEXCEPTION}. */
	MOSIPILLEGALARGUMENTEXCEPTIONMESSAGE("String value of size is not in expected format"),
	/** Message for empty {@link #FILENAMENOTPROVIDED}. */
	FILENAMENOTPROVIDEDMESSAGEEMPTY("File name is empty"),
	/** Message for null {@link #FILENAMENOTPROVIDED}. */
	FILENAMENOTPROVIDEDMESSAGENULL("File name is null"),
	/** Message for {@link #IMPLEMENTATIONNOTFOUND}. */
	IMPLEMENTATIONNOTFOUNDMESSAGE("Log Implementation not found"),
	/** Message for {@link #MOSIPILLEGALFILEACCESS}. */
	MOSIPILLEGALFILEACCESSMESSAGE("File location not accessible"),
	/** Message: pattern must contain {@code %d{SimpleDateFormat}}. */
	PATTERNSYNTAXEXCEPTIONMESSAGED("Pattern should contain %d{SimpleDateFormat}"),
	/** Message: size-based rolling requires {@code %i} in the pattern. */
	PATTERNSYNTAXEXCEPTIONMESSAGEI("Pattern should contain %i"),
	/** Message: time-only rolling must not contain {@code %i}. */
	PATTERNSYNTAXEXCEPTIONMESSAGENOTI("Pattern should not contain %i"),
	/** Message for {@link #MOSIPCONFIGURATIONXMLPARSE}. */
	MOSIPCONFIGURATIONXMLPARSEMESSAGE("invalid xml configuration");

	/**
	 * Value of exception constants constant {@link Enum} value
	 */
	private final String value;

	/**
	 * Constructor for this class
	 * 
	 * @param value set {@link Enum} value
	 */
	private LogExeptionCodeConstant(final String value) {
		this.value = value;
	}

	/**
	 * Getter for value
	 * 
	 * @return get {@link Enum} value
	 */
	public String getValue() {
		return value;
	}
}
