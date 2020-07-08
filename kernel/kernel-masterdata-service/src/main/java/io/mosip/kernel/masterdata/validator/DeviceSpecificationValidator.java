package io.mosip.kernel.masterdata.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.util.EmptyCheckUtils;
import io.mosip.kernel.masterdata.constant.DeviceSpecificationErrorCode;
import io.mosip.kernel.masterdata.dto.DeviceSpecificationDto;
import io.mosip.kernel.masterdata.entity.DeviceType;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.DeviceTypeRepository;

/**
 * The Class DeviceSpecificationValidator.
 */
@Component
public class DeviceSpecificationValidator {

	/** The device type repository. */
	@Autowired
	DeviceTypeRepository deviceTypeRepository;

	/**
	 * Validate.
	 *
	 * @param request the request
	 */
	public void validate(DeviceSpecificationDto request) {

		DeviceType entity = deviceTypeRepository
				.findDeviceTypeByCodeAndByLangCode(request.getDeviceTypeCode(), request.getLangCode());
		if (EmptyCheckUtils.isNullEmpty(entity)) {
			throw new RequestException(DeviceSpecificationErrorCode.INVALID_DEVICE_TYPE_CODE__EXCEPTION.getErrorCode(),
					DeviceSpecificationErrorCode.INVALID_DEVICE_TYPE_CODE__EXCEPTION.getErrorMessage());
		}
	}
}
