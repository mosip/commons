package io.mosip.kernel.packetmanager.dto;



import io.mosip.kernel.core.http.ResponseWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 
 * @author Sowmya
 *
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CryptomanagerResponseDto extends ResponseWrapper<DecryptResponseDto> {

}
