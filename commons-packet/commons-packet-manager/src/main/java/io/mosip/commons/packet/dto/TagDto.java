package io.mosip.commons.packet.dto;


import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode
public class TagDto {
    
	private String id;
	private Map<String, String> tags;

}
