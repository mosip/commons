/**
 * Default ISO format identifiers used when assembling CBEFF records.
 */
package io.mosip.kernel.core.cbeffutil.constant;

/**
 * Default CBEFF format-owner and ISO format-identifier constants.
 * <p>
 * Contract: this type is not instantiable. Values follow ISO/IEC 19785 and
 * MOSIP biometric format identifiers. Use when writing BDB headers or
 * validating ISO images.
 * </p>
 *
 * @author Ramadurai Pandian
 */
public class CbeffConstant {

	/**
	 * Default CBEFF format owner identifier used by MOSIP.
	 */
	public static final long FORMAT_OWNER = 257;

	/**
	 * CBEFF format type for iris images.
	 */
	public static final long FORMAT_TYPE_IRIS = 9;

	/**
	 * CBEFF format type for face images.
	 */
	public static final long FORMAT_TYPE_FACE = 8;

	/**
	 * CBEFF format type for fingerprint images.
	 */
	public static final long FORMAT_TYPE_FINGER = 7;

	/**
	 * CBEFF format type for fingerprint minutiae templates.
	 */
	public static final long FORMAT_TYPE_FINGER_MINUTIAE = 2;

	/**
	 * ISO fingerprint format identifier ({@code FIR\\0}).
	 */
	public static final int FINGER_FORMAT_IDENTIFIER = 0x46495200;

	/**
	 * ISO iris format identifier ({@code IIR\\0}).
	 */
	public static final int IRIS_FORMAT_IDENTIFIER = 0x49495200;

	/**
	 * ISO face format identifier currently aliased to the finger identifier
	 * pending a dedicated Face ISO constant.
	 */
	public static final int FACE_FORMAT_IDENTIFIER = 0x46495200;

	// TODO Actual face identifier waiting for Face ISO image
	// public static final int FACE_FORMAT_IDENTIFIER = 0x46414300;

}
