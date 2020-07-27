package io.mosip.kernel.bioextractor.service.impl.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncHelper {
	@Async
	public void runAsync(Runnable runnable) {
		runnable.run();
	}
	
}
