package io.mosip.kernel.cryptomanager.dto;

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

	//CryptomanagerController
	
	private List<String> postencrypt;
	
	private List<String> postdecrypt;
	
	private List<String> postencryptwithpin;
	
	private List<String> postdecryptwithpin;
	
	private List<String> postencryptdt;
	
	private List<String> postdecryptdt;

}	