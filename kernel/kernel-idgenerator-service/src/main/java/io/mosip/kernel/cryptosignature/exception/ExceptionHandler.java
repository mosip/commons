package io.mosip.kernel.cryptosignature.exception;

import java.io.IOException;
import java.util.List;

import io.mosip.kernel.core.authmanager.exception.AuthNException;
import io.mosip.kernel.core.authmanager.exception.AuthZException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.client.HttpStatusCodeException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;
import io.mosip.kernel.core.signatureutil.exception.ParseResponseException;
import io.mosip.kernel.core.signatureutil.exception.SignatureUtilClientException;
import io.mosip.kernel.cryptosignature.constant.SigningDataErrorCode;

/**
 * Maps keymanager REST failures into MOSIP auth and signature exceptions.
 *
 * @author Urvil Joshi
 *
 */
public class ExceptionHandler {

	/**
	 * Prevents instantiation of this utility type.
	 */
	private ExceptionHandler() {
	}

	/**
	 * Converts HTTP 401/403 from keymanager into MOSIP authentication exceptions.
	 * <p>
	 * HTTP 401 throws {@link AuthNException} when {@code validationErrorsList} is
	 * non-empty, otherwise {@link BadCredentialsException}. HTTP 403 throws
	 * {@link AuthZException} when the list is non-empty, otherwise
	 * {@link AccessDeniedException}.
	 * </p>
	 *
	 * @param ex                   the HTTP status exception from the REST call
	 * @param validationErrorsList MOSIP {@link ServiceError} entries parsed from the body
	 * @param source               logical source name included in fallback messages
	 * @throws AuthNException          when the response is HTTP 401 with service errors
	 * @throws AuthZException          when the response is HTTP 403 with service errors
	 * @throws BadCredentialsException when the response is HTTP 401 without service errors
	 * @throws AccessDeniedException   when the response is HTTP 403 without service errors
	 */
	public static void authExceptionHandler(HttpStatusCodeException ex, List<ServiceError> validationErrorsList,
			String source) {
		if (ex.getStatusCode().value() == 401) {
			if (!validationErrorsList.isEmpty()) {
				throw new AuthNException(validationErrorsList);
			} else {
				throw new BadCredentialsException("Authentication failed for " + source);
			}
		}
		if (ex.getStatusCode().value() == 403) {
			if (!validationErrorsList.isEmpty()) {
				throw new AuthZException(validationErrorsList);
			} else {
				throw new AccessDeniedException("Access denied for " + source);
			}
		}
	}

	/**
	 * Fails when the keymanager response is missing or contains MOSIP service errors.
	 *
	 * @param response the HTTP entity returned by keymanager; may be {@code null}
	 * @throws ParseResponseException      when {@code response} is {@code null}
	 * @throws SignatureUtilClientException when the body lists {@link ServiceError} entries
	 */
	public static void throwExceptionIfExist(ResponseEntity<String> response) {
		if (response == null) {
			throw new ParseResponseException(SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorCode(),
					SigningDataErrorCode.REST_CRYPTO_CLIENT_EXCEPTION.getErrorMessage());
		}
		String responseBody = response.getBody();
		List<ServiceError> validationErrorList = ExceptionUtils.getServiceErrorList(responseBody);
		if (!validationErrorList.isEmpty()) {
			throw new SignatureUtilClientException(validationErrorList);
		}
	}

	/**
	 * Deserializes the {@code response} node of a MOSIP wrapper into {@code clazz}.
	 *
	 * @param <S>          target DTO type
	 * @param objectMapper Jackson mapper used to parse the body
	 * @param response     HTTP entity whose body contains a {@code response} field
	 * @param clazz        target type of the {@code response} node
	 * @return the mapped DTO
	 * @throws ParseResponseException when the body cannot be parsed
	 */
	public static <S> S getResponse(ObjectMapper objectMapper, ResponseEntity<String> response, Class<S> clazz) {
		try {
			JsonNode res = objectMapper.readTree(response.getBody());
			return objectMapper.readValue(res.get("response").toString(), clazz);
		} catch (IOException | NullPointerException exception) {
			throw new ParseResponseException(SigningDataErrorCode.RESPONSE_PARSE_EXCEPTION.getErrorCode(),
					SigningDataErrorCode.RESPONSE_PARSE_EXCEPTION.getErrorMessage() + exception.getMessage(),
					exception);
		}
	}

}
