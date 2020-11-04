package io.mosip.kernel.masterdata.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.WorkingNonWorkingDayErrorCode;
import io.mosip.kernel.masterdata.dto.DayNameAndSeqListDto;
import io.mosip.kernel.masterdata.dto.WeekDaysResponseDto;
import io.mosip.kernel.masterdata.dto.WorkingDaysResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.WeekDaysDto;
import io.mosip.kernel.masterdata.dto.getresponse.WorkingDaysDto;
import io.mosip.kernel.masterdata.entity.DaysOfWeek;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.DaysOfWeekListRepo;
import io.mosip.kernel.masterdata.repository.RegWorkingNonWorkingRepo;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.service.RegWorkingNonWorkingService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;

@Service
public class RegWorkingNonWorkingServiceImpl implements RegWorkingNonWorkingService {

	@Autowired
	@Qualifier("workingDaysRepo")
	private RegWorkingNonWorkingRepo workingDaysRepo;

	@Autowired
	@Qualifier("daysOfWeekRepo")
	private DaysOfWeekListRepo daysOfWeekRepo;

	/**
	 * Reference to RegistrationCenterRepository.
	 */
	@Autowired
	private RegistrationCenterRepository registrationCenterRepository;

	@Override
	public WeekDaysResponseDto getWeekDaysList(String regCenterId, String langCode) {

		List<WeekDaysDto> weekdayList = null;
		
		WeekDaysResponseDto weekdays = new WeekDaysResponseDto();
		RegistrationCenter registrationCenter = null;

		Objects.requireNonNull(regCenterId);
		Objects.requireNonNull(langCode);

		try {
			weekdayList = workingDaysRepo.findByregistrationCenterIdAndlangCodeForWeekDays(regCenterId, langCode);
			registrationCenter = registrationCenterRepository.findByIdAndLangCode(regCenterId, langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					WorkingNonWorkingDayErrorCode.WORKING_DAY_TABLE_NOT_ACCESSIBLE.getErrorCode(),
					WorkingNonWorkingDayErrorCode.WORKING_DAY_TABLE_NOT_ACCESSIBLE.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (registrationCenter == null) {
			throw new DataNotFoundException(WorkingNonWorkingDayErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					WorkingNonWorkingDayErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		} else {
			if (weekdayList != null && !weekdayList.isEmpty()) {
				weekdays.setWeekdays(weekdayList);
			}
			// Fetch from global level .
			else {
				List<DaysOfWeek> globalDaysList = daysOfWeekRepo.findByAllGlobalWorkingTrue(langCode);
				if (globalDaysList != null && !globalDaysList.isEmpty()) {
					weekdayList = globalDaysList.stream().map(day -> {
						WeekDaysDto globalWorkingDay = new WeekDaysDto();
						globalWorkingDay.setDayCode(day.getCode());
						globalWorkingDay.setGlobalWorking(day.isGlobalWorking());
						globalWorkingDay.setLanguagecode(day.getLangCode());
						globalWorkingDay.setName(day.getName());
						return globalWorkingDay;
					}).collect(Collectors.toList());

					weekdays.setWeekdays(weekdayList);
				} else {
				throw new DataNotFoundException(
						WorkingNonWorkingDayErrorCode.WEEK_DAY_DATA_FOUND_EXCEPTION.getErrorCode(),
						WorkingNonWorkingDayErrorCode.WEEK_DAY_DATA_FOUND_EXCEPTION.getErrorMessage());
			}

		}
		}

		return weekdays;
	}

	@Override
	public WorkingDaysResponseDto getWorkingDays(String regCenterId, String langCode) {

		List<WorkingDaysDto> workingDayList = null;
		List<DayNameAndSeqListDto> nameSeqList = null;
		WorkingDaysResponseDto responseDto = new WorkingDaysResponseDto();
		Objects.requireNonNull(regCenterId);
		Objects.requireNonNull(langCode);
		RegistrationCenter registrationCenter = null;
		try {
			nameSeqList = workingDaysRepo.findByregistrationCenterIdAndlanguagecodeForWorkingDays(regCenterId, langCode);
			registrationCenter = registrationCenterRepository.findByIdAndLangCode(regCenterId, langCode);
		} catch (DataAccessException | DataAccessLayerException e) {
			throw new MasterDataServiceException(
					WorkingNonWorkingDayErrorCode.WORKING_DAY_TABLE_NOT_ACCESSIBLE.getErrorCode(),
					WorkingNonWorkingDayErrorCode.WORKING_DAY_TABLE_NOT_ACCESSIBLE.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		if (registrationCenter == null) {
			throw new DataNotFoundException(WorkingNonWorkingDayErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					WorkingNonWorkingDayErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		} else {
			// Fetch from DB.
			if (nameSeqList != null && !nameSeqList.isEmpty()) {

				nameSeqList.sort((d1, d2) -> d1.getDaySeq() - d2.getDaySeq());
				workingDayList = nameSeqList.stream().map(nameSeq -> {
					WorkingDaysDto dto = new WorkingDaysDto();
					dto.setLanguageCode(langCode);
					dto.setName(nameSeq.getName());
					dto.setOrder(nameSeq.getDaySeq());
					return dto;
				}).collect(Collectors.toList());
				responseDto.setWorkingdays(workingDayList);

			}
			 else {
					throw new DataNotFoundException(
							WorkingNonWorkingDayErrorCode.WORKING_DAY_DATA_FOUND_EXCEPTION.getErrorCode(),
							WorkingNonWorkingDayErrorCode.WORKING_DAY_DATA_FOUND_EXCEPTION.getErrorMessage());
				}

			
		}

		return responseDto;
	}

}
