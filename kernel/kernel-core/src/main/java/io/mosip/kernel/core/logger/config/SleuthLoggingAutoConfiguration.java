package io.mosip.kernel.core.logger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;
import io.micrometer.tracing.Tracer;

/**
 * Registers Tomcat valves and an MDC filter so Micrometer Tracing (Sleuth)
 * identifiers appear in access logs and Logback MDC.
 * <p>
 * Contract: enabled when {@code spring.sleuth.enabled} is true or omitted.
 * Mutates the Tomcat pipeline at startup. Call is automatic via Spring Boot
 * auto-configuration; do not instantiate manually.
 * </p>
 */
@ConditionalOnProperty(value = "spring.sleuth.enabled", matchIfMissing = true)
@Configuration
public class SleuthLoggingAutoConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {


	/**
	 * Micrometer tracer used to create spans when inbound headers are missing.
	 * Property key: none (injected bean).
	 */
	@Autowired
	private Tracer tracer;
	
    /**
     * Builds a servlet filter that inserts request data into Logback MDC.
     *
     * @return never-null MDC inserting filter
     */
    @Bean
    public MDCInsertingServletFilter mdcInsertingServletFilter() {
        return new MDCInsertingServletFilter();
    }

    /**
     * Builds the Tomcat valve that injects B3 trace headers when absent.
     *
     * @return never-null Sleuth valve bound to {@link #tracer}
     */
    @Bean
    public SleuthValve sleuthValve() {
        return new SleuthValve(tracer);
    }

    /**
     * Adds {@link SleuthValve} to the Tomcat context pipeline when the factory
     * is a {@link TomcatServletWebServerFactory}.
     *
     * @param factory never-null web-server factory; non-Tomcat factories are ignored
     */
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        if(factory instanceof TomcatServletWebServerFactory) {
            ((TomcatServletWebServerFactory)factory).addContextCustomizers(context ->
                    context.getPipeline().addValve(sleuthValve()));
        }
    }
}
