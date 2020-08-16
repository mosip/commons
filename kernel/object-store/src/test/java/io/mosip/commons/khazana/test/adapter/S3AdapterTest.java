/*
package io.mosip.commons.khazana.test.adapter;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import io.findify.s3mock.S3Mock;
import io.mosip.commons.khazana.impl.S3Adapter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class S3AdapterTest {

    private static S3Mock api;

    private AmazonS3 client;

    private static final String account = "acc";
    private static final String container = "reg123";
    private static final String objectName = "927479538402.zip";

    private S3Adapter s3Adapter;

    @Autowired
    private Environment env = mock(Environment.class);

    private static final String FAILURE_ENROLMENT_ID = "1234";


    @BeforeClass
    public static void init() throws IOException {
        api = new S3Mock.Builder().withPort(6001).withInMemoryBackend().build();
        api.start();
    }

    @Before
    public void setup() throws IOException {

        AwsClientBuilder.EndpointConfiguration endpoint = new AwsClientBuilder.EndpointConfiguration("http://localhost:6001", "us-west-2");
        client = AmazonS3ClientBuilder.standard().withPathStyleAccessEnabled(true).withEndpointConfiguration(endpoint)
                .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials())).build();

        // Putting a file to mocked ceph instance

        ClassLoader classLoader = getClass().getClassLoader();
        String filePath = classLoader.getResource(objectName).getFile();
        File packet = new File(filePath);
        s3Adapter = new S3Adapter();
        s3Adapter.putObject(account, container, objectName, new FileInputStream(packet));
    }

    @Test
    @Ignore
    public void testUploadPacketSuccess() throws IOException {
        InputStream result = this.s3Adapter.getObject(account, container, objectName);
        assertNotNull("Result should not be null.", result);
    }
}
*/
