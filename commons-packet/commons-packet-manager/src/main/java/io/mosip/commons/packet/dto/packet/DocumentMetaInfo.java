package io.mosip.commons.packet.dto.packet;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentMetaInfo {
	private String documentName;
	private String documentType;
}
