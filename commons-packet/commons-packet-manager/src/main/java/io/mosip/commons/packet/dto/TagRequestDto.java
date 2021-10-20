package io.mosip.commons.packet.dto;

import java.io.Serializable;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class TagRequestDto implements Serializable {
	
	private String id;
	private List<String> tagNames;
}
