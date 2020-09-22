package io.mosip.kernel.keymanagerservice.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Mahammed Taheer
 *
 */

@Entity
@Table(name = "partner_cert_store")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class PartnerCertificateStore extends BaseEntity {
    
    /**
	 * The field cert_id
	 */
	@Id
	@Column(name = "cert_id", nullable = false, length = 36)
    private String certId;

    /**
	 * The field cert_id
	 */
    @Column(name = "cert_subject", nullable = false)
    private String certSubject;

    /**
	 * The field cert_issuer
	 */
    @Column(name = "cert_issuer", nullable = false)
    private String certIssuer;

    /**
	 * The field issuer_id
	 */
    @Column(name = "issuer_id", nullable = false)
    private String issuerId;

    /**
	 * The field cert_not_nefore
	 */
    @Column(name = "cert_not_before", nullable = false)
    private LocalDateTime certNotBefore;

    /**
	 * The field cert_not_after
	 */
    @Column(name = "cert_not_after", nullable = false)
    private LocalDateTime certNotAfter;

    /**
	 * The field partner_domain
	 */
    @Column(name = "partner_domain", nullable = false)
    private String partnerDomain;

    /**
	 * The field cert_data
	 */
    @Column(name = "cert_data", nullable = false)
    private String certData;

    /**
	 * The field key_usage
	 */
    @Column(name = "key_usage")
    private String keyUsage;

    /**
	 * The field organization_name
	 */
    @Column(name = "organization_name")
    private String organizationName;

    /**
	 * The field cert_thumbprint
	 */
    @Column(name = "cert_thumbprint")
    private String certThumbprint;

    /**
	 * The field cert_serial_no
	 */
    @Column(name = "cert_serial_no")
    private String certSerialNo;

    /**
	 * The field cert_serial_no
	 */
    @Column(name = "signed_cert_data")
    private String signedCertData;
    
}