package io.mosip.kernel.websub.api.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;


import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
import io.mosip.kernel.websub.api.client.PublisherClientImpl;
import io.mosip.kernel.websub.api.config.IntentVerificationConfig;
import io.mosip.kernel.websub.api.constants.WebSubClientConstants;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.verifier.IntentVerifier;
import io.swagger.models.HttpMethod;
import lombok.Setter;

/**
 * This filter is used for handle intent verification request by hub after subscribe and unsubscribe
 * operations with help of metadata collected by {@link PreAuthenticateContentAndVerifyIntent} and
 * {@link IntentVerificationConfig} class.
 * 
 * @author Urvil Joshi
 *
 */
public class IntentVerificationFilter extends OncePerRequestFilter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(IntentVerificationFilter.class);

	private IntentVerifier intentVerifier;

	@Setter
	private Map<String, String> mappings = null;

	public IntentVerificationFilter(IntentVerifier intentVerifier) {
		this.intentVerifier = intentVerifier;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		LOGGER.info("filtering possible intent verification callback for uri"+request.getRequestURI());
		String topic=matchCallbackURL(request.getRequestURI());
		LOGGER.info("topic received for uri"+topic);
		if (request.getMethod().equals(HttpMethod.GET.name()) && topic!=null) {
			String topicReq = request.getParameter(WebSubClientConstants.HUB_TOPIC);
			LOGGER.info("HUB_TOPIC"+topicReq);
			String modeReq = request.getParameter(WebSubClientConstants.HUB_MODE);
			LOGGER.info("HUB_TOPIC"+modeReq);
			if (intentVerifier.isIntentVerified(topic,
					request.getParameter("intentMode"), topicReq, modeReq)) {
				response.setStatus(HttpServletResponse.SC_ACCEPTED);
				try {
					response.getWriter().write(request.getParameter(WebSubClientConstants.HUB_CHALLENGE));
					LOGGER.info("writing hub challange back"+WebSubClientConstants.HUB_CHALLENGE);
					response.getWriter().flush();
					response.getWriter().close();
				} catch (IOException exception) {
					LOGGER.error("error received while writing challange back"+exception.getMessage());
					throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
							WebSubClientErrorCode.IO_ERROR.getErrorMessage().concat(exception.getMessage()));
				}

			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}

		} else {
			filterChain.doFilter(request, response);
		}
	}

	private String matchCallbackURL(String requestURI) {
		if(mappings.containsKey(requestURI)) {
			return mappings.get(requestURI);
		}else {
			Set<String> mappingKeys=mappings.keySet();
			for(String keys:mappingKeys){
				int pathParamIndex=keys.indexOf("/{");
				if(pathParamIndex==-1) {
					continue;
				}
				String url =keys.substring(0,pathParamIndex );
				if(requestURI.contains(url)) {
					int pathParamCount=keys.split("\\/\\{").length - 1;
					if(requestURI.substring(pathParamIndex).split("\\/").length-1==pathParamCount) {
						return mappings.get(keys);
					};
				}
			}
		}
		return null;
	}
}
