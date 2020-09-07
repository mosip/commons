package io.mosip.commons.khazana.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import io.mosip.commons.khazana.spi.ObjectStoreAdapter;

@Service
@Qualifier("S3Adapter")
public class S3Adapter implements ObjectStoreAdapter {

    @Value("${object.store.s3.accesskey:accesskey:accesskey}")
    private String accessKey;

    @Value("${object.store.s3.secretkey:secretkey:secretkey}")
    private String secretKey;

    @Value("${object.store.s3.url:null}")
    private String url;

    @Value("${object.store.s3.region:null}")
    private String region;

    @Value("${object.store.s3.readlimit:10000000}")
    private int readlimit;

    private AmazonS3 connection = null;

    @Override
    public InputStream getObject(String account, String container, String objectName) {
        S3Object s3Object = getConnection(container).getObject(container, objectName);
        try {
            if (s3Object != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(IOUtils.toByteArray(s3Object.getObjectContent()));
                s3Object.close();
                return bis;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    @Override
    public boolean exists(String account, String container, String objectName) {
        return getObject(account, container, objectName) != null;
    }

    @Override
    public boolean putObject(String account, final String container, String objectName, InputStream data) {
        Optional<Bucket> optionalBucket = getConnection(container).listBuckets().stream().filter(b -> b.getName().equalsIgnoreCase(container)).findAny();
        Bucket bucket = !optionalBucket.isPresent() ? getConnection(container).createBucket(container) : optionalBucket.get();

        getConnection(container).putObject(bucket.getName(), objectName, data, null);
        return true;
    }

    @Override
    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, Map<String, Object> metadata) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        metadata.entrySet().stream().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue() != null ? m.getValue().toString() : null));
        S3Object s3Object = null;
        try {
            s3Object = getConnection(container).getObject(container, objectName);
            if (s3Object.getObjectMetadata() != null && s3Object.getObjectMetadata().getUserMetadata() != null)
                s3Object.getObjectMetadata().getUserMetadata().entrySet().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue()));

            PutObjectRequest putObjectRequest = new PutObjectRequest(container, objectName, s3Object.getObjectContent(), objectMetadata);
            putObjectRequest.getRequestClientOptions().setReadLimit(readlimit);
            getConnection(container).putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            metadata = null;
        } finally {
            try {
                if (s3Object != null)
                    s3Object.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return metadata;
    }

    @Override
    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, String key, String value) {
        Map<String, Object> meta = new HashMap<>();
        meta.put(key, value);
        return addObjectMetaData(account, container, objectName, meta);
    }

    @Override
    public Map<String, Object> getMetaData(String account, String container, String objectName) {
        Map<String, Object> metaData = new HashMap<>();
        ObjectMetadata objectMetadata = getConnection(container).getObject(container, objectName).getObjectMetadata();
        if (objectMetadata != null && objectMetadata.getUserMetadata() != null)
            objectMetadata.getUserMetadata().entrySet().forEach(entry -> metaData.put(entry.getKey(), entry.getValue()));

        return metaData;
    }

    private AmazonS3 getConnection(String container) {
        try {
            if (connection != null) {
                connection.doesBucketExistV2(container);
                return connection;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Exception occured. Will try to create new connection");
        }
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        connection = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).enablePathStyleAccess()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, region)).build();

        return connection;
    }

	@Override
	public int incMetadata(String account, String container, String objectName, String metaDataKey) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int decMetadata(String account, String container, String objectName, String metaDataKey) {
		// TODO Auto-generated method stub
		return 0;
	}
}
