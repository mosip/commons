package io.mosip.kernel.core.logger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.tracing.BraveAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;
import io.micrometer.tracing.Tracer;

@ConditionalOnProperty(value = "spring.sleuth.enabled", matchIfMissing = true)
@Configuration
//@ComponentScan(basePackages = { "org.springframework.cloud.sleuth.autoconfig.*"})
@Import({BraveAutoConfiguration.class}) 
public class SleuthLoggingAutoConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {


	@Autowired
	private Tracer tracer;
	
    @Bean
    public MDCInsertingServletFilter mdcInsertingServletFilter() {
        return new MDCInsertingServletFilter();
    }

    @Bean
    public SleuthValve sleuthValve() {
        return new SleuthValve(tracer);
    }

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        if(factory instanceof TomcatServletWebServerFactory) {
            ((TomcatServletWebServerFactory)factory).addContextCustomizers(context ->
                    context.getPipeline().addValve(sleuthValve()));
        }
    }
}
