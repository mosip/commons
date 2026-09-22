package io.mosip.kernel.auth.defaultadapter.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import io.mosip.kernel.auth.defaultadapter.config.NoAuthenticationEndPoint.GlobalEndPoint;
import io.mosip.kernel.auth.defaultadapter.config.NoAuthenticationEndPoint.ServiceEndPoint;
import io.mosip.kernel.auth.defaultadapter.filter.AuthFilter;
import io.mosip.kernel.auth.defaultadapter.filter.CorsFilter;
import io.mosip.kernel.auth.defaultadapter.handler.AuthHandler;
import io.mosip.kernel.auth.defaultadapter.handler.AuthSuccessHandler;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Spring Security 7 configuration applied when this adapter is on a MOSIP
 * service classpath.
 * <p>
 * Builds a {@link ProviderManager} from optional extra
 * {@link AbstractUserDetailsAuthenticationProvider} beans plus
 * {@link AuthHandler}. {@link AuthFilter} matches {@link AnyRequestMatcher}
 * and then skips paths listed in {@link NoAuthenticationEndPoint}. Matching
 * follows {@code spring.mvc.pathmatch.matching-strategy} like Boot 3.4:
 * {@code ANT_PATH_MATCHER} uses Ant; {@code PATH_PATTERN_PARSER} (default)
 * uses PathPattern. CSRF and CORS are optional via
 * {@code mosip.security.csrf-enable} and {@code mosip.security.cors-enable}.
 *
 * @author Sabbu Uday Kumar
 * @author Ramadurai Saravana Pandian
 * @author Raj Jha
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(2)
public class SecurityConfig {

	/**
	 * Logger for custom auth-provider registration.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

	/**
	 * CSRF ignore patterns when CSRF is enabled.
	 */
	@Value("${mosip.kernel.csrf_ignore.url:}")
	private String[] csrfIgnoreUrls;

	/**
	 * When {@code false}, CSRF protection is disabled on the filter chain.
	 */
	@Value("${mosip.security.csrf-enable:false}")
	private boolean isCSRFEnable;

	/**
	 * When {@code true}, {@link CorsFilter} is inserted before {@link AuthFilter}.
	 */
	@Value("${mosip.security.cors-enable:false}")
	private boolean isCORSEnable;

	/**
	 * Comma-separated origins passed to {@link CorsFilter}.
	 */
	@Value("${mosip.security.origins:localhost:8080}")
	private String origins;

	/**
	 * Used to resolve extra authentication-provider beans by name.
	 */
	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * Default JWT authentication provider.
	 */
	@Autowired
	private AuthHandler authProvider;

	/**
	 * Environment for per-application provider bean names and application name.
	 */
	@Autowired
	private Environment environment;
	
	/**
	 * Bound no-auth global and service path lists.
	 */
	@Autowired
	private NoAuthenticationEndPoint noAuthenticationEndPoint;

	/**
	 * Builds a {@link ProviderManager} from optional extra providers named in
	 * {@code mosip.security.authentication.provider.beans.list.<app>} plus
	 * {@link #authProvider}.
	 *
	 * @return the authentication manager used by {@link AuthFilter}
	 */
	// @ConditionalOnMissingBean(AuthenticationManager.class)
	@Bean
	@SuppressWarnings("unchecked")
	public AuthenticationManager authenticationManager() {
		List<AuthenticationProvider> authProviders = new ArrayList<>();
		String applName = getApplicationName();
		List<String> otherAuthProviders = (List<String>) environment.getProperty(
				"mosip.security.authentication.provider.beans.list." + applName, List.class, Collections.EMPTY_LIST);
		otherAuthProviders.stream().forEach(beanName -> {
			try {
				if (Objects.nonNull(beanName) && !beanName.equals("")) {
					authProviders
							.add(applicationContext.getBean(beanName, AbstractUserDetailsAuthenticationProvider.class));
					LOGGER.info("Added Custom Auth Provider Bean in the list {} ", beanName);
				}
			} catch (Exception ex) {
				LOGGER.error("Error Adding bean to providers list: " + beanName, ex);
			}
		});
		authProviders.add(authProvider);
		return new ProviderManager(authProviders);
	}

	/**
	 * Creates {@link AuthFilter} for {@link AnyRequestMatcher#INSTANCE}, bound to
	 * {@link #authenticationManager()} and {@link AuthSuccessHandler}.
	 *
	 * @return the authentication processing filter
	 */
	// @ConditionalOnMissingBean(AbstractAuthenticationProcessingFilter.class)
	@Bean
	public AbstractAuthenticationProcessingFilter authFilter() {
		AuthFilter filter = new AuthFilter(AnyRequestMatcher.INSTANCE, noAuthenticationEndPoint, environment);
		filter.setAuthenticationManager(authenticationManager());
		filter.setAuthenticationSuccessHandler(new AuthSuccessHandler());
		return filter;
	}

