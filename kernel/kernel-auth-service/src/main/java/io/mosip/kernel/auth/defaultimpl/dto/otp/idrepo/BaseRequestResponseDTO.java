package io.mosip.kernel.auth.defaultimpl.dto.otp.idrepo;

import java.util.List;

import lombok.Data;

/**
 * The Class ResponseDTO.
 *
 * @author Manoj SP
 */
@Data
public class BaseRequestResponseDTO {

	/** The status. */
	private String status;

	/** The identity. */
	private Object identity;

	private List<Documents> documents;
}
