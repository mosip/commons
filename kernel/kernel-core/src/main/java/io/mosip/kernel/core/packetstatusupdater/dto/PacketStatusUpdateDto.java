package io.mosip.kernel.core.packetstatusupdater.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Packet-status lookup result pairing a registration ID with its status code.
 * <p>
 * Contract: returned by
 * {@link io.mosip.kernel.core.packetstatusupdater.spi.PacketStatusUpdateService#getStatus(String)}.
 * Fields may be null if the RID is unknown. Does not perform I/O.
 * </p>
 *
 * @author Srinivasan
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacketStatusUpdateDto {

	/**
	 * Registration ID whose status was queried; may be null.
	 */
	private String registrationId;

	/**
	 * Processing status code such as {@code PROCESSED}; may be null.
	 */
	private String statusCode;
}
