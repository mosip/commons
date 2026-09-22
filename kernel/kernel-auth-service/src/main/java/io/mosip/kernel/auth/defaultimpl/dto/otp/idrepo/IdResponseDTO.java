package io.mosip.kernel.auth.defaultimpl.dto.otp.idrepo;

import io.mosip.kernel.core.http.ResponseWrapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MOSIP {@link ResponseWrapper} around an ID Repository {@link ResponseDTO}
 * returned from UIN identity lookup during OTP flows.
 *
 * @author Manoj SP
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IdResponseDTO extends ResponseWrapper<ResponseDTO> {

}
