package io.mosip.kernel.biosdk.provider.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.biosdk.provider.util.BioSDKProviderLoggerFactory;
import io.mosip.kernel.biosdk.provider.util.ErrorCode;
import io.mosip.kernel.biosdk.provider.util.ProviderConstants;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.logger.spi.Logger;

@ConfigurationProperties(prefix = "mosip.biometric.sdk.provider")
@Component
public class BioAPIFactory {
	
	private static final Logger LOGGER = BioSDKProviderLoggerFactory.getLogger(BioAPIFactory.class);		

	private Map<String, String> finger;
	
	private Map<String, String> iris;
	
	private Map<String, String> face;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private List<iBioProviderApi> providerApis;

	private Map<BiometricType, Map<BiometricFunction, iBioProviderApi>> providerRegistry = new HashMap<>();
	
	/**
	 * 
	 * @throws BiometricException
	 */
	@PostConstruct
	public void initializeBioAPIProviders() throws BiometricException {
		if(providerApis == null || providerApis.isEmpty()) {
			throw new BiometricException(ErrorCode.NO_PROVIDERS.getErrorCode(), ErrorCode.NO_PROVIDERS.getErrorMessage());
		}		
		
		Map<BiometricType, Map<String, String>> params = new HashMap<>();
		params.put(BiometricType.FINGER, finger);
		params.put(BiometricType.IRIS, iris);
		params.put(BiometricType.FACE, face);
		
		LOGGER.info(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, "initializeBioAPIProviders invoked", 
				"With params >> " + params);
		
		if(params.isEmpty())
			throw new BiometricException(ErrorCode.NO_SDK_CONFIG.getErrorCode(), ErrorCode.NO_SDK_CONFIG.getErrorMessage());
		
		//pass params per modality to each provider, each providers will initialize supported SDK's
		for(iBioProviderApi provider : providerApis) {
			Map<BiometricType, List<BiometricFunction>> supportedModalities = provider.init(params);
			if(supportedModalities != null && !supportedModalities.isEmpty()) {
				supportedModalities.forEach((modality, functions) -> {
					functions.forEach(function -> {
						addToRegistry(modality, function, provider);
					});					
				});
			}
		}
	}
	
	/**
	 * Returns BioAPIProvider for provided modality and Function
	 * @param modality
	 * @param biometricFunction
	 * @return
	 * @throws BiometricException
	 */
	public iBioProviderApi getBioProvider(BiometricType modality, BiometricFunction biometricFunction) throws BiometricException {
		if(providerRegistry.get(modality) != null && providerRegistry.get(modality).get(biometricFunction) != null)
			return providerRegistry.get(modality).get(biometricFunction);
		
		throw new BiometricException(ErrorCode.NO_PROVIDERS.getErrorCode(), ErrorCode.NO_PROVIDERS.getErrorMessage());
	}
	
	private void addToRegistry(BiometricType modality, BiometricFunction function, iBioProviderApi provider) {
		if(providerRegistry.get(modality) == null)
			providerRegistry.put(modality, new HashMap<>());
		
		providerRegistry.get(modality).put(function, provider);
	}

	public void setFinger(Map<String, String> finger) {
		this.finger = finger;
	}

	public void setIris(Map<String, String> iris) {
		this.iris = iris;
	}

	public void setFace(Map<String, String> face) {
		this.face = face;
	}
}
