package io.mosip.kernel.core.authmanager.authadapter.model;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Spring Security {@link UserDetails} holding the authenticated MOSIP user,
 * token, and granted authorities.
 * <p>
 * Contract: constructed from {@link MosipUserDto} after successful AuthN.
 * Obtain the current instance via {@code SecurityContextHolder.getPrincipal()}.
 * Authorities are prefixed with {@link #ROLE_AUTHORITY_PREFIX} or
 * {@link #SCOPE_AUTHORITY_PREFIX}. Does not perform HTTP; callers populate
 * roles and scopes after construction.
 * </p>
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class AuthUserDetails implements UserDetails {

	/**
	 * Prefix applied to OAuth scope names when stored as granted authorities.
	 */
	public static final String SCOPE_AUTHORITY_PREFIX = "SCOPE_";

	/**
	 * Prefix applied to MOSIP role names when stored as granted authorities.
	 */
	public static final String ROLE_AUTHORITY_PREFIX = "ROLE_";

	/**
	 * Serializable version ID.
	 */
	private static final long serialVersionUID = 4068560701182593212L;

	/**
	 * Unique user identifier used as the Spring username.
	 */
	private String userId;
	/**
	 * Access token bound to this principal; sensitive.
	 */
	private String token;
	/**
	 * Email address; may be null.
	 */
	private String mail;
	/**
	 * Mobile number; may be null.
	 */
	private String mobile;
	/**
	 * Registration ID; may be null.
	 */
	private String rId;
	/**
	 * OpenID Connect ID token; sensitive; may be null.
	 */
	private String idToken;

	/**
	 * Granted role and scope authorities; may be null until first add.
	 */
	private Collection<? extends GrantedAuthority> authorities;

	/**
	 * Builds user details from a MOSIP user DTO and access token.
	 *
	 * @param mosipUserDto never-null source profile
	 * @param token        access token; may be null if not yet issued
	 */
	public AuthUserDetails(MosipUserDto mosipUserDto, String token) {
		this.userId = mosipUserDto.getUserId();
		this.token = token;
		this.mail = mosipUserDto.getMail();
		this.mobile = mosipUserDto.getMobile();
		this.rId = mosipUserDto.getRId();
	}

	/**
	 * Builds user details from a MOSIP user DTO, access token, and ID token.
	 *
	 * @param mosipUserDto never-null source profile
	 * @param token        access token; may be null if not yet issued
	 * @param idToken      OpenID Connect ID token; may be null
	 */
	public AuthUserDetails(MosipUserDto mosipUserDto, String token, String idToken) {
		this.userId = mosipUserDto.getUserId();
		this.token = token;
		this.mail = mosipUserDto.getMail();
		this.mobile = mosipUserDto.getMobile();
		this.rId = mosipUserDto.getRId();
		this.idToken = idToken;
	}

	/**
	 * Returns the granted role and scope authorities.
	 *
	 * @return authorities collection; may be null if none were added
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/**
	 * Appends authorities with an optional prefix, replacing the current set
	 * with an unmodifiable union.
	 *
	 * @param authorities     never-null source authorities
	 * @param authorityPrefix prefix such as {@link #ROLE_AUTHORITY_PREFIX}; null
	 *                        means no prefix
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
	 * Adds MOSIP roles as {@code ROLE_}-prefixed granted authorities.
	 *
	 * @param authorities never-null roles to add; empty collection is a no-op
	 *                    aside from prefix mapping
	 */
	public void addRoleAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.addAuthorities(authorities, ROLE_AUTHORITY_PREFIX);
	}
	
	/**
	 * Adds OAuth scopes as {@code SCOPE_}-prefixed granted authorities.
	 *
	 * @param authorities never-null scopes to add; empty collection is a no-op
	 *                    aside from prefix mapping
	 */
	public void addScopeAuthorities(Collection<? extends GrantedAuthority> authorities) {
		this.addAuthorities(authorities, SCOPE_AUTHORITY_PREFIX);
	}

	/**
	 * Returns the password; always {@code null} because MOSIP uses tokens.
	 *
	 * @return always {@code null}
	 */
	@Override
	public String getPassword() {
		return null;
	}

	/**
	 * Returns the user id as the Spring Security username.
	 *
	 * @return user id; may be null if the source DTO omitted it
	 */
	@Override
	public String getUsername() {
		return userId;
	}

	/**
	 * Returns whether the account is non-expired; always {@code true}.
	 *
	 * @return always {@code true}
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * Returns whether the account is non-locked; always {@code true}.
	 *
	 * @return always {@code true}
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * Returns whether credentials are non-expired; always {@code true}.
	 *
	 * @return always {@code true}
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * Returns whether the account is enabled; always {@code true}.
	 *
	 * @return always {@code true}
	 */
	@Override
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Returns the unique user identifier.
	 *
	 * @return user id; may be null
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Sets the unique user identifier (also the Spring username).
	 *
	 * @param userName user id; may be null
	 */
	public void setUserName(String userName) {
		this.userId = userName;
	}

	/**
	 * Returns the access token bound to this principal.
	 *
	 * @return access token; may be null; sensitive
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Sets the access token bound to this principal.
	 *
	 * @param token access token; may be null; sensitive
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * Returns the email address.
	 *
	 * @return email; may be null
	 */
	public String getMail() {
		return mail;
	}

	/**
	 * Sets the email address.
	 *
	 * @param mail email; may be null
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}

	/**
	 * Returns the mobile number.
	 *
	 * @return mobile number; may be null
	 */
	public String getMobile() {
		return mobile;
	}

	/**
	 * Sets the mobile number.
	 *
	 * @param mobile mobile number; may be null
	 */
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	/**
	 * Returns the registration ID associated with the user.
	 *
	 * @return registration ID; may be null
	 */
	public String getrId() {
		return rId;
	}

	/**
	 * Sets the registration ID associated with the user.
	 *
	 * @param rId registration ID; may be null
	 */
	public void setrId(String rId) {
		this.rId = rId;
	}

	/**
	 * Returns the OpenID Connect ID token.
	 *
	 * @return ID token; may be null; sensitive
	 */
	public String getIdToken() {
		return idToken;
	}

	/**
	 * Sets the OpenID Connect ID token.
	 *
	 * @param idToken ID token; may be null; sensitive
	 */
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}
}
