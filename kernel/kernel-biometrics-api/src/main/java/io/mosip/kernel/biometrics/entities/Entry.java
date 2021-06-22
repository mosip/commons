package io.mosip.kernel.biometrics.entities;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Entry", propOrder = { "key", "value" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonDeserialize(builder = Entry.EntryBuilder.class)
public class Entry implements Serializable {

    @XmlElement(name = "Key")
    protected String key;
    @XmlElement(name = "Value")
    protected String value;

    public Entry(EntryBuilder entryBuilder) {
		this.key = entryBuilder.key;
        this.value = entryBuilder.value;
	}

	public static class EntryBuilder {
        private String key;
        private String value;

		public EntryBuilder withKey(String key) {
			this.key = key;
			return this;
		}

        public EntryBuilder withValue(String value) {
			this.value = value;
			return this;
		}

		public Entry build() {
			return new Entry(this);
		}
	}
}
