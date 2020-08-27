package io.mosip.kernel.auth.defaultimpl.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Roles {
	@JsonInclude(value = Include.NON_NULL)
	private String id;

	@JsonInclude(value = Include.NON_NULL)
	private String name;
}
