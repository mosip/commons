package io.mosip.kernel.syncdata.service.helper.beans;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationCenterUserMachineHistory extends BaseEntity implements Serializable {

	/**
	 * Generated Serial Id
	 */
	private static final long serialVersionUID = -4167453471874926985L;

	/**
	 * Composite key for this table
	 */
	
	/**
	 * Center Id
	 */
	private String cntrId;

	/**
	 * User Id
	 */
	private String usrId;

	/**
	 * Machine Id
	 */
	private String machineId;

	/**
	 * Effective TimeStamp
	 */
	private LocalDateTime effectivetimes;

	
	private String langCode;
}
