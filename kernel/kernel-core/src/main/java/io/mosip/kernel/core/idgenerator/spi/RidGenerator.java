package io.mosip.kernel.core.idgenerator.spi;

/**
 * Generates a MOSIP Registration ID (RID) from center and machine identifiers.
 * <p>
 * Contract: implementations typically persist a sequence in {@code mosip_kernel}
 * and concatenate center id, machine id, sequence, and timestamp. Call from
 * the RID HTTP API ({@code /v1/ridgenerator}) or registration client.
 * Returned RID is never null; inputs must be non-blank numeric strings of the
 * configured length.
 * </p>
 *
 * @param <T> generated RID type, typically {@link String}
 * @author Sidhant Agarwal
 * @since 1.0.0
 */
public interface RidGenerator<T> {
	/**
	 * Generates a RID using configured center-id, machine-id, sequence, and
	 * timestamp lengths.
	 * <p>
	 * Contract: may perform database I/O to increment the RID sequence.
	 * </p>
	 *
	 * @param agentId   never-null, never-blank registration-center (agent) id
	 * @param machineId never-null, never-blank machine id
	 * @return never-null generated RID
	 */
	public T generateId(String agentId, String machineId);

	/**
	 * Generates a RID using caller-supplied field lengths.
	 * <p>
	 * Contract: may perform database I/O to increment the RID sequence. Length
	 * arguments must be positive and match the corresponding id string lengths.
	 * </p>
	 *
	 * @param centreId        never-null, never-blank registration-center id
	 * @param machineId       never-null, never-blank machine id
	 * @param centerIdLength  expected character length of {@code centreId}; must
	 *                        be positive
	 * @param machineIdLength expected character length of {@code machineId}; must
	 *                        be positive
	 * @param sequenceLength  number of sequence digits; must be positive
	 * @param timestampLength number of timestamp digits; must be positive
	 * @return never-null generated RID
	 */
	public T generateId(String centreId, String machineId, int centerIdLength, int machineIdLength, int sequenceLength,
			int timestampLength);

}
