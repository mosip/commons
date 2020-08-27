package io.mosip.commons.packet.dto.packet;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode
public class AuditDto {
	private Map<String, String> audits;
}
