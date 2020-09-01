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
public class TimeToken {

	private String token;
	private long expTime;

}
