package io.mosip.kernel.core.authmanager.model;


import lombok.Data;

/**
 * Username/password login request that also supplies OAuth client credentials.
 * <p>
 * Contract: preferred over {@link LoginUser} for
 * {@link io.mosip.kernel.core.authmanager.spi.AuthNService#authenticateUser(LoginUserWithClientId)}.
 * All fields must be non-null and non-empty. Treat {@code password} and
 * {@code clientSecret} as sensitive; do not log them.
 * </p>
 *
 * @see LoginUser
 */
@Data
public class LoginUserWithClientId {
    /**
     * Login name of the user; must be non-blank.
     */
    private String userName;
    /**
     * Clear-text password; must be non-blank; never log this value.
     */
    private String password;
    /**
     * MOSIP application identifier the user is logging into; must be non-blank.
     */
    private String appId;
    /**
     * OAuth client identifier; must be non-blank.
     */
    private String clientId;
    /**
     * OAuth client secret; must be non-blank; never log this value.
     */
    private String clientSecret;
}
