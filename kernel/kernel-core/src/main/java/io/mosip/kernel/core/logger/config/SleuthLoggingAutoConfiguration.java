package io.mosip.kernel.core.logger.config;

import brave.Tracer;
import ch.qos.logback.classic.helpers.MDCInsertingServletFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(value = "spring.sleuth.enabled", matchIfMissing = true)
@Configuration
@ComponentScan(basePackages = { "org.springframework.cloud.sleuth.autoconfig.*"})
public class SleuthLoggingAutoConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Autowired
    private SleuthValve sleuthValve;

    @Bean
    public MDCInsertingServletFilter mdcInsertingServletFilter() {
        return new MDCInsertingServletFilter();
    }

    @Bean
    public SleuthValve sleuthValve(Tracer tracer) {
        sleuthValve = new SleuthValve(tracer);
        return sleuthValve;
    }

    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        if(factory instanceof TomcatServletWebServerFactory) {
            ((TomcatServletWebServerFactory)factory).addContextCustomizers(context ->
                    context.getPipeline().addValve(sleuthValve));
        }
    }
}
