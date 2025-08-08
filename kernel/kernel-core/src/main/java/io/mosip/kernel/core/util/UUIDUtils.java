package io.mosip.kernel.core.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.UUID;

/**
 * This class is used to generate UUID of Type 5.
 *
 * @author Bal Vikash Sharma
 *
 */
public class UUIDUtils {

	private static final Charset UTF8 = StandardCharsets.UTF_8;

	// RFC 4122 namespaces
	public static final UUID NAMESPACE_DNS = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
	public static final UUID NAMESPACE_URL = UUID.fromString("6ba7b811-9dad-11d1-80b4-00c04fd430c8");
	public static final UUID NAMESPACE_OID = UUID.fromString("6ba7b812-9dad-11d1-80b4-00c04fd430c8");
	public static final UUID NAMESPACE_X500 = UUID.fromString("6ba7b814-9dad-11d1-80b4-00c04fd430c8");

	// Precompute namespace bytes once (hot path win)
	private static final byte[] NS_DNS_BYTES  = toBytes(NAMESPACE_DNS);
	private static final byte[] NS_URL_BYTES  = toBytes(NAMESPACE_URL);
	private static final byte[] NS_OID_BYTES  = toBytes(NAMESPACE_OID);
	private static final byte[] NS_X500_BYTES = toBytes(NAMESPACE_X500);

	// Thread-local digests (MessageDigest is NOT thread-safe)
	private static final ThreadLocal<MessageDigest> SHA256_TL = ThreadLocal.withInitial(() -> getDigest("SHA-256"));

	private UUIDUtils() {
		super();
	}

	/**
	 * This method takes UUID <code>namespace</code> and a <code>name</code> and
	 * generate Type 5 UUID.
	 *
	 * @param namespace is the {@link UUID}
	 * @param name      for which UUID needs to be generated.
	 * @return type 5 UUID as per given <code>namespace</code> and <code>name</code>
	 * @throws NullPointerException when either <code>namespace</code> or
	 *                              <code>name</code> is null.
	 */
	public static UUID getUUID(UUID namespace, String name) {
		return getUUIDFromBytes(namespace, Objects.requireNonNull(name, "name == null").getBytes(UTF8));
	}

	/**
	 *
	 * This method takes UUID <code>namespace</code> and a <code>name</code> as a
	 * byte array and generate Type 5 UUID.
	 *
	 * @param namespace is the {@link UUID}
	 * @param name      is a byte array
	 * @return type 5 UUID as per given <code>namespace</code> and <code>name</code>
	 *
	 * @throws NullPointerException when either <code>namespace</code> or
	 *                              <code>name</code> is null.
	 */
	public static UUID getUUIDFromBytes(UUID namespace, byte[] name) {
		if (namespace == null) throw new NullPointerException("namespace is null");
		if (name == null)      throw new NullPointerException("name is null");

		final MessageDigest md = SHA256_TL.get();
		md.reset();
		md.update(toBytes(namespace));
		md.update(name);
		byte[] sha1Bytes = md.digest(); // 32 bytes
		sha1Bytes[6] &= 0x0f; /* clear version */
		sha1Bytes[6] |= 0x50; /* set to version 5 */
		sha1Bytes[8] &= 0x3f; /* clear variant */
		sha1Bytes[8] |= 0x80; /* set to IETF variant */
		return fromBytes(sha1Bytes);
	}

	private static UUID fromBytes(byte[] data) {
		// Based on the private UUID(bytes[]) constructor
		long msb = 0;
		long lsb = 0;
		assert data.length >= 16;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (data[i] & 0xff);
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (data[i] & 0xff);
		return new UUID(msb, lsb);
	}

	private static byte[] toBytes(UUID uuid) {
		// inverted logic of fromBytes()
		byte[] out = new byte[16];
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		for (int i = 0; i < 8; i++)
			out[i] = (byte) ((msb >> ((7 - i) * 8)) & 0xff);
		for (int i = 8; i < 16; i++)
			out[i] = (byte) ((lsb >> ((15 - i) * 8)) & 0xff);
		return out;
	}

	private static byte[] fastNamespaceBytes(UUID ns) {
		// Identity compares are fine; constants are interned singletons
		if (ns == NAMESPACE_DNS)  return NS_DNS_BYTES;
		if (ns == NAMESPACE_URL)  return NS_URL_BYTES;
		if (ns == NAMESPACE_OID)  return NS_OID_BYTES;
		if (ns == NAMESPACE_X500) return NS_X500_BYTES;
		return null;
	}

	private static MessageDigest getDigest(String algo) {
		try {
			return MessageDigest.getInstance(algo);
		} catch (NoSuchAlgorithmException e) {
			// Should not happen for standard algorithms
			throw new IllegalStateException(algo + " not supported", e);
		}
	}
}
