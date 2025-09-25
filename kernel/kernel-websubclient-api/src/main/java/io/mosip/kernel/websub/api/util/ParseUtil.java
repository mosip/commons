package io.mosip.kernel.websub.api.util;

import io.mosip.kernel.websub.api.constants.WebSubClientErrorCode;
import io.mosip.kernel.websub.api.exception.WebSubClientException;
import io.mosip.kernel.websub.api.model.HubResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.util.StringUtils;
/**
 * Utility class for parsing WebSub hub responses.
 * <p>
 * Provides a static method to parse WebSub hub response strings (e.g., "hub.result=accepted&hub.reason=invalid_request")
 * into {@link HubResponse} objects, as per RFC 7033. Used by components like
 * {@link io.mosip.kernel.websub.api.client.SubscriberClientImpl} to process hub responses for subscription
 * and unsubscription operations. Validates input and throws {@link WebSubClientException} for malformed responses.
 * </p>
 *
 * @author Urvil Joshi
 * @since 1.0.0
 * @see HubResponse
 * @see WebSubClientErrorCode
 */
public class ParseUtil {

	/**
	 * Logger for debugging and error reporting.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ParseUtil.class);

	/**
	 * Private constructor to prevent instantiation.
	 */
	private ParseUtil() {
		//
	}

	/**
	 * Parses a WebSub hub response string into a {@link HubResponse}.
	 * <p>
	 * Expects a string in the format "hub.result=value[&hub.reason=reason]" (e.g., "hub.result=accepted&hub.reason=invalid_request").
	 * Extracts the result and optional reason, setting them on a {@link HubResponse} object. Throws
	 * {@link WebSubClientException} with {@link WebSubClientErrorCode#INTERNAL_ERROR} if the input is
	 * malformed or null.
	 * </p>
	 *
	 * @param responseString the hub response string
	 * @return the parsed {@link HubResponse}
	 * @throws WebSubClientException if the response string is invalid
	 */
	public static HubResponse parseHubResponse(String responseString) {
		if (!StringUtils.hasText(responseString)) {
			LOGGER.error("Invalid hub response: responseString is null or empty");
			throw new WebSubClientException(WebSubClientErrorCode.INTERNAL_ERROR.getErrorCode(),
					WebSubClientErrorCode.INTERNAL_ERROR.getErrorMessage() + ": responseString is null or empty");
		}

		HubResponse hubResponse = new HubResponse();
		try {
			String[] responsePairs = responseString.split("&");
			if (responsePairs.length == 0) {
				LOGGER.error("Invalid hub response: no result found in '{}'", responseString);
				throw new WebSubClientException(WebSubClientErrorCode.INTERNAL_ERROR.getErrorCode(),
						WebSubClientErrorCode.INTERNAL_ERROR.getErrorMessage() + ": no result found");
			}

			String[] resultPair = responsePairs[0].split("=");
			if (resultPair.length < 2 || !StringUtils.hasText(resultPair[1])) {
				LOGGER.error("Invalid hub response: invalid hub.result in '{}'", responsePairs[0]);
				throw new WebSubClientException(WebSubClientErrorCode.INTERNAL_ERROR.getErrorCode(),
						WebSubClientErrorCode.INTERNAL_ERROR.getErrorMessage() + ": invalid hub.result");
			}
			hubResponse.setHubResult(resultPair[1]);

			if (responsePairs.length > 1) {
				String[] errorReasonPairs = responsePairs[1].split("=");
				if (errorReasonPairs.length > 1 && StringUtils.hasText(errorReasonPairs[1])) {
					hubResponse.setErrorReason(errorReasonPairs[1]);
				}
			}

			LOGGER.debug("Parsed hub response: result='{}', reason='{}'", hubResponse.getHubResult(), hubResponse.getErrorReason());
			return hubResponse;
		} catch (Exception e) {
			LOGGER.error("Failed to parse hub response '{}': {}", responseString, e.getMessage());
			throw new WebSubClientException(WebSubClientErrorCode.INTERNAL_ERROR.getErrorCode(),
					WebSubClientErrorCode.INTERNAL_ERROR.getErrorMessage() + ": failed to parse response", e);
		}
	}
}