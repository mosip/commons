package io.mosip.kernel.biosdk.provider.util;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.util.ReflectionUtils;

import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;

public class BioProviderUtil {

	private static Map<String, Object> sdkInstances = new HashMap<>();

	private static final Logger LOGGER = BioSDKProviderLoggerFactory.getLogger(BioProviderUtil.class);

	public static Object getSDKInstance(Map<String, String> modalityParams) throws BiometricException {
		try {
			String instanceKey = modalityParams.entrySet().stream().sorted(Map.Entry.comparingByKey())
					.map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("-"));
			if (sdkInstances.containsKey(instanceKey)) {
				LOGGER.debug("SDK instance reused for modality class >>> {}", modalityParams.get(ProviderConstants.CLASSNAME));
				return sdkInstances.get(instanceKey);
			}
			Class<?> object = Class.forName(modalityParams.get(ProviderConstants.CLASSNAME));
			Object[] args = new Object[0];
			if (modalityParams.get(ProviderConstants.ARGUMENTS) != null
					&& !modalityParams.get(ProviderConstants.ARGUMENTS).isEmpty())
				args = modalityParams.get(ProviderConstants.ARGUMENTS).split(",");

			Optional<Constructor<?>> result = ReflectionUtils.findConstructor(object, args);
			if (result.isPresent()) {
				Constructor<?> constructor = result.get();
				constructor.setAccessible(true);
				LOGGER.debug("SDK instance created with params >>> {}", modalityParams);
				Object newInstance = constructor.newInstance(args);
				sdkInstances.put(instanceKey, newInstance);
				return newInstance;
			} else {
				throw new BiometricException(ErrorCode.NO_CONSTRUCTOR_FOUND.getErrorCode(),
						String.format(ErrorCode.NO_CONSTRUCTOR_FOUND.getErrorMessage(),
								modalityParams.get(ProviderConstants.CLASSNAME),
								modalityParams.get(ProviderConstants.ARGUMENTS)));
			}
		} catch (Exception e) {
			throw new BiometricException(ErrorCode.SDK_INITIALIZATION_FAILED.getErrorCode(),
					String.format(ErrorCode.SDK_INITIALIZATION_FAILED.getErrorMessage(),
							modalityParams.get(ProviderConstants.CLASSNAME), ExceptionUtils.getStackTrace(e)));
		}
	}

}
