package io.mosip.kernel.biosdk.provider.impl;

import org.springframework.stereotype.Component;
@Component
public class BioProviderImpl_V_1_2 extends BioProviderImpl_V_0_9 {
	/*
	 * Since both class implementations of BioProviderImpl_V_1_2 and
	 * BioProviderImpl_V_0_9 are same, to avoid the code duplication, extending the
	 * implementation of BioProviderImpl_V_0_9 to BioProviderImpl_V_1_2
	 */
	private static final String API_VERSION = "0.9";

	@Override
	protected String getApiVersion() {
		return API_VERSION;
	}
	
}