package io.mosip.kernel.keymigrate.dto;

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

//KeyMigratorController
	
	private List<String> postmigratebasekey;
	
	private List<String> getgetzktempcertificate;
	
	private List<String> postmigratezkkeys;

}	