package io.mosip.kernel.idgenerator.config;

import java.util.Map;

import lombok.Data;

/**
 * Actuator-style health check payload written by Vert.x health handlers.
 * 
 * @author Urvil joshi
 * 
 * @since 1.0.0
 *
 */
@Data
public class BaseHealthCheckModel {

	/**
	 * Final result of check (UP or DOWN)
	 */
	private String status;

	/**
	 * Details about check
	 */
	private Map<String, Object> details;

}
