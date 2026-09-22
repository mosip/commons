package io.mosip.kernel.emailnotification.dto;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Role lists used by {@code @PreAuthorize} expressions on email and SMS send
 * endpoints.
 * <p>
 * Bound from {@code mosip.role.kernel.postemailsend} and
 * {@code mosip.role.kernel.postsmssend}. Empty arrays deny access.
 */
@Component("authorizedRoles")
public class AuthorizedRolesDto {

	/**
	 * Roles allowed to call {@code POST /email/send}. Bound to
	 * {@code mosip.role.kernel.postemailsend}.
	 */
	@Value("${mosip.role.kernel.postemailsend:}")
	private String[] postemailsend = new String[0];

	/**
	 * Roles allowed to call {@code POST /sms/send}. Bound to
	 * {@code mosip.role.kernel.postsmssend}.
	 */
	@Value("${mosip.role.kernel.postsmssend:}")
	private String[] postsmssend = new String[0];

	/**
	 * Indicates whether the current authentication has any role configured for
	 * email send.
	 *
	 * @return {@code true} if the caller may invoke email send; {@code false}
	 *         otherwise
	 */
	public boolean hasEmailSendAccess() {
		return hasAnyConfiguredRole(postemailsend);
	}

	/**
	 * Indicates whether the current authentication has any role configured for SMS
	 * send.
	 *
	 * @return {@code true} if the caller may invoke SMS send; {@code false}
	 *         otherwise
	 */
	public boolean hasSmsSendAccess() {
		return hasAnyConfiguredRole(postsmssend);
	}

	/**
	 * Returns whether the current {@link Authentication} holds any of the given
	 * roles, accepting both the bare role name and the {@code ROLE_} prefix.
	 *
	 * @param roles configured role names; may be empty
	 * @return {@code true} if at least one configured role is granted
	 */
	private boolean hasAnyConfiguredRole(String[] roles) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || roles == null || roles.length == 0) {
			return false;
		}
		Set<String> granted = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());
		return Arrays.stream(roles)
				.map(String::trim)
				.anyMatch(role -> granted.contains(role) || granted.contains("ROLE_" + role));
	}
}
