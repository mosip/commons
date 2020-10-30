package io.mosip.kernel.core.websub.spi;

/**
 * Implementer of this interface should be responsible basic tasks of a
 * publisher according to <a href= "https://www.w3.org/TR/websub/">websub
 * specifications</a>.
 * 
 * @author Urvil Joshi
 *
 * @param <T> type of topic
 * @param <P> type of payload
 * @param <H> type of header
 */
public interface PublisherClient<T, P, H> {

	/**
	 * This method is responsible for registering a topic at hub according to
	 * <a href= "https://www.w3.org/TR/websub/">websub specifications</a>.
	 * 
	 * @param topic  the topic to register by publisher.
	 * @param hubURL url for register endpoint of hub.
	 */
	public void registerTopic(T topic, String hubURL);

	/**
	 * This method is responsible for unregistering a topic at hub according to
	 * <a href= "https://www.w3.org/TR/websub/">websub specifications</a>.
	 * 
	 * @param topic  the topic to unregister by publisher.
	 * @param hubURL url for unregister endpoint of hub.
	 */
	public void unregisterTopic(T topic, String hubURL);

	/**
	 * This method is responsible for publishing a update for a particular topic at
	 * hub according to <a href= "https://www.w3.org/TR/websub/">websub
	 * specifications</a>.
	 * 
	 * @param topic       the topic to be updated by publisher.
	 * @param payload     payload to be send as update.
	 * @param contentType content type of payload.
	 * @param headers     additional headers to be sent with update.
	 * @param hubURL      url for publish endpoint of hub.
	 */
	public void publishUpdate(T topic, P payload, String contentType, H headers, String hubURL);

	/**
	 * This method Notifies a remote WebSub Hub from which an update is available to
	 * fetch for hubs that require publishing to happen as such.
	 * 
	 * @param topic   the topic to be notified by publisher
	 * @param headers additional headers to be sent with notify.
	 * @param hubURL  url for notify endpoint of hub.
	 */
	public void notifyUpdate(T topic, H headers, String hubURL);

}
