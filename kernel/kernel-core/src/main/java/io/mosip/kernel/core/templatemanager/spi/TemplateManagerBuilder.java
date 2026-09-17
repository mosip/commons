package io.mosip.kernel.core.templatemanager.spi;

/**
 * Fluent builder for a {@link TemplateManager} instance.
 * <p>
 * Contract: each setter returns {@code this} for chaining. {@link #build()}
 * produces a manager with the accumulated settings (classpath loader, UTF-8,
 * cache enabled unless overridden). Does not perform MOSIP HTTP.
 * </p>
 *
 * @author Abhishek Kumar
 * @version 1.0.0
 * @since 22-11-2018
 */
public interface TemplateManagerBuilder {
	/**
	 * Sets where templates are loaded from (for example {@code classpath} or {@code file}).
	 *
	 * @param resourceLoader never-null loader name; default is classpath
	 * @return this builder
	 */
	TemplateManagerBuilder resourceLoader(String resourceLoader);

	/**
	 * Sets the root path or classpath prefix for templates.
	 *
	 * @param templatePath never-null template location
	 * @return this builder
	 */
	TemplateManagerBuilder resourcePath(String templatePath);

	/**
	 * Enables or disables in-memory template caching.
	 *
	 * @param cache {@code true} to cache compiled templates; default is {@code true}
	 * @return this builder
	 */
	TemplateManagerBuilder enableCache(boolean cache);

	/**
	 * Sets the charset used to read template files.
	 *
	 * @param defaultEncoding never-null charset name; default is UTF-8
	 * @return this builder
	 */
	TemplateManagerBuilder encodingType(String defaultEncoding);

	/**
	 * Creates a {@link TemplateManager} with the configured settings.
	 *
	 * @return never-null manager
	 */
	TemplateManager build();
}
