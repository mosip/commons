/**
 * 
 */
package io.mosip.kernel.openid.bridge.api.constants;

/**
 * JWT claim names used when mapping a Keycloak access token to a MOSIP user.
 *
 * @author Ramadurai Saravana Pandian
 *
 */
public class AuthConstant {

	/**
	 * OIDC {@code preferred_username} claim; used as MOSIP user id / display name.
	 */
	public static final String PREFERRED_USERNAME = "preferred_username";
	
	/**
	 * OIDC {@code azp} (authorized party) claim; the client id that requested the
	 * token.
	 */
	public static final String AZP = "azp";

	/**
	 * JWT {@code iss} (issuer) claim; Keycloak realm issuer URL.
	 */
	public static final String ISSUER = "iss";


}
