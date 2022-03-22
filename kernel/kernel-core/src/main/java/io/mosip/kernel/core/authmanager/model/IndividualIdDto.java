package io.mosip.kernel.core.authmanager.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * (non-Javadoc)
 *
 * @see java.lang.Object#toString()
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndividualIdDto {

    /** The virtual ID. */
    private String individualId;
}
