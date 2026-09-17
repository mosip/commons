package io.mosip.kernel.vidgenerator.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.authmanager.authadapter.spi.VertxAuthenticationProvider;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.vidgenerator.constant.VIDGeneratorErrorCode;
import io.mosip.kernel.vidgenerator.constant.VidLifecycleStatus;
import io.mosip.kernel.vidgenerator.dto.VidFetchResponseDto;
import io.mosip.kernel.vidgenerator.entity.VidAssignedEntity;
import io.mosip.kernel.vidgenerator.entity.VidEntity;
import io.mosip.kernel.vidgenerator.exception.VidGeneratorServiceException;
import io.mosip.kernel.vidgenerator.repository.VidAssignedRepository;
import io.mosip.kernel.vidgenerator.repository.VidRepository;
import io.mosip.kernel.vidgenerator.service.VidService;
import io.mosip.kernel.vidgenerator.utils.ExceptionUtils;
import io.mosip.kernel.vidgenerator.utils.VIDMetaDataUtil;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Transactional implementation that issues, expires, releases, and isolates VIDs.
 */
@Service
public class VidServiceImpl implements VidService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidServiceImpl.class);

	/**
	 * Days after expiry before an expired VID may be deleted ({@code mosip.kernel.vid.time-to-release-after-expiry}).
	 */
	@Value("${mosip.kernel.vid.time-to-release-after-expiry}")
	private long timeToRelaseAfterExpiry;

	/**
	 * Unused VID pool.
	 */
	@Autowired
	private VidRepository vidRepository;

	/**
	 * Assigned-VID table used after isolation from the unused pool.
	 */
	@Autowired
	private VidAssignedRepository vidAssignedRepository;

	/**
	 * Sets create/update audit columns on assigned entities.
	 */
	@Autowired
	private VIDMetaDataUtil metaDataUtil;
	
	/**
	 * Resolves the authenticated user for {@code updatedBy} on assign.
	 */
	@Autowired
	private VertxAuthenticationProvider authHandler;

	/**
	 * Marks one unused VID as assigned and optionally sets {@code vidExpiry}.
	 *
	 * @param vidExpiry      optional UTC expiry
	 * @param routingContext Vert.x routing context of the fetch request
	 * @return issued VID
	 * @throws VidGeneratorServiceException when none remain or persistence fails
	 */
	@Override
	@Transactional
	public VidFetchResponseDto fetchVid(LocalDateTime vidExpiry, RoutingContext routingContext) {
		VidFetchResponseDto vidFetchResponseDto = new VidFetchResponseDto();
		VidEntity vidEntity = null;
		try {
			vidEntity = vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE);
		} catch (DataAccessException exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
			throw new VidGeneratorServiceException(VIDGeneratorErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
					exception.getMessage(), exception.getCause());
		} catch (Exception exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
			throw new VidGeneratorServiceException(VIDGeneratorErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
					exception.getMessage(), exception.getCause());
		}
		if (vidEntity != null) {
			if (vidExpiry != null) {
				vidEntity.setVidExpiry(vidExpiry);
			}
			vidFetchResponseDto.setVid(vidEntity.getVid());
			try {
				vidRepository.updateVid(VidLifecycleStatus.ASSIGNED, authHandler.getContextUser(routingContext),
						DateUtils.getUTCCurrentDateTime(), vidEntity.getVid());
			} catch (DataAccessException exception) {
				LOGGER.error(ExceptionUtils.parseException(exception));
				throw new VidGeneratorServiceException(VIDGeneratorErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
						exception.getMessage(), exception.getCause());
			} catch (Exception exception) {
				LOGGER.error(ExceptionUtils.parseException(exception));
				throw new VidGeneratorServiceException(VIDGeneratorErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
						exception.getMessage(), exception.getCause());
			}
		} else {
			LOGGER.info("vid not available");
			throw new VidGeneratorServiceException(VIDGeneratorErrorCode.VID_NOT_AVAILABLE.getErrorCode(),
					VIDGeneratorErrorCode.VID_NOT_AVAILABLE.getErrorMessage());
		}
		return vidFetchResponseDto;
	}

	/**
	 * Counts unused or assigned VIDs; returns {@code 0} when the query throws.
	 *
	 * @param status lifecycle status
	 * @return row count
	 */
	@Override
	public long fetchVidCount(String status) {
		long vidCount = 0;
		try {
			vidCount = vidRepository.countByStatusAndIsDeletedFalse(status);
		} catch (DataAccessException exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
		} catch (Exception exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
		}
		return vidCount;

	}

	/**
	 * Expires assigned VIDs past {@code vid_expiry} and deletes those past the release window.
	 */
	@Override
	public void expireAndRelease() {
		try {
			expireEligibleVids();
			releaseEligibleVids();
		} catch (DataAccessException exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
		} catch (Exception exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
		}

	}

	/**
	 * Marks assigned rows whose expiry is at or before UTC now as {@code EXPIRED}.
	 */
	private void expireEligibleVids() {
		List<VidAssignedEntity> vidAssignedEntities = vidAssignedRepository
			.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED);
		vidAssignedEntities.forEach(this::expireIfEligible);
		vidAssignedRepository.saveAll(vidAssignedEntities);
	}

	/**
	 * Deletes expired rows whose release window has elapsed.
	 */
	private void releaseEligibleVids() {
		List<VidAssignedEntity> vidExpiredEntities = vidAssignedRepository
			.findByStatusAndIsDeletedFalse(VidLifecycleStatus.EXPIRED);
		List<VidAssignedEntity> releasableVidAssignedEntities = new ArrayList<VidAssignedEntity>();
		vidExpiredEntities.forEach(entity -> {
			if(isEligibleToRelease(entity)) {
				releasableVidAssignedEntities.add(entity);
			}
		});
		if(releasableVidAssignedEntities.size() > 0) {
			vidAssignedRepository.deleteAll(releasableVidAssignedEntities);
		}
	}

	/**
	 * Sets {@code EXPIRED} when {@code entity} expiry is at or before UTC now.
	 *
	 * @param entity assigned VID to inspect
	 */
	private void expireIfEligible(VidAssignedEntity entity) {
		LocalDateTime currentTime = DateUtils.getUTCCurrentDateTime();
		LOGGER.debug("currenttime {} for checking entity with expiry time {}", currentTime, entity.getVidExpiry());
		if (entity.getVidExpiry() != null && (entity.getVidExpiry().isBefore(currentTime) || entity.getVidExpiry().isEqual(currentTime))
				&& entity.getStatus().equals(VidLifecycleStatus.ASSIGNED)) {
			metaDataUtil.setUpdateMetaData(entity);
			entity.setStatus(VidLifecycleStatus.EXPIRED);
		}
	}

	/**
	 * Returns whether {@code entity} expiry plus {@code timeToRelaseAfterExpiry} days is at or before UTC now.
	 *
	 * @param entity expired assigned VID
	 * @return {@code true} when the row may be deleted
	 */
	private boolean isEligibleToRelease(VidAssignedEntity entity) {
		LocalDateTime currentTime = DateUtils.getUTCCurrentDateTime();
		LocalDateTime releaseElegibleTime = entity.getVidExpiry().plusDays(timeToRelaseAfterExpiry);
		LOGGER.debug("currenttime {} for checking entity with release elegible time {}", currentTime, releaseElegibleTime);
		if ((releaseElegibleTime.isBefore(currentTime) || releaseElegibleTime.isEqual(currentTime))
				&& entity.getStatus().equals(VidLifecycleStatus.EXPIRED)) {
			return true;
		}
		return false;
	}

	/**
	 * Inserts {@code vid} when it is not already in pool or assigned tables.
	 *
	 * @param vid entity to persist
	 * @return {@code true} when inserted
	 */
	@Override
	public boolean saveVID(VidEntity vid) {

		if (!(this.vidRepository.existsById(vid.getVid()) || 
				this.vidAssignedRepository.existsById(vid.getVid()))) {
			try {
				this.vidRepository.saveAndFlush(vid);
			} catch (DataAccessException exception) {
				LOGGER.error(ExceptionUtils.parseException(exception));
				return false;
			} catch (Exception exception) {
				LOGGER.error(ExceptionUtils.parseException(exception));
				return false;
			}
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Copies {@code ASSIGNED} rows to {@code vid_assigned} and deletes them from {@code vid}.
	 */
	@Transactional(transactionManager = "transactionManager")
	@Override
	public void isolateAssignedVids() {
		List<VidEntity> vidEntities = vidRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED);
		LOGGER.info("isolateAssignedVids called for entity count {} ", vidEntities.size());
		List<VidAssignedEntity> vidEntitiesAssined = convertVidEntitiesToVidAssignedEntity(vidEntities);
		vidAssignedRepository.saveAll(vidEntitiesAssined);
	    vidRepository.deleteAll(vidEntities);
	}

	/**
	 * Maps pool entities to assigned-table entities.
	 *
	 * @param vidEntities source pool rows
	 * @return assigned-table copies
	 */
	private List<VidAssignedEntity> convertVidEntitiesToVidAssignedEntity(List<VidEntity> vidEntities) {
		return vidEntities.stream()
				.map(VidAssignedEntity::new)
				.collect(Collectors.toList());
	}
}