package io.mosip.kernel.core.authmanager.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Ramadurai Pandian
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MosipUserSaltListDto {
	List<MosipUserSalt> mosipUserSaltList;
}
