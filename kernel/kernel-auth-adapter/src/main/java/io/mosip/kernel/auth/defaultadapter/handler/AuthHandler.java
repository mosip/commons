
package io.mosip.kernel.auth.defaultadapter.handler;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.impl.NullClaim;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import io.mosip.kernel.auth.defaultadapter.config.RestTemplateInterceptor;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultadapter.helper.TokenValidationHelper;
import io.mosip.kernel.auth.defaultadapter.model.AuthToken;
import io.mosip.kernel.openid.bridge.model.AuthUserDetails;
import io.mosip.kernel.openid.bridge.model.MosipUserDto;
import jakarta.annotation.PostConstruct;

/**
 * Default Spring Security provider that validates the inbound JWT and builds
 * {@link AuthUserDetails} with role and scope authorities.
 * <p>
 * Validation is delegated to {@link TokenValidationHelper}. Optional SSL bypass
 * uses an anonymous {@link HostnameVerifier} that always returns {@code true}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Ramadurai Saravana Pandian
 * @author Raj Jha
 * @author Urvil Joshi
 * @since 1.0.0
 */
@Component
public class AuthHandler extends AbstractUserDetailsAuthenticationProvider {

	/**
	 * Logger for this provider.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthHandler.class);

	/**
	 * Load-balancing interceptor attached to {@link #restTemplate}.
	 */
	@Autowired
	private RestTemplateInterceptor restInterceptor;
	
	/**
	 * RestTemplate used by {@link TokenValidationHelper} for online validation.
	 */
	private RestTemplate restTemplate = null;

	/**
	 * Offline/online token validator.
	 */
	@Autowired
	private TokenValidationHelper validationHelper;
	
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
	 * No extra checks; token validity is established in {@link #retrieveUser}.
	 *
	 * @param userDetails                         unused
	 * @param usernamePasswordAuthenticationToken unused
	 * @throws AuthenticationException never thrown by this implementation
	 */
	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
	}

	/**
	 * Validates the {@link AuthToken} JWT and returns {@link AuthUserDetails}
	 * with role authorities and optional space-delimited {@code scope} authorities.
	 *
	 * @param userName                            unused (user comes from the JWT)
	 * @param usernamePasswordAuthenticationToken must be an {@link AuthToken}
	 * @return authenticated user details
	 * @throws AuthenticationException if token validation fails
	 */
	@Override
	protected UserDetails retrieveUser(String userName,
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
		AuthToken authToken = (AuthToken) usernamePasswordAuthenticationToken;
		String token = authToken.getToken();
		String idToken = authToken.getIdToken();
		MosipUserDto mosipUserDto;
		try {
			mosipUserDto = validationHelper.getTokenValidatedUserResponse(token, restTemplate);
		} catch (JWTDecodeException e) {
			throw new AuthManagerException(AuthAdapterErrorCode.INVALID_TOKEN.getErrorCode(),
					AuthAdapterErrorCode.INVALID_TOKEN.getErrorMessage());
		}
		
		List<GrantedAuthority> roleAuthorities = AuthorityUtils
				.commaSeparatedStringToAuthorityList(mosipUserDto.getRole());
		
		AuthUserDetails authUserDetails;
		if(idToken!=null){
			authUserDetails = new AuthUserDetails(mosipUserDto, token, idToken);
		} else{
			authUserDetails = new AuthUserDetails(mosipUserDto, token);
		}
		authUserDetails.addRoleAuthorities(roleAuthorities);
		
		Optional<String> scopeClaimOpt = getScopeClaim(token);
		if(scopeClaimOpt.isPresent()) {
			List<GrantedAuthority> scopeAuthorities = AuthorityUtils
					.createAuthorityList(StringUtils
							.tokenizeToStringArray(scopeClaimOpt.get() , " "));
			authUserDetails.addScopeAuthorities(scopeAuthorities);
		}
		return authUserDetails;

	}

	/**
	 * Reads the JWT {@code scope} claim when it is present and not a null claim.
	 *
	 * @param jwtToken the access-token JWT
	 * @return the scope string, or empty
	 */
	private Optional<String> getScopeClaim(String jwtToken) {
		DecodedJWT decodedJWT;
		try {
			decodedJWT = JWT.decode(jwtToken);
		} catch (JWTDecodeException e) {
			return Optional.empty();
		}
		Claim claim = decodedJWT.getClaim(AuthAdapterConstant.SCOPE);
		if(claim != null && !(claim instanceof NullClaim)) {
			String scopesStr = claim.asString();
			return Optional.of(scopesStr);
		}
		return Optional.empty();
	}
}
