package io.mosip.commons.packet.dto.packet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModalityInfo {	
	@JsonProperty("BIRIndex")
	private String birIndex;
	private int numRetry;
	private boolean forceCaptured;
}
