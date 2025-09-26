package io.mosip.kernel.emailnotification.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.util.ContentCachingRequestWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.EmptyCheckUtils;

/**
 * Configuration class to wrap the service response in {@link ResponseWrapper}
 * and set the request attributes.
 *
 * @author Sagar Mahapatra
 * @since 1.0.0
 *
 */
@RestControllerAdvice
public class ResponseBodyAdviceConfig implements ResponseBodyAdvice<ResponseWrapper<?>> {

    private static final Logger mosipLogger = LoggerConfiguration.logConfig(ResponseBodyAdviceConfig.class);


    /**
     * Autowired reference for {@link ObjectMapper}.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice#
     * supports(org.springframework.core.MethodParameter, java.lang.Class)
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(ResponseFilter.class);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice#
     * beforeBodyWrite(java.lang.Object, org.springframework.core.MethodParameter,
     * org.springframework.http.MediaType, java.lang.Class,
     * org.springframework.http.server.ServerHttpRequest,
     * org.springframework.http.server.ServerHttpResponse)
     */
    @Override
    public ResponseWrapper<?> beforeBodyWrite(ResponseWrapper<?> body, MethodParameter returnType,
                                              MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                              ServerHttpRequest request, ServerHttpResponse response) {

        // 0) Null-safety
        if (body == null) return null;

        // 1) Only touch JSON responses
        if (!MediaType.APPLICATION_JSON.includes(selectedContentType)) {
            return body;
        }

        try {
            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();

            // 3) Only attempt if request was JSON
            String reqContentType = servletRequest.getContentType();
            if (reqContentType == null || !reqContentType.toLowerCase().contains("application/json")) {
                return body; // e.g., multipart/form-data for email
            }

            byte[] cached = null;
            if (servletRequest instanceof ContentCachingRequestWrapper c) {
                cached = c.getContentAsByteArray();
            } else if (servletRequest instanceof HttpServletRequestWrapper w &&
                    w.getRequest() instanceof ContentCachingRequestWrapper c2) {
                cached = c2.getContentAsByteArray();
            }

            if (cached == null || cached.length == 0) {
                return body;
            }

            // 4) Parse minimally: read only id/version from a Map
            var node = objectMapper.readTree(cached);
            var idNode = node.get("id");
            var versionNode = node.get("version");

            if (idNode != null && !idNode.isNull()) {
                body.setId(idNode.asText());
            }
            if (versionNode != null && !versionNode.isNull()) {
                body.setVersion(versionNode.asText());
            }

            // 5) Only null errors for success responses (optional policy)
            // if (body.getErrors() == null || body.getErrors().isEmpty()) {
            //   body.setErrors(null);
            // }

        } catch (Exception e) {
            mosipLogger.error("", "", "Response wrapping failed", e.getMessage());
        }
        return body;
    }
}