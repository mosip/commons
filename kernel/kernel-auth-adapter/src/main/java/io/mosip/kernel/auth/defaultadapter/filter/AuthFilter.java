/**
 * 
 */
package io.mosip.kernel.auth.defaultadapter.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;

import io.mosip.kernel.auth.defaultadapter.config.NoAuthenticationEndPoint;
import io.mosip.kernel.auth.defaultadapter.config.PathPatternSupport;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterConstant;
import io.mosip.kernel.auth.defaultadapter.constant.AuthAdapterErrorCode;
import io.mosip.kernel.auth.defaultadapter.exception.AuthManagerException;
import io.mosip.kernel.auth.defaultadapter.model.AuthToken;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.util.DateUtils2;
import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.openid.bridge.api.constants.Constants;
import io.mosip.kernel.openid.bridge.api.constants.Errors;
import io.mosip.kernel.openid.bridge.api.exception.ClientException;
import io.mosip.kernel.openid.bridge.api.utils.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet authentication filter that extracts JWTs from cookies, skips
 * configured no-auth paths, and delegates to the authentication manager.
 * <p>
 * No-auth matching uses {@link PathPatternSupport} with the same Boot 3.4
 * switch as MVC ({@code spring.mvc.pathmatch.matching-strategy}). After
 * success, the filter chain continues. Compliance Toolkit data-share token
 * handling is an optional fail-safe behind {@code auth.handle.ctk.flow}.
 * <p>
 * This adapter is a library other MOSIP services put on the classpath.
 *
 * @author Ramadurai Saravana Pandian
 * @author Raj Jha
 * @author Urvil Joshi
 * @author GOVINDARAJ VELU -> End-points modification
 */
public class AuthFilter extends AbstractAuthenticationProcessingFilter {

	/**
	 * Logger for token extraction and authentication failures.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilter.class);

	/**
	 * Bound global and service no-auth path lists.
	 */
	private NoAuthenticationEndPoint noAuthenticationEndPoint;

	/**
	 * Mapper used to serialize MOSIP error envelopes and parse cached request JSON.
	 */
	private ObjectMapper mapper;
	/**
	 * HTTP methods allowed to skip auth on service-specific no-auth paths
	 * (default GET).
	 */
	private List<String> allowedHttpMethods;

	/**
	 * When {@code true}, the id-token cookie must be present and its {@code sub}
	 * must match the access token.
	 */
	@Value("${auth.validate.id-token:false}")
	private boolean validateIdToken;
	
	/**
	 * When {@code true}, successful auth may call Compliance Toolkit data-share
	 * token APIs.
	 */
	@Value("${auth.handle.ctk.flow:false}")
	private boolean flagToHandleCtkFlow;
	
	/**
	 * Compliance Toolkit URL that stores a data-share token.
	 */
	@Value("${mosip.compliance.toolkit.saveDataShareToken.url:}")
	private String ctkSaveUrl;
	
	/**
	 * Compliance Toolkit URL that invalidates a data-share token.
	 */
	@Value("${mosip.compliance.toolkit.invalidateDataShareToken.url:}")
	private String ctkInvalidateUrl;
	
	/**
	 * Test-case id that selects {@link #ctkInvalidateUrl} instead of
	 * {@link #ctkSaveUrl}.
	 */
	@Value("${mosip.compliance.toolkit.invalidateDataShareToken.testCaseId:}")
	private String ctkInvalidateTestCaseId;
	
	/**
	 * Environment used to resolve the id-token cookie name and JWT subject claim.
	 */
	@Autowired
	private Environment environment;
	
	/**
	 * RestTemplate used only for optional Compliance Toolkit HTTP calls.
	 */
	private RestTemplate restTemplate = new RestTemplate();

