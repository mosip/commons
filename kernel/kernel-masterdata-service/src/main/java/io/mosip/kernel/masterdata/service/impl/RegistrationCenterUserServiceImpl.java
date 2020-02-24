package io.mosip.kernel.masterdata.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.MasterDataConstant;
import io.mosip.kernel.masterdata.constant.RegistrationCenterDeviceErrorCode;
import io.mosip.kernel.masterdata.constant.RegistrationCenterUserErrorCode;
import io.mosip.kernel.masterdata.dto.RegistrationCenterUserDto;
import io.mosip.kernel.masterdata.dto.UserAndRegCenterMappingResponseDto;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceService;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.RegistrationCenterUser;
import io.mosip.kernel.masterdata.entity.RegistrationCenterUserHistory;
import io.mosip.kernel.masterdata.entity.RegistrationCenterUserHistoryPk;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.entity.ZoneUser;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterUserID;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterUserHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterUserRepository;
import io.mosip.kernel.masterdata.repository.ZoneRepository;
import io.mosip.kernel.masterdata.repository.ZoneUserRepository;
import io.mosip.kernel.masterdata.service.RegistrationCenterUserService;
import io.mosip.kernel.masterdata.utils.AuditUtil;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.ZoneUtils;

/**
 * 
 * @author Megha Tanga
 *
 */

@Service
public class RegistrationCenterUserServiceImpl implements RegistrationCenterUserService {
	@Autowired
	AuditUtil auditUtil;
	@Autowired
	RegistrationCenterUserRepository registrationCenterUserRepository;

	@Autowired
	RegistrationCenterRepository registrationCenterRepository;

	@Autowired
	RegistrationCenterUserHistoryRepository registrationCenterUserHistoryRepository;

	@Autowired
	ZoneUserRepository zoneUserRepository;

	@Autowired
	ZoneUtils zoneUtils;

	@Autowired
	ZoneRepository zoneRepository;

