package io.mosip.kernel.emailnotification.dto;

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

//EmailNotificationController
	
	private List<String> postemailsend;
	
//SmsNotificationController
	
	private List<String>postsmssend;
}