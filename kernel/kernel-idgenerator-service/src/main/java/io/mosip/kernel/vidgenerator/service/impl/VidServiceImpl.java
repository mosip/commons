package io.mosip.kernel.vidgenerator.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.auth.defaultadapter.handler.AuthHandler;
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

@Service
public class VidServiceImpl implements VidService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VidServiceImpl.class);

	@Value("${mosip.kernel.vid.time-to-renew-after-expiry}")
	private long timeToRenewAfterExpiry;

	@Autowired
	private VidRepository vidRepository;

	@Autowired
	private VidAssignedRepository vidAssignedRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private VIDMetaDataUtil metaDataUtil;
	
	@Autowired
	private AuthHandler authHandler;

	@Override
	@Transactional
	public VidFetchResponseDto fetchVid(LocalDateTime vidExpiry) {
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
				vidRepository.updateVid(VidLifecycleStatus.ASSIGNED, authHandler.getContextUser(),
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

	@Override
	public void expireAndRenew() {
		try {
			List<VidAssignedEntity> vidAssignedEntities = vidAssignedRepository
					.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED);
			vidAssignedEntities.forEach(this::expireIfEligible);
			vidAssignedRepository.saveAll(vidAssignedEntities);
			List<VidAssignedEntity> vidExpiredEntities = vidAssignedRepository
					.findByStatusAndIsDeletedFalse(VidLifecycleStatus.EXPIRED);
			List<VidAssignedEntity> renewableVidAssigenedEntities = new ArrayList<VidAssignedEntity>();
			vidExpiredEntities.forEach(entity -> {
				if(isRenewable(entity)) {
					entity.setVidExpiry(null);
					metaDataUtil.setUpdateMetaData(entity);
					entity.setStatus(VidLifecycleStatus.AVAILABLE);
					renewableVidAssigenedEntities.add(entity);
				}
			});
			if(renewableVidAssigenedEntities.size() > 0) {
				List<VidEntity> renewableVidEntities = modelMapper.map(renewableVidAssigenedEntities, 
						new TypeToken<List<VidEntity>>() {}.getType());
				vidRepository.saveAll(renewableVidEntities);
				vidAssignedRepository.deleteAll(renewableVidAssigenedEntities);
			}
		} catch (DataAccessException exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
		} catch (Exception exception) {
			LOGGER.error(ExceptionUtils.parseException(exception));
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

	private boolean isRenewable(VidAssignedEntity entity) {
		LocalDateTime currentTime = DateUtils.getUTCCurrentDateTime();
		LocalDateTime renewElegibleTime = entity.getVidExpiry().plusDays(timeToRenewAfterExpiry);
		LOGGER.debug("currenttime {} for checking entity with renew elegible time {}", currentTime, renewElegibleTime);
		if ((renewElegibleTime.isBefore(currentTime) || renewElegibleTime.isEqual(currentTime))
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

	@Transactional(transactionManager = "transactionManager")
	@Override
	public void isolateAssignedVids() {
		List<VidEntity> vidEntities = vidRepository.findByStatusAndIsDeletedFalse(VidLifecycleStatus.ASSIGNED);
		LOGGER.info("isolateAssignedVids called for entity count {} ", vidEntities.size());
		List<VidAssignedEntity> vidEntitiesAssined = modelMapper.map(vidEntities, 
			new TypeToken<List<VidAssignedEntity>>() {}.getType());
		vidAssignedRepository.saveAll(vidEntitiesAssined);
	    vidRepository.deleteAll(vidEntities);
	}
}