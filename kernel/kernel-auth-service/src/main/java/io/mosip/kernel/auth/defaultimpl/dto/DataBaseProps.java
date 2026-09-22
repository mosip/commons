/**
 * 
 */
package io.mosip.kernel.auth.defaultimpl.dto;

import lombok.Data;

/**
 * Leftover JDBC connection properties used when an application-specific
 * datasource was configured (URL, port, credentials, schema list, and driver).
 *
 * @author Ramadurai Pandian
 *
 */
@Data
public class DataBaseProps {

	/**
	 * JDBC URL host or full connection URL fragment.
	 */
	private String url;

	/**
	 * Database listener port.
	 */
	private String port;

	/**
	 * Database login user name.
	 */
	private String username;

	/**
	 * Database login password.
	 */
	private String password;

	/**
	 * Schema name or comma-separated schema list.
	 */
	private String schemas;

	/**
	 * Fully qualified JDBC driver class name.
	 */
	private String driverName;
}
