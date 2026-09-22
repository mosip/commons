package io.mosip.kernel.auth.defaultimpl.dto.otp.idrepo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ID Repository retrieve/update request body: identity JSON plus the registration id.
 *
 * @author Manoj SP
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RequestDTO extends BaseRequestResponseDTO {

	/**
	 * Registration id (RID) associated with the identity request.
	 */
	private String registrationId;
}
