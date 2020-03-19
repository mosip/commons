package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.RegistrationCenterErrorCode;
import io.mosip.kernel.masterdata.constant.ZoneErrorCode;
import io.mosip.kernel.masterdata.dto.getresponse.ZoneNameResponseDto;
import io.mosip.kernel.masterdata.dto.getresponse.extn.ZoneExtnDto;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.entity.Zone;
import io.mosip.kernel.masterdata.entity.ZoneUser;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import io.mosip.kernel.masterdata.repository.ZoneRepository;
import io.mosip.kernel.masterdata.repository.ZoneUserRepository;
import io.mosip.kernel.masterdata.service.ZoneService;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.ZoneUtils;

/**
 * Zone Service Implementation
 * 
 * @author Abhishek Kumar
 * @author Srinivasan
 * @since 1.0.0
 *
 */
@Service
public class ZoneServiceImpl implements ZoneService {

	@Autowired
	private ZoneUtils zoneUtils;

	@Autowired
	ZoneUserRepository zoneUserRepository;

	@Autowired
	ZoneRepository zoneRepository;

	@Autowired
	private RegistrationCenterRepository registrationCenterRepo;

	@Value("${mosip.kernel.registrationcenterid.length}")
	private int centerIdLength;

	@Value("${mosip.primary-language}")
	private String primaryLangCode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.ZoneService#getUserZoneHierarchy(java.lang
	 * .String)
	 */
	@Override
	public List<ZoneExtnDto> getUserZoneHierarchy(String langCode) {
		List<Zone> zones = zoneUtils.getUserZones();
		if (zones != null && !zones.isEmpty()) {
			List<Zone> zoneList = zones.parallelStream().filter(z -> z.getLangCode().equals(langCode))
					.collect(Collectors.toList());
			return MapperUtils.mapAll(zoneList, ZoneExtnDto.class);
		}
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.ZoneService#getUserLeafZone(java.lang.
	 * String)
	 */
	@Override
	public List<ZoneExtnDto> getUserLeafZone(String langCode) {
		List<Zone> zones = zoneUtils.getUserLeafZones(langCode);
		if (zones != null && !zones.isEmpty()) {
			List<Zone> zoneList = zones.parallelStream().filter(z -> z.getLangCode().equals(langCode))
					.collect(Collectors.toList());
			return MapperUtils.mapAll(zoneList, ZoneExtnDto.class);
		}
		return Collections.emptyList();
	}

	@Override
	public ZoneNameResponseDto getZoneNameBasedOnLangCodeAndUserID(String userID, String langCode) {
		ZoneNameResponseDto zoneNameResponseDto = new ZoneNameResponseDto();
		ZoneUser zoneUser = null;
		Zone zone = null;
		try {
			zoneUser = zoneUserRepository.findZoneByUserIdNonDeleted(userID);
			if (zoneUser == null) {
				throw new DataNotFoundException(ZoneErrorCode.ZONEUSER_ENTITY_NOT_FOUND.getErrorCode(),
						ZoneErrorCode.ZONEUSER_ENTITY_NOT_FOUND.getErrorMessage());
			}
			zone = zoneRepository.findZoneByCodeAndLangCodeNonDeleted(zoneUser.getZoneCode(), langCode);
			if (zone == null) {
				throw new DataNotFoundException(ZoneErrorCode.ZONE_ENTITY_NOT_FOUND.getErrorCode(),
						ZoneErrorCode.ZONE_ENTITY_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(ZoneErrorCode.INTERNAL_SERVER_ERROR.getErrorCode(),
					ZoneErrorCode.INTERNAL_SERVER_ERROR.getErrorMessage());
		}
		zoneNameResponseDto.setZoneName(zone.getName());
		return zoneNameResponseDto;
	}

	@Override
	public boolean getUserValidityZoneHierarchy(String langCode, String zoneCode) {
		List<Zone> zones = zoneUtils.getUserZones();
		boolean zoneValid = false;
		List<ZoneExtnDto> zoneExtnList = new ArrayList<>();
		if (zones != null && !zones.isEmpty()) {
			List<Zone> zoneList = zones.parallelStream().filter(z -> z.getLangCode().equals(langCode))
					.collect(Collectors.toList());
			zoneExtnList = MapperUtils.mapAll(zoneList, ZoneExtnDto.class);
		}
		for (ZoneExtnDto zoneExtnDto : zoneExtnList) {
			if (zoneCode.equals(zoneExtnDto.getCode())) {
				zoneValid = true;
			}
		}
		return zoneValid;
	}

	@Override
	public boolean authorizeZone(String rId) {
		String centerId = rId.substring(0, centerIdLength);
		String zoneCode = getZoneBasedOnTheRId(centerId, primaryLangCode);
		return isPresentInTheHierarchy(zoneCode, primaryLangCode);
	}

	private String getZoneBasedOnTheRId(String centerId, String primaryLangCode) {
		RegistrationCenter registrationCenter = null;
		try {
			registrationCenter = registrationCenterRepo.findByIdAndLangCode(centerId, primaryLangCode);
		} catch (DataAccessException | DataAccessLayerException ex) {
			throw new MasterDataServiceException("ADM-PKT-500", "Error occured while fetching packet");
		}
		if(registrationCenter==null) {
			throw new DataNotFoundException(RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorCode(), RegistrationCenterErrorCode.REGISTRATION_CENTER_NOT_FOUND.getErrorMessage());
		}
		
		return registrationCenter.getZoneCode();
	}

	private boolean isPresentInTheHierarchy(String zoneCode, String primaryLangCode) {
		List<Zone> zones = zoneUtils.getUserLeafZones(primaryLangCode);
		boolean isAuthorized = zones.stream().anyMatch(zone -> zone.getCode().equals(zoneCode));
		if (!isAuthorized) {
			throw new RequestException(ZoneErrorCode.ADMIN_UNAUTHORIZED.getErrorCode(),
					ZoneErrorCode.ADMIN_UNAUTHORIZED.getErrorMessage());
		}

		return isAuthorized;
	}
}
