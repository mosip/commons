package io.mosip.kernel.keymigrate.dto;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Zero Knowledge Migrate Keys Response DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.6
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing response for Zero Knowledge Keys Migration.")
public class ZKKeyMigrateResponseDto {

     /**
	 * List of status message for ZK keys to be migrated.
	 */
	@ApiModelProperty(notes = "ZK migration response List.", required = true)
	List<ZKKeyResponseDto> zkEncryptedDataList;
}
