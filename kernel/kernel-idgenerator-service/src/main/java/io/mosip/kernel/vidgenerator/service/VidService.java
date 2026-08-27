package io.mosip.kernel.vidgenerator.service;

import java.time.LocalDateTime;
import java.util.List;

import io.mosip.kernel.vidgenerator.dto.VidFetchResponseDto;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.vertx.ext.web.RoutingContext;

public interface VidService {

	VidFetchResponseDto fetchVid(LocalDateTime expiry, RoutingContext routingContext);

	long fetchVidCount(String status);

	void expireAndRelease();

	boolean saveVID(VidEntity vid);

	/**
	 * Batch-saves a list of VIDs in a single transaction.
	 * Duplicate candidates (detected via bloom filter + DB fallback) are skipped.
	 *
	 * @return number of VIDs actually persisted
	 */
	int saveAllVIDs(List<VidEntity> vids);

	void isolateAssignedVids();

}
