package io.mosip.commons.khazana.impl;


import static io.mosip.commons.khazana.config.LoggerConfiguration.REGISTRATIONID;
import static io.mosip.commons.khazana.config.LoggerConfiguration.SESSIONID;
import static io.mosip.commons.khazana.constant.KhazanaConstant.TAGS_FILENAME;
import static io.mosip.commons.khazana.constant.KhazanaErrorCodes.OBJECT_STORE_NOT_ACCESSIBLE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
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
import com.amazonaws.services.s3.model.S3ObjectSummary;

import io.mosip.commons.khazana.config.LoggerConfiguration;
import io.mosip.commons.khazana.dto.ObjectDto;
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

    private int retry = 0;
    
    private AmazonS3 connection = null;
    
    private static final String SEPARATOR = "/";

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
        return getConnection(bucketName).doesObjectExist(bucketName,finalObjectName);
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
    public boolean pack(String account, String container, String source, String process, String refId) {
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

    public List<ObjectDto> getAllObjects(String account, String id) {

        List<S3ObjectSummary> os = null;
   	   if(useAccountAsBucketname)
           os = getConnection(account).listObjects(account, id).getObjectSummaries();
   	   else
           os = getConnection(id).listObjects(id).getObjectSummaries();

        if (os != null && os.size() > 0) {
            List<ObjectDto> objectDtos = new ArrayList<>();
            os.forEach(o -> {
				// ignore the Tag file
				String[] tempKeys = o.getKey().split("/");
				if (useAccountAsBucketname) {
					if (tempKeys[1] != null && tempKeys[1].endsWith(TAGS_FILENAME))
						tempKeys = null;
				} else {
					if (tempKeys[0] != null && tempKeys[0].endsWith(TAGS_FILENAME))
						tempKeys = null;
				}

                String[] keys = removeIdFromObjectPath(useAccountAsBucketname, tempKeys);
                if (ArrayUtils.isNotEmpty(keys)) {
                    ObjectDto objectDto = null;
                    switch (keys.length) {
                        case 1:
                            objectDto = new ObjectDto(null, null, keys[0], o.getLastModified());
                            break;
                        case 2:
                            objectDto = new ObjectDto(keys[0], null, keys[1], o.getLastModified());
                            break;
                        case 3:
                            objectDto = new ObjectDto(keys[0], keys[1], keys[2], o.getLastModified());
                            break;
                    }
                    if (objectDto != null)
                        objectDtos.add(objectDto);
                }
            });
            return objectDtos;
        }

        return null;
    }

    /**
     * If account is used as bucket name then first element of array is the packet id.
     * This method removes packet id from array so that path is same irrespective of useAccountAsBucketname is true or false
     *
     * @param useAccountAsBucketname
     * @param keys
     */
    private String[] removeIdFromObjectPath(boolean useAccountAsBucketname, String[] keys) {
        return (useAccountAsBucketname && ArrayUtils.isNotEmpty(keys)) ?
                (String[]) ArrayUtils.remove(keys, 0) : keys;
    }

	@Override
	public Map<String, String> addTags(String account, String container, Map<String, String> tags) {
		try {
	
        	 String bucketName=null;
        	 String finalObjectName=null;
        	if(useAccountAsBucketname) {
        		 bucketName=account;
        		 finalObjectName = ObjectStoreUtil.getName(container,null,TAGS_FILENAME);
        	}else {
        		 bucketName=container;
        		 finalObjectName = TAGS_FILENAME;
        	}
			AmazonS3 connection = getConnection(bucketName);
			if (!connection.doesBucketExistV2(bucketName))
	            connection.createBucket(bucketName);
			for(Entry<String, String> entry:tags.entrySet()) {
				String tagName=null;
				InputStream data=IOUtils.toInputStream(entry.getValue(), StandardCharsets.UTF_8);
				 tagName=ObjectStoreUtil.getName(finalObjectName, entry.getKey());
		        connection.putObject(bucketName, tagName, data, null);
			}
			

		} catch (Exception e) {
			LOGGER.error(SESSIONID, REGISTRATIONID, "Exception occured while addTags for : " + container,
					ExceptionUtils.getStackTrace(e));
			throw new ObjectStoreAdapterException(OBJECT_STORE_NOT_ACCESSIBLE.getErrorCode(),
					OBJECT_STORE_NOT_ACCESSIBLE.getErrorMessage(), e);
		}
		return tags;
	}

	@Override
	public Map<String, String> getTags(String account, String container) {
		Map<String, String> objectTags = new HashMap<String, String>();
		try {

		 String bucketName=null;
		 String finalObjectName=null;
			if (useAccountAsBucketname) {
     		 bucketName=account;
				finalObjectName = ObjectStoreUtil.getName(container, null, TAGS_FILENAME) + SEPARATOR;
     	}else {
     		 bucketName=container;
				finalObjectName = TAGS_FILENAME + SEPARATOR;
     	}
		AmazonS3 connection = getConnection(bucketName);
		
			List<S3ObjectSummary> objectSummary = null;
	   	   if(useAccountAsBucketname)
				objectSummary = connection.listObjects(bucketName, finalObjectName).getObjectSummaries();
	   	   else
				objectSummary = connection.listObjects(bucketName).getObjectSummaries();

	   	   List<String> tagNames=new ArrayList<String>();	   
			if (objectSummary != null && objectSummary.size() > 0) {
        
				objectSummary.forEach(o -> {
                String[] keys = o.getKey().split("/");
                if (ArrayUtils.isNotEmpty(keys)) {
						if (useAccountAsBucketname) {
							if (keys[1] != null && keys[1].endsWith(TAGS_FILENAME))
								tagNames.add(keys[2]);
						} else {
							if (keys[0] != null && keys[0].endsWith(TAGS_FILENAME))
								tagNames.add(keys[1]);
						}

                }
            });
            
        }
	   	for(String tagName:tagNames) {
	   		objectTags.put(tagName, connection.getObjectAsString(bucketName, finalObjectName+tagName));

	   	}
	   	
		return objectTags;

		}catch(Exception e){
			LOGGER.error(SESSIONID, REGISTRATIONID, "Exception occured while getTags for : " + container,
					ExceptionUtils.getStackTrace(e));
			throw new ObjectStoreAdapterException(OBJECT_STORE_NOT_ACCESSIBLE.getErrorCode(),
					OBJECT_STORE_NOT_ACCESSIBLE.getErrorMessage(), e);
		}
	
	}
	
	@Override
	public void deleteTags(String account, String container, List<String> tags) {
		try {
			 String bucketName=null;
			 String finalObjectName=null;
	     	if(useAccountAsBucketname) {
	     		 bucketName=account;
	     		 finalObjectName = ObjectStoreUtil.getName(container,null,TAGS_FILENAME);
	     	}else {
	     		 bucketName=container;
	     		 finalObjectName = TAGS_FILENAME;
	     	}
			AmazonS3 connection = getConnection(container);
			if (!connection.doesBucketExistV2(container))
	            connection.createBucket(container);
			for(String tag:tags) {
				String tagName=null;
                tagName=ObjectStoreUtil.getName(finalObjectName, tag);
				connection.deleteObject(bucketName, tagName);
			}

		} catch (Exception e) {
			LOGGER.error(SESSIONID, REGISTRATIONID, "Exception occured while deleteTags for : " + container,
					ExceptionUtils.getStackTrace(e));
			throw new ObjectStoreAdapterException(OBJECT_STORE_NOT_ACCESSIBLE.getErrorCode(),
					OBJECT_STORE_NOT_ACCESSIBLE.getErrorMessage(), e);
		}

	}

}
