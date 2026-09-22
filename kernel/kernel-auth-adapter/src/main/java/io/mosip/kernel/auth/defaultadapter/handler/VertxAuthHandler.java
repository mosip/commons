package io.mosip.kernel.auth.defaultadapter.handler;

import static io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant.AUTH_HEADER;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.kernel.auth.defaultadapter.config.Generated;
import io.mosip.kernel.auth.defaultadapter.config.RestTemplateInterceptor;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultadapter.helper.VertxTokenValidationHelper;
import io.mosip.kernel.core.authmanager.authadapter.spi.VertxAuthenticationProvider;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.PostConstruct;

/**
 * Vert.x {@link VertxAuthenticationProvider} implementation for MOSIP services
 * that use Vert.x HTTP rather than Spring MVC.
 * <p>
 * Token validation is delegated to {@link VertxTokenValidationHelper}. Vert.x
 * 3.9.16 remains a provided dependency. Optional SSL bypass uses an anonymous
 * {@link HostnameVerifier}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 */
@Lazy
@Component
public class VertxAuthHandler implements VertxAuthenticationProvider {
    
    /**
     * Load-balancing interceptor attached to {@link #restTemplate}.
     */
    @Autowired
	private RestTemplateInterceptor restInterceptor;
	
	/**
	 * RestTemplate used by {@link VertxTokenValidationHelper}.
	 */
	private RestTemplate restTemplate = null;

	/**
	 * Offline/online token validator for Vert.x requests.
	 */
	@Autowired
	private VertxTokenValidationHelper validationHelper;
	
	/**
	 * Fallback user id when the routing context has no authenticated user.
	 */
	private static final String DEFAULTADMIN_MOSIP_IO = "defaultadmin@mosip.io";

	/**
	 * When {@code true}, the validation RestTemplate trusts all TLS certificates.
	 */
	@Value("${mosip.kernel.auth.adapter.ssl-bypass:true}")
	private boolean sslBypass;
	
