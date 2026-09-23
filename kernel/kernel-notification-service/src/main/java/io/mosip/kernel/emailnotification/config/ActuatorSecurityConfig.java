package io.mosip.kernel.emailnotification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ActuatorSecurityConfig {

    @Bean
    @Order(0)
    SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(request ->
                "GET".equals(request.getMethod())
                && (request.getRequestURI().equals(request.getContextPath()
                        + "/v1/notifier/actuator/health")
                    || request.getRequestURI().equals(request.getContextPath()
                        + "/v1/notifier/actuator/prometheus")))
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
        return http.build();
    }
}
