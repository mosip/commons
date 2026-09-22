package io.mosip.kernel.auth.test.config;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Test {@link SecurityFilterChain} for auth-service tests: CSRF off, all
 * requests permitted, stateless sessions, HTTP Basic, an in-memory user store
 * with MOSIP roles, and a 401 entry point.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

	/**
	 * RestTemplate used by auth-service tests that need HTTP clients.
	 *
	 * @return a default RestTemplate
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	
	/**
	 * Permissive firewall so encoded URL tests are not rejected.
	 *
	 * @return the default HTTP firewall
	 */
	@Bean
	public HttpFirewall defaultHttpFirewall() {
		return new DefaultHttpFirewall();
	}

	/**
	 * Builds a permit-all, CSRF-disabled, stateless HTTP Basic filter chain.
	 *
	 * @param httpSecurity Spring Security HTTP builder
	 * @return the built filter chain
	 * @throws Exception if configuration fails
	 */
	@Bean
	protected SecurityFilterChain configure(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(cfg -> cfg.disable());
		httpSecurity.authorizeHttpRequests(
				http -> http.anyRequest().permitAll());
		httpSecurity.exceptionHandling(cfg -> cfg.authenticationEntryPoint(unauthorizedEntryPoint()));
		httpSecurity.sessionManagement(cfg -> cfg.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		httpSecurity.httpBasic(Customizer.withDefaults());
		return httpSecurity.build();
	}

	/**
	 * Sends HTTP 401 when authentication fails.
	 *
	 * @return the unauthorized entry point
	 */
	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

	/**
	 * In-memory users covering registration, IDA, individual, and test roles.
	 *
	 * @return the test {@link UserDetailsService}
	 */
	@Bean
	public UserDetailsService userDetailsService() {
		List<UserDetails> users = new ArrayList<>();
		users.add(new User("reg-officer", "mosip",
				Arrays.asList(new SimpleGrantedAuthority("ROLE_REGISTRATION_OFFICER"))));
		users.add(new User("reg-supervisor", "mosip",
				Arrays.asList(new SimpleGrantedAuthority("ROLE_REGISTRATION_SUPERVISOR"))));
		users.add(new User("reg-admin", "mosip", Arrays.asList(new SimpleGrantedAuthority("ROLE_REGISTRATION_ADMIN"))));
		users.add(new User("reg-processor", "mosip",
				Arrays.asList(new SimpleGrantedAuthority("ROLE_REGISTRATION_PROCESSOR"))));
		users.add(new User("id-auth", "mosip", Arrays.asList(new SimpleGrantedAuthority("ROLE_ID_AUTHENTICATION"))));
		users.add(new User("individual", "mosip", Arrays.asList(new SimpleGrantedAuthority("ROLE_INDIVIDUAL"))));
		users.add(new User("test", "mosip", Arrays.asList(new SimpleGrantedAuthority("ROLE_TEST"))));
		return new InMemoryUserDetailsManager(users);
	}
	
	

}
