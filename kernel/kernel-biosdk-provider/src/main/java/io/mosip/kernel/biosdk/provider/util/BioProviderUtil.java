package io.mosip.kernel.biosdk.provider.util;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.util.ReflectionUtils;

import io.mosip.kernel.core.bioapi.exception.BiometricException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;

public class BioProviderUtil {
	
	private static final Logger LOGGER = BioSDKProviderLoggerFactory.getLogger(BioProviderUtil.class);
	
	public static Object getSDKInstance(Map<String, String> modalityParams) throws BiometricException {
		try {					
			Class<?> object = (Class<?>) Class.forName(modalityParams.get(ProviderConstants.CLASSNAME));
			Object[] args = new Object[0];				
			if(modalityParams.get(ProviderConstants.ARGUMENTS) != null && !modalityParams.get(ProviderConstants.ARGUMENTS).isEmpty())
				args = modalityParams.get(ProviderConstants.ARGUMENTS).split(",");
			
			Optional<Constructor<?>> result = ReflectionUtils.findConstructor(object, args);
			if(result.isPresent()) {
				Constructor<?> constructor = (Constructor<?>) result.get();
				constructor.setAccessible(true);
				LOGGER.info(ProviderConstants.LOGGER_SESSIONID, ProviderConstants.LOGGER_IDTYPE, 
						"GET SDK INSTANCE", "SDK instance created with params >>> " + modalityParams);
				return constructor.newInstance(args);
			}
			else
				throw new BiometricException(ErrorCode.NO_CONSTRUCTOR_FOUND.getErrorCode(), 
						String.format(ErrorCode.NO_CONSTRUCTOR_FOUND.getErrorMessage(), 
								modalityParams.get(ProviderConstants.CLASSNAME),
								modalityParams.get(ProviderConstants.ARGUMENTS)));
			
		} catch (Exception e) {
			throw new BiometricException(ErrorCode.SDK_INITIALIZATION_FAILED.getErrorCode(), 
					String.format(ErrorCode.SDK_INITIALIZATION_FAILED.getErrorMessage(), 
							modalityParams.get(ProviderConstants.CLASSNAME),
							ExceptionUtils.getStackTrace(e)));
		}
	}

}
