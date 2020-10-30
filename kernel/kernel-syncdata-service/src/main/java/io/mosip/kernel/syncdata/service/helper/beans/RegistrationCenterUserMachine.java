package io.mosip.kernel.syncdata.service.helper.beans;

import java.io.Serializable;

import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineUserID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RegistrationCenterUserMachine extends BaseEntity implements Serializable {

	/**
	 * Generated Serial Id
	 */
	private static final long serialVersionUID = -4167453471874926985L;

	/**
	 * Composite key for this table
	 */
	
	private RegistrationCenterMachineUserID registrationCenterMachineUserID;

	
	private String langCode;

}
