/**
 * 
 */
package io.mosip.kernel.uingenerator.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

	/**
	 * Field for {@link #uinRepository}
	 */
	@Autowired
	private UinRepository uinRepository;
	
	@Autowired
	private UinRepositoryAssigned uinRepositoryAssigned;

	/**
	 * instance of {@link UINMetaDataUtil}
	 */
	@Autowired
	private UINMetaDataUtil metaDataUtil;
	
	@Autowired
	private VertxAuthenticationProvider authHandler;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.uingenerator.service.UinGeneratorService#getId()
	 */
	@Transactional
	@Override
	public UinResponseDto getUin(RoutingContext routingContext) {
		UinEntity uinBean = Optional.ofNullable(uinRepository.findFirstByStatus(UinGeneratorConstant.UNUSED))
				.orElseThrow(() -> new UinNotFoundException(UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorCode(),
						UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorMessage()));

		String user = authHandler.getContextUser(routingContext);
		uinRepository.updateStatus(UinGeneratorConstant.ISSUED, user, DateUtils.getUTCCurrentDateTime(),
				uinBean.getUin());

		// LOGGER.info("Issued UIN {} to user {}", uinBean.getUin(), user);

		UinResponseDto response = new UinResponseDto();
		response.setUin(uinBean.getUin());
		return response;
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
		UinEntity existingUin = Optional.ofNullable(uinRepository.findByUin(uinAck.getUin()))
				.orElseThrow(() -> new UinNotFoundException(UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorCode(),
						UinGeneratorErrorCode.UIN_NOT_FOUND.getErrorMessage()));

		if (!UinGeneratorConstant.ISSUED.equals(existingUin.getStatus())) {
			throw new UinNotIssuedException(UinGeneratorErrorCode.UIN_NOT_ISSUED.getErrorCode(),
					UinGeneratorErrorCode.UIN_NOT_ISSUED.getErrorMessage());
		}

		metaDataUtil.setUpdateMetaData(existingUin, routingContext);

		switch (uinAck.getStatus()) {
			case UinGeneratorConstant.ASSIGNED:
				existingUin.setStatus(UinGeneratorConstant.ASSIGNED);
				break;
			case UinGeneratorConstant.UNASSIGNED:
				existingUin.setStatus(UinGeneratorConstant.UNUSED);
				break;
			default:
				throw new UinStatusNotFoundException(UinGeneratorErrorCode.UIN_STATUS_NOT_FOUND.getErrorCode(),
						UinGeneratorErrorCode.UIN_STATUS_NOT_FOUND.getErrorMessage());
		}

		uinRepository.save(existingUin);
		// LOGGER.info("Updated UIN {} status to {}", existingUin.getUin(),
		// existingUin.getStatus());

		UinStatusUpdateReponseDto responseDto = new UinStatusUpdateReponseDto();
		responseDto.setUin(existingUin.getUin());
		responseDto.setStatus(existingUin.getStatus());
		return responseDto;
	}

	@Transactional(transactionManager = "transactionManager")
	@Override
	public void transferUin() {
		List<UinEntity> uinEntities = uinRepository.findByStatus(UinGeneratorConstant.ASSIGNED);

		if (uinEntities.isEmpty()) {
			LOGGER.info("No ASSIGNED UINs to transfer.");
			return;
		}

		List<UinEntityAssigned> assignedList = convertToAssignedList(uinEntities);
		uinRepositoryAssigned.saveAll(assignedList);
		uinRepository.deleteAll(uinEntities);

		// LOGGER.info("Transferred {} UINs to assigned table and deleted from main
		// pool.", uinEntities.size());
	}

	private List<UinEntityAssigned> convertUinEntitiesListToUinEntitiesAssignedList(List<UinEntity> uinEntities) {
		return uinEntities.stream()
				.map(UinEntityAssigned::new)
				.collect(Collectors.toList());
	}

	@Override
	public boolean uinExist(String uin) {
		return uinRepositoryAssigned.findById(uin).isPresent();
	}

	/**
	 * Converts UinEntity list to UinEntityAssigned list.
	 */
	private List<UinEntityAssigned> convertToAssignedList(List<UinEntity> uinEntities) {
		return uinEntities.stream().map(UinEntityAssigned::new).collect(Collectors.toList());
	}
}