	/**
	 * Disables servlet-container registration of {@link AuthFilter} so it runs
	 * only inside the Spring Security chain.
	 *
	 * @param filter the auth filter bean
	 * @return a disabled {@link FilterRegistrationBean}
	 */
	@Bean
	public FilterRegistrationBean<AbstractAuthenticationProcessingFilter> registration(
			AbstractAuthenticationProcessingFilter filter) {
		FilterRegistrationBean<AbstractAuthenticationProcessingFilter> registration = new FilterRegistrationBean<>(
				filter);
		registration.setEnabled(false);
		return registration;
	}

	/**
	 * Stateless Security 7 filter chain: optional CSRF, permit-all for no-auth
	 * endpoints (Ant or PathPattern per matching-strategy), authenticated for
	 * everything else, {@link AuthEntryPoint} on failure, and {@link AuthFilter}
	 * before {@link UsernamePasswordAuthenticationFilter}.
	 *
	 * @param http the HTTP security builder
	 * @return the built filter chain
	 * @throws Exception if the chain cannot be built
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		boolean ant = PathPatternSupport.isAntPathMatcher(environment);
		if (!isCSRFEnable) {
			http = http.csrf(httpEntry -> httpEntry.disable());
		} else{
			RequestMatcher[] csrfIgnoreMatchers = Stream.of(csrfIgnoreUrls)
					.map(url -> PathPatternSupport.requestMatcher(url, ant))
					.toArray(RequestMatcher[]::new);
			http.csrf(httpEntry -> {
				if (csrfIgnoreMatchers.length > 0) {
					httpEntry.ignoringRequestMatchers(csrfIgnoreMatchers);
				}
				httpEntry.csrfTokenRepository(this.getCsrfTokenRepository());
			});
		}

		RequestMatcher[] exclusionMatchers = Stream.concat(
				Optional.ofNullable(noAuthenticationEndPoint.getGlobal()).map(GlobalEndPoint::getEndPoints)
						.map(List::stream).orElseGet(Stream::empty),
				Optional.ofNullable(noAuthenticationEndPoint.getService()).map(ServiceEndPoint::getEndPoints)
						.map(List::stream).orElseGet(Stream::empty))
				.distinct()
				.map(pattern -> PathPatternSupport.requestMatcher(pattern, ant))
				.toArray(RequestMatcher[]::new);

		http.authorizeHttpRequests(authorizeRequests -> {
			if (exclusionMatchers.length > 0) {
				authorizeRequests.requestMatchers(exclusionMatchers).permitAll();
			}
			authorizeRequests.anyRequest().authenticated();
		});
		http.exceptionHandling(exceptionConfigurer -> exceptionConfigurer.authenticationEntryPoint(new AuthEntryPoint()));
		http.sessionManagement(sessionConfigurer -> sessionConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.addFilterBefore(authFilter(), UsernamePasswordAuthenticationFilter.class);
		if (isCORSEnable) {
			http.addFilterBefore(new CorsFilter(origins), AuthFilter.class);
		}
		http.headers(headersEntry -> {
			headersEntry.cacheControl(Customizer.withDefaults());
			headersEntry.frameOptions(frameOptions -> frameOptions.sameOrigin());
		});
		
		return http.build();
	}

	/**
	 * Returns the first comma-separated {@code spring.application.name} value.
	 *
	 * @return the hosting application name
	 * @throws RuntimeException if the property is missing or blank
	 */
	@SuppressWarnings("java:S2259") // added suppress for sonarcloud. Null check is performed at line # 211
	private String getApplicationName() {
		String appNames = environment.getProperty("spring.application.name");
		if (appNames != null && !EmptyCheckUtils.isNullEmpty(appNames)) {
			List<String> appNamesList = Stream.of(appNames.split(",")).collect(Collectors.toList());
			return appNamesList.get(0);
		} else {
			throw new RuntimeException("property spring.application.name is not found");
		}
	}

	/**
	 * Cookie CSRF repository with {@code HttpOnly} false and cookie path {@code /}.
	 *
	 * @return the CSRF token repository
	 */
	private CsrfTokenRepository getCsrfTokenRepository() {
		CookieCsrfTokenRepository cookieCsrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
		cookieCsrfTokenRepository.setCookiePath("/");
		return cookieCsrfTokenRepository;
	} 
}

/**
 * Authentication entry point that returns HTTP 401 {@code UNAUTHORIZED} without
 * a login redirect (stateless JWT services).
 */
class AuthEntryPoint implements AuthenticationEntryPoint {

	/**
	 * Sends HTTP 401 with reason {@code UNAUTHORIZED}.
	 *
	 * @param request       the failed request
	 * @param response      the response
	 * @param authException the authentication failure
	 * @throws IOException      if the error cannot be written
	 * @throws ServletException never thrown by this implementation
	 */
	@Override
	public void commence(jakarta.servlet.http.HttpServletRequest request,
			jakarta.servlet.http.HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED");
	}

}
