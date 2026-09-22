package io.mosip.kernel.auth.config;

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

import io.mosip.kernel.auth.defaultimpl.config.LoggerConfiguration;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.http.ResponseFilter;
import io.mosip.kernel.core.http.ResponseWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.EmptyCheckUtils;

/**
 * Copies {@code id} and {@code version} from the cached MOSIP {@link RequestWrapper}
 * into the {@link ResponseWrapper} for controller methods annotated with
 * {@link ResponseFilter}. Relies on {@link ReqResFilter} wrapping the request.
 *
 * @author Bal Vikash Sharma
 *
 */
@RestControllerAdvice
public class ResponseBodyAdviceConfig implements ResponseBodyAdvice<ResponseWrapper<?>> {

	/**
	 * Logger used when request-body parsing fails.
	 */
	private static final Logger mosipLogger = LoggerConfiguration.logConfig(ResponseBodyAdviceConfig.class);

	/**
	 * Mapper used to deserialize the cached request body as {@link RequestWrapper}.
	 */
	@Autowired
	private ObjectMapper objectMapper;
	
	/**
	 * Applies this advice only when the handler method has {@link ResponseFilter}.
	 *
	 * @param returnType     controller method return type
	 * @param converterType  selected HTTP message converter
	 * @return {@code true} when the method is annotated with {@link ResponseFilter}
	 */
	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return returnType.hasMethodAnnotation(ResponseFilter.class);
	}

	/**
	 * Reads the cached request JSON, copies {@code id}/{@code version} onto the
	 * response wrapper, and clears {@code errors} on the success path.
	 *
	 * @param body                  response wrapper about to be written
	 * @param returnType            controller method return type
	 * @param selectedContentType   negotiated media type
	 * @param selectedConverterType converter that will write the body
	 * @param request               current HTTP request
	 * @param response              current HTTP response
	 * @return the same or updated {@link ResponseWrapper}
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
			if (!EmptyCheckUtils.isNullEmpty(requestBody)) {
				requestWrapper = objectMapper.readValue(requestBody, RequestWrapper.class);
				body.setId(requestWrapper.getId());
				body.setVersion(requestWrapper.getVersion());
			}
			body.setErrors(null);
			return body;
		} catch (Exception e) {
			mosipLogger.error("", "", "", ExceptionUtils.getStackTrace(e));
		}

		return body;
	}

}
