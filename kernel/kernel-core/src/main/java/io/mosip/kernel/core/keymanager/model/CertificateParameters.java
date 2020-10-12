package io.mosip.kernel.core.keymanager.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Certificate Parameters for generating Certficate.
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateParameters {

    /**
	 * Field for Certficate Attribute - CN
	 */
    private String commonName;

    /**
	 * Field for Certficate Attribute - OU
	 */
    private String organizationUnit;

    /**
	 * Field for Certficate Attribute - O
	 */
    private String organization;

    /**
	 * Field for Certficate Attribute - L
	 */
    private String location;
    
    /**
	 * Field for Certficate Attribute - S
	 */
    private String state;

    /**
	 * Field for Certficate Attribute - C
	 */
    private String country;

    /**
	 * Field for Certficate Attribute - notBefore
	 */
    private LocalDateTime notBefore;

    /**
	 * Field for Certficate Attribute - notAfter
	 */
    private LocalDateTime notAfter;

}