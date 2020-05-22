package io.mosip.kernel.packetmanager.dto.metadata;

import lombok.Data;

@Data
public class DeviceMetaInfo {

	private String deviceCode;
	private String deviceServiceVersion;
	private DigitalId digitalId;
}
