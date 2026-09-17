package io.mosip.kernel.qrcode.generator.zxing.util;

import io.mosip.kernel.core.exception.NullPointerException;
import io.mosip.kernel.core.qrcodegenerator.exception.InvalidInputException;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrVersion;
import io.mosip.kernel.qrcode.generator.zxing.constant.QrcodeExceptionConstants;

/**
 * Utils class for QR code generator
 * 
 * @author Urvil Joshi
 *
 * @since 1.0.0
 */
public class QrcodegeneratorUtils {
	/**
	 * Constructor for this class
	 */
	private QrcodegeneratorUtils() {

	}

	/**
	 * Rejects {@code null} or blank {@code data} and a {@code null} {@code version}.
	 *
	 * @param data    payload to encode
	 * @param version QR version
	 * @throws NullPointerException  if {@code data} or {@code version} is {@code null}
	 * @throws InvalidInputException if {@code data} is blank
	 */
	public static void verifyInput(String data, QrVersion version) {
		if (data == null) {
			throw new NullPointerException(QrcodeExceptionConstants.INVALID_INPUT_DATA_NULL.getErrorCode(),
					QrcodeExceptionConstants.INVALID_INPUT_DATA_NULL.getErrorMessage());
		} else if (data.trim().isEmpty()) {
			throw new InvalidInputException(QrcodeExceptionConstants.INVALID_INPUT_DATA_EMPTY.getErrorCode(),
					QrcodeExceptionConstants.INVALID_INPUT_DATA_EMPTY.getErrorMessage());
		} else if (version == null) {
			throw new NullPointerException(QrcodeExceptionConstants.INVALID_INPUT_VERSION.getErrorCode(),
					QrcodeExceptionConstants.INVALID_INPUT_VERSION.getErrorMessage());
		}
	}
}
