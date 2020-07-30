package io.mosip.kernel.core.authmanager.model;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class UserOtp {

	@NotBlank
	private String userId;
	@NotBlank
	private String otp;
	@NotBlank
	private String appId;

}
