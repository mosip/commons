
package io.mosip.kernel.core.idgenerator.spi;

/**
 * Pre-generates Unique Identification Numbers (UIN) into the MOSIP UIN pool.
 * <p>
 * Contract: implementations insert unused UINs into {@code mosip_kernel}
 * pool tables. Call from the ID-generator service when the unused-UIN count
 * falls below threshold. {@code noOfUINToGenerate} must be positive. This
 * method is a batch side effect and returns void.
 * </p>
 *
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 */
public interface UinGenerator {

	/**
	 * Generates and persists the requested number of unused UINs.
	 * <p>
	 * Contract: performs database I/O. Does not return assigned UINs; those
	 * are issued by the ID-generator HTTP API from the pool.
	 * </p>
	 *
	 * @param noOfUINToGenerate number of UINs to add to the pool; must be
	 *                          positive
	 */
	void generateId(long noOfUINToGenerate);

}