	/**
	 * Builds {@link #restTemplate} with optional SSL bypass and
	 * {@link RestTemplateInterceptor}.
	 *
	 * @throws KeyManagementException   if the SSL context cannot be initialized
	 * @throws NoSuchAlgorithmException if the SSL context cannot be built
	 * @throws KeyStoreException        if trust material cannot be loaded
	 */
	@SuppressWarnings("java:S5527") // added suppress for sonarcloud. 
	@PostConstruct
	void init() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		HttpClientBuilder httpClientBuilder = HttpClients.custom().disableCookieManagement();
		var connnectionManagerBuilder = PoolingHttpClientConnectionManagerBuilder.create();
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		if (sslBypass) {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
					.loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new HostnameVerifier() {
				/**
				 * Always returns {@code true}; used only when {@code sslBypass} is enabled
				 * for internal MOSIP service hostnames.
				 *
				 * @param arg0 unused hostname
				 * @param arg1 unused SSL session
				 * @return {@code true}
				 */
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			});
			connnectionManagerBuilder.setSSLSocketFactory(csf);
		}
		httpClientBuilder.setConnectionManager(connnectionManagerBuilder.build());
		requestFactory.setHttpClient(httpClientBuilder.build());
		List<ClientHttpRequestInterceptor> list = new ArrayList<>();
		list.add(restInterceptor);
		restTemplate = new RestTemplate(requestFactory);
		restTemplate.setInterceptors(list);
	}

	/**
	 * Installs OWASP-oriented security headers on every Vert.x route of
	 * {@code httpServer}.
	 *
	 * @param httpServer the Vert.x HTTP server
	 * @param vertx      the Vert.x instance used to create a router
	 */
	@Generated // coverage exclusion as this is a filter
	@Override
    public void addCorsFilter(HttpServer httpServer, Vertx vertx) {
		Router router = Router.router(vertx);
		
		// Basic security headers by OWASP
		router.route().handler(routingContext -> {
			HttpServerResponse httpServerResponse = routingContext.response();
			httpServerResponse.putHeader("Cache-Control", "no-store, no-cache,max-age=0, must-revalidate")
					.putHeader("Pragma", "no-cache").putHeader("X-Content-Type-Options", "nosniff")
					.putHeader("Strict-Transport-Security", "max-age=" + 15768000 + "; includeSubDomains")
					.putHeader("X-Download-Options", "noopen").putHeader("X-XSS-Protection", "1; mode=block")
					.putHeader("X-FRAME-OPTIONS", "DENY");

			routingContext.next();
		});
		httpServer.requestHandler(router);
	}

	/**
	 * Registers a Vert.x route that validates the Authorization cookie and required
	 * roles before calling {@code next}.
	 *
	 * @param router              the Vert.x router
	 * @param path                route path
	 * @param httpMethod          required HTTP method; must not be {@code null}
	 * @param commaSepratedRoles  required roles, comma-separated
	 */
	@Generated // coverage exclusion as this is a filter
	@Override
	public void addAuthFilter(Router router, String path, HttpMethod httpMethod,
			String commaSepratedRoles) {
		Objects.requireNonNull(httpMethod, AuthAdapterConstant.HTTP_METHOD_NOT_NULL);
		Route filterRoute = router.route(httpMethod, path);
		filterRoute.handler(routingContext -> {
			tokenValidation(routingContext, commaSepratedRoles);
		});
	}

	/**
	 * Validates the current {@link RoutingContext} cookie and roles without
	 * registering a new route.
	 *
	 * @param routingContext     the current Vert.x routing context
	 * @param commaSepratedRoles required roles, comma-separated
	 */
	@Generated // coverage exclusion as this is a filter
	@Override
	public void addAuthFilter(RoutingContext routingContext, String commaSepratedRoles) {
		tokenValidation(routingContext, commaSepratedRoles);
	}

	/**
	 * Validates roles and token, then echoes the token as {@code Set-Cookie} and
	 * continues the route.
	 *
	 * @param routingContext     the current Vert.x routing context
	 * @param commaSepratedRoles required roles, comma-separated
	 * @throws AuthManagerException if roles are empty or validation fails
	 */
	private void tokenValidation(RoutingContext routingContext, String commaSepratedRoles) {
		try {
			if (EmptyCheckUtils.isNullEmpty(commaSepratedRoles)) {
				throw new NullPointerException(AuthAdapterConstant.ROLES_NOT_EMPTY_NULL);
			}
			String[] roles = commaSepratedRoles.split(",");
			String token = validateToken(routingContext, roles);
			if (!token.isEmpty()) {
				HttpServerResponse httpServerResponse = routingContext.response();
				if (!token.startsWith(AUTH_HEADER))
					token = AUTH_HEADER + token;
				httpServerResponse.putHeader(AuthAdapterConstant.AUTH_HEADER_SET_COOKIE, token);
				routingContext.next();
			}
		} catch (Exception e) {
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), e.getMessage(), e);
		}
	}

	/**
	 * Runs {@link VertxTokenValidationHelper}, stores {@link MosipUserDto} on the
	 * routing context, and sets Spring {@link SecurityContextHolder}.
	 *
	 * @param routingContext the current Vert.x routing context
	 * @param roles          required roles
	 * @return the validated token, or empty when validation wrote an error
	 * @throws RestClientException     if remote validation fails
	 * @throws KeyManagementException  unused; retained from historical signature
	 * @throws NoSuchAlgorithmException unused; retained from historical signature
	 * @throws KeyStoreException       unused; retained from historical signature
	 * @throws JsonParseException      if the error body cannot be parsed
	 * @throws JsonMappingException    if the error body cannot be mapped
	 * @throws JsonProcessingException if the error body cannot be written
	 * @throws IOException             if I/O fails during validation
	 */
	private String validateToken(RoutingContext routingContext, String[] roles)
			throws RestClientException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException,
			JsonParseException, JsonMappingException, JsonProcessingException, IOException {
		
		MosipUserDto mosipUserDto = validationHelper.getTokenValidatedVertxUserResponse(restTemplate, routingContext, roles);
		if (Objects.isNull(mosipUserDto)) {
			return "";
		}

		AuthUserDetails authUserDetails = new AuthUserDetails(mosipUserDto, mosipUserDto.getToken());
		Authentication authentication = new UsernamePasswordAuthenticationToken(authUserDetails,
				authUserDetails.getPassword(), null);
		routingContext.put(AuthAdapterConstant.ROUTING_CONTEXT_USER, mosipUserDto);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		
		return mosipUserDto.getToken();
	}

	/**
	 * Returns the authenticated user id from the routing context, or
	 * {@link #DEFAULTADMIN_MOSIP_IO} when absent.
	 *
	 * @param routingContext the current Vert.x routing context
	 * @return the user id
	 */
	@Override
	public String getContextUser(RoutingContext routingContext) {
		MosipUserDto mosipUser = routingContext.get(AuthAdapterConstant.ROUTING_CONTEXT_USER);
		return mosipUser == null ? DEFAULTADMIN_MOSIP_IO : mosipUser.getUserId();
	}
}
