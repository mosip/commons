package io.mosip.kernel.websub.api.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import io.mosip.kernel.websub.api.annotation.PreAuthenticateContentAndVerifyIntent;
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
 */
public class IntentVerificationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntentVerificationFilter.class);

    private IntentVerifier intentVerifier;

    @Setter
    private Map<String, String> mappings = null;

    public IntentVerificationFilter(IntentVerifier intentVerifier) {
        logger.info("inside intentVerification filter intentverifier");
        logger.info("intentVerifier" + intentVerifier);
        this.intentVerifier = intentVerifier;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.info("inside doFilterInternal");
        logger.info("HttpServletRequest request- " + request);
        if (request.getRequestURI().contains("/health")) {
            filterChain.doFilter(request, response);
            return;
        }
        String topic = matchCallbackURL(request.getRequestURI());
        if (request.getMethod().equals(HttpMethod.GET.name()) && topic != null) {
            String topicReq = request.getParameter(WebSubClientConstants.HUB_TOPIC);
            String modeReq = request.getParameter(WebSubClientConstants.HUB_MODE);
            String mode = request.getParameter("intentMode");
            if (modeReq.equals("denied")) {
                String reason = request.getParameter("hub.reason");
                reason = reason.replaceAll("[\n\r\t]", " - ");
                LOGGER.error("intent verification failed : {}", reason);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().flush();
                response.getWriter().close();
            }
            if (intentVerifier.isIntentVerified(topic,
                    mode, topicReq, modeReq)) {
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
                try {
                    String challange = request.getParameter(WebSubClientConstants.HUB_CHALLENGE);
                    String encodedChallange = Encode.forHtml(challange);
                    response.getWriter().write(encodedChallange);
                    response.getWriter().flush();
                    response.getWriter().close();
                } catch (IOException exception) {
                    LOGGER.error("error received while writing challange back" + exception.getMessage());
                    throw new WebSubClientException(WebSubClientErrorCode.IO_ERROR.getErrorCode(),
                            WebSubClientErrorCode.IO_ERROR.getErrorMessage().concat(exception.getMessage()));
                }

            } else {
                topicReq = topicReq.replaceAll("[\n\r\t]", " - ");
                modeReq = modeReq.replaceAll("[\n\r\t]", " - ");
                mode = mode.replaceAll("[\n\r\t]", " - ");
                LOGGER.error("intent verification failed: {} {} {} {}", topic, mode, topicReq, modeReq);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().flush();
                response.getWriter().close();
            }

        } else {
            filterChain.doFilter(request, response);
        }

    }

    private String matchCallbackURL(String requestURI) {
        logger.info("mappings" + mappings);
        logger.info("requestURI-" + requestURI);
        if (mappings.containsKey(requestURI)) {
            return mappings.get(requestURI);
        } else {
            Set<String> mappingKeys = mappings.keySet();
            for (String keys : mappingKeys) {
                int pathParamIndex = keys.indexOf("/{");
                if (pathParamIndex == -1) {
                    continue;
                }
                String url = keys.substring(0, pathParamIndex);
                if (requestURI.contains(url)) {
                    int pathParamCount = keys.split("\\/\\{").length - 1;
                    if (requestURI.substring(pathParamIndex).split("\\/").length - 1 == pathParamCount) {
                        return mappings.get(keys);
                    }
                    ;
                }
            }
        }
        return null;
    }
}
