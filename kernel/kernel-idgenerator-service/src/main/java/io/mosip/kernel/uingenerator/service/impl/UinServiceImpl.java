/**
 * 
 */
package io.mosip.kernel.uingenerator.service.impl;

import java.util.ArrayList;
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
import org.springframework.beans.factory.annotation.Value;

/**
 * Issues unused UINs, updates status, and transfers assigned rows to {@code uin_assigned}.
 *
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
	
	/**
	 * Page size for assigned-UIN transfer ({@code mosip.kernel.uin.page.size}).
	 */
	@Value("${mosip.kernel.uin.page.size:50000}")
	private int pageSize;

	/**
	 * Marks one {@code UNUSED} UIN as {@code ISSUED} and returns it.
	 *
	 * @param routingContext Vert.x routing context of the fetch request
	 * @return issued UIN
	 * @throws UinNotFoundException when no unused UIN remains
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

	

	/**
	 * Updates UIN status from {@code ISSUED} to {@code ASSIGNED} or {@code UNUSED}.
	 *
	 * @param uinAck         entity carrying the UIN and target status
	 * @param routingContext Vert.x routing context of the update request
	 * @return updated UIN and status
	 * @throws UinNotFoundException       when the UIN is unknown
	 * @throws UinNotIssuedException      when the UIN is not {@code ISSUED}
	 * @throws UinStatusNotFoundException when the target status is invalid
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

	/**
	 * Copies {@code ISSUED} rows to {@code uin_assigned} and deletes them from {@code uin}.
	 */
	@Transactional(transactionManager = "transactionManager")
	@Override
	public void transferUin() {
		List<UinEntity> uinEntities=uinRepository.findByStatus(UinGeneratorConstant.ISSUED, pageSize);
		List<UinEntityAssigned> uinEntitiesAssined = convertUinEntitiesListToUinEntitiesAssignedList(uinEntities);
		uinRepositoryAssigned.saveAll(uinEntitiesAssined);
	    uinRepository.deleteAll(uinEntities);
	}

	/**
	 * Maps pool entities to assigned-table entities.
	 *
	 * @param uinEntities source pool rows
	 * @return assigned-table copies
	 */
	private List<UinEntityAssigned> convertUinEntitiesListToUinEntitiesAssignedList(List<UinEntity> uinEntities) {
		return uinEntities.stream()
				.map(UinEntityAssigned::new)
				.collect(Collectors.toList());
	}

	/**
	 * Returns whether {@code uin} exists in {@code uin_assigned}.
	 *
	 * @param uin identifier to look up
	 * @return {@code true} when present
	 */
	@Override
	public boolean uinExist(String uin) {
	Optional<UinEntityAssigned> uinEntityAssignedOptional=uinRepositoryAssigned.findById(uin);
	return uinEntityAssignedOptional.isPresent();
	}
	
}