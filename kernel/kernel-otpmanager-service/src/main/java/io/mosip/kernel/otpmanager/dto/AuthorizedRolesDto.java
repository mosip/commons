package io.mosip.kernel.otpmanager.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.kernel")
@Getter
@Setter
public class AuthorizedRolesDto {

//OtpGeneratorController
	
	private List<String>postotpgenerate;
	
//	OtpValidatorController
	
	private List<String>getotpvalidate;
	
}