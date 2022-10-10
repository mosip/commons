package io.mosip.kernel.core.authmanager.authadapter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MOSIP USER IS THE STANDARD SPEC THAT WILL BE TUNED BASED ON THE DETAILS
 * STORED IN LDAP FOR A USER
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MosipUserDto {
	private String userId;
	private String mobile;
	private String mail;
	private String langCode;
	private String userPassword;
	private String name;
	private String role;
	private String rId;
	private String token;
	private String idToken;

	public MosipUserDto(String userId, String mobile, String mail, String langCode, String userPassword, String name, String role, String rId, String token) {
		this.userId = userId;
		this.mobile = mobile;
		this.mail = mail;
		this.langCode = langCode;
		this.userPassword = userPassword;
		this.name = name;
		this.role = role;
		this.rId = rId;
		this.token = token;
	}
}