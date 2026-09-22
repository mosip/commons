package io.mosip.kernel.idgenerator.mispid.constant;

/**
 * Property constant for MISPID generator.
 * 
 * @author Ritesh Sinha
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
public enum MispIdPropertyConstant {

	/** Numeric base used to compute the first MISPID ({@code 10^(length-1)}). */
	ID_BASE("10");

	/**
	 * The property of MISPID generator.
	 */
	private String property;

	/**
	 * Getter for property.
	 * 
	 * @return the property.
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Constructor for MispIdPropertyConstant.
	 * 
	 * @param property the property.
	 */
	MispIdPropertyConstant(String property) {
		this.property = property;
	}

}
