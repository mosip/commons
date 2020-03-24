package io.mosip.kernel.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshTokenResponse {
	private AuthNResponse authNResponse;
	private String accesstoken;
	private String refreshToken;
	private String accessTokenExpTime;
	private String refreshTokenExpTime;
}
