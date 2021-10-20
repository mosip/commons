package io.mosip.commons.packet.dto;


import java.io.Serializable;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;
@Data
@EqualsAndHashCode
public class TagDto implements Serializable {
    
	private String id;
	private Map<String, String> tags;

}
