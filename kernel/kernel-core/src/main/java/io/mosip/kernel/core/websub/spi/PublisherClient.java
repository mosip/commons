package io.mosip.kernel.core.websub.spi;

/**
 * Publishes WebSub topics and content according to
 * <a href="https://www.w3.org/TR/websub/">WebSub</a>.
 * <p>
 * Contract: implementations perform HTTP to the hub. {@code topic} and
 * {@code hubURL} must be non-null and non-blank. Call from MOSIP modules that
 * emit domain events (IDA, credential, etc.).
 * </p>
 *
 * @param <T> type of topic
 * @param <P> type of payload
 * @param <H> type of header
 * @author Urvil Joshi
 */
public interface PublisherClient<T, P, H> {

	/**
	 * Registers a topic at the hub so subscribers can discover it.
	 * <p>
	 * Contract: performs HTTP POST to the hub register endpoint.
	 * </p>
	 *
	 * @param topic  never-null, never-blank topic name
	 * @param hubURL never-null, never-blank hub register URL
	 */
	public void registerTopic(T topic, String hubURL);

	/**
	 * Unregisters a previously registered topic at the hub.
	 * <p>
	 * Contract: performs HTTP POST to the hub unregister endpoint.
	 * </p>
	 *
	 * @param topic  never-null, never-blank topic name
	 * @param hubURL never-null, never-blank hub unregister URL
	 */
	public void unregisterTopic(T topic, String hubURL);

	/**
	 * Publishes a content update for a topic to the hub.
	 * <p>
	 * Contract: performs HTTP POST. {@code payload} must be non-null;
	 * {@code contentType} must be a valid MIME type; {@code headers} may be null.
	 * </p>
	 *
	 * @param topic       never-null, never-blank topic name
	 * @param payload     never-null update body
	 * @param contentType never-null MIME type of {@code payload}
	 * @param headers     optional extra headers; may be null
	 * @param hubURL      never-null, never-blank hub publish URL
	 */
	public void publishUpdate(T topic, P payload, String contentType, H headers, String hubURL);

	/**
	 * Notifies the hub that an update is available to fetch (intent-to-publish).
	 * <p>
	 * Contract: performs HTTP POST. Used by hubs that require a notify step
	 * before content is fetched.
	 * </p>
	 *
	 * @param topic   never-null, never-blank topic name
	 * @param headers optional extra headers; may be null
	 * @param hubURL  never-null, never-blank hub notify URL
	 */
	public void notifyUpdate(T topic, H headers, String hubURL);

}
