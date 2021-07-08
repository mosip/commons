package io.mosip.kernel.clientcrypto.dto;

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

	//ClientCrypto
	
	private List<String>postcssign ;
	
	private List<String> postcsverifysign;
	
	private List<String> posttpmencrypt;
	
	private List<String> posttpmdecrypt;
	
	private List<String> posttpmsigningpublickey;
	
	private List<String> posttpmencryptionpublickey;
	
}