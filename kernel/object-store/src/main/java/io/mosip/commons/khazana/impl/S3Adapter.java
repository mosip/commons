package io.mosip.commons.khazana.impl;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Qualifier("S3Adapter")
public class S3Adapter implements ObjectStoreAdapter {

    private AmazonS3 connection = null;

    @Override
    public InputStream getObject(String account, String container, String objectName) {
        S3Object s3Object = getConnection().getObject(container, objectName);
        return s3Object == null ? null : s3Object.getObjectContent();
    }

    @Override
    public boolean exists(String account, String container, String objectName) {
        return false;
    }

    @Override
    public boolean putObject(String account, final String container, String objectName, InputStream data) {
        Optional<Bucket> optionalBucket = getConnection().listBuckets().stream().filter(b -> b.getName().equalsIgnoreCase(container)).findAny();
        Bucket bucket = !optionalBucket.isPresent() ? getConnection().createBucket(container) : optionalBucket.get();

        getConnection().putObject(bucket.getName(), objectName, data, null);
        return true;
    }

    @Override
    public Map<String, Object> addObjectMetaData(String account, String container, String objectName, Map<String, Object> metadata) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        metadata.entrySet().stream().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue() != null ? m.getValue().toString() : null));

        try {
            S3Object s3Object = getConnection().getObject(container, objectName);
            if (s3Object.getObjectMetadata() != null && s3Object.getObjectMetadata().getUserMetadata() != null)
                s3Object.getObjectMetadata().getUserMetadata().entrySet().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue()));

            PutObjectRequest putObjectRequest = new PutObjectRequest(container, objectName, s3Object.getObjectContent(), objectMetadata);
            putObjectRequest.getRequestClientOptions().setReadLimit(10000000);
            getConnection().putObject(putObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
            metadata = null;
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
        ObjectMetadata objectMetadata = getConnection().getObject(container, objectName).getObjectMetadata();
        if (objectMetadata != null && objectMetadata.getUserMetadata() != null)
            objectMetadata.getUserMetadata().entrySet().forEach(entry -> metaData.put(entry.getKey(), entry.getValue()));

        return metaData;
    }

    private AmazonS3 getConnection() {
        if (connection == null) {
            AWSCredentials awsCredentials = new BasicAWSCredentials("minio", "minio123");
            connection = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("http://52.172.53.239:9000", null)).build();
        }
        return connection;
    }
}
