package io.mosip.kernel.vidgenerator.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

@Service
public class VidServiceImpl implements VidService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidServiceImpl.class);

	@Value("${mosip.kernel.vid.time-to-release-after-expiry}")
	private long timeToRelaseAfterExpiry;

	@Autowired
	private VidRepository vidRepository;

	@Autowired
	private VidAssignedRepository vidAssignedRepository;

	@Autowired
	private VIDMetaDataUtil metaDataUtil;
	
	@Autowired
	private VertxAuthenticationProvider authHandler;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Override
	@Transactional
	public VidFetchResponseDto fetchVid(LocalDateTime vidExpiry, RoutingContext routingContext) {
		VidFetchResponseDto vidFetchResponseDto = new VidFetchResponseDto();
		VidEntity vidEntity = null;
		try {
			vidEntity = vidRepository.findFirstByStatus(VidLifecycleStatus.AVAILABLE);
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

	@Override
	public long fetchVidCount(String status) {
		long vidCount = 0;
		try {
			vidCount = vidRepository.countByStatusAndIsDeletedFalse(status);
		} catch (Exception exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
		}
		return vidCount;
	}

	@Override
	public void expireAndRelease() {
		try {
			expireEligibleVids();
			releaseEligibleVids();
		} catch (Exception exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
		}
	}

	private void expireEligibleVids() {
		List<VidAssignedEntity> vidAssignedEntities = vidAssignedRepository
			.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED);
		vidAssignedEntities.forEach(this::expireIfEligible);
		vidAssignedRepository.saveAll(vidAssignedEntities);
	}

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

	private void expireIfEligible(VidAssignedEntity entity) {
		LocalDateTime currentTime = DateUtils.getUTCCurrentDateTime();
		LOGGER.debug("currenttime {} for checking entity with expiry time {}", currentTime, entity.getVidExpiry());
		if (entity.getVidExpiry() != null && (entity.getVidExpiry().isBefore(currentTime) || entity.getVidExpiry().isEqual(currentTime))
				&& entity.getStatus().equals(VidLifecycleStatus.ASSIGNED)) {
			metaDataUtil.setUpdateMetaData(entity);
			entity.setStatus(VidLifecycleStatus.EXPIRED);
		}
	}

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

	@Override
	public boolean saveVID(VidEntity vid) {

		if (!(this.vidRepository.existsById(vid.getVid()) || 
				this.vidAssignedRepository.existsById(vid.getVid()))) {
			try {
				this.vidRepository.saveAndFlush(vid);
			} catch (Exception exception) {
				LOGGER.error(ExceptionUtils.parseException(exception));
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	@Transactional
	public int saveVIDsInBulk(List<VidEntity> vidList) {
		if (vidList == null || vidList.isEmpty()) return 0;

		List<String> vidIds = vidList.stream().map(VidEntity::getVid).toList();

		// Check for already existing VIDs
		Set<String> existingVids = new HashSet<>();
		existingVids.addAll(
				vidRepository.findAllById(vidIds).stream().map(VidEntity::getVid).toList()
		);
		existingVids.addAll(
				vidAssignedRepository.findAllById(vidIds).stream().map(VidAssignedEntity::getVid).toList()
		);

		List<VidEntity> filtered = vidList.stream()
				.filter(vid -> !existingVids.contains(vid.getVid()))
				.toList();

		if (filtered.isEmpty()) return 0;

		// Perform batch insert with thread-local EntityManager
		EntityManager em = entityManagerFactory.createEntityManager();
		EntityTransaction tx = null;
		int inserted = 0;

		try {
			tx = em.getTransaction();
			tx.begin();

			for (int i = 0; i < filtered.size(); i++) {
				em.persist(filtered.get(i));
				inserted++;

				// Flush and clear periodically to avoid memory issues
				if (i % 50 == 0) {
					em.flush();
					em.clear();
				}
			}

			tx.commit();
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			LOGGER.error("❌ Error in saveVIDsInBulk: {}", ExceptionUtils.parseException(e));
			inserted = 0;
		} finally {
			em.close();
		}

		return inserted;
	}

	@Transactional(transactionManager = "transactionManager")
	@Override
	public void isolateAssignedVids() {
		List<VidEntity> vidEntities = vidRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED);
		LOGGER.info("isolateAssignedVids called for entity count {} ", vidEntities.size());
		List<VidAssignedEntity> vidEntitiesAssined = convertVidEntitiesToVidAssignedEntity(vidEntities);
		vidAssignedRepository.saveAll(vidEntitiesAssined);
	    vidRepository.deleteAll(vidEntities);
	}

	private List<VidAssignedEntity> convertVidEntitiesToVidAssignedEntity(List<VidEntity> vidEntities) {
		return vidEntities.stream()
				.map(VidAssignedEntity::new)
				.collect(Collectors.toList());
	}
}