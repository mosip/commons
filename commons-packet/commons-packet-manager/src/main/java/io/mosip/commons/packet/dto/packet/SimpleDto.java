package io.mosip.commons.packet.dto.packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class SimpleDto {
	
	private String language;
	private String value;

}
