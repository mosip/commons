package io.mosip.kernel.masterdata.utils;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;

import io.mosip.kernel.core.util.CryptoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.MachineErrorCode;
import io.mosip.kernel.masterdata.constant.MachineSpecificationErrorCode;
import io.mosip.kernel.masterdata.constant.MachineTypeErrorCode;
import io.mosip.kernel.masterdata.constant.RegistrationCenterErrorCode;
import io.mosip.kernel.masterdata.entity.MachineSpecification;
import io.mosip.kernel.masterdata.entity.MachineType;
import io.mosip.kernel.masterdata.entity.RegistrationCenter;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.MachineSpecificationRepository;
import io.mosip.kernel.masterdata.repository.MachineTypeRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterRepository;
import tss.tpm.TPMT_PUBLIC;

@Component
public class MachineUtil {
	
	private final Logger logger = LoggerFactory.getLogger(MachineUtil.class);
	
	private static final String ALGORITHM = "RSA";

	@Autowired
	private MachineTypeRepository machineTypeRepository;

	@Autowired
	private MachineSpecificationRepository machineSpecificationRepository;

	@Value("${mosip.syncdata.tpm.required}")
	private boolean isTPMRequired;


	@Autowired
	private RegistrationCenterRepository centerRepository;

	public List<MachineSpecification> getMachineSpec() {
		try {
			return machineSpecificationRepository.findAllMachineSpecByIsActiveAndIsDeletedIsNullOrFalse();
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					MachineSpecificationErrorCode.MACHINE_SPECIFICATION_FETCH_EXCEPTION.getErrorCode(),
					MachineSpecificationErrorCode.MACHINE_SPECIFICATION_FETCH_EXCEPTION.getErrorMessage());
		}
	}

	public List<MachineType> getMachineTypes() {
		try {
			return machineTypeRepository.findAllMachineTypeByIsActiveAndIsDeletedFalseOrNull();

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(MachineTypeErrorCode.MACHINE_TYPE_FETCH_EXCEPTION.getErrorCode(),
					MachineTypeErrorCode.MACHINE_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
	}


	public List<RegistrationCenter> getAllRegistrationCenters() {
		try {
			return centerRepository.findAllByIsDeletedFalseOrIsDeletedIsNull();
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorCode(),
					RegistrationCenterErrorCode.REGISTRATION_CENTER_FETCH_EXCEPTION.getErrorMessage());
		}
	}
	
	public String getX509EncodedPublicKey(String encodedKey) {		
		try {
			if(isTPMRequired) {
				TPMT_PUBLIC tpmPublic = TPMT_PUBLIC.fromTpm(CryptoUtil.decodeBase64(encodedKey));
				return CryptoUtil.encodeBase64(tpmPublic.toTpm());
			}

			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(CryptoUtil.decodeBase64(encodedKey));
			KeyFactory kf = KeyFactory.getInstance(ALGORITHM);
			PublicKey publicKey = kf.generatePublic(keySpec);
			return CryptoUtil.encodeBase64(publicKey.getEncoded());
		} catch (Exception e) {
			logger.error("Invalid public key provided", e);
		}
		throw new RequestException(MachineErrorCode.INVALID_PUBLIC_KEY.getErrorCode(), 
				MachineErrorCode.INVALID_PUBLIC_KEY.getErrorMessage());
	}
}
