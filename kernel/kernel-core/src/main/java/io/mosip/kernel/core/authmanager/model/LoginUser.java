/**
 * 
 */
package io.mosip.kernel.core.authmanager.model;

import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class LoginUser {

	private String userName;
	private String password;
	private String appId;
}
