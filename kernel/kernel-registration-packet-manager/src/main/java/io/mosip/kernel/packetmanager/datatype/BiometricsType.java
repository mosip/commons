package io.mosip.kernel.packetmanager.datatype;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BiometricsType {
	
	private String format;
	private double version;
	private String value;
	
}
