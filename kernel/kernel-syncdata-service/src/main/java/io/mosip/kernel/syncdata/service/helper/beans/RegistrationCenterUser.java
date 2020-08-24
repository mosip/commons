package io.mosip.kernel.syncdata.service.helper.beans;

import java.io.Serializable;

import io.mosip.kernel.syncdata.entity.id.RegistrationCenterUserID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RegistrationCenterUser extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 3941306023356031908L;

	
	private RegistrationCenterUserID registrationCenterUserID;

	
	private String langCode;

}