	/**
	 * Builds the filter for {@code requiresAuthenticationRequestMatcher} and loads
	 * allowed no-auth HTTP methods for the hosting application.
	 *
	 * @param requiresAuthenticationRequestMatcher typically
	 *                                             {@code AnyRequestMatcher.INSTANCE}
	 * @param noAuthenticationEndPoint             configured no-auth paths
	 * @param environment                          used for application name and
	 *                                             method exclusions
	 */
	@SuppressWarnings("unchecked")
	public AuthFilter(RequestMatcher requiresAuthenticationRequestMatcher,
			NoAuthenticationEndPoint noAuthenticationEndPoint, Environment environment) {
		super(requiresAuthenticationRequestMatcher);
		this.noAuthenticationEndPoint = noAuthenticationEndPoint;
		this.environment = environment;
		String applName = getApplicationName(environment);
		allowedHttpMethods = (List<String>) environment.getProperty(
				"mosip.service.exclude.auth.allowed.method." + applName, List.class, environment.getProperty(
						"mosip.service.exclude.auth.allowed.method", List.class, Collections.singletonList("GET")));
		mapper = JsonMapper.builder().addModule(new AfterburnerModule()).build();
		mapper.registerModule(new JavaTimeModule());
	}

	/**
	 * Returns {@code false} (skip authentication) for global no-auth paths, or for
	 * service paths when the servlet context matches and the HTTP method is
	 * allowed.
	 *
	 * @param request  the inbound request
	 * @param response the outbound response
	 * @return {@code true} if the authentication manager must run
	 */
	@Override
	protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
		// To check the global end-points
		if (isPresent(request, noAuthenticationEndPoint.getGlobal() == null ? null
				: noAuthenticationEndPoint.getGlobal().getEndPoints())) {
			return false;
		}
		// As the request not a part of the global end-points, check in master data
		// end-points
		boolean isValid = isValid(noAuthenticationEndPoint);
		if (isValid) {
			if (request.getServletContext().getContextPath()
					.equalsIgnoreCase(noAuthenticationEndPoint.getServiceContext())) {
				return (allowedHttpMethods.contains(request.getMethod())
						&& isPresent(request, noAuthenticationEndPoint.getService().getEndPoints())) ? false : true;
			}
		}
		return true;
	}

	/**
	 * Returns whether any configured pattern matches {@code request} using
	 * {@link PathPatternSupport} and {@code spring.mvc.pathmatch.matching-strategy}.
	 *
	 * @param request   the inbound request
	 * @param endPoints configured patterns, possibly {@code null} or empty
	 * @return {@code true} if at least one pattern matches
	 */
	private boolean isPresent(HttpServletRequest request, List<String> endPoints) {
		if (endPoints == null || endPoints.isEmpty()) {
			return false;
		}
		return endPoints.stream()
				.anyMatch(pattern -> PathPatternSupport.matches(request, pattern, environment));
	}

	/**
	 * Returns whether service-level no-auth configuration has a context path and
	 * endpoint list.
	 *
	 * @param noAuthenticationEndPoint the bound no-auth properties
	 * @return {@code true} if service exclusions can be evaluated
	 */
	private boolean isValid(NoAuthenticationEndPoint noAuthenticationEndPoint) {
		if (noAuthenticationEndPoint.getServiceContext() == null
				|| noAuthenticationEndPoint.getServiceContext().isEmpty())
			return false;
		if (noAuthenticationEndPoint.getService() == null
				|| noAuthenticationEndPoint.getService().getEndPoints() == null
				|| noAuthenticationEndPoint.getService().getEndPoints().isEmpty())
			return false;
		return true;
	}

	/**
	 * Reads Authorization (and optional id-token) cookies, optionally checks
	 * subject claims, and authenticates an {@link AuthToken}.
	 *
	 * @param httpServletRequest  the inbound request
	 * @param httpServletResponse the outbound response used for 401 JSON
	 * @return the authenticated token, or {@code null} after writing 401
	 * @throws AuthenticationException if the authentication manager fails
	 * @throws IOException             if the error body cannot be written
	 * @throws ServletException        if authentication processing fails
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse)
			throws AuthenticationException, IOException, ServletException {
		String token = null;
		String idToken = null;
		Cookie[] cookies = null;
		String authTokenSub = null;
		String idTokenSub = null;
		boolean isIdTokenAvailable = false;
		try {
			cookies = httpServletRequest.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					if (cookie.getName().contains(AuthAdapterConstant.AUTH_REQUEST_COOOKIE_HEADER)) {
						LOGGER.debug("extract token from cookie named " + cookie.getName());
						token = cookie.getValue();
						if(validateIdToken){
							authTokenSub = JWTUtils.
									getSubClaimValueFromToken(cookie.getValue(), this.environment.getProperty(Constants.TOKEN_SUBJECT_CLAIM_NAME));
						}
					} else {
						String idTokenName=this.environment.getProperty(AuthAdapterConstant.ID_TOKEN);
						if(idTokenName!=null){
							if(cookie.getName().contains(idTokenName)){
								LOGGER.debug("extract token from cookie named " + cookie.getName());
								idToken = cookie.getValue();
								if(validateIdToken){
									if(idToken == null || idToken.isEmpty()) {
										throw new ClientException(Errors.TOKEN_NOTPRESENT_ERROR.getErrorCode(),
												Errors.TOKEN_NOTPRESENT_ERROR.getErrorMessage() + ": " + idTokenName);
									}
									isIdTokenAvailable = true;
									idTokenSub = JWTUtils.
											getSubClaimValueFromToken(idToken,
													this.environment.getProperty(Constants.TOKEN_SUBJECT_CLAIM_NAME));

								}

							}
						}
					}
				}
			}

		} catch (Exception e) {
			LOGGER.debug("extract token from cookie failed for request " + httpServletRequest.getRequestURI());
		}
		if(validateIdToken && !isIdTokenAvailable){
			LOGGER.error("Id token not available.");
			return sendAuthenticationFailure(httpServletRequest, httpServletResponse);
		}
		if(validateIdToken && (idTokenSub == null || !idTokenSub.equalsIgnoreCase(authTokenSub))){
			LOGGER.error("Sub of Id token and auth token didn't match.");
			return sendAuthenticationFailure(httpServletRequest, httpServletResponse);
		}

		if (token == null) {
			LOGGER.error("\n\n Exception : Authorization token not present > " + httpServletRequest.getRequestURL()
					+ "\n\n");
			return sendAuthenticationFailure(httpServletRequest, httpServletResponse);
		}
		AuthToken authToken = null;
		if(idToken==null){
			 authToken = new AuthToken(token);
		} else{
			authToken = new AuthToken(token, idToken);
		}

		LOGGER.debug("Extracted auth token for request " + httpServletRequest.getRequestURL());
		Authentication auth = getAuthenticationManager().authenticate(authToken);
		/*
		 * This is custom  fail-safe handling added only for Compliance Toolkit, to enable ABIS
		 * data share testing.
		 */
		if (auth != null && auth.isAuthenticated() && flagToHandleCtkFlow) {
			handleCtkTokenFlow(httpServletRequest, token);
		}
		return auth;
	}

	/**
	 * Writes HTTP 401 MOSIP JSON and returns {@code null}.
	 *
	 * @param httpServletRequest  the failed request
	 * @param httpServletResponse the response
	 * @return always {@code null}
	 * @throws IOException if the body cannot be written
	 */
	private Authentication sendAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(httpServletRequest);
		ServiceError error = new ServiceError(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(),
				"Authentication Failed");
		errorResponse.getErrors().add(error);
		httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
		httpServletResponse.setContentType("application/json");
		httpServletResponse.setCharacterEncoding("UTF-8");
		httpServletResponse.getWriter().write(convertObjectToJson(errorResponse));
		return null;
	}
	
	/**
	 * Invokes the default success handling then continues the filter chain.
	 *
	 * @param request    the authenticated request
	 * @param response   the response
	 * @param chain      the remaining filters
	 * @param authResult the successful authentication
	 * @throws IOException      if the chain throws
	 * @throws ServletException if the chain throws
	 */
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		super.successfulAuthentication(request, response, chain, authResult);
		chain.doFilter(request, response);
	}

	/**
	 * Writes HTTP 401 MOSIP JSON from {@link AuthManagerException} errors.
	 *
	 * @param request  the failed request
	 * @param response the response
	 * Writes HTTP 401 MOSIP JSON for any authentication failure. Malformed JWTs
	 * are wrapped as {@link AuthManagerException} or
	 * {@link org.springframework.security.authentication.InternalAuthenticationServiceException};
	 * both must stay 401 (not a ClassCastException 500).
	 *
	 * @param request  the failed request
	 * @param response the response
	 * @param failed   authentication failure
	 * @throws IOException      if the body cannot be written
	 * @throws ServletException never thrown by this implementation
	 */
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {
		ResponseWrapper<ServiceError> errorResponse = setErrors(request);
		if (failed instanceof AuthManagerException exception && exception.getList() != null
				&& !exception.getList().isEmpty()) {
			errorResponse.getErrors().addAll(exception.getList());
		} else if (failed instanceof AuthManagerException exception && exception.getErrorCode() != null) {
			errorResponse.getErrors().add(new ServiceError(exception.getErrorCode(), exception.getMessage()));
		} else {
			ServiceError error = new ServiceError(AuthAdapterErrorCode.UNAUTHORIZED.getErrorCode(),
					"Authentication Failed");
			errorResponse.getErrors().add(error);
		}
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		ExceptionUtils.logRootCause(failed);
		response.getWriter().write(convertObjectToJson(errorResponse));
	}

	/**
	 * Builds a {@link ResponseWrapper} with UTC time ({@link DateUtils2}) and copies
	 * {@code id} and {@code version} from a cached JSON body when present.
	 *
	 * @param httpServletRequest the inbound request
	 * @return an error wrapper ready for {@link ServiceError} entries
	 * @throws IOException if the cached body is not valid JSON
	 */
	private ResponseWrapper<ServiceError> setErrors(HttpServletRequest httpServletRequest) throws IOException {
		ResponseWrapper<ServiceError> responseWrapper = new ResponseWrapper<>();
		responseWrapper.setResponsetime(DateUtils2.getUTCCurrentDateTime());
		String requestBody = null;
		if (httpServletRequest instanceof ContentCachingRequestWrapper) {
			requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
		}
		if (EmptyCheckUtils.isNullEmpty(requestBody)) {
			return responseWrapper;
		}

		JsonNode reqNode = mapper.readTree(requestBody);
		responseWrapper.setId(reqNode.path("id").asText());
		responseWrapper.setVersion(reqNode.path("version").asText());
		return responseWrapper;
	}

	/**
	 * Serializes {@code object} to JSON, or returns {@code null} if {@code object}
	 * is {@code null}.
	 *
	 * @param object the value to serialize
	 * @return JSON text, or {@code null}
	 * @throws JsonProcessingException if serialization fails
	 */
	private String convertObjectToJson(Object object) throws JsonProcessingException {
		if (object == null) {
			return null;
		}

		return mapper.writeValueAsString(object);
	}

	/**
	 * Returns the first comma-separated {@code spring.application.name} value.
	 *
	 * @param environment Spring environment
	 * @return the hosting application name
	 * @throws RuntimeException if the property is missing or blank
	 */
	@SuppressWarnings("java:S2259") // added suppress for sonarcloud. Null check is performed at line # 211
	private String getApplicationName(Environment environment) {
		String appNames = environment.getProperty("spring.application.name");
		if (appNames != null && !EmptyCheckUtils.isNullEmpty(appNames)) {
			List<String> appNamesList = Stream.of(appNames.split(",")).collect(Collectors.toList());
			return appNamesList.get(0);
		} else {
			throw new RuntimeException("property spring.application.name not found");
		}
	}
	
	/**
	 * This is custom fail-safe handling added only for Compliance Toolkit, to
	 * enable ABIS data share testing.
	 *
	 * @param httpServletRequest the authenticated request
	 * @param token              the access token forwarded to Compliance Toolkit
	 */
	private void handleCtkTokenFlow(HttpServletRequest httpServletRequest, String token) {
		String ctkTestCaseId = null;
		String ctkTestRunId = null;
		Map<String, String[]> requestParams = httpServletRequest.getParameterMap();
		String[] testCaseIdArr = requestParams.get(AuthAdapterConstant.CTK_TEST_CASE_ID);
		if (testCaseIdArr != null && testCaseIdArr.length > 0) {
			ctkTestCaseId = testCaseIdArr[0];
			LOGGER.debug("Recvd ctkTestCaseId {}", sanitize(ctkTestCaseId));
		}
		String[] testRunIdArr = requestParams.get(AuthAdapterConstant.CTK_TEST_RUN_ID);
		if (testRunIdArr != null && testRunIdArr.length > 0) {
			ctkTestRunId = testRunIdArr[0];
			LOGGER.debug("Recvd ctkTestRunId {}", sanitize(ctkTestRunId));
		}
		if (ctkTestCaseId != null && ctkTestRunId != null) {
			if (ctkSaveUrl == null) {
				LOGGER.info("Invalid value for property 'mosip.compliance.toolkit.saveDataShareToken.url' {}", ctkSaveUrl);
				return;
			}
			if (ctkInvalidateUrl == null && ctkInvalidateTestCaseId != null) {
				LOGGER.info("Invalid value for property 'mosip.compliance.toolkit.invalidateDataShareToken.url' {}", ctkInvalidateUrl);
				return;
			}
			// get the partnerId from URL
			String path = httpServletRequest.getPathInfo();
			String[] splits = path.split("/");
			String partnerId = null;
			if (splits.length > 2) {
				partnerId = splits[splits.length - 2];
			}
			if (partnerId == null) {
				LOGGER.info("Invalid DataShare URL {}", httpServletRequest.getRequestURI());
				return;
			}
			LOGGER.debug("Recvd partnerId {}", sanitize(partnerId));
			// add the token first
			HttpHeaders headers = new HttpHeaders();
			headers.add(AuthAdapterConstant.AUTH_HEADER_COOKIE, AuthAdapterConstant.AUTH_HEADER + token);
			headers.setContentType(MediaType.APPLICATION_JSON);
			// create request
			Map<String, String> valueMap = new HashMap<String, String>();
			valueMap.put(AuthAdapterConstant.PARTNER_ID, partnerId);
			valueMap.put(AuthAdapterConstant.CTK_TEST_CASE_ID, ctkTestCaseId);
			valueMap.put(AuthAdapterConstant.CTK_TEST_RUN_ID, ctkTestRunId);
			RequestWrapper<Object> requestWrapper = new RequestWrapper<>();
			requestWrapper.setId("mosip.toolkit.abis.datashare.token");
			requestWrapper.setVersion("1.0");
			requestWrapper.setRequesttime(DateUtils2.getUTCCurrentDateTime());
			requestWrapper.setRequest(valueMap);
			
			ResponseEntity<ResponseWrapper<String>> responseEntity = null;
			try {
				HttpEntity<RequestWrapper<Object>> requestEntity = new HttpEntity<>(requestWrapper, headers);
				String tokenUrl = new StringBuilder(ctkSaveUrl).toString();
				if (ctkInvalidateTestCaseId != null && ctkInvalidateTestCaseId.equals(ctkTestCaseId)) {
					tokenUrl = new StringBuilder(ctkInvalidateUrl).toString();
				}
				LOGGER.debug("Calling Compliance Toolkit URL: " + tokenUrl);
				responseEntity = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity,
						new ParameterizedTypeReference<ResponseWrapper<String>>() {
						});
				ResponseWrapper<String> body = responseEntity.getBody();
				if (body != null) {
					LOGGER.debug("Response from Compliance Toolkit: " + body.getResponse());
					return;
				}
				LOGGER.debug("Response from Compliance Toolkit response body is null");
			} catch (Exception e) {
				// This is FailSafe, so just log the err
				LOGGER.error("error connecting to compliance toolkit: " + e.getStackTrace(), e);
			}
		}
	}

	/**
	 * Replaces CR/LF in log arguments to avoid log injection.
	 *
	 * @param msg raw log fragment
	 * @return {@code msg} with newlines replaced by spaces
	 */
	private String sanitize(String msg) {
		return msg.replaceAll("[\n\r]", " ");
	}
}