package io.mosip.kernel.syncdata.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The Class SyncUserSaltDto.
 */
@Data

/**
 * Instantiates a new sync user salt dto.
 */
@NoArgsConstructor

/**
 * Instantiates a new sync user salt dto.
 *
 * @param mosipUserSaltList the mosip user salt list
 */
@AllArgsConstructor
public class SyncUserSaltDto {

	/** The mosip user salt list. */
	private List<UserSaltDto> mosipUserSaltList;

	private String lastSyncTime;
}
