package io.mosip.commons.packet.dto.packet;

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
