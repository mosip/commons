package io.mosip.kernel.vidgenerator.service;

import java.time.LocalDateTime;

import io.mosip.kernel.vidgenerator.dto.VidFetchResponseDto;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.mosip.kernel.vidgenerator.exception.VidGeneratorServiceException;
import io.vertx.ext.web.RoutingContext;

/**
 * Issues unused VIDs from {@code kernel.vid} and manages assigned/expired lifecycle.
 */
public interface VidService {

	/**
	 * Marks one {@code AVAILABLE} VID as {@code ASSIGNED} and optionally sets expiry.
	 *
	 * @param expiry         optional UTC expiry; {@code null} leaves expiry unset
	 * @param routingContext Vert.x routing context of the fetch request
	 * @return issued VID
	 * @throws VidGeneratorServiceException when none remain or persistence fails
	 */
	VidFetchResponseDto fetchVid(LocalDateTime expiry, RoutingContext routingContext);

	/**
	 * Counts non-deleted VIDs in {@code status}.
	 *
	 * @param status lifecycle status such as {@code AVAILABLE}
	 * @return row count, or {@code 0} when the query fails
	 */
	long fetchVidCount(String status);

	/**
	 * Marks assigned VIDs past expiry as {@code EXPIRED} and deletes those past the release window.
	 */
	void expireAndRelease();

	/**
	 * Inserts {@code vid} when the identifier is not already present in pool or assigned tables.
	 *
	 * @param vid entity to persist
	 * @return {@code true} when inserted; {@code false} when duplicate or persistence failed
	 */
	boolean saveVID(VidEntity vid);

	/**
	 * Moves {@code ASSIGNED} rows from {@code kernel.vid} to {@code kernel.vid_assigned}.
	 */
	void isolateAssignedVids();

}
