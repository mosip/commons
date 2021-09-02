package io.mosip.kernel.websub.api.util;

import io.mosip.kernel.websub.api.model.HubResponse;

public class ParseUtil {
	
	private ParseUtil() {
		//
	}
	
	public static HubResponse parseHubResponse(String responseString) {
		String[] responsePairs = responseString.split("&");
		HubResponse hubResponse = new HubResponse();
		if (responsePairs.length > 1) {
			hubResponse.setHubResult(responsePairs[1].split("=")[1]);
		}
		hubResponse.setHubResult(responsePairs[0].split("=")[1]);
		return hubResponse;
	}

}
