package io.mosip.kernel.openid.bridge.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MOSIP USER IS THE STANDARD SPEC THAT WILL BE TUNED BASED ON THE DETAILS
 * STORED IN LDAP FOR A USER
 * <p>
 * OpenID Bridge view of a MOSIP principal. This type adds no extra fields; it
 * inherits user id, name, email, mobile, role, RID, token, and related
 * attributes from
 * {@link io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto}.
 * Used by the auth-code proxy after token validation and by
 * {@link AuthUserDetails} as the Spring Security principal payload.
 *
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */

@Data
@EqualsAndHashCode(callSuper=true)
public class MosipUserDto extends io.mosip.kernel.core.authmanager.authadapter.model.MosipUserDto {
}
