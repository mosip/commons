package io.mosip.kernel.emailnotification.test.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
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

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

	@Bean
	public HttpFirewall defaultHttpFirewall() {
		return new DefaultHttpFirewall();
	}

	@Bean
	public Filter configureWebSecurityFilter(WebSecurity webSecurity) throws Exception {
		webSecurity.ignoring().requestMatchers(allowedEndPoints());
		webSecurity.httpFirewall(defaultHttpFirewall());
		return webSecurity.build();
	}

	private String[] allowedEndPoints() {
		return new String[] { "/assets/**", "/icons/**", "/screenshots/**", "/favicon**", "/**/favicon**", "/css/**",
				"/js/**", "/*/error**", "/*/webjars/**", "/*/v2/api-docs", "/*/configuration/ui",
				"/*/configuration/security", "/*/swagger-resources/**", "/*/swagger-ui.html" };
	}

	@Bean
	protected SecurityFilterChain configureSecurityFilterChain(final HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(http -> http.disable());
		httpSecurity.httpBasic().and().authorizeRequests().anyRequest().authenticated().and().sessionManagement()
				.sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().exceptionHandling()
				.authenticationEntryPoint(unauthorizedEntryPoint());
		return httpSecurity.build();
	}

	@Bean
	public AuthenticationEntryPoint unauthorizedEntryPoint() {
		return (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	}

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