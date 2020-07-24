package io.mosip.kernel.packetmanager.dto.metadata;

import lombok.Data;

@Data
public class DigitalId {
	private String serialNo;
	private String make;
	private String model;
	private String type;
	private String deviceSubType;
	private String deviceProviderId;
	private String deviceProvider;
	private String dateTime;
}
