package io.mosip.kernel.vidgenerator.service;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.kernel.vidgenerator.dto.VidFetchResponseDto;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.vertx.ext.web.RoutingContext;

/**
 * Service interface for operations related to Virtual ID (VID) management.
 * Provides methods for fetching, storing, expiring, and managing VIDs.
 *
 * @author Urvil Joshi
 * @since 1.0.0
 */
public interface VidService {

	/**
	 * Fetches an available VID and marks it as assigned.
	 *
	 * @param expiry The expiry time to associate with the VID.
	 * @param routingContext The Vert.x routing context to extract user metadata.
	 * @return {@link VidFetchResponseDto} containing the assigned VID.
	 */
	VidFetchResponseDto fetchVid(LocalDateTime expiry, RoutingContext routingContext);

	/**
	 * Fetches the total number of VIDs with the specified status.
	 *
	 * @param status The lifecycle status (e.g., AVAILABLE, ASSIGNED).
	 * @return The count of VIDs with the given status.
	 */
	long fetchVidCount(String status);

	/**
	 * Expires all eligible VIDs and releases expired ones
	 * that have surpassed the grace period.
	 */
	void expireAndRelease();

	/**
	 * Saves a single VID to the database if it's not already present.
	 *
	 * @param vid The {@link VidEntity} to persist.
	 * @return {@code true} if persisted successfully, {@code false} otherwise.
	 */
	boolean saveVID(VidEntity vid);

	/**
	 * Moves all VIDs in ASSIGNED state to a separate table for isolation.
	 * This is useful for archiving or auditing.
	 */
	void isolateAssignedVids();

	/**
	 * Saves a list of VIDs in bulk, skipping duplicates already present in the system.
	 *
	 * @param vidList List of {@link VidEntity} to be saved.
	 * @return Number of VIDs successfully persisted.
	 */
	int saveVIDsInBulk(List<VidEntity> vidList);
}
