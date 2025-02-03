package io.mosip.kernel.pdfgenerator.util;

import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;

public class SignatureHandler implements SignatureInterface {

    private final PrivateKey privateKey;
    private final Certificate[] certificateChain;
    private final Provider provider;

    public static final String RS256_ALGORITHM = "SHA256withRSA";

    public static final String EC256_ALGORITHM = "SHA256withECDSA";

    public static final String RSA_SIGN_KEY_ALGORITHM = "RSA";

    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureHandler.class);


    public SignatureHandler(PrivateKey privateKey, Certificate[] certificateChain, Provider provider) {
        this.privateKey = privateKey;
        this.certificateChain = certificateChain;
        this.provider = provider;
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
            String signAlgorithm;
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

            String pkAlgorithm = privateKey.getAlgorithm();
            if (pkAlgorithm.equalsIgnoreCase(RSA_SIGN_KEY_ALGORITHM)) {
                signAlgorithm = RS256_ALGORITHM;
            } else {
                signAlgorithm = EC256_ALGORITHM;
            }

            ContentSigner signer = new JcaContentSignerBuilder(signAlgorithm)
                    .setProvider(provider)
                    .build(privateKey);
            generator.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder().build())
                    .build(signer, (java.security.cert.X509Certificate) certificateChain[0]));

            generator.addCertificates(new JcaCertStore(Arrays.asList(certificateChain)));

            CMSTypedData data = new CMSProcessableByteArray(content.readAllBytes());
            CMSSignedData signedData = generator.generate(data, false);

            return signedData.getEncoded();
        } catch (CMSException | OperatorCreationException | CertificateException | IOException e) {
            LOGGER.error("Error while signing the content",e);
            throw new IOException("Error while signing the content",e);
        }
    }
}
