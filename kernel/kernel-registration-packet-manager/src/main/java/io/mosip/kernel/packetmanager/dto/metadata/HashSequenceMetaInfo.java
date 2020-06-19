package io.mosip.kernel.packetmanager.dto.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Data
public class HashSequenceMetaInfo {
	
	public HashSequenceMetaInfo(String label) {
		this.label = label;
		this.value = new LinkedList<String>();
		this.hashSource = new HashMap<>();
	}
	
	private String label;
	private List<String> value;
	
	@JsonIgnore
	private Map<String, byte[]> hashSource;
	
	public void addHashSource(String key, byte[] bytes) {
		value.add(key);
		hashSource.put(key, bytes);		
	}
}
