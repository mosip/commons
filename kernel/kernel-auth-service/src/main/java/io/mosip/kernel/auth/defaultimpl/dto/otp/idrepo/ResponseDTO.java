package io.mosip.kernel.auth.defaultimpl.dto.otp.idrepo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ID Repository retrieve response body used by {@code UinServiceImpl} to read
 * identity JSON (and optional documents) when sending OTP against a UIN.
 *
 * @author Manoj SP
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResponseDTO extends BaseRequestResponseDTO {

	/**
	 * ID Repository entity identifier returned with the identity payload.
	 */
	private String entity;

}
