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
public class BasicTokenDto {

	private String authToken;
	private String refreshToken;
	private long expiryTime;
}
