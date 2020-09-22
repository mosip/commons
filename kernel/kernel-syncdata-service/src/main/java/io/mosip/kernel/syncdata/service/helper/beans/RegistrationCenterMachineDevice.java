package io.mosip.kernel.syncdata.service.helper.beans;

import java.io.Serializable;

import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineDeviceID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RegistrationCenterMachineDevice extends BaseEntity implements Serializable {

	private static final long serialVersionUID = -8541947587557590379L;

	private RegistrationCenterMachineDeviceID registrationCenterMachineDevicePk;

	private String langCode;

}
