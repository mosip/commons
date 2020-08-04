package io.mosip.kernel.bioextractor.service.helper;

@FunctionalInterface
public interface RunnableWithException<E extends Exception> {
	void run() throws E;
}
