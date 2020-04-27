## kernel-keymanager-service

[Background & Design](../../docs/design/kernel/kernel-keymanager.md)

[Api Documentation](https://github.com/mosip/mosip/wiki/Kernel-APIs#key-manager)

Default Port and Context Path

```
server.port=8088
server.servlet.path=/keymanager

```

localhost:8088/keymanager/swagger-ui.html


**Application Properties**

[application-dev.properties](../../config/application-dev.properties)

[kernel-keymanager-service-dev.properties](../../config/kernel-keymanager-service-dev.properties)


```
#mosip.kernel.keymanager.softhsm.config-path=/pathto/softhsm.conf
mosip.kernel.keymanager.softhsm.config-path=D\:\\SoftHSM2\\etc\\softhsm2-demo.conf
mosip.kernel.keymanager.softhsm.keystore-type=PKCS11
mosip.kernel.keymanager.softhsm.keystore-pass=pwd

mosip.kernel.keymanager.softhsm.certificate.common-name=www.mosip.io
mosip.kernel.keymanager.softhsm.certificate.organizational-unit=MOSIP
mosip.kernel.keymanager.softhsm.certificate.organization=IITB
mosip.kernel.keymanager.softhsm.certificate.country=IN

mosip.kernel.keygenerator.asymmetric-algorithm-name=RSA
mosip.kernel.keygenerator.asymmetric-key-length=2048
mosip.kernel.keygenerator.symmetric-algorithm-name=AES
mosip.kernel.keygenerator.symmetric-key-length=256

mosip.kernel.crypto.asymmetric-algorithm-name=RSA
mosip.kernel.crypto.symmetric-algorithm-name=AES

mosip.kernel.data-key-splitter=#KEY_SPLITTER#

# DB Properties For Development
--------------------------------------
javax.persistence.jdbc.driver=org.postgresql.Driver
javax.persistence.jdbc.url = jdbc:postgresql://localhost:8888/mosip_kernel
javax.persistence.jdbc.password = dbpwd
javax.persistence.jdbc.user = dbuser

hibernate.hbm2ddl.auto=update
hibernate.dialect=org.hibernate.dialect.PostgreSQL95Dialect
hibernate.jdbc.lob.non_contextual_creation=true
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.connection.charSet=utf8
hibernate.cache.use_second_level_cache=false
hibernate.cache.use_query_cache=false
hibernate.cache.use_structured_entries=false
hibernate.generate_statistics=false
hibernate.current_session_context_class=org.springframework.orm.hibernate5.SpringSessionContext

```

**The inputs which have to be provided are:**
1. Encrypted Key provided to decrypt should be encoded to BASE64.
2. Decrypted Key received after decrypt should be decoded from BASE64 encoding.

**Usage Sample**


  *Get Public Key*
  
  *Request*
  
  ```
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
.url("http://localhost:8088/keymanager/v1.0/publickey/REGISTRATION?referenceId=ref1&timeStamp=2018-12-11T06%3A12%3A52.994Z")
  .get()
  .build();

Response response = client.newCall(request).execute();
  ```
  
  *Response*
  
  Status:200
  
  ```
 {
    "publicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAzaFwykABfN683Mp5SNpBQU2_tIRKILIDBReeuTWQuS-6B8Z7kQmQ0cv2fG8fr8XTx7avyY3su25YFfNuIliBmdC3ZKqWVvsL9EpTCCQolcKo9a0351ieKxe_wCg5DIRLS1CciyK_cr2IqcUwh_Y3zkkZs0cF2R945vA_7RMTUth1_9zdobrxYMrMsIf2L1431vLP0-mUuAonQ9GU34L-SyAP1uscWcbk6Xj_EdZRvqrj2aOXrHy0FbQltrwNuTyX0-ZLBwMH7U50Nrh4BeQBA1ioeFKmdzSEY95Fs2jJGmxDUK77dsHw77jmg125HlEuu-NwIvDlcwCFuGQheUQFvwIDAQAB",
    "issuedAt": "2018-12-11T06:12:52.994",
    "expiryAt": "2019-12-11T06:12:52.994"
}
  ```
 
  *Decrypt Symmetric Key*
  
  *Request*
  
  ```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");

RequestBody body = RequestBody.create(mediaType, "{\n  \"applicationId\": \"REGISTRATION\",\n  \"encryptedSymmetricKey\": \"NuIMhUHds-5SlmcVWob1Kg2PA7mVRbzYLrXwb24JGX767CKdTC67wVYM3wGz9_8vmuNk-Yh_SExT6uJJHZyuY3q7pZ-BbBy-ZRWTEJqxXmnF9EWWADDQCQMQajtU-fyszBzQeIjM6gRcvwjXAuq48bC6LsEEL-9Zm6Cu6iL5oHbE77tCENrvcvdWlXY5SQx8p_w6XFlEoU_0f1ZWqjDYlW5iYHBz4XJsgrJjx7nhywOvqvJkOJZeCXSmbbvHCC6o8nIvzdF0Vd-2S2bGTKlICoLIsj9EUGKFgNLM8chI0QqPILFw2BQQfI3AQMsM2Rc04AoMRT_VYFU5Acs_fuHn3g\",\n  \"referenceId\": \"ref123\",\n  \"timeStamp\": \"2018-12-07T12:07:44.403Z\"\n}");

Request request = new Request.Builder()
  .url("http://localhost:8088/keymanager/v1.0/symmetrickey")
  .post(body)
  .addHeader("content-type", "application/json")
  .build();

Response response = client.newCall(request).execute();
  ```
  
  *Response*
  
  Status:200
  
  ```
 {
    "symmetricKey": "sq9oJCdwV-mHEdxEXRh91WkQcGJ6Q83quNaP9OZa_p0"
 }
  ```
  *Sign pdf*
  
  *Request*
  
  ```
final String DEST = DEST;
final String SRC = SRC;
File outFile = new File(DEST);
File inFile = new File(SRC);
RestTemplate restTemplate = new RestTemplate();
RequestWrapper<PDFSignatureRequestDto> requestWrapper = new RequestWrapper<>();
PDFSignatureRequestDto request = new PDFSignatureRequestDto(400, 400, 600, 600, "signing", 1, "password");
		request.setApplicationId("KERNEL");
		request.setReferenceId("SIGN");
		request.setData(Base64.encodeBase64String(FileUtils.readFileToByteArray(inFile)));
		request.setTimeStamp("2019-12-10T06:12:52.994Z");
		requestWrapper.setRequest(request);
		HttpHeaders headers= new HttpHeaders();
		headers.add("Cookie", Token);
HttpEntity<RequestWrapper<PDFSignatureRequestDto>> httpEntity = new HttpEntity<RequestWrapper<PDFSignatureRequestDto>>(requestWrapper, headers);
ResponseEntity<String> responseEntity =restTemplate.exchange("http://HOST:PORT/v1/keymanager/pdf/sign", HttpMethod.POST, httpEntity, String.class);
ObjectMapper mapper= new ObjectMapper();
JsonNode jsonNode= mapper.readTree(responseEntity.getBody());
SignatureResponseDto signatureResponseDto=mapper.readValue(jsonNode.get("response").toString(), SignatureResponseDto.class);
FileUtils.writeByteArrayToFile(outFile,Base64.decodeBase64(signatureResponseDto.getData()));
 ```
 
*Response*
  
Status:200
  
```
 {
    "data": "sq9oJCdwV-mHEdxEXRh91WkQcGJ6Q83quNaP9OZa_p0"
 }
```
  
 
    
  
## Setup steps:

### Linux (Docker) -- OLD Style

1. (First time only) Rename the  kernel-keymanager-softhsm Dockerfile in softhsm directory to `Dockerfile`. Build kernel-keymanager-softhsm docker image using this Dockerfile with command:

```
docker build --build-arg softhsm_pin=1234 --tag kernel-keymanager-softhsm:0.1 .
```

The pin passed to the variable `softhsm_pin` in docker build command should be same as the value of property
`mosip.kernel.keymanager.softhsm.keystore-pass` in properties file.

2. (First time only) Modify the `FROM` in kernel-keymanager-service Dockerfile with kernel-keymanager-softhsm docker image name:
```
FROM kernel-keymanager-softhsm:0.1
```

OR

2. (First time only) Push kernel-keymanager-softhsm docker image to private repository and modify the `FROM` in kernel-keymanager-service Dockerfile with kernel-keymanager-softhsm docker image URI:
```
FROM <your-repository>/kernel-keymanager-softhsm:0.1
```

3. Build kernel-keymanager-service docker image with command:

```
docker build --tag kernel-keymanager-service:1.0 .
```

4. Run docker container using command:

```
docker run -tid --ulimit memlock=-1  -p 8088:8088 -e spring_config_url_env=<spring_config_url_env> -e spring_config_label_env=<spring_config_label_env> -e active_profile_env=<active_profile_env> -v softhsm:/softhsm --name kernel-keymanager-service kernel-keymanager-service:1.0
```
#### Note:
- Remember to use docker volume using `-v softhsm:/softhsm` and do not add bind mount `(-v /softhsm:/softhsm)`.
- Keys will be stored in a docker volume named softhsm. To view information of this volume, run:

```
docker volume inspect softhsm
```

Know more about docker volume: https://docs.docker.com/storage/volumes/
- It is recommended to set ulimit for memlock (the maximum size that may be locked into memory) to unlimited using 
`--ulimit memlock=-1`. If not, the softhsm will warn with this message:

```
SoftHSM has been configured to store sensitive data in non-page RAM
(i.e. memory that is not swapped out to disk). This is the default and
most secure configuration. Your system, however, is not configured to
support this model in non-privileged accounts (i.e. user accounts).

You can check the setting on your system by running the following
command in a shell:

        ulimit -l

If this does not return "unlimited" and you plan to run SoftHSM from
non-privileged accounts then you should edit the configuration file
/etc/security/limits.conf (on most systems).

You will need to add the following lines to this file:

#<domain>       <type>          <item>          <value>
*               -               memlock         unlimited

Alternatively, you can elect to disable this feature of SoftHSM by
re-running configure with the option "--disable-non-paged-memory".
Please be advised that this may seriously degrade the security of
SoftHSM.
```

### Linux (Docker) -- New Style
With the new docker file the key manager is an independent of the type of HSM. The HSM is abstracted out of this layer using PKCS11 library and dynamic installation of client libraries for the HSM.

However the key manager needs the hsm client (vendor specific) to interact with the various models of HSM. The Docker file is structured to download and install this client from the artifactory url $artifactory_url_env/artifactory/libs-release-local/hsm/client.zip. 

In our environment we will use a network based HSM from The HSM is run in network mode baed on the https://hub.docker.com/repository/docker/mosipdev/softhsm. The source code of this project is part of the mosip-mock-services projects.

So in order to connect the key manager with the softhsm the following has to be done.

1. Load the client.zip file from https://github.com/mosip/mosip-mock-services/softhsm to the artifactory in the path /artifactory/libs-release-local/hsm/client.zip
1. Run the keymanager docker using the following command or its equivalent yml
    docker run -e artifactory_url_env="url pointing to the artifactory" -e PKCS11_PROXY_SOCKET="tcp://servicenameofsofthsm:5666" kernel-keymanager-service:<version>


### Windows

1. Download softhsm portable zip archive from https://github.com/disig/SoftHSM2-for-Windows#download
2. Extract it to any location, e.g `D:\SoftHSM2`. SoftHSM2 searches for its configuration file in the following locations:
```
  1. Path specified by SOFTHSM2_CONF environment variable
  2. User specific path %HOMEDRIVE%%HOMEPATH%\softhsm2.conf
  3. File softhsm2.conf in the current working directory
```
3. Modify following in environment variables:
```
> set SOFTHSM2_CONF=D:\SoftHSM2\etc\softhsm2.conf
> set PATH=%PATH%;D:\SoftHSM2\lib\
```
4. Create another conf file at `D:\SoftHSM2\etc\softhsm-application.conf` with below content
```
# Sun PKCS#11 provider configuration file for SoftHSMv2
name = SoftHSM2
library = D:\SoftHSM2\lib\softhsm2-x64.dll 
slotListIndex = 0
```
5. Install JCE With an Unlimited Strength Jurisdiction Policy as shown here:
https://dzone.com/articles/install-java-cryptography-extension-jce-unlimited
6. Go to `D:\SoftHSM2\bin` and run below command:
```
> softhsm2-util.exe --init-token --slot 0 --label "My token 1"
```
Check token is initialized in slot with below command:
```
> softhsm2-util.exe --show-slots
```
The output should be like below:
```
Slot 569035518
    Slot info:
        Description:      SoftHSM slot ID 0x21eacafe
        Manufacturer ID:  SoftHSM project
        Hardware version: 2.4
        Firmware version: 2.4
        Token present:    yes
    Token info:
        Manufacturer ID:  SoftHSM project
        Model:            SoftHSM v2
        Hardware version: 2.4
        Firmware version: 2.4
        Serial number:    b1ee933e21eacafe
        Initialized:      yes
        User PIN init.:   yes
        Label:            My token 1
Slot 1
    Slot info:
        Description:      SoftHSM slot ID 0x1
        Manufacturer ID:  SoftHSM project
        Hardware version: 2.4
        Firmware version: 2.4
        Token present:    yes
    Token info:
        Manufacturer ID:  SoftHSM project
        Model:            SoftHSM v2
        Hardware version: 2.4
        Firmware version: 2.4
        Serial number:
        Initialized:      no
        User PIN init.:   no
        Label:
```
5. Put the newly created conf filepath `D:\SoftHSM2\etc\softhsm-application.conf` in `mosip.kernel.keymanager.softhsm.config-path` property. Softhsm is ready to be used. 

For more information, check https://github.com/opendnssec/SoftHSMv2








