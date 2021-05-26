package io.mosip.kernel.keymigrate.dto;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Zero Knowledge Migrate Keys Request DTO.
 * 
 * @author Mahammed Taheer
 * @since 1.1.6
*/

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Model representing request for Zero Knowledge Keys Migration.")
public class ZKKeyMigrateRequestDto {

    /**
	 * List of ZK key to be migrated.
	*/
	@ApiModelProperty(notes = "ZK Encrypted Keys List.", required = true)
	List<ZKKeyDataDto> zkEncryptedDataList;

    /**
	 * Flag to purge the temporary generated key.
	*/
	@ApiModelProperty(notes = "Flag to purge the key.", required = true)
	Boolean purgeTempKeyFlag;
}
