package io.mosip.kernel.biometrics.entities;

import java.io.Serializable;
import java.util.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import io.mosip.kernel.core.cbeffutil.common.Base64Adapter;
import io.mosip.kernel.core.cbeffutil.jaxbclasses.BIRType;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 
 * BIR class with Builder to create data
 * 
 * @author Ramadurai Pandian
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BIRType", propOrder = { "version", "cbeffversion", "birInfo", "bdbInfo",  "bdb",
		"sb" ,"birs","sbInfo","others"})
@XmlRootElement(name = "BIR")
@Data
@NoArgsConstructor
@JsonDeserialize(builder = BIR.BIRBuilder.class)
public class BIR implements Serializable {

	@XmlElement(name = "Version")
	private VersionType version;
	@XmlElement(name = "CBEFFVersion")
	private VersionType cbeffversion;
	@XmlElement(name = "BIRInfo", required = true)
	private BIRInfo birInfo;
	@XmlElement(name = "BDBInfo")
	private BDBInfo bdbInfo;
	@XmlElement(name = "BDB")
	@XmlJavaTypeAdapter(Base64Adapter.class)
	private byte[] bdb;
	@XmlElement(name = "SB")
	private byte[] sb;
	@XmlElement(name = "BIR")
	protected List<BIR> birs;
	@XmlElement(name = "SBInfo")
	private SBInfo sbInfo;
	@XmlElement(name = "Others")
	private List<Entry> others;

	public BIR(BIRBuilder birBuilder) {
		this.version = birBuilder.version;
		this.cbeffversion = birBuilder.cbeffversion;
		this.birInfo = birBuilder.birInfo;
		this.bdbInfo = birBuilder.bdbInfo;
		this.bdb = birBuilder.bdb;
		this.sb = birBuilder.sb;
		this.sbInfo = birBuilder.sbInfo;
		this.others = birBuilder.others;
	}

	public static class BIRBuilder {
		private VersionType version;
		private VersionType cbeffversion;
		private BIRInfo birInfo;
		private BDBInfo bdbInfo;
		private byte[] bdb;
		private byte[] sb;
		private SBInfo sbInfo;
		private List<Entry> others;

		public BIRBuilder withOthers(List<Entry> others) {
			if(Objects.isNull(others))
				this.others = new ArrayList<>();
			else
				this.others = others;
			return this;
		}
		
		/*public BIRBuilder withOthers(Map<String, String> others) {
			if(Objects.isNull(this.others))
				this.others = new HashMap<>();
			if(!Objects.isNull(others))
				this.others.putAll(others);
			return this;
		}*/

		public BIRBuilder withVersion(VersionType version) {
			this.version = version;
			return this;
		}

		public BIRBuilder withCbeffversion(VersionType cbeffversion) {
			this.cbeffversion = cbeffversion;
			return this;
		}

		public BIRBuilder withBirInfo(BIRInfo birInfo) {
			this.birInfo = birInfo;
			return this;
		}

		public BIRBuilder withBdbInfo(BDBInfo bdbInfo) {
			this.bdbInfo = bdbInfo;
			return this;
		}

		public BIRBuilder withBdb(byte[] bdb) {
			this.bdb = bdb;
			return this;
		}

		public BIRBuilder withSb(byte[] sb) {
			this.sb = sb == null ? new byte[0] : sb;
			return this;
		}

		public BIRBuilder withSbInfo(SBInfo sbInfo) {
			this.sbInfo = sbInfo;
			return this;
		}

		public BIR build() {
			return new BIR(this);
		}

	}

}
