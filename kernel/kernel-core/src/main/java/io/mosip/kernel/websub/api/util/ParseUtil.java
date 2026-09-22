package io.mosip.kernel.websub.api.util;

import io.mosip.kernel.websub.api.model.HubResponse;

/**
 * Parses WebSub hub form bodies of the shape {@code hub.mode=accepted} or
 * {@code hub.mode=denied&amp;hub.reason=...} into {@link HubResponse}.
 *
 * @author Urvil Joshi
 */
public class ParseUtil {

	/**
	 * Not instantiable; use {@link #parseHubResponse(String)}.
	 */
	private ParseUtil() {
		//
	}

	/**
	 * Splits a hub {@code application/x-www-form-urlencoded} body on {@code &amp;} and
	 * {@code =}. The first pair's value is {@link HubResponse#setHubResult(String)};
	 * a second pair, if present, is treated as the denial reason.
	 *
	 * @param responseString hub response body; must contain at least {@code name=value}
	 * @return parsed hub result and optional error reason
	 */
	public static HubResponse parseHubResponse(String responseString) {
		String[] responsePairs = responseString.split("&");
		HubResponse hubResponse = new HubResponse();
		if (responsePairs.length > 1) {
			String[] errorReasonPairs = responsePairs[1].split("=");
			if (errorReasonPairs.length > 1) {
				hubResponse.setErrorReason(errorReasonPairs[1]);
			}
		}
		hubResponse.setHubResult(responsePairs[0].split("=")[1]);
		return hubResponse;
	}

}
