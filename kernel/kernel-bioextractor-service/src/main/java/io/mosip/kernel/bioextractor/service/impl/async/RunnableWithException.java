package io.mosip.kernel.bioextractor.service.impl.async;

@FunctionalInterface
public interface RunnableWithException<E extends Exception> {
	void run() throws E;
}
