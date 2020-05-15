package io.mosip.kernel.packetmanager.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class FieldValue {
	
	private String label;
	private String value;

}
