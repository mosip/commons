package io.mosip.kernel.packetmanager.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class TokenRequestDTO<T> {
	public String id;
	public Metadata metadata;
	public T request;
	public String requesttime;
	public String version;
}
