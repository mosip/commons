package io.mosip.kernel.syncdata.service.helper.beans;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class RegistrationCenterDeviceHistoryPk implements Serializable {

	private static final long serialVersionUID = 1L;

	
	private String regCenterId;

	
	private String deviceId;

	
	private LocalDateTime effectivetimes;

}
