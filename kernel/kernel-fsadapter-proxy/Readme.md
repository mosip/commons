## kernel-fsadapter-proxy

This adapter is created for opensource developers to easily have fsa capablities instead of installing servers like HDFS and CEPH

[Api Documentation]


```
mvn javadoc:javadoc
```

**Application Properties**

```
#Basepath to save all hadoop templates
kernel.proxy.fsa.basepath=D:\\fsa

# HDFS log level. Change this to debug to see hdfs logs
logging.level.org.apache.hadoop=warn
```

  
- To use this api, add this to dependency list:

```
<dependency>
	<groupId>io.mosip.kernel</groupId>
	<artifactId>kernel-fsadapter-proxy</artifactId>
	<version>${version}</version>
</dependency>
```


**Exceptions to be handled while using this functionality:**

1. FSAdapterException


**Usage Sample**
  
Usage1: Store Packet
 
 ```
 
 @Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.storePacket("91001984930000120", FileUtils.openInputStream(new File("D:/hdfstest/testfolder/91001984930000120.zip")));

```

Usage2: Store Packet 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.storePacket("91001984930000120", new File("D:/hdfstest/testfolder/91001984930000120.zip"));

```

Usage3: Store File
 
 ```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.storeFile("91001984930000120", "BIOMETRIC/APPLICANT/BOTH_THUMBS", FileUtils.openInputStream(new File("D:/hdfstest/testfolder/91001984930000120/Biometric/Applicant/BothThumbs.jpg")));

 ```

Usage4: Get Packet 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.getPacket("91001984930000120");

```

Usage5: Get File 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.getFile("91001984930000120", "BIOMETRIC/APPLICANT/BOTHTHUMBS");

```

Usage6: Unpack Packet 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.unpackPacket("91001984930000120");

```

Usage7: Is Packet Present 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.isPacketPresent("91001984930000120");

```

Usage8: Check File Existence 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.checkFileExistence("91001984930000120", "BIOMETRIC/APPLICANT/BOTHTHUMBS");

```

Usage9: Delete Packet 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.deletePacket("91001984930000120");

```

Usage10: Delete File 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.deleteFile("91001984930000120", "BIOMETRIC/APPLICANT/BOTHTHUMBS")

```

Usage11: Copy File 

```
@Autowired
private FileSystemAdapter proxyAdapterImpl;

proxyAdapterImpl.copyFile("91001984930000120", "BIOMETRIC/APPLICANT/BOTHTHUMBS", "202020202", "BIOMETRIC/APPLICANT/BOTHTHUMBS");

```
