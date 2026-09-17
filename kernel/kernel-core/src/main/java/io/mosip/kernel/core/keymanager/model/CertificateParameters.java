package io.mosip.kernel.core.keymanager.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * X.500 subject and validity used when generating a keystore certificate.
 * <p>
 * Contract: {@code commonName} should be non-blank. Other DN fields may be
 * null. {@code notBefore} / {@code notAfter} should be UTC. Does not perform
 * I/O.
 * </p>
 *
 * @author Mahammed Taheer
 * @since 1.1.2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificateParameters {

    /**
     * Certificate attribute CN (common name); should be non-blank.
     */
    private String commonName;

    /**
     * Certificate attribute OU (organization unit); may be null.
     */
    private String organizationUnit;

    /**
     * Certificate attribute O (organization); may be null.
     */
    private String organization;

    /**
     * Certificate attribute L (locality); may be null.
     */
    private String location;
    
    /**
     * Certificate attribute S / ST (state); may be null.
     */
    private String state;

    /**
     * Certificate attribute C (country); may be null.
     */
    private String country;

    /**
     * Certificate not-before instant; should be UTC; may be null.
     */
    private LocalDateTime notBefore;

    /**
     * Certificate not-after instant; should be UTC; may be null.
     */
    private LocalDateTime notAfter;

}
