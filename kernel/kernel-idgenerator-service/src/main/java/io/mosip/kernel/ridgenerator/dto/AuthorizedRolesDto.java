package io.mosip.kernel.ridgenerator.dto;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Spring Security role check for RID generation, bound from {@code mosip.role.kernel.getgenerateridcenteridmachineid}.
 */
@Component("authorizedRoles")
@Getter
@Setter
public class AuthorizedRolesDto {

	/**
	 * Allowed authorities for {@code GET /generate/rid/{centerid}/{machineid}}
	 * ({@code mosip.role.kernel.getgenerateridcenteridmachineid}).
	 */
	@Value("${mosip.role.kernel.getgenerateridcenteridmachineid:}")
	private String[] getgenerateridcenteridmachineid = new String[0];

	/**
	 * Returns whether the current authentication has any configured RID-generate role.
	 *
	 * @return {@code true} when a granted authority matches a configured role
	 */
	public boolean hasGenerateRidAccess() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || getgenerateridcenteridmachineid == null
				|| getgenerateridcenteridmachineid.length == 0) {
			return false;
		}
		Set<String> granted = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());
		return Arrays.stream(getgenerateridcenteridmachineid).map(String::trim)
				.anyMatch(role -> granted.contains(role) || granted.contains("ROLE_" + role));
	}
}
