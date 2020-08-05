package io.mosip.kernel.bioextractor.service.helper;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import io.mosip.kernel.bioextractor.logger.BioExtractorLogger;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;

@Component
public class AsyncHelper<E extends Exception> {
	
	private static final Logger LOGGER = BioExtractorLogger.getLogger(AsyncHelper.class);
	
	@Async
	public void runAsync(RunnableWithException<E> runnable) {
		try {
			runnable.run();
		} catch (Exception ex) {
			LOGGER.error("", AsyncHelper.class.getSimpleName(), "runAsync",
					ex.getMessage() + "\n" + ExceptionUtils.getStackTrace(ex));
		}
	}
	
}
