package io.mosip.kernel.packetmanager.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BiometricsException {
	private String type;
	private String missingBiometric;
	private String reason;
	private String exceptionType;
	private String individualType;
}
