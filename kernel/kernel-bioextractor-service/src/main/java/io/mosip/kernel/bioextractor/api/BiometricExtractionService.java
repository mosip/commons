package io.mosip.kernel.bioextractor.api;

import io.mosip.kernel.bioextractor.dto.BioExtractPromiseResponseDTO;
import io.mosip.kernel.bioextractor.dto.BioExtractRequestDTO;

public interface BiometricExtractionService {

	BioExtractPromiseResponseDTO extractBiometrics(BioExtractRequestDTO bioExtractRequestDTO);

}
