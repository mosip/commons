package io.mosip.kernel.auth.defaultadapter.config;

import java.util.List;

import lombok.Data;

/**
 * @author GOVINDARAJ VELU
 * It is used to store the admin master end-points to access without authentication
 */
@Data
public class AdminMasterEndPoint {
	List<String> endPoints;
}
