package io.mosip.kernel.core.packetstatusupdater.spi;

import io.mosip.kernel.core.packetstatusupdater.dto.PacketStatusUpdateDto;

/**
 * Looks up the registration-packet processing status for a RID.
 * <p>
 * Contract: implementations typically perform HTTP or database I/O against
 * registration processor. {@code rid} must be non-blank. Call when a client
 * needs the current packet status.
 * </p>
 */
public interface PacketStatusUpdateService {

	/**
	 * Returns the current status for the given registration ID.
	 *
	 * @param rid never-null, never-blank registration ID
	 * @return never-null status DTO; fields may be null if the RID is unknown
	 */
	public PacketStatusUpdateDto getStatus(String rid);
}
