package io.mosip.kernel.templatemanager.velocity.builder;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.templatemanager.spi.TemplateManager;
import io.mosip.kernel.core.templatemanager.spi.TemplateManagerBuilder;
import io.mosip.kernel.templatemanager.velocity.impl.TemplateManagerImpl;
import lombok.Getter;

/**
 * TemplateManagerBuilderImpl will build the {@link TemplateManager} with the
 * configuration either custom or default.
 * 
 * @author Abhishek Kumar
 * @since 05-10-2018
 * @version 1.0.0
 */
@Getter
@Component
public class TemplateManagerBuilderImpl implements TemplateManagerBuilder {
	/** Velocity resource loader name ({@code classpath} or {@code file}). */
	private String resourceLoader = "classpath";
	/** File-loader root path when {@link #resourceLoader} is {@code file}. */
	private String templatePath = ".";
	/** Whether Velocity caches file resources. */
	private boolean cache = Boolean.TRUE;
	/** Default input/output encoding (UTF-8). */
	private String defaultEncoding = StandardCharsets.UTF_8.name();

	/**
	 * Sets the Velocity resource loader name.
	 *
	 * @param resourceLoader {@code classpath} or {@code file}
	 * @return this builder
	 */
	@Override
	public TemplateManagerBuilder resourceLoader(String resourceLoader) {
		this.resourceLoader = resourceLoader;
		return this;
	}

	/**
	 * Sets the file-loader root path.
	 *
	 * @param templatePath directory used when the loader is {@code file}
	 * @return this builder
	 */
	@Override
	public TemplateManagerBuilder resourcePath(String templatePath) {
		this.templatePath = templatePath;
		return this;
	}

	/**
	 * Enables or disables Velocity file-resource caching.
	 *
	 * @param cache {@code true} to cache templates
	 * @return this builder
	 */
	@Override
	public TemplateManagerBuilder enableCache(boolean cache) {
		this.cache = cache;
		return this;
	}

	/**
	 * Sets Velocity input and output encoding.
	 *
	 * @param defaultEncoding charset name (for example {@code UTF-8})
	 * @return this builder
	 */
	@Override
	public TemplateManagerBuilder encodingType(String defaultEncoding) {
		this.defaultEncoding = defaultEncoding;
		return this;
	}

	/**
	 * Initializes a {@link VelocityEngine} with the configured properties and returns a {@link TemplateManagerImpl}.
	 *
	 * @return template manager ready to merge templates
	 */
	@Override
	public TemplateManager build() {
		final Properties properties = new Properties();
		properties.setProperty("resource.default_encoding", defaultEncoding);
		properties.setProperty("resource.loaders", resourceLoader);
		properties.setProperty("resource.loader.classpath.class", ClasspathResourceLoader.class.getName());
		properties.setProperty("resource.loader.file.class", FileResourceLoader.class.getName());
		properties.setProperty("resource.loader.file.path", templatePath);
		properties.setProperty("resource.loader.file.cache", Boolean.toString(cache));
		VelocityEngine engine = new VelocityEngine(properties);
		engine.init();
		return new TemplateManagerImpl(engine);
	}
}
