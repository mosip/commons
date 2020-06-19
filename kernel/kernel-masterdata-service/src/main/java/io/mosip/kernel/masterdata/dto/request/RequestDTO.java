package io.mosip.kernel.masterdata.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.mosip.kernel.masterdata.dto.KeyValues;
import lombok.Data;

/**
 * 
 * @author Bal Vikash Sharma
 *
 */
@Data
public class RequestDTO {
	@NotNull
	private List<KeyValues<String, Object>> attributes;
}