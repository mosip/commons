/**
 *
 */
package io.mosip.kernel.uingenerator.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.authmanager.authadapter.spi.VertxAuthenticationProvider;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.uingenerator.constant.UinGeneratorConstant;
import io.mosip.kernel.uingenerator.constant.UinGeneratorErrorCode;
import io.mosip.kernel.uingenerator.dto.UinResponseDto;
import io.mosip.kernel.uingenerator.dto.UinStatusUpdateReponseDto;
import io.mosip.kernel.uingenerator.entity.UinEntity;
import io.mosip.kernel.uingenerator.entity.UinEntityAssigned;
import io.mosip.kernel.uingenerator.exception.UinNotFoundException;
import io.mosip.kernel.uingenerator.exception.UinNotIssuedException;
import io.mosip.kernel.uingenerator.exception.UinStatusNotFoundException;
import io.mosip.kernel.uingenerator.repository.UinRepository;
import io.mosip.kernel.uingenerator.repository.UinRepositoryAssigned;
import io.mosip.kernel.uingenerator.service.UinService;
import io.mosip.kernel.uingenerator.util.UINMetaDataUtil;
import io.mosip.kernel.uingenerator.util.UinBloomFilter;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Dharmesh Khandelwal
 * @author Megha Tanga
 * @author Urvil Joshi
 * @since 1.0.0
 *
 */
@Component
public class UinServiceImpl implements UinService {

	private Logger LOGGER = LoggerFactory.getLogger(UinServiceImpl.class);

	@Autowired
	private UinRepository uinRepository;

	@Autowired
	private UinRepositoryAssigned uinRepositoryAssigned;

	@Autowired
	private UINMetaDataUtil metaDataUtil;

	@Autowired
	private VertxAuthenticationProvider authHandler;

	@Autowired
	private UinBloomFilter uinBloomFilter;

	@Value("${mosip.kernel.uin.page.size:50000}")
	private int pageSize;

	/*
	 * (non-Javadoc)
	 *
	 * @see io.mosip.kernel.core.uingenerator.service.UinGeneratorService#getId()
	 */
	@Transactional
	@Override
	public UinResponseDto getUin(RoutingContext routingContext) {
		UinResponseDto uinResponseDto = new UinResponseDto();
		UinEntity uinBean = uinRepository.findFirstByStatus(UinGeneratorConstant.UNUSED);
		if (uinBean != null) {
			uinRepository.updateStatus(UinGeneratorConstant.ISSUED, authHandler.getContextUser(routingContext),
					DateUtils.getUTCCurrentDateTime(), uinBean.getUin());
			uinResponseDto.setUin(uinBean.getUin());
		} else {
			throw new UinNotFoundException(UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorCode(),
					UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorMessage());
		}
		return uinResponseDto;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.kernel.uingenerator.service.UinGeneratorService#updateUinStatus(io.
	 * vertx.core.json.JsonObject)
	 */
	@Override
	public UinStatusUpdateReponseDto updateUinStatus(UinEntity uinAck, RoutingContext routingContext) {
		UinStatusUpdateReponseDto uinResponseDto = new UinStatusUpdateReponseDto();
		UinEntity existingUin = uinRepository.findByUin(uinAck.getUin());
		if (existingUin != null) {
			if (UinGeneratorConstant.ISSUED.equals(existingUin.getStatus())) {
				metaDataUtil.setUpdateMetaData(existingUin, routingContext);
				if (UinGeneratorConstant.ASSIGNED.equals(uinAck.getStatus())) {
					existingUin.setStatus(UinGeneratorConstant.ASSIGNED);
					uinRepository.save(existingUin);
				} else if (UinGeneratorConstant.UNASSIGNED.equals(uinAck.getStatus())) {
					existingUin.setStatus(UinGeneratorConstant.UNUSED);
					uinRepository.save(existingUin);
				} else {
					throw new UinStatusNotFoundException(UinGeneratorErrorCode.UIN_STATUS_NOT_FOUND.getErrorCode(),
							UinGeneratorErrorCode.UIN_STATUS_NOT_FOUND.getErrorMessage());
				}
			} else {
				throw new UinNotIssuedException(UinGeneratorErrorCode.UIN_NOT_ISSUED.getErrorCode(),
						UinGeneratorErrorCode.UIN_NOT_ISSUED.getErrorMessage());
			}
		} else {
			throw new UinNotFoundException(UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorCode(),
					UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorMessage());
		}
		uinResponseDto.setUin(existingUin.getUin());
		uinResponseDto.setStatus(existingUin.getStatus());
		return uinResponseDto;
	}

	@Transactional(transactionManager = "transactionManager")
	@Override
	public void transferUin() {
		List<UinEntity> uinEntities = uinRepository.findByStatus(UinGeneratorConstant.ISSUED, pageSize);
		if (uinEntities.isEmpty()) {
			return;
		}
		List<UinEntityAssigned> uinEntitiesAssigned = convertUinEntitiesListToUinEntitiesAssignedList(uinEntities);
		uinRepositoryAssigned.saveAll(uinEntitiesAssigned);
		uinRepository.deleteAll(uinEntities);
		// Update bloom filter after DB writes succeed within the transaction.
		// If the transaction rolls back, phantom entries in the filter are harmless:
		// they cause an extra DB lookup (false positive path) which correctly returns false.
		uinEntities.forEach(u -> uinBloomFilter.put(u.getUin()));
		LOGGER.info("Transferred {} UIns to assigned table and updated bloom filter", uinEntities.size());
	}

	private List<UinEntityAssigned> convertUinEntitiesListToUinEntitiesAssignedList(List<UinEntity> uinEntities) {
		return uinEntities.stream()
				.map(UinEntityAssigned::new)
				.collect(Collectors.toList());
	}

	/**
	 * Checks if a UIN already exists in either the active pool or the assigned table.
	 *
	 * Fast path: bloom filter covers both tables at startup — a definite miss means
	 * the UIN is new and safe to insert.
	 * Slow path: bloom filter returns true (possible match or false positive) —
	 * confirm with DB against both tables.
	 */
	@Override
	public boolean uinExist(String uin) {
		if (!uinBloomFilter.mightContain(uin)) {
			return false;
		}
		return uinRepository.existsById(uin) || uinRepositoryAssigned.existsById(uin);
	}

}
