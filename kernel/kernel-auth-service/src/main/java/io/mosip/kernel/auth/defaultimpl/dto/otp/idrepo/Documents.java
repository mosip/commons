package io.mosip.kernel.auth.defaultimpl.dto.otp.idrepo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ID Repository document entry: a category (document type) and its encoded value.
 *
 * @author Manoj SP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Documents {

	/**
	 * Document category or type code from ID Repository.
	 */
	private String category;

	/**
	 * Encoded document content (typically base64).
	 */
	private String value;
}
