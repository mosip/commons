package io.mosip.kernel.auth.defaultimpl.dto.otp.idrepo;

import java.util.List;

import lombok.Data;

/**
 * Shared ID Repository identity payload used by both {@link RequestDTO} and
 * {@link ResponseDTO}. Carries identity JSON, optional documents, and status.
 *
 * @author Manoj SP
 */
@Data
public class BaseRequestResponseDTO {

	/**
	 * ID Repository processing status.
	 */
	private String status;

	/**
	 * Identity JSON object (schema-defined demographic attributes).
	 */
	private Object identity;

	/**
	 * Optional identity documents attached to the ID Repository record.
	 */
	private List<Documents> documents;
}
