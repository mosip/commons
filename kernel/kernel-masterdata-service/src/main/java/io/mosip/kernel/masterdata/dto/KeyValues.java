package io.mosip.kernel.masterdata.dto;

import lombok.Data;

/**
 * @author Bal Vikash Sharma
 *
 */
@Data
public class KeyValues<K, V> {

	private K attribute;
	private V value;

}
