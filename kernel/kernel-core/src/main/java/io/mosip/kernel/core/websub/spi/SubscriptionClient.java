package io.mosip.kernel.core.websub.spi;

/**
 * Implementer of this interface should be responsible basic tasks of a
 * subscriber according to <a href= "https://www.w3.org/TR/websub/">websub
 * specifications</a>.
 * 
 * @author Urvil Joshi
 *
 * @param <S> Metadata for subscribe and unsubscribe request.
 * @param <R> Subscribe/UnSubcribe response.
 */
public interface SubscriptionClient<S,U,R> {
	/**
	 * This method sends a subscription request to a WebSub Hub according to
	 * <a href= "https://www.w3.org/TR/websub/">websub specifications</a>.
	 * 
	 * @param subscriptionRequest metadata required for request to subscribe.
	 * @return response for subscribe request.
	 */
	@Deprecated
	default R subscribe(S subscriptionRequest) {
		return subscribe(subscriptionRequest, null);
	}

	/**
	 * This method sends a unsubscription request to a WebSub Hub according to
	 * <a href= "https://www.w3.org/TR/websub/">websub specifications</a>.
	 * 
	 * @param unSubscriptionRequest metadata required for request to unsubscribe.
	 * @return response for unsubscribe request.
	 */
	@Deprecated
	default R unSubscribe(U unSubscriptionRequest) {
		return unSubscribe(unSubscriptionRequest, null);
	}
	
	/**
	 * This method sends a subscription request to a WebSub Hub according to
	 * <a href= "https://www.w3.org/TR/websub/">websub specifications</a>.
	 * 
	 * @param subscriptionRequest metadata required for request to subscribe.
	 * @return response for subscribe request.
	 */
	R subscribe(S subscriptionRequest,String authToken);

	/**
	 * This method sends a unsubscription request to a WebSub Hub according to
	 * <a href= "https://www.w3.org/TR/websub/">websub specifications</a>.
	 * 
	 * @param unSubscriptionRequest metadata required for request to unsubscribe.
	 * @return response for unsubscribe request.
	 */
	R unSubscribe(U unSubscriptionRequest,String authToken);
}
