/**
 * 
 */
package io.mosip.kernel.biometrics.entities;

import java.util.List;


/**
 * 
 * BIR class with Builder to create data
 * 
 * @author Ramadurai Pandian
 *
 */
public class BiometricRecord {	

	private BIRVersion version;
	private BIRVersion cbeffversion;
	private BIRInfo birInfo;
	
	/**
	 * This can be of any modality, each subtype is an element in this list.
	 * it has type and subtype info in it
	 */
	private List<BIR> segments;

	/**
	 * @return the version
	 */
	public BIRVersion getVersion() {
		return version;
	}

	/**
	 * @return the cbeffversion
	 */
	public BIRVersion getCbeffversion() {
		return cbeffversion;
	}

	/**
	 * @return the birInfo
	 */
	public BIRInfo getBirInfo() {
		return birInfo;
	}

	public List<BIR> getSegments() {
		return segments;
	}

	public void setSegments(List<BIR> segments) {
		this.segments = segments;
	}

}
