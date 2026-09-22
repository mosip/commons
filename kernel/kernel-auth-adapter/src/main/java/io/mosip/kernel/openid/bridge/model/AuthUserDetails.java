package io.mosip.kernel.openid.bridge.model;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Used by spring security to store user details like roles and use this across
 * the application for Authorization purpose. The user details can be fetched
 * using principal in SecurityContextHolder
 * <p>
 * Extends the kernel-core {@code AuthUserDetails} with OIDC ID-token storage
 * and separate role vs scope authorities. Role authorities are prefixed
 * {@link #ROLE_AUTHORITY_PREFIX}; OIDC scopes are prefixed
 * {@link #SCOPE_AUTHORITY_PREFIX} so {@link io.mosip.kernel.openid.bridge.api.service.validator.ScopeValidator}
 * can distinguish them.
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */

public class AuthUserDetails extends io.mosip.kernel.core.authmanager.authadapter.model.AuthUserDetails {

	/**
	 * Serialization identifier.
	 */
	private static final long serialVersionUID = 867530372652743714L;

	/**
	 * Prefix applied to OIDC scope names when they are stored as Spring
	 * {@link GrantedAuthority} values (for example {@code SCOPE_openid}).
	 */
	public static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";

	/**
	 * Prefix applied to Keycloak/MOSIP role names when they are stored as Spring
	 * {@link GrantedAuthority} values (for example {@code ROLE_REGISTRATION_ADMIN}).
	 */
	public static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

	/**
	 * OIDC ID token (JWT) associated with this session, if the IAM returned one.
	 */
	private String idToken;

	/**
	 * Combined role and scope authorities for this principal. Built via
	 * {@link #addRoleAuthorities(Collection)} and
	 * {@link #addScopeAuthorities(Collection)}; never mutated in place.
	 */
	private Collection<? extends GrantedAuthority> authorities;

	/**
	 * Creates a principal from MOSIP user attributes and the access token.
	 *
	 * @param mosipUserDto MOSIP user (id, roles, and related claims)
	 * @param token        access token stored on the parent user-details object
	 */
	public AuthUserDetails(MosipUserDto mosipUserDto, String token) {
		super(mosipUserDto, token);
	}

	/**
	 * Creates a principal that also retains the OIDC ID token.
	 *
	 * @param mosipUserDto MOSIP user (id, roles, and related claims)
	 * @param token        access token stored on the parent user-details object
	 * @param idToken      OIDC ID token from the token endpoint
	 */
	public AuthUserDetails(MosipUserDto mosipUserDto, String token, String idToken) {
		this(mosipUserDto, token);
		this.idToken = idToken;
	}

	/**
	 * Returns the accumulated role and scope authorities, or {@code null} if none
	 * have been added yet.
	 *
	 * @return unmodifiable authority collection, or {@code null}
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * Appends authorities, optionally prefixing each value, and replaces
	 * {@link #authorities} with an unmodifiable concatenation.
	 *
	 * @param authorities     authorities to add
	 * @param authorityPrefix prefix such as {@link #ROLE_AUTHORITY_PREFIX}, or
	 *                        {@code null} to keep the original authority string
	 */
	private void addAuthorities(Collection<? extends GrantedAuthority> authorities, String authorityPrefix) {
		Stream<SimpleGrantedAuthority> authortiesStream = authorities.stream().map(grantedAuthority -> {
			String authority = authorityPrefix == null ?  grantedAuthority.getAuthority() : authorityPrefix + grantedAuthority.getAuthority();
			return new SimpleGrantedAuthority(authority);
		});
		
		if(this.authorities == null) {
			this.authorities = Collections.unmodifiableCollection(authortiesStream
					.collect(Collectors.toList()));
		} else {
			this.authorities = Collections.unmodifiableCollection(Stream.concat(this.authorities.stream(), authortiesStream)
					.collect(Collectors.toList()));
		}
	}
	
	/**
	 * Adds the given authorities as MOSIP/Keycloak roles, each prefixed with
	 * {@link #ROLE_AUTHORITY_PREFIX}.
	 *
	 * @param authorities role authorities whose {@code getAuthority()} values are
	 *                    role names
	 */
	public void addRoleAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.addAuthorities(authorities, ROLE_AUTHORITY_PREFIX);
	}
	
	/**
	 * Adds the given authorities as OIDC scopes, each prefixed with
	 * {@link #SCOPE_AUTHORITY_PREFIX}.
	 *
	 * @param authorities scope authorities whose {@code getAuthority()} values are
	 *                    scope names
	 */
	public void addScopeAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.addAuthorities(authorities, SCOPE_AUTHORITY_PREFIX);
	}
	
	/**
	 * Adds authorities as roles. Delegates to {@link #addRoleAuthorities(Collection)}.
	 *
	 * @param authorities role authorities to add
	 * @deprecated use {@link #addRoleAuthorities(Collection)}; this method does not
	 *             replace the collection, it only appends role-prefixed authorities
	 */
	@Deprecated
	public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.addRoleAuthorities(authorities);
	}

	/**
	 * Returns the OIDC ID token stored on this principal, if any.
	 *
	 * @return ID token JWT, or {@code null}
	 */
	public String getIdToken() {
		return idToken;
	}

	/**
	 * Sets the OIDC ID token for this session.
	 *
	 * @param idToken ID token JWT from the IAM
	 */
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
}
