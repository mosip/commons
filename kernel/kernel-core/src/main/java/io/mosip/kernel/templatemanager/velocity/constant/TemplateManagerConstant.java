package io.mosip.kernel.templatemanager.velocity.constant;

/**
 * constants for NullPointerException Messages
 * 
 * @author Abhishek Kumar
 * @since 10-10-2018
 * @version 1.0.0
 */
public enum TemplateManagerConstant {
	/** {@link java.io.Writer} passed to merge is {@code null}. */
	WRITER_NULL("Writer cannot be null"),
	/** Values map passed to merge is {@code null}. */
	TEMPLATE_VALUES_NULL("Values cannot be null, it requires process template"),
	/** Template {@link java.io.InputStream} is {@code null}. */
	TEMPLATE_INPUT_STREAM_NULL("Template cannot be null"),
	/** Encoding name is {@code null}. */
	ENCODING_TYPE_NULL("Encoding type cannot be null"),
	/** Template resource name is {@code null}. */
	TEMPATE_NAME_NULL("Template name cannot be null");

	/**
	 * This variable contains the message
	 */
	private String message;

	/**
	 * Constructor for setting message.
	 *
	 * @param message NullPointerException detail shown to callers
	 */
	TemplateManagerConstant(String message) {
		this.message = message;
	}

	/**
	 * Getter for getting the message
	 * 
	 * @return message
	 */
	public String getMessage() {
		return message;
	}
}
