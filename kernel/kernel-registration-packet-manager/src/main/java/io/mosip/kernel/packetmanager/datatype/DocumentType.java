package io.mosip.kernel.packetmanager.datatype;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DocumentType {
	
	private String value;
	private String type;
	private String format;

}
