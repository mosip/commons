package io.mosip.kernel.bioextractor.service.impl.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AsyncHelper<E extends Exception> {
	@Async
	public void runAsync(RunnableWithException<E> runnable) throws E{
		runnable.run();
	}
	
}
