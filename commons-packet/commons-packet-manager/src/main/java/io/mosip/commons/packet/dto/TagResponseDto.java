package io.mosip.commons.packet.dto;

import java.io.Serializable;
import java.util.Map;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TagResponseDto implements Serializable {
	
	Map<String, String> tags;
}
