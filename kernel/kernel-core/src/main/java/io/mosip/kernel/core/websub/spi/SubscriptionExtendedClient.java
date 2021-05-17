package io.mosip.kernel.core.websub.spi;

/**
 * Implementer of this interface should be responsible for extended tasks of a
 * subscriber.
 * 
 * @author Urvil Joshi
 *
 * @param <T> Metadata for getting failed messages.
 * @param <W> Failed content response.
 */
public interface SubscriptionExtendedClient<T,W> {
	/**
	 * This method sends a failed  content get request to a WebSub Hub.
	 * 
	 * @param failedContentRequest metadata required for getting failed content.
	 * @return response for failed content get request.
	 */
	T getFailedContent(W failedContentRequest);
}
