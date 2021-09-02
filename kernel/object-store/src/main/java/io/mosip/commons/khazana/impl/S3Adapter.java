package io.mosip.commons.khazana.impl;


import static io.mosip.commons.khazana.config.LoggerConfiguration.REGISTRATIONID;
import static io.mosip.commons.khazana.config.LoggerConfiguration.SESSIONID;
import static io.mosip.commons.khazana.constant.KhazanaErrorCodes.OBJECT_STORE_NOT_ACCESSIBLE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import io.mosip.commons.khazana.config.LoggerConfiguration;
import io.mosip.commons.khazana.exception.ObjectStoreAdapterException;
import io.mosip.commons.khazana.spi.ObjectStoreAdapter;
import io.mosip.commons.khazana.util.ObjectStoreUtil;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;

@Service
@Qualifier("S3Adapter")
public class S3Adapter implements ObjectStoreAdapter {

    private final Logger LOGGER = LoggerConfiguration.logConfig(S3Adapter.class);

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

    @Value("${object.store.connection.max.retry:20}")
    private int maxRetry;

    @Value("${object.store.max.connection:200}")
    private int maxConnection;
    
    @Value("${object.store.s3.use.account.as.bucketname:false}")
    private boolean useAccountAsBucketname;
    
    public static String TAGS_FILENAME="Tags";

    private int retry = 0;

    private AmazonS3 connection = null;

    @Override
    public InputStream getObject(String account, String container, String source, String process, String objectName) {
    	 String finalObjectName=null;
    	 String bucketName=null;
    	if(useAccountAsBucketname) {
    		 finalObjectName = ObjectStoreUtil.getName(container,source, process, objectName);
    		 bucketName=account;
    	}else {
    		 finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
    		 bucketName=container;
    	}
        S3Object s3Object = null;
        try {
            s3Object = getConnection(bucketName).getObject(bucketName, finalObjectName);
            if (s3Object != null) {
                ByteArrayOutputStream temp = new ByteArrayOutputStream();
                IOUtils.copy(s3Object.getObjectContent(), temp);
                ByteArrayInputStream bis = new ByteArrayInputStream(temp.toByteArray());
                return bis;
            }
        } catch (Exception e) {
            LOGGER.error(SESSIONID, REGISTRATIONID, "Exception occured to getObject for : " + container, ExceptionUtils.getStackTrace(e));
            throw new ObjectStoreAdapterException(OBJECT_STORE_NOT_ACCESSIBLE.getErrorCode(), OBJECT_STORE_NOT_ACCESSIBLE.getErrorMessage(), e);
        } finally {
            if (s3Object != null) {
                try {
                    s3Object.close();
                } catch (IOException e) {
                    LOGGER.error(SESSIONID, REGISTRATIONID, "IO occured : " + container, ExceptionUtils.getStackTrace(e));
                }
            }
        }
        return null;
    }

    @Override
    public boolean exists(String account, String container, String source, String process, String objectName) {
    	
    	 String finalObjectName=null;
    	 String bucketName=null;
    	if(useAccountAsBucketname) {
    		 finalObjectName = ObjectStoreUtil.getName(container,source, process, objectName);
    		 bucketName=account;
    	}else {
    		 finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
    		 bucketName=container;
    	}
        return getConnection(bucketName).doesObjectExist(bucketName, finalObjectName);
    }

    @Override
    public boolean putObject(String account, final String container, String source, String process, String objectName, InputStream data) {
    	 String finalObjectName=null;
    	 String bucketName=null;
    	if(useAccountAsBucketname) {
    		 finalObjectName = ObjectStoreUtil.getName(container,source, process, objectName);
    		 bucketName=account;
    	}else {
    		 finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
    		 bucketName=container;
    	}
    	AmazonS3 connection = getConnection(bucketName);
        if (!connection.doesBucketExistV2(bucketName))
            connection.createBucket(bucketName);

        connection.putObject(bucketName, finalObjectName, data, null);
        return true;
    }

