package io.mosip.kernel.packetmanager.dto.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentMetaInfo {
	private String documentName;
	private String documentCategory;
	private String documentOwner;
	private String documentType;
}
