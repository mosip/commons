package io.mosip.kernel.biosdk.provider.factory;

import java.util.*;

import javax.annotation.PostConstruct;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.biosdk.provider.util.BioSDKProviderLoggerFactory;
import io.mosip.kernel.biosdk.provider.util.ErrorCode;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.logger.spi.Logger;

@ConfigurationProperties(prefix = "mosip.biometric.sdk.providers")
@Component
public class BioAPIFactory {
	
	private static final Logger LOGGER = BioSDKProviderLoggerFactory.getLogger(BioAPIFactory.class);

	@Getter
	@Setter
	private Map<String, Map<String, String>> finger;

	@Getter
	@Setter
	private Map<String, Map<String, String>> iris;

	@Getter
	@Setter
	private Map<String, Map<String, String>> face;

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

		List<String> vendorIds = new ArrayList<>();
		vendorIds.addAll(this.finger == null ? Collections.EMPTY_LIST : this.finger.keySet());
		vendorIds.addAll(this.iris == null ? Collections.EMPTY_LIST : this.iris.keySet());
		vendorIds.addAll(this.face == null ? Collections.EMPTY_LIST : this.face.keySet());

		for (String vendorId : new HashSet<>(vendorIds)) {

			if(isProviderRegistryFilled()) {
				LOGGER.info("Provider registry is already filled : {}", providerRegistry.keySet());
				break;
			}

			Map<BiometricType, Map<String, String>> params = new HashMap<>();
			params.put(BiometricType.FINGER, getFingerEntry(vendorId));
			params.put(BiometricType.IRIS, getIrisEntry(vendorId));
			params.put(BiometricType.FACE, getFaceEntry(vendorId));

			LOGGER.info("Starting initialization for vendor {} with params >> {}", vendorId, params);

			if(params.isEmpty())
				throw new BiometricException(ErrorCode.NO_SDK_CONFIG.getErrorCode(), ErrorCode.NO_SDK_CONFIG.getErrorMessage());

			//pass params per modality to each provider, each providers will initialize supported SDK's
			for(iBioProviderApi provider : providerApis) {
				try {
					Map<BiometricType, List<BiometricFunction>> supportedModalities = provider.init(params);
					if(supportedModalities != null && !supportedModalities.isEmpty()) {
						supportedModalities.forEach((modality, functions) -> {
							functions.forEach(function -> {
								addToRegistry(modality, function, provider);
							});
						});
					}
				} catch (BiometricException ex) {
					LOGGER.error("Failed to initialize SDK instance", ex);
				}
			}
		}

		if(!isProviderRegistryFilled())
			throw new BiometricException(ErrorCode.SDK_REGISTRY_EMPTY.getErrorCode(),
					ErrorCode.SDK_REGISTRY_EMPTY.getErrorMessage());
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

	private boolean isProviderRegistryFilled() {
		if(isModalityConfigured(BiometricType.FINGER)  && !providerRegistry.containsKey(BiometricType.FINGER))
			return false;

		if(isModalityConfigured(BiometricType.IRIS) && !providerRegistry.containsKey(BiometricType.IRIS))
			return false;

		if(isModalityConfigured(BiometricType.FACE) && !providerRegistry.containsKey(BiometricType.FACE))
			return false;

		return true;
	}

	private boolean isModalityConfigured(BiometricType modality) {
		switch (modality) {
			case FINGER: return this.finger != null && !this.finger.isEmpty();
			case IRIS: return this.iris != null && !this.iris.isEmpty();
			case FACE: return this.face != null && !this.face.isEmpty();
		}
		return false;
	}

	private Map getFingerEntry(String key) {
		return this.finger == null ? Collections.EMPTY_MAP : this.finger.getOrDefault(key, Collections.EMPTY_MAP);
	}
	private Map getIrisEntry(String key) {
		return this.iris == null ? Collections.EMPTY_MAP : this.iris.getOrDefault(key, Collections.EMPTY_MAP);
	}
	private Map getFaceEntry(String key) {
		return this.face == null ? Collections.EMPTY_MAP : this.face.getOrDefault(key, Collections.EMPTY_MAP);
	}
}
