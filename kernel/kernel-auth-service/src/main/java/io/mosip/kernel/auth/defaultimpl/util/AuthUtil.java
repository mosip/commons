package io.mosip.kernel.auth.defaultimpl.util;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.auth.defaultimpl.constant.AuthErrorCode;
import io.mosip.kernel.auth.defaultimpl.exception.AuthManagerException;

/**
 * The Class AuthUtil.
 */
@Component
public class AuthUtil {

	/** The propertyname. */
	@Value("#{${mosip.kernel.auth.appid-realm-map}}")
	private Map<String, String> propertyname;

	/**
	 * Gets the realm id from app id.
	 *
	 * @param appId
	 *            the app id
	 * @return the realm id from app id
	 */
	public String getRealmIdFromAppId(String appId) {
		String realmId = null;

		if (propertyname.get(appId)!= null) {
			realmId = propertyname.get(appId).toLowerCase();
		} else {
			throw new AuthManagerException(AuthErrorCode.REALM_NOT_FOUND.getErrorCode(),
					String.format(AuthErrorCode.REALM_NOT_FOUND.getErrorMessage(), appId));
		}
		return realmId;
	}
}
