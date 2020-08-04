package io.mosip.kernel.authcodeflowproxy.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//TODO use this dto from core when adapter changes are pushed
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
}
