package io.mosip.commons.packet.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TagRequestDto {
	
	private String id;
	private List<String> tagNames;
}
