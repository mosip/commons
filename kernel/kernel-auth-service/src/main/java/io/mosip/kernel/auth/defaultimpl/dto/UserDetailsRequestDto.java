/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import java.util.List;

import lombok.Data;

/**
 * Bulk user-id list posted when fetching MOSIP user details for an application.
 *
 * @author Ramadurai Pandian
 *
 */
@Data
public class UserDetailsRequestDto {

	/**
	 * User identifiers to look up.
	 */
	List<String> userDetails;

}
