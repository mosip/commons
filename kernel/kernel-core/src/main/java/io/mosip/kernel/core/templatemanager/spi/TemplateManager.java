package io.mosip.kernel.core.templatemanager.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.Map;

/**
 * Merges a template with placeholder values to produce rendered text.
 * <p>
 * Contract: implementations typically use Velocity or similar. {@code template}
 * / {@code templateName} and {@code values} must be non-null. Default encoding
 * is UTF-8. Does not perform MOSIP HTTP.
 * </p>
 *
 * @author Abhishek Kumar
 * @since 2018-10-01
 * @version 1.0.0
 */
public interface TemplateManager {
	/**
	 * Merges template bytes with {@code values} and returns the rendered stream.
	 *
	 * @param template never-null template content
	 * @param values   never-null map of placeholder name to value; values may be null
	 * @return never-null rendered content
	 * @throws IOException when reading the template or writing the result fails
	 */
	public InputStream merge(InputStream template, Map<String, Object> values) throws IOException;

	/**
	 * Merges a named template into {@code writer} using UTF-8.
	 *
	 * @param templateName never-null, never-blank template resource name
	 * @param writer       never-null destination for rendered text
	 * @param values       never-null map of placeholder name to value
	 * @return {@code true} if merge succeeded
	 * @throws IOException when reading the template or writing the result fails
	 */
	public boolean merge(String templateName, Writer writer, Map<String, Object> values) throws IOException;

	/**
	 * Merges a named template into {@code writer} using {@code encodingType}.
	 *
	 * @param templateName never-null, never-blank template resource name
	 * @param writer       never-null destination for rendered text
	 * @param values       never-null map of placeholder name to value
	 * @param encodingType never-null charset name such as {@code UTF-8}
	 * @return {@code true} if merge succeeded
	 * @throws IOException when reading the template or writing the result fails
	 */
	public boolean merge(String templateName, Writer writer, Map<String, Object> values, final String encodingType)
			throws IOException;
}
