package io.mosip.kernel.core.websub.spi;

/**
 * Retrieves failed WebSub content that the hub stored for a subscriber.
 * <p>
 * Contract: implementations perform HTTP to the hub failed-content endpoint.
 * Request metadata must be non-null. Call after reconnecting a subscriber that
 * missed notifications.
 * </p>
 *
 * @param <T> Failed content response.
 * @param <W> Metadata for getting failed messages.
 * @author Urvil Joshi
 */
public interface SubscriptionExtendedClient<T,W> {
	/**
	 * Fetches missed notifications from the hub.
	 * <p>
	 * Contract: performs HTTP GET/POST depending on the hub.
	 * </p>
	 *
	 * @param failedContentRequest never-null request metadata (topic, callback,
	 *                             time window)
	 * @return failed-content payload; never null on a successful HTTP exchange
	 */
	T getFailedContent(W failedContentRequest);
}
