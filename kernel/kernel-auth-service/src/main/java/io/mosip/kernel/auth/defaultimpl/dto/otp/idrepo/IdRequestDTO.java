package io.mosip.kernel.auth.defaultimpl.dto.otp.idrepo;

import io.mosip.kernel.core.http.RequestWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MOSIP {@link RequestWrapper} around an ID Repository {@link RequestDTO}
 * used when calling ID repo for UIN-based identity lookup during OTP flows.
 *
 * @author Manoj SP
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdRequestDTO extends RequestWrapper<RequestDTO> {

}
