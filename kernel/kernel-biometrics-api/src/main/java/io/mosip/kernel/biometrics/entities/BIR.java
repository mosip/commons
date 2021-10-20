package io.mosip.kernel.biometrics.entities;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;


/**
 * 
 * BIR class with Builder to create data
 * 
 * @author Ramadurai Pandian
 *
 */
@Data
@JsonDeserialize(builder = BIR.BIRBuilder.class)
public class BIR implements Serializable {

	private VersionType version;
	private VersionType cbeffversion;
	private BIRInfo birInfo;
	private BDBInfo bdbInfo;
	private byte[] bdb;
	private byte[] sb;
	private SBInfo sbInfo;
	private Map<String, Object> others;

	public BIR(BIRBuilder birBuilder) {
		this.version = birBuilder.version;
		this.cbeffversion = birBuilder.cbeffversion;
		this.birInfo = birBuilder.birInfo;
		this.bdbInfo = birBuilder.bdbInfo;
		this.bdb = birBuilder.bdb;
		this.sb = birBuilder.sb;
		this.sbInfo = birBuilder.sbInfo;
		this.setOthers(birBuilder.others);
	}

	public static class BIRBuilder {
		private VersionType version;
		private VersionType cbeffversion;
		private BIRInfo birInfo;
		private BDBInfo bdbInfo;
		private byte[] bdb;
		private byte[] sb;
		private SBInfo sbInfo;
		private Map<String, Object> others;

		public BIRBuilder withOther(String key, Object value) {
			if(Objects.isNull(others))
				this.others = new HashMap<>();
			
			this.others.put(key, value);
			return this;
		}

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
			this.sb = sb;
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
