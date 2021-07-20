package io.mosip.kernel.partnercertservice.helper;

import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.keymanagerservice.entity.CACertificateStore;
import io.mosip.kernel.keymanagerservice.entity.PartnerCertificateStore;
import io.mosip.kernel.keymanagerservice.logger.KeymanagerLogger;
import io.mosip.kernel.keymanagerservice.repository.CACertificateStoreRepository;
import io.mosip.kernel.keymanagerservice.repository.PartnerCertificateStoreRepository;
import io.mosip.kernel.keymanagerservice.util.KeymanagerUtil;
import io.mosip.kernel.partnercertservice.constant.PartnerCertManagerConstants;
import io.mosip.kernel.partnercertservice.util.PartnerCertificateManagerUtil;

/**
 * DB Helper class for Keymanager
 * 
 * @author Mahammed Taheer
 * @since 1.1.2
 *
 */

@Component
public class PartnerCertManagerDBHelper {
    
    private static final Logger LOGGER = KeymanagerLogger.getLogger(PartnerCertManagerDBHelper.class);

    /**
	 * {@link KeyAliasRepository} instance
	 */
	@Autowired
    CACertificateStoreRepository caCertificateStoreRepository;

    /**
	 * {@link KeyAliasRepository} instance
	 */
	@Autowired
    PartnerCertificateStoreRepository partnerCertificateStoreRepository;
    
    /**
	 * Utility to generate Metadata
	*/
	@Autowired
    KeymanagerUtil keymanagerUtil;
    
    public boolean isCertificateExist(String certThumbprint, String partnerDomain){
        CACertificateStore caCertificate = caCertificateStoreRepository
                                            .findByCertThumbprintAndPartnerDomain(certThumbprint, partnerDomain);
        if (Objects.nonNull(caCertificate)) {
            return true;
        }
        return false;
    }

    public boolean isPartnerCertificateExist(String certThumbprint, String partnerDomain){
        List<PartnerCertificateStore> partnerCertificateList = partnerCertificateStoreRepository
                                                     .findByCertThumbprintAndPartnerDomain(certThumbprint, partnerDomain);
        if (partnerCertificateList.size() > 0) {
            return true;
        }
        return false;
    }

    public void storeCACertificate(String certId, String certSubject, String certIssuer, String issuerId, 
                    X509Certificate reqX509Cert, String certThumbprint, String partnerDomain) {

        String certSerialNo = reqX509Cert.getSerialNumber().toString();
        LocalDateTime notBeforeDate = DateUtils.parseDateToLocalDateTime(reqX509Cert.getNotBefore());
        LocalDateTime notAfterDate = DateUtils.parseDateToLocalDateTime(reqX509Cert.getNotAfter());
        String certData = keymanagerUtil.getPEMFormatedData(reqX509Cert);
        CACertificateStore certStoreObj = new CACertificateStore();
        certStoreObj.setCertId(certId);
        certStoreObj.setCertSubject(certSubject);
        certStoreObj.setCertIssuer(certIssuer);
        certStoreObj.setIssuerId(issuerId);
        certStoreObj.setCertNotBefore(notBeforeDate);
        certStoreObj.setCertNotAfter(notAfterDate);
        certStoreObj.setCertData(certData);
        certStoreObj.setCertThumbprint(certThumbprint);
        certStoreObj.setCertSerialNo(certSerialNo);
        certStoreObj.setPartnerDomain(partnerDomain);
        caCertificateStoreRepository.saveAndFlush(keymanagerUtil.setMetaData(certStoreObj));
    }

    public Map<String, Set<?>> getTrustAnchors(String partnerDomain) {
        Set<TrustAnchor> rootTrust = new HashSet<>();
        Set<X509Certificate> intermediateCerts = new HashSet<>();
        caCertificateStoreRepository.findByPartnerDomain(partnerDomain).stream().forEach(
            trustCert -> {
                String certificateData = trustCert.getCertData();
                X509Certificate x509Cert = (X509Certificate) keymanagerUtil.convertToCertificate(certificateData);
                if (PartnerCertificateManagerUtil.isCertificateDatesValid(x509Cert)) {
                    if (PartnerCertificateManagerUtil.isSelfSignedCertificate(x509Cert)) {
                        rootTrust.add(new TrustAnchor(x509Cert, null));
                    } else{
                        intermediateCerts.add(x509Cert);
                    }
                }
            }
        );
        Map<String, Set<?>> hashMap = new HashMap<>();
        hashMap.put(PartnerCertManagerConstants.TRUST_ROOT, rootTrust);
        hashMap.put(PartnerCertManagerConstants.TRUST_INTER, intermediateCerts);
        return hashMap;
    }

    public String getIssuerCertId(String certIssuerDn) {
        LocalDateTime currentDateTime = DateUtils.getUTCCurrentDateTime();
        List<CACertificateStore> certificates = caCertificateStoreRepository.findByCertSubject(certIssuerDn)
                        .stream().filter(cert -> PartnerCertificateManagerUtil.isValidTimestamp(currentDateTime, cert))
                        .collect(Collectors.toList());

        if (certificates.size() == 1) {
            return certificates.get(0).getCertId();
        }
        List<CACertificateStore> sortedCerts = certificates.stream()
                                               .sorted((cert1, cert2) -> cert1.getCertNotBefore().compareTo(cert2.getCertNotBefore()))
                                               .collect(Collectors.toList());
        return sortedCerts.get(0).getCertId();
    }

    public void storePartnerCertificate(String certId, String certSubject, String certIssuer, String issuerId, 
                    X509Certificate reqX509Cert, String certThumbprint, String orgName, String partnerDomain,
                    String signedCertData) {

        String certSerialNo = reqX509Cert.getSerialNumber().toString();
        LocalDateTime notBeforeDate = DateUtils.parseDateToLocalDateTime(reqX509Cert.getNotBefore());
        LocalDateTime notAfterDate = DateUtils.parseDateToLocalDateTime(reqX509Cert.getNotAfter());
        String certData = keymanagerUtil.getPEMFormatedData(reqX509Cert);
        
        PartnerCertificateStore partnerStoreObj = new PartnerCertificateStore();
        partnerStoreObj.setCertId(certId);
        partnerStoreObj.setCertSubject(certSubject);
        partnerStoreObj.setCertIssuer(certIssuer);
        partnerStoreObj.setIssuerId(issuerId);
        partnerStoreObj.setCertNotBefore(notBeforeDate);
        partnerStoreObj.setCertNotAfter(notAfterDate);
        partnerStoreObj.setCertData(certData);
        partnerStoreObj.setCertThumbprint(certThumbprint);
        partnerStoreObj.setCertSerialNo(certSerialNo);
        partnerStoreObj.setOrganizationName(orgName);
        partnerStoreObj.setPartnerDomain(partnerDomain);
        partnerStoreObj.setKeyUsage(PartnerCertManagerConstants.EMPTY); //TODO update key usage later.
        partnerStoreObj.setSignedCertData(signedCertData);
        partnerCertificateStoreRepository.saveAndFlush(keymanagerUtil.setMetaData(partnerStoreObj));
    }

    public PartnerCertificateStore getPartnerCert(String certId) {
        return partnerCertificateStoreRepository.findByCertId(certId);
    }
}