/**
 * 
 */
package io.mosip.kernel.biometrics.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;


/**
 * 
 * BIR class with Builder to create data
 * 
 * @author Ramadurai Pandian
 *
 */
@Data
public class BiometricRecord implements Serializable {

	private VersionType version;
	private VersionType cbeffversion;
	private BIRInfo birInfo;
	/**
	 * This can be of any modality, each subtype is an element in this list.
	 * it has type and subtype info in it
	 */
	private List<BIR> segments;
	
	public BiometricRecord() {
		this.segments = new ArrayList<BIR>();
	}
	
	public BiometricRecord(VersionType version, VersionType cbeffversion, BIRInfo birInfo) {
		this.version = version;
		this.cbeffversion = cbeffversion;
		this.birInfo = birInfo;
		this.segments = new ArrayList<BIR>();
	}	

}
