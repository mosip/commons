package io.mosip.kernel.vidgenerator.service;

import java.time.LocalDateTime;

import io.mosip.kernel.vidgenerator.dto.VidFetchResponseDto;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.vertx.ext.web.RoutingContext;

public interface VidService {

	VidFetchResponseDto fetchVid(LocalDateTime expiry, RoutingContext routingContext);

	long fetchVidCount(String status);

	void expireAndRelease();

	boolean saveVID(VidEntity vid);

	void isolateAssignedVids();

}
