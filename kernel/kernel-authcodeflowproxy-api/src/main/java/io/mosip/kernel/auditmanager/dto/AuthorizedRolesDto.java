package io.mosip.kernel.auditmanager.dto;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;


/**
 * Role names authorized to invoke kernel audit-manager APIs.
 * <p>
 * Bound from properties with prefix {@code mosip.role.kernel} and registered as the
 * Spring bean {@code authorizedRoles}. Lombok generates accessors for each field.
 */
@Component("authorizedRoles")
@ConfigurationProperties(prefix = "mosip.role.kernel")
@Getter
@Setter
public class AuthorizedRolesDto {

	/**
	 * Roles permitted to POST audit events ({@code mosip.role.kernel.postaudits}).
	 */
	//AuditManager
	private List<String> postaudits;
	
}