	@Value("${mosip.primary-language}")
	private String primaryLanguage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterUserService#
	 * unmapUserRegCenter(java.lang.String, java.lang.String)
	 */
	@Transactional
	@Override
	public UserAndRegCenterMappingResponseDto unmapUserRegCenter(String userId, String regCenterId) {

		UserAndRegCenterMappingResponseDto responseDto = new UserAndRegCenterMappingResponseDto();
		try {

			// find given User id and registration center are in DB or not
			RegistrationCenterUser registrationCenterUser = registrationCenterUserRepository
					.findByUserIdAndRegCenterId(userId, regCenterId);
			if (registrationCenterUser != null) {

				// call a method to validate the zones
				validateRegistrationCenterUserIdZones(userId, regCenterId);

				if (!registrationCenterUser.getIsActive()) {
					auditUtil.auditRequest(
							String.format(
									MasterDataConstant.FAILURE_UNMAP, RegistrationCenterUser.class.getCanonicalName()),
							MasterDataConstant.AUDIT_SYSTEM,
							String.format(MasterDataConstant.FAILURE_DESC,
									RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_UNMAPPED_EXCEPTION
											.getErrorCode(),
									RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_UNMAPPED_EXCEPTION
											.getErrorMessage()),
							"ADM-767");
					throw new RequestException(
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_UNMAPPED_EXCEPTION
									.getErrorCode(),
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_UNMAPPED_EXCEPTION
									.getErrorMessage());
				}
				if (registrationCenterUser.getIsActive()) {
					RegistrationCenterUser updRegistrationCenterUser;

					registrationCenterUser.setIsActive(false);
					registrationCenterUser.setUpdatedBy(MetaDataUtils.getContextUser());
					registrationCenterUser.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));

					updRegistrationCenterUser = registrationCenterUserRepository.update(registrationCenterUser);

					// ----------------update history-------------------------------
					RegistrationCenterUserHistory registrationCenterUserHistory = new RegistrationCenterUserHistory();
					MapperUtils.map(updRegistrationCenterUser, registrationCenterUserHistory);
					MapperUtils.setBaseFieldValue(updRegistrationCenterUser, registrationCenterUserHistory);

					registrationCenterUserHistory.setRegistrationCenterUserHistoryPk(MapperUtils.map(
							new RegistrationCenterUserID(regCenterId, userId), RegistrationCenterUserHistoryPk.class));

					registrationCenterUserHistory.getRegistrationCenterUserHistoryPk()
							.setEffectivetimes(updRegistrationCenterUser.getUpdatedDateTime());
					registrationCenterUserHistory.setUpdatedDateTime(updRegistrationCenterUser.getUpdatedDateTime());
					registrationCenterUserHistoryRepository.create(registrationCenterUserHistory);

					// set success response
					responseDto.setStatus(MasterDataConstant.UNMAPPED_SUCCESSFULLY);
					responseDto.setMessage(
							String.format(MasterDataConstant.USER_AND_REGISTRATION_CENTER_UNMAPPING_SUCCESS_MESSAGE,
									userId, regCenterId));

				}

			} else {
				auditUtil.auditRequest(
						String.format(
								MasterDataConstant.FAILURE_UNMAP, RegistrationCenterUser.class.getCanonicalName()),
						MasterDataConstant.AUDIT_SYSTEM,
						String.format(MasterDataConstant.FAILURE_DESC,
								RegistrationCenterUserErrorCode.USER_AND_REG_CENTER_MAPPING_NOT_FOUND_EXCEPTION
										.getErrorCode(),
								RegistrationCenterUserErrorCode.USER_AND_REG_CENTER_MAPPING_NOT_FOUND_EXCEPTION
										.getErrorMessage()),
						"ADM-768");
				throw new RequestException(
						RegistrationCenterUserErrorCode.USER_AND_REG_CENTER_MAPPING_NOT_FOUND_EXCEPTION.getErrorCode(),
						String.format(RegistrationCenterUserErrorCode.USER_AND_REG_CENTER_MAPPING_NOT_FOUND_EXCEPTION
								.getErrorMessage(), userId, regCenterId));
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_UNMAP, RegistrationCenterUser.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_UNMAPPING_EXCEPTION.getErrorCode(),
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_UNMAPPING_EXCEPTION
									.getErrorMessage()),
					"ADM-769");
			throw new MasterDataServiceException(
					RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_UNMAPPING_EXCEPTION.getErrorCode(),
					RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_UNMAPPING_EXCEPTION.getErrorMessage());
		}
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterUserService#
	 * mapUserRegCenter(java.lang.String, java.lang.String)
	 */
	@Transactional
	@Override
	public UserAndRegCenterMappingResponseDto mapUserRegCenter(String userId, String regCenterId) {
		UserAndRegCenterMappingResponseDto responseDto = new UserAndRegCenterMappingResponseDto();
		try {

			// call a method to validate the zones
			validateRegistrationCenterUserIdZones(userId, regCenterId);

			// find given User id and registration center are in DB or not
			RegistrationCenterUser registrationCenterUser = registrationCenterUserRepository
					.findByUserIdAndRegCenterId(userId, regCenterId);
			if (registrationCenterUser != null) {

				// given user id has already mapped with given registration center id
				if (registrationCenterUser.getIsActive()) {
					auditUtil.auditRequest(
							String.format(
									MasterDataConstant.FAILURE_MAP, RegistrationCenterUser.class.getCanonicalName()),
							MasterDataConstant.AUDIT_SYSTEM,
							String.format(MasterDataConstant.FAILURE_DESC,
									RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_MAPPED_EXCEPTION
											.getErrorCode(),
									RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_MAPPED_EXCEPTION
											.getErrorMessage()),
							"ADM-765");
					throw new RequestException(
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_MAPPED_EXCEPTION
									.getErrorCode(),
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_ALREADY_MAPPED_EXCEPTION
									.getErrorMessage());
				}

				// given user id has NOT mapped with given registration center id
				if (!registrationCenterUser.getIsActive()) {

					// update the mapping as true
					RegistrationCenterUser updRegistrationCenterUser;

					registrationCenterUser.setIsActive(true);
					registrationCenterUser.setUpdatedBy(MetaDataUtils.getContextUser());
					registrationCenterUser.setUpdatedDateTime(LocalDateTime.now(ZoneId.of("UTC")));

					updRegistrationCenterUser = registrationCenterUserRepository.update(registrationCenterUser);

					// ----------update the history table -------
					RegistrationCenterUserHistory registrationCenterUserHistory = new RegistrationCenterUserHistory();
					MapperUtils.map(updRegistrationCenterUser, registrationCenterUserHistory);
					MapperUtils.setBaseFieldValue(updRegistrationCenterUser, registrationCenterUserHistory);
					registrationCenterUserHistory.setRegistrationCenterUserHistoryPk(MapperUtils.map(
							new RegistrationCenterUserID(regCenterId, userId), RegistrationCenterUserHistoryPk.class));
					registrationCenterUserHistory.getRegistrationCenterUserHistoryPk()
							.setEffectivetimes(updRegistrationCenterUser.getUpdatedDateTime());
					registrationCenterUserHistory.setUpdatedDateTime(updRegistrationCenterUser.getUpdatedDateTime());
					registrationCenterUserHistoryRepository.create(registrationCenterUserHistory);

					// ------set success response------
					responseDto.setStatus(MasterDataConstant.SUCCESS);
					responseDto.setMessage(
							String.format(MasterDataConstant.USER_AND_REGISTRATION_CENTER_MAPPING_SUCCESS_MESSAGE,
									userId, regCenterId));
				}

			} else {

				if (!((registrationCenterUserRepository.findByUserId(userId)).isEmpty())) {
					auditUtil.auditRequest(
							String.format(
									MasterDataConstant.FAILURE_MAP, RegistrationCenterUser.class.getCanonicalName()),
							MasterDataConstant.AUDIT_SYSTEM,
							String.format(MasterDataConstant.FAILURE_DESC,
									RegistrationCenterUserErrorCode.USER_MAPPED_REGISTRATION_CENTER.getErrorCode(),
									RegistrationCenterUserErrorCode.USER_MAPPED_REGISTRATION_CENTER.getErrorMessage()),
							"ADM-766");
					throw new MasterDataServiceException(
							RegistrationCenterUserErrorCode.USER_MAPPED_REGISTRATION_CENTER.getErrorCode(),
							RegistrationCenterUserErrorCode.USER_MAPPED_REGISTRATION_CENTER.getErrorMessage());

				}
				// create new Mapping for the given IDs
				RegistrationCenterUserDto registrationCenterUserDto = new RegistrationCenterUserDto();
				registrationCenterUserDto.setUserId(userId);
				registrationCenterUserDto.setRegCenterId(regCenterId);
				registrationCenterUserDto.setIsActive(true);
				registrationCenterUserDto.setLangCode(primaryLanguage);
				RegistrationCenterUserID registrationCenterUserID = createRegistrationCenterUser(
						registrationCenterUserDto);
				responseDto.setStatus(MasterDataConstant.SUCCESS);
				responseDto.setMessage(
						String.format(MasterDataConstant.USER_AND_REGISTRATION_CENTER_MAPPING_SUCCESS_MESSAGE,
								registrationCenterUserID.getUserId(), registrationCenterUserID.getRegCenterId()));
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			auditUtil.auditRequest(
					String.format(MasterDataConstant.FAILURE_MAP, RegistrationCenterUser.class.getCanonicalName()),
					MasterDataConstant.AUDIT_SYSTEM,
					String.format(MasterDataConstant.FAILURE_DESC,
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_MAPPING_EXCEPTION.getErrorCode(),
							RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_MAPPING_EXCEPTION
									.getErrorMessage()),
					"ADM-767");
			throw new MasterDataServiceException(
					RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_MAPPING_EXCEPTION.getErrorCode(),
					RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_MAPPING_EXCEPTION.getErrorMessage());
		}
		return responseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterUserService#
	 * createRegistrationCenterUser(io.mosip.kernel.masterdata.dto.
	 * RegistrationCenterUserDto)
	 */

	@Transactional(propagation = Propagation.MANDATORY)
	@Override
	public RegistrationCenterUserID createRegistrationCenterUser(RegistrationCenterUserDto regCenterUser) {

		RegistrationCenterUserHistory registrationCenterUserHistory = new RegistrationCenterUserHistory();

		RegistrationCenterUser registrationCenterUser = MetaDataUtils.setCreateMetaData(regCenterUser,
				RegistrationCenterUser.class);

		try {
			registrationCenterUser = registrationCenterUserRepository.create(registrationCenterUser);

			// ----- add new record to history table-------
			MapperUtils.map(registrationCenterUser, registrationCenterUserHistory);
			MapperUtils.setBaseFieldValue(registrationCenterUser, registrationCenterUserHistory);
			registrationCenterUserHistory.setRegistrationCenterUserHistoryPk(MapperUtils.map(
					new RegistrationCenterUserID(regCenterUser.getRegCenterId(), regCenterUser.getUserId()),
					RegistrationCenterUserHistoryPk.class));
			registrationCenterUserHistory.getRegistrationCenterUserHistoryPk()
					.setEffectivetimes(registrationCenterUser.getCreatedDateTime());
			registrationCenterUserHistory.setCreatedDateTime(registrationCenterUser.getCreatedDateTime());
			registrationCenterUserHistoryRepository.create(registrationCenterUserHistory);

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_INSERT_EXCEPTION.getErrorCode(),
					RegistrationCenterUserErrorCode.REGISTRATION_CENTER_USER_INSERT_EXCEPTION.getErrorMessage());
		}

		RegistrationCenterUserID registrationCenterUserID = new RegistrationCenterUserID();
		registrationCenterUserID.setUserId(registrationCenterUser.getRegistrationCenterUserID().getUserId());
		registrationCenterUserID.setRegCenterId(registrationCenterUser.getRegistrationCenterUserID().getRegCenterId());
		return registrationCenterUserID;
	}

	// method to validate the zone
	private void validateRegistrationCenterUserIdZones(String userId, String regCenterId) {

		// get given user id zone
		ZoneUser zoneUser = zoneUserRepository.findByIdAndLangCode(userId);

		if (zoneUser == null) {
			// check the user is De-commissioned
			throw new RequestException(RegistrationCenterUserErrorCode.USER_NOT_FOUND.getErrorCode(),
					RegistrationCenterUserErrorCode.USER_NOT_FOUND.getErrorMessage());
		}

		List<String> zoneIds;
		// get user zone and child zones list
		List<Zone> userZones = zoneUtils.getUserZones();
		zoneIds = userZones.parallelStream().map(Zone::getCode).collect(Collectors.toList());

		// check the given user zones will come under access user zone
		if (!(zoneIds.contains(zoneUser.getZoneCode()))) {
			throw new RequestException(RegistrationCenterUserErrorCode.INVALIDE_USER_ZONE.getErrorCode(),
					RegistrationCenterUserErrorCode.INVALIDE_USER_ZONE.getErrorMessage());
		}

		// get given registration center zone id
		RegistrationCenter regCenterZone = registrationCenterRepository.findByLangCodeAndId(regCenterId,
				primaryLanguage);
		if (regCenterZone == null) {
			// check the registration center is De-commissioned
			throw new RequestException(RegistrationCenterUserErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(),
					RegistrationCenterUserErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		if (!(zoneIds.contains(regCenterZone.getZoneCode()))) {
			// check the given registration center zones will come under accessed user zones
			throw new RequestException(RegistrationCenterUserErrorCode.INVALIDE_CENTER_ZONE.getErrorCode(),
					RegistrationCenterUserErrorCode.INVALIDE_CENTER_ZONE.getErrorMessage());
		}

		List<String> userIdZoneCodes;
		// list of zone and all child zones of given userId
		List<Zone> userIdZones = zoneUtils.getUserZonesByUserId(userId);
		userIdZoneCodes = userIdZones.parallelStream().map(Zone::getCode).collect(Collectors.toList());

		if (!(userIdZoneCodes.contains(regCenterZone.getZoneCode()))) {
			// check the given registration center zones will come under given user zones
			throw new RequestException(RegistrationCenterUserErrorCode.INVALIDE_CENTER_USER_ZONE.getErrorCode(),
					RegistrationCenterUserErrorCode.INVALIDE_CENTER_USER_ZONE.getErrorMessage());
		}
	}

}
