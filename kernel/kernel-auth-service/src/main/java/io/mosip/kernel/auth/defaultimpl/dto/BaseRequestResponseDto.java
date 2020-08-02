/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * @author Ramadurai Pandian
 *
 */
@Data
public class BaseRequestResponseDto {

	/** The id. */
	private String id;

	/** The ver. */
	private String version;

	/** The timestamp. */
	private String timestamp;
}