    @Override
    public Map<String, Object> addObjectMetaData(String account, String container, String source, String process,
                                                 String objectName, Map<String, Object> metadata) {
        S3Object s3Object = null;
        try {
        	 String finalObjectName=null;
        	 String bucketName=null;
        	if(useAccountAsBucketname) {
        		 finalObjectName = ObjectStoreUtil.getName(container,source, process, objectName);
        		 bucketName=account;
        	}else {
        		 finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
        		 bucketName=container;
        	}
            ObjectMetadata objectMetadata = new ObjectMetadata();
            //changed usermetadata getting  overrided
            //metadata.entrySet().stream().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue() != null ? m.getValue().toString() : null));
          
            s3Object = getConnection(bucketName).getObject(bucketName, finalObjectName);
            if (s3Object.getObjectMetadata() != null && s3Object.getObjectMetadata().getUserMetadata() != null)
                s3Object.getObjectMetadata().getUserMetadata().entrySet().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue()));
            metadata.entrySet().stream().forEach(m -> objectMetadata.addUserMetadata(m.getKey(), m.getValue() != null ? m.getValue().toString() : null));
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, finalObjectName, s3Object.getObjectContent(), objectMetadata);
            putObjectRequest.getRequestClientOptions().setReadLimit(readlimit);
            getConnection(bucketName).putObject(putObjectRequest);
            return metadata;
        } catch (Exception e) {
            LOGGER.error(SESSIONID, REGISTRATIONID,"Exception occured to addObjectMetaData for : " + container, ExceptionUtils.getStackTrace(e));
            metadata = null;
            throw new ObjectStoreAdapterException(OBJECT_STORE_NOT_ACCESSIBLE.getErrorCode(), OBJECT_STORE_NOT_ACCESSIBLE.getErrorMessage(), e);
        } finally {
            try {
                if (s3Object != null)
                    s3Object.close();
            } catch (IOException e) {
                LOGGER.error(SESSIONID, REGISTRATIONID,"IO occured : " + container, ExceptionUtils.getStackTrace(e));
            }
        }
    }

    @Override
    public Map<String, Object> addObjectMetaData(String account, String container, String source, String process,
                                                 String objectName, String key, String value) {
        Map<String, Object> meta = new HashMap<>();
        meta.put(key, value);
        String finalObjectName=null;
   	    
    	   if(useAccountAsBucketname) {
    		  finalObjectName = ObjectStoreUtil.getName(container,source, process, objectName);
    		
    	   }else {
    		 finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
    		}
       
        return addObjectMetaData(account, container, source, process, finalObjectName, meta);
    }

    @Override
    public Map<String, Object> getMetaData(String account, String container, String source, String process,
                                           String objectName) {
        S3Object s3Object = null;
        try {
        	 String finalObjectName=null;
        	 String bucketName=null;
        	if(useAccountAsBucketname) {
        		 finalObjectName = ObjectStoreUtil.getName(container,source, process, objectName);
        		 bucketName=account;
        	}else {
        		 finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
        		 bucketName=container;
        	}
            Map<String, Object> metaData = new HashMap<>();
         
            s3Object = getConnection(bucketName).getObject(bucketName, finalObjectName);
            ObjectMetadata objectMetadata = s3Object.getObjectMetadata();
            if (objectMetadata != null && objectMetadata.getUserMetadata() != null)
                objectMetadata.getUserMetadata().entrySet().forEach(entry -> metaData.put(entry.getKey(), entry.getValue()));
            return metaData;
        } catch (Exception e) {
            LOGGER.error(SESSIONID, REGISTRATIONID,"Exception occured to getMetaData for : " + container, ExceptionUtils.getStackTrace(e));
            throw new ObjectStoreAdapterException(OBJECT_STORE_NOT_ACCESSIBLE.getErrorCode(), OBJECT_STORE_NOT_ACCESSIBLE.getErrorMessage(), e);
        } finally {
            try {
                if (s3Object != null)
                    s3Object.close();
            } catch (IOException e) {
                LOGGER.error(SESSIONID, REGISTRATIONID,"IO occured : " + container, ExceptionUtils.getStackTrace(e));
            }
        }
    }

    @Override
    public Integer incMetadata(String account, String container, String source, String process, String objectName, String metaDataKey) {
        Map<String, Object> metadata = getMetaData(account, container, source, process, objectName);
        if (metadata.get(metaDataKey) != null) {
            metadata.put(metaDataKey, Integer.valueOf(metadata.get(metaDataKey).toString()) + 1);
            addObjectMetaData(account, container, source, process, objectName, metadata);
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
    	
    	String finalObjectName=null;
   	    String bucketName=null;
   	   if(useAccountAsBucketname) {
   		 finalObjectName = ObjectStoreUtil.getName(container,source, process, objectName);
   		 bucketName=account;
   	   }else {
   		 finalObjectName = ObjectStoreUtil.getName(source, process, objectName);
   	  	 bucketName=container;
   	   }
        
        getConnection(bucketName).deleteObject(bucketName, finalObjectName);
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

    private AmazonS3 getConnection(String bucketName) {
        try {
            if (connection != null) {
                // test connection once before returning it
                connection.doesBucketExistV2(bucketName);
                return connection;
            }
        } catch (Exception e) {
            LOGGER.error(SESSIONID, REGISTRATIONID,"Exception occured while using existing connection for " + bucketName +". Will try to create new. Retry count : " + retry, ExceptionUtils.getStackTrace(e));
        }
        try {
            AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            connection = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .enablePathStyleAccess().withClientConfiguration(new ClientConfiguration().withMaxConnections(maxConnection)
                            .withMaxErrorRetry(maxRetry))
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(url, region)).build();
            // test connection once before returning it
            connection.doesBucketExistV2(bucketName);
            retry = 0;
            return connection;

        } catch (Exception e) {
            if (retry >= maxRetry) {
                LOGGER.error(SESSIONID, REGISTRATIONID,"Maximum retry limit exceeded. Could not obtain connection for "+ bucketName +". Retry count :" + retry, ExceptionUtils.getStackTrace(e));
                throw new ObjectStoreAdapterException(OBJECT_STORE_NOT_ACCESSIBLE.getErrorCode(), OBJECT_STORE_NOT_ACCESSIBLE.getErrorMessage(), e);
            } else {
                retry = retry + 1;
                LOGGER.error(SESSIONID, REGISTRATIONID,"Exception occured while obtaining connection for "+ bucketName +". Will try again. Retry count : " + retry, ExceptionUtils.getStackTrace(e));
                getConnection(bucketName);
            }
        }
        return null;
    }


}
