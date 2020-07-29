package io.mosip.kernel.bioextractor.api;

import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;
import io.mosip.kernel.bioextractor.exception.BiometricExtractionException;

public interface BiometricExtractionService {

	BioExtractPromiseResponseDTO extractBiometrics(BioExtractRequestDTO bioExtractRequestDTO) throws BiometricExtractionException;

}
