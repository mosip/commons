package io.mosip.kernel.zkcryptoservice.dto;

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

//ZKCryptoManagerController
  	
	private List<String>postzkencrypt;
	
	private List<String>postzkdecrypt;
	
	private List<String>postzkreencryptrandomkey;

}	