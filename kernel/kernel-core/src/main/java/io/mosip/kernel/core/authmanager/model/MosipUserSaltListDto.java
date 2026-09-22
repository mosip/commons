package io.mosip.kernel.core.authmanager.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for a list of {@link MosipUserSalt} records.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.authmanager.spi.AuthService#getAllUserDetailsWithSalt(java.util.List, String)}.
 * The list may be null or empty. Treat salts as sensitive. Does not perform I/O.
 * </p>
 *
 * @author Ramadurai Pandian
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MosipUserSaltListDto {
	/**
	 * User-to-salt mappings; may be null or empty.
	 */
	List<MosipUserSalt> mosipUserSaltList;
}
