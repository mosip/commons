package io.mosip.kernel.auth.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RefreshTokenRequest {
	@NotBlank
	private String clientID;
	@NotBlank
	private String clientSecret;
}
