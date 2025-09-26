package io.mosip.kernel.emailnotification.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;

@RestControllerAdvice
public class ResponseBodyAdviceConfig implements ResponseBodyAdvice<ResponseWrapper<?>> {

    private static final Logger LOGGER = LoggerConfiguration.logConfig(ResponseBodyAdviceConfig.class);

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(ResponseFilter.class);
    }

    @Override
    public ResponseWrapper<?> beforeBodyWrite(
            ResponseWrapper<?> body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        if (body == null) return null;
        if (!MediaType.APPLICATION_JSON.includes(selectedContentType)) return body;

        try {
            HttpServletRequest servletReq = ((ServletServerHttpRequest) request).getServletRequest();
            Object id = servletReq.getAttribute("REQ_ID");
            Object ver = servletReq.getAttribute("REQ_VER");
            if (id instanceof String s && !s.isBlank()) body.setId(s);
            if (ver instanceof String s && !s.isBlank()) body.setVersion(s);

            // Optional: only clear errors if you *always* want null on success.
            // if (body.getErrors() == null || body.getErrors().isEmpty()) body.setErrors(null);

        } catch (Exception e) {
            LOGGER.error("Response wrapping failed", e);
        }
        return body;
    }
}