package io.mosip.kernel.core.authmanager.model;

import jakarta.validation.constraints.NotBlank;

import io.mosip.kernel.core.authmanager.constant.AuthConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to set or reset a user's password using RID as the correlation id.
 * <p>
 * Contract: all fields are required and non-blank. Treat {@code password} as
 * sensitive. Used by password-set APIs over HTTP.
 * </p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordRequestDto {
	/**
	 * MOSIP application identifier; required, non-blank.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String appId;
	/**
	 * Login name of the user; required, non-blank.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String userName;
	/**
	 * Registration ID used to authorize the password change; required, non-blank.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String rid;
	/**
	 * New password in clear text; required, non-blank; never log this value.
	 */
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String password;

}
