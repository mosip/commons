/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.service;

/**
 * Obtains client-credential tokens for internal authmanager and IDA calls.
 *
 * @author Ramadurai Pandian
 *
 */
public interface TokenGenerationService {

	/**
	 * Token for authmanager's own client ({@code mosip.kernel.auth.*}).
	 *
	 * @return access token
	 * @throws Exception if client-secret authentication fails
	 */
	String getInternalTokenGenerationService() throws Exception;

	/**
	 * Token for IDA client ({@code mosip.kernel.ida.*}) used in UIN OTP flows.
	 *
	 * @return access token
	 * @throws Exception if client-secret authentication fails
	 */
	String getUINBasedToken() throws Exception;

}
