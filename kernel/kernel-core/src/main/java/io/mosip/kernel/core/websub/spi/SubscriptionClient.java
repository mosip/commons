package io.mosip.kernel.core.websub.spi;

/**
 * Subscribes and unsubscribes from WebSub topics according to
 * <a href="https://www.w3.org/TR/websub/">WebSub</a>.
 * <p>
 * Contract: implementations perform HTTP to the hub. Request metadata must be
 * non-null. Call from MOSIP modules that consume domain events.
 * </p>
 *
 * @param <S> Metadata for subscribe request.
 * @param <U> Metadata for unsubscribe request.
 * @param <R> Subscribe/unsubscribe response.
 * @author Urvil Joshi
 */
public interface SubscriptionClient<S,U,R> {
	/**
	 * Sends a subscribe request to the hub.
	 * <p>
	 * Contract: performs HTTP. The hub later verifies the callback.
	 * </p>
	 *
	 * @param subscriptionRequest never-null subscribe metadata (topic, callback,
	 *                            secret)
	 * @return hub response; never null on a successful HTTP exchange
	 */
	R subscribe(S subscriptionRequest);

	/**
	 * Sends an unsubscribe request to the hub.
	 * <p>
	 * Contract: performs HTTP.
	 * </p>
	 *
	 * @param unSubscriptionRequest never-null unsubscribe metadata
	 * @return hub response; never null on a successful HTTP exchange
	 */
	R unSubscribe(U unSubscriptionRequest);
}
