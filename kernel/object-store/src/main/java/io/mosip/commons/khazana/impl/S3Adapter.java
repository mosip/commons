package io.mosip.commons.khazana.impl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.commons.khazana.util.ObjectStoreUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
@Qualifier("S3Adapter")
public class S3Adapter implements ObjectStoreAdapter {

    private final Logger LOGGER = LoggerFactory.getLogger(S3Adapter.class);

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

    @Value("${object.store.connection.max.retry:5}")
    private int maxRetry;

    private int retry = 0;

    private AmazonS3 connection = null;

    @Override
    public InputStream getObject(String account, String container, String source, String process, String objectName) {
        String finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
        S3Object s3Object = null;
        try {
            s3Object = getConnection(container).getObject(container, finalObjectName);
            if (s3Object != null) {
                ByteArrayInputStream bis = new ByteArrayInputStream(IOUtils.toByteArray(s3Object.getObjectContent()));
                s3Object.close();
                return bis;
            }
        } catch (Exception e) {
            LOGGER.error("Exception occured to getObject for : " + container, e);
        } finally {
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException e) {
                    LOGGER.error("IO occured : " + container, e);
                }
            }
        }
        return null;
    }

    @Override
    public boolean exists(String account, String container, String source, String process, String objectName) {
        return getObject(account, container, source, process, objectName) != null;
    }

    @Override
    public boolean putObject(String account, final String container, String source, String process, String objectName, InputStream data) {
        AmazonS3 connection = getConnection(container);
        if (!connection.doesBucketExistV2(container))
            connection.createBucket(container);

        String finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
        connection.putObject(container, finalObjectName, data, null);
        return true;
    }

    @Override
    public Map<String, Object> addObjectMetaData(String account, String container, String source, String process,
                                                 String objectName, Map<String, Object> metadata) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        metadata.entrySet().stream().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue() != null ? m.getValue().toString() : null));
        S3Object s3Object = null;
        try {
            String finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
            s3Object = getConnection(container).getObject(container, finalObjectName);
            if (s3Object.getObjectMetadata() != null && s3Object.getObjectMetadata().getUserMetadata() != null)
                s3Object.getObjectMetadata().getUserMetadata().entrySet().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue()));

            PutObjectRequest putObjectRequest = new PutObjectRequest(container, finalObjectName, s3Object.getObjectContent(), objectMetadata);
            putObjectRequest.getRequestClientOptions().setReadLimit(readlimit);
            getConnection(container).putObject(putObjectRequest);
        } catch (Exception e) {
            LOGGER.error("Exception occured to addObjectMetaData for : " + container, e);
            metadata = null;
        } finally {
            try {
                if (s3Object != null)
                    s3Object.close();
            } catch (IOException e) {
                LOGGER.error("IO occured : " + container, e);
            }
        }

        return metadata;
    }

    @Override
    public Map<String, Object> addObjectMetaData(String account, String container, String source, String process,
                                                 String objectName, String key, String value) {
        Map<String, Object> meta = new HashMap<>();
        meta.put(key, value);
        String finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
        return addObjectMetaData(account, container, source, process, finalObjectName, meta);
    }

    @Override
    public Map<String, Object> getMetaData(String account, String container, String source, String process,
                                           String objectName) {
        Map<String, Object> metaData = new HashMap<>();
        String finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
        ObjectMetadata objectMetadata = getConnection(container).getObject(container, finalObjectName).getObjectMetadata();
        if (objectMetadata != null && objectMetadata.getUserMetadata() != null)
            objectMetadata.getUserMetadata().entrySet().forEach(entry -> metaData.put(entry.getKey(), entry.getValue()));

        return metaData;
    }

    @Override
    public Integer incMetadata(String account, String container, String source, String process, String objectName, String metaDataKey) {
        Map<String, Object> metadata = getMetaData(account, container, source, process, objectName);
        if (metadata.get(metaDataKey) != null) {
            addObjectMetaData(account, container, source, process, objectName, metadata);
            metadata.put(metaDataKey, Integer.valueOf(metadata.get(metaDataKey).toString()) + 1);
            return Integer.valueOf(metadata.get(metaDataKey).toString());
        }
        return null;
    }

    @Override
    public Integer decMetadata(String account, String container, String source, String process, String objectName, String metaDataKey) {
        Map<String, Object> metadata = getMetaData(account, container, source, process, objectName);
        if (metadata.get(metaDataKey) != null) {
            metadata.put(metaDataKey, Integer.valueOf(metadata.get(metaDataKey).toString()) - 1);
            addObjectMetaData(account, container, source, process, objectName, metadata);
            return Integer.valueOf(metadata.get(metaDataKey).toString());
        }
        return null;
    }

    @Override
    public boolean deleteObject(String account, String container, String source, String process, String objectName) {
        String finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
        getConnection(container).deleteObject(container, finalObjectName);
        return true;
    }

    /**
     * Removing container not supported in S3Adapter
     *
     * @param account
     * @param container
     * @param source
     * @param process
     * @return
     */
    @Override
    public boolean removeContainer(String account, String container, String source, String process) {
        return false;
    }

    /**
     * Not Supported in S3Adapter
     *
     * @param account
     * @param container
     * @param source
     * @param process
     * @return
     */
    @Override
    public boolean pack(String account, String container, String source, String process) {
        return false;
    }

    private AmazonS3 getConnection(String container) {
        try {
            if (connection != null) {
                connection.doesBucketExistV2(container);
                retry = 0;
                return connection;
            }
        } catch (Exception e) {
            retry = retry + 1;
            LOGGER.error("Exception occured while using existing connection for " + container +". Will try to create new. Retry count : " + retry, e);
        }
        try {
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            connection = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials)).enablePathStyleAccess()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, region)).build();
            retry = 0;
            return connection;

        } catch (Exception e) {
            if (retry == maxRetry) {
                LOGGER.error("Maximum retry limit exceeded. Could not obtain connection for "+ container +". Retry count :" + retry, e);
                throw e;
            } else {
                retry = retry + 1;
                LOGGER.error("Exception occured while obtaining connection for "+ container +". Will try again. Retry count : " + retry, e);
                getConnection(container);
            }
        }
        return null;
    }


}
