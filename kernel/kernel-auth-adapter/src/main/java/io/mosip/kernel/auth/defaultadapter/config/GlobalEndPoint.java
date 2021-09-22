package io.mosip.kernel.auth.defaultadapter.config;

import java.util.List;

import lombok.Data;


/**
 * @author GOVINDARAJ VELU
 * It is used to store all the common end-points to access without authentication
 *
 */
@Data
public class GlobalEndPoint {
	List<String> endPoints;
}
