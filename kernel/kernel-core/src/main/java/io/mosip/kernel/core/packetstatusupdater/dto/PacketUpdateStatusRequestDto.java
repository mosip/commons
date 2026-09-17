package io.mosip.kernel.core.packetstatusupdater.dto;

import lombok.Data;

/**
 * Request to look up packet status by registration ID.
 * <p>
 * Contract: {@code registrationId} must be non-blank when submitted. Does not
 * perform I/O.
 * </p>
 */
@Data
public class PacketUpdateStatusRequestDto {

	/**
	 * Registration ID to query; must be non-blank for a valid request.
	 */
	private String registrationId;
}
