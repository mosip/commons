package io.mosip.commons.packet.dto.packet;

import lombok.Data;

@Data
public class DeviceMetaInfo {

	private String deviceCode;
	private String deviceServiceVersion;
	private DigitalId digitalId;
}
