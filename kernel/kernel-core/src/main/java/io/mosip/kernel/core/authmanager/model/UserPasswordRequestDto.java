package io.mosip.kernel.core.authmanager.model;

import jakarta.validation.constraints.NotBlank;

import io.mosip.kernel.core.authmanager.constant.AuthConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPasswordRequestDto {
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String appId;
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String userName;
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String rid;
	@NotBlank(message = AuthConstant.INVALID_REQUEST)
	private String password;

}
