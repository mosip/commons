package io.mosip.kernel.vidgenerator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response payload containing one issued VID.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VidFetchResponseDto {
	/**
	 * Issued virtual identifier.
	 */
	String vid;
}
