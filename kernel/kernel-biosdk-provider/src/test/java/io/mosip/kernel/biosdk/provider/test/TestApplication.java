package io.mosip.kernel.biosdk.provider.test;

import io.mosip.kernel.biometrics.constant.BiometricFunction;
import io.mosip.kernel.biometrics.constant.BiometricType;
import io.mosip.kernel.biosdk.provider.factory.BioAPIFactory;
import io.mosip.kernel.biosdk.provider.impl.BioProviderImpl_V_0_9;
import io.mosip.kernel.biosdk.provider.spi.iBioProviderApi;
import io.mosip.kernel.core.bioapi.exception.BiometricException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@EnableAutoConfiguration
@PropertySource(value = { "application-test.properties" })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { BioAPIFactory.class, BioProviderImpl_V_0_9.class })
@ComponentScan(basePackages = { "io.mosip.kernel.biosdk.provider.factory", "io.mosip.kernel.biosdk.provider.impl" })
public class TestApplication {

    @Autowired
    private BioProviderImpl_V_0_9 providerImpl_v_0_9;

    @Autowired
    private BioAPIFactory bioAPIFactory;

    @Test
    public void testGetFaceMatchFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.FACE, BiometricFunction.MATCH);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetFingerMatchFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.FINGER, BiometricFunction.MATCH);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetIrisMatchFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.IRIS, BiometricFunction.MATCH);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetFaceExtractFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.FACE, BiometricFunction.EXTRACT);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetFingerExtractFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.FINGER, BiometricFunction.EXTRACT);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetIrisExtractFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.IRIS, BiometricFunction.EXTRACT);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetFaceQCFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.FACE, BiometricFunction.QUALITY_CHECK);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetFingerQCFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.FINGER, BiometricFunction.QUALITY_CHECK);
        Assert.assertNotNull(providerApi);
    }

    @Test
    public void testGetIrisQCFunction() throws BiometricException {
        iBioProviderApi providerApi = bioAPIFactory.getBioProvider(BiometricType.IRIS, BiometricFunction.QUALITY_CHECK);
        Assert.assertNotNull(providerApi);
    }
}
