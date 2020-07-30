package io.mosip.kernel.core.authmanager.model;

import lombok.Data;

@Data
public class UserOtp {

	private String userId;
	private String otp;
	private String appId;

}
