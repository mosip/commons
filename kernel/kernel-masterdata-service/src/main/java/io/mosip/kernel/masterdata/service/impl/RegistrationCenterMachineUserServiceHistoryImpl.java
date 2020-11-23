package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.RegistrationCenterErrorCode;
import io.mosip.kernel.masterdata.constant.RegistrationCenterUserMappingHistoryErrorCode;
import io.mosip.kernel.masterdata.dto.RegistrationCenterUserMachineMappingHistoryDto;
import io.mosip.kernel.masterdata.dto.getresponse.RegistrationCenterUserMachineMappingHistoryResponseDto;
import io.mosip.kernel.masterdata.entity.MachineHistory;
import io.mosip.kernel.masterdata.entity.UserDetailsHistory;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.MachineHistoryRepository;
import io.mosip.kernel.masterdata.repository.UserDetailsHistoryRepository;
import io.mosip.kernel.masterdata.service.RegistrationCenterMachineUserHistoryService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;

/**
 * Implementation class for user machine mapping service
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Service
public class RegistrationCenterMachineUserServiceHistoryImpl implements RegistrationCenterMachineUserHistoryService {

	/**
	 * {@link RegistrationCenterUserMachineHistoryRepository} instance
	 */
	@Autowired
	MachineHistoryRepository machineHistoryRepository;
	
	@Autowired
	UserDetailsHistoryRepository usersHistoryRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.
	 * RegistrationCenterMachineUserHistoryService#
	 * getRegistrationCentersMachineUserMapping(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public RegistrationCenterUserMachineMappingHistoryResponseDto getRegistrationCentersMachineUserMapping(
			String effectiveTimestamp, String registrationCenterId, String machineId, String userId) {
		List<MachineHistory> machinesHistories = null;
		List<UserDetailsHistory> usersHistories = null;
		RegistrationCenterUserMachineMappingHistoryResponseDto centerUserMachineMappingResponseDto = new RegistrationCenterUserMachineMappingHistoryResponseDto();
		LocalDateTime lDateAndTime = null;
		try {
			lDateAndTime = MapperUtils.parseToLocalDateTime(effectiveTimestamp);
		} catch (DateTimeParseException e) {
			throw new RequestException(RegistrationCenterErrorCode.DATE_TIME_PARSE_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.DATE_TIME_PARSE_EXCEPTION.getErrorMessage());
		}
		try {
			machinesHistories = machineHistoryRepository
					.findByCntrIdAndMachineIdAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
							registrationCenterId,  machineId, lDateAndTime);
			usersHistories=usersHistoryRepository
					.findByCntrIdAndUsrIdAndEffectivetimesLessThanEqualAndIsDeletedFalseOrIsDeletedIsNull(
							registrationCenterId,  userId, lDateAndTime);
		} catch (DataAccessLayerException dataAccessLayerException) {
			throw new MasterDataServiceException(
					RegistrationCenterUserMappingHistoryErrorCode.REGISTRATION_CENTER_USER_MACHINE_MAPPING_HISTORY_FETCH_EXCEPTION
							.getErrorCode(),
					RegistrationCenterUserMappingHistoryErrorCode.REGISTRATION_CENTER_USER_MACHINE_MAPPING_HISTORY_FETCH_EXCEPTION
							.getErrorMessage() + ExceptionUtils.parseException(dataAccessLayerException));
		}
		if (machinesHistories == null || machinesHistories.isEmpty() || usersHistories== null || usersHistories.isEmpty()) {
			throw new DataNotFoundException(
					RegistrationCenterUserMappingHistoryErrorCode.REGISTRATION_CENTER_USER_MACHINE_MAPPING_HISTORY_NOT_FOUND
							.getErrorCode(),
					RegistrationCenterUserMappingHistoryErrorCode.REGISTRATION_CENTER_USER_MACHINE_MAPPING_HISTORY_NOT_FOUND
							.getErrorMessage());
		} else {
			List<RegistrationCenterUserMachineMappingHistoryDto> registrationCenters = new ArrayList<>();
			for(MachineHistory machinesHistory: machinesHistories) {
				for(UserDetailsHistory userHistory: usersHistories) {
					
					RegistrationCenterUserMachineMappingHistoryDto dto=new RegistrationCenterUserMachineMappingHistoryDto();
					dto.setCntrId(userHistory.getRegCenterId());
					if(userHistory.getIsActive() == null)dto.setIsActive(machinesHistory.getIsActive());
					if(machinesHistory.getIsActive() == null)dto.setIsActive(userHistory.getIsActive());
					if(userHistory.getIsActive() != null && machinesHistory.getIsActive()!= null) {
						dto.setIsActive(userHistory.getIsActive() && machinesHistory.getIsActive());
					}
					
					if(userHistory.getEffDTimes() == null)dto.setEffectivetimes(machinesHistory.getEffectDateTime());
					if(machinesHistory.getEffectDateTime() == null)dto.setEffectivetimes(userHistory.getEffDTimes());
					if(userHistory.getEffDTimes() != null && machinesHistory.getEffectDateTime()!= null) {
						dto.setEffectivetimes(machinesHistory.getEffectDateTime().isAfter( userHistory.getEffDTimes())? machinesHistory.getEffectDateTime() : userHistory.getEffDTimes() );
					}
					dto.setLangCode(machinesHistory.getLangCode());
					dto.setMachineId(machinesHistory.getId());
					dto.setUsrId(userHistory.getId());
					dto.setEffectivetimes(userHistory.getEffDTimes().isBefore(machinesHistory.getEffectDateTime()) ? userHistory.getEffDTimes() :machinesHistory.getEffectDateTime());
					registrationCenters.add(dto);
				}
				
			}
			centerUserMachineMappingResponseDto.setRegistrationCenters(registrationCenters);
		}
		return centerUserMachineMappingResponseDto;
	}

}
