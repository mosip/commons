package io.mosip.kernel.websub.api.utils;

import org.springframework.http.HttpHeaders;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;

public class ParameterValidationUtil {

	private static final String COOKIE = "Cookie";
	private static final String AUTHOTRIZATION = "Authotrization=";
	private static final String AUTH_TOKEN_NULL_OR_EMPTY = "auth token null or empty";

	private ParameterValidationUtil() {
	}

	public static void checkMissingToken(boolean isAuthFilterDisable, String authToken, HttpHeaders headers) {
		if (!isAuthFilterDisable) {
			if (EmptyCheckUtils.isNullEmpty(authToken))
				throw new WebSubClientException(WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorCode(),
						WebSubClientErrorCode.INPUT_VERIFICATION_ERROR.getErrorMessage() + AUTH_TOKEN_NULL_OR_EMPTY);
			headers.add(COOKIE, AUTHOTRIZATION + authToken);
		}
	}
}
