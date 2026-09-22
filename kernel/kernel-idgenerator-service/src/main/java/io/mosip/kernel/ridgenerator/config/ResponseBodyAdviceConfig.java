package io.mosip.kernel.ridgenerator.config;

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

	/**
	 * Applies this advice only to controller methods annotated with {@link ResponseFilter}.
	 *
	 * @param returnType    controller method return type
	 * @param converterType selected HTTP message converter
	 * @return {@code true} when the method has {@link ResponseFilter}
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.hasMethodAnnotation(ResponseFilter.class);
	}

	/**
	 * Copies {@code id} and {@code version} from the cached request body onto {@code body}.
	 *
	 * @param body                 MOSIP response wrapper
	 * @param returnType           controller method return type
	 * @param selectedContentType  selected media type
	 * @param selectedConverterType selected converter
	 * @param request              current request
	 * @param response             current response
	 * @return {@code body} with id/version set when the request JSON could be parsed
	 */
	@Override
	public ResponseWrapper<?> beforeBodyWrite(ResponseWrapper<?> body, MethodParameter returnType,
			MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
			ServerHttpRequest request, ServerHttpResponse response) {
		RequestWrapper<?> requestWrapper = null;
		String requestBody = null;
		try {
			HttpServletRequest httpServletRequest = ((ServletServerHttpRequest) request).getServletRequest();

			if (httpServletRequest instanceof ContentCachingRequestWrapper) {
				requestBody = new String(((ContentCachingRequestWrapper) httpServletRequest).getContentAsByteArray());
			} else if (httpServletRequest instanceof HttpServletRequestWrapper
					&& ((HttpServletRequestWrapper) httpServletRequest)
							.getRequest() instanceof ContentCachingRequestWrapper) {
				requestBody = new String(
						((ContentCachingRequestWrapper) ((HttpServletRequestWrapper) httpServletRequest).getRequest())
								.getContentAsByteArray());
			}
			objectMapper.registerModule(new JavaTimeModule());
			if (!EmptyCheckUtils.isNullEmpty(requestBody)) {
				requestWrapper = objectMapper.readValue(requestBody, RequestWrapper.class);
				body.setId(requestWrapper.getId());
				body.setVersion(requestWrapper.getVersion());
			}
			body.setErrors(null);
			return body;
		} catch (Exception e) {
			mosipLogger.error("", "", "", e.getMessage());
		}
		return body;
	}

}
