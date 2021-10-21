## kernel-keymanager-service

[Background & Design KEYMANAGER](../../docs/design/kernel/kernel-keymanager.md)

[Background & Design CRYPTOMANAGER](../../docs/design/kernel/kernel-cryptomanager.md)

[Background & Design SIGNATURE](../../docs/design/kernel/kernel-cryptography-digitalsignature.md)

[Background & Design TOKENIDGENERATOR](../../docs/design/kernel/kernel-idgenerator-statictoken.md)

[Background & Design -TBA- LICENSEKEYMANAGER](../../docs/design/kernel/kernel-licensekeymanager.md)

[Api Documentation KEYMANAGER](https://github.com/mosip/mosip/wiki/Kernel-APIs#key-manager)

[Api Documentation CRYPTOMANAGER](https://github.com/mosip/mosip/wiki/Kernel-APIs#crypto-manager)

[Api Documentation SIGNATURE](https://github.com/mosip/mosip/wiki/Kernel-APIs#signature)

[Api Documentation TOKENIDGENERATOR](https://github.com/mosip/mosip/wiki/Kernel-APIs#tokenid-generator)

[Api Documentation -TBA- LICENSEKEYMANAGER](https://github.com/mosip/mosip/wiki/Kernel-APIs#licensekey-manager)


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

mosip.kernel.keymanager.softhsm.config-path=B\:\\softhsm2\\etc\\softhsm2-demo.conf
mosip.kernel.keymanager.softhsm.keystore-type=PKCS11
mosip.kernel.keymanager.softhsm.keystore-pass=1234

mosip.kernel.keymanager.softhsm.certificate.common-name=www.mosip.io
mosip.kernel.keymanager.softhsm.certificate.organizational-unit=MOSIP
mosip.kernel.keymanager.softhsm.certificate.organization=IITB
mosip.kernel.keymanager.softhsm.certificate.country=IN

#----------------------- Crypto --------------------------------------------------
#Crypto asymmetric algorithm name
mosip.kernel.crypto.asymmetric-algorithm-name=RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING
#Crypto symmetric algorithm name
mosip.kernel.crypto.symmetric-algorithm-name=AES/GCM/PKCS5Padding
#Keygenerator asymmetric algorithm name
mosip.kernel.keygenerator.asymmetric-algorithm-name=RSA
#Keygenerator symmetric algorithm name
mosip.kernel.keygenerator.symmetric-algorithm-name=AES
#Asymmetric algorithm key length
mosip.kernel.keygenerator.asymmetric-key-length=2048
#Symmetric algorithm key length
mosip.kernel.keygenerator.symmetric-key-length=256

#Encrypted data and encrypted symmetric key separator
mosip.kernel.data-key-splitter=#KEY_SPLITTER#
#GCM tag length
mosip.kernel.crypto.gcm-tag-length=128
#Hash algo name
mosip.kernel.crypto.hash-algorithm-name=PBKDF2WithHmacSHA512
#Symmtric key length used in hash
mosip.kernel.crypto.hash-symmetric-key-length=256
#No of iterations in hash
mosip.kernel.crypto.hash-iteration=100000
#Sign algo name
mosip.kernel.crypto.sign-algorithm-name=RS256


keymanager.persistence.jdbc.driver=org.postgresql.Driver
keymanager_database_url=jdbc:postgresql://localhost:9001/mosip_kernel
keymanager_database_username=kerneluser
keymanager_database_password=Mosip@dev123

licensekeymanager.persistence.jdbc.driver=org.postgresql.Driver
licensekeymanager_database_url=jdbc:postgresql://localhost:9001/mosip_master
licensekeymanager_database_username=masteruser
licensekeymanager_database_password=Mosip@dev123

hibernate.hbm2ddl.auto=none
hibernate.dialect=org.hibernate.dialect.PostgreSQL95Dialect
hibernate.jdbc.lob.non_contextual_creation=true
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.connection.charSet=utf8
hibernate.cache.use_second_level_cache=false
hibernate.cache.use_query_cache=false
hibernate.cache.use_structured_entries=false
hibernate.generate_statistics=false
hibernate.current_session_context_class=org.springframework.orm.hibernate5.SpringSessionContext

auth.server.validate.url=https://dev.mosip.io/v1/authmanager/authorize/admin/validateToken
auth.server.admin.validate.url=https://dev.mosip.io/v1/authmanager/authorize/admin/validateToken
auth.role.prefix=ROLE_
auth.header.name=Authorization

mosip.kernel.pdf_owner_password=PDFADMIN
#------
mosip.kernel.signature.signature-request-id=SIGNATURE.REQUEST
mosip.kernel.signature.signature-version-id=v1.0

mosip.sign.applicationid=KERNEL
mosip.sign.refid=SIGN
mosip.sign-certificate-refid=SIGN
mosip.signed.header=response-signature
mosip.kernel.signature.encrypt-url=http://localhost:8088/v1/keymanager/sign
mosip.kernel.keymanager-service-publickey-url=http://localhost:8088/v1/keymanager/publickey/{applicationId}
mosip.kernel.keymanager-service-sign-url=http://localhost:8088/v1/keymanager/sign


#---
  
mosip.kernel.tokenid.uin.salt=zHuDEAbmbxiUbUShgy6pwUhKh9DE0EZn9kQDKPPKbWscGajMwf
mosip.kernel.tokenid.partnercode.salt=yS8w5Wb6vhIKdf1msi4LYTJks7mqkbmITk2O63Iq8h0bkRlD0d
mosip.kernel.tokenid.length=36

#---
#Length of license key to be generated.
mosip.kernel.licensekey.length=16
#List of permissions
# NOTE: ',' in the below list is used as splitter in the implementation. 
# Use of ',' in the values for below key should be avoided.
# Use of spaces before and after ',' also should be avoided.
mosip.kernel.licensekey.permissions=OTP Trigger,OTP Authentication,Demo Authentication - Identity Data Match,Demo Authentication - Address Data Match,Demo Authentication - Full Address Data Match,Demo Authentication - Secondary Language Match,Biometric Authentication - FMR Data Match,Biometric Authentication - IIR Data Match,Biometric Authentication - FID Data Match,Static Pin Authentication,eKYC - limited,eKYC - Full,eKYC - No
```

**Usage Sample**


  **Get Public Key**
  
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

  **Sign pdf**
  
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

**Encrypt Request**
  
  ```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("multipart/form-data;boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");

RequestBody body = RequestBody.create(mediaType, "{\r\n  \"applicationId\": \"REGISTRATION\",\r\n  \"data\": \"VGhpcyBpcyBhIHBsYWluIHRleHQ=\",\r\n  \"referenceId\": \"ref123\",\r\n  \"timeStamp\": \"2018-12-06T12:07:44.403Z\"\r\n}");

Request request = new Request.Builder()
  .url("http://localhost:8087/cryptomanager/v1.0/encrypt")
  .post(body)
  .addHeader("content-type", "application/json")
  .build();

Response response = client.newCall(request).execute();
  ```
  
  *Response*
  
  Status:200
  
  ```
{
"data":"EsGmECXJucN7AH6DHoKzzGs3bwspfOftQHwhpOWHUpptyFU1MYOz_iJxi1dBcLDXKQE_OV1xrY8Jyw0XUcSDbNYW9qHr5Hfbe30kTc-hCVNKItYN0OYOSBvgq9pd6TAatzlADvW6PRbRyHuumRqoD2ZL0tddiZqe6pa_Ya3hlTYsZm-L_65IJnkGDJLmxmMVS-pqqKqqtrXnTdYMjvK2wMkuZIFz4SX6F0jxnHz7XhrKSBzY8b8O4z1ZUterB450kKPzbRsZ3fySdjlpqhwtuVXZV6gkAA_n1iACOksvSyUZ7BN5AgWKnnsUHaNyF6f-e564G6nTN4M3Fyd_Z_KzxCNLRVlfU1BMSVRURVIjcvEHI6pM3H-kRWMRBZJDyte4BHKuUj4PBtU3dJ4kb_Vmd4nFBuguSh_tFHiz62GB"
}
  ```
  
  **Decrypt Request**
  
  ```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");

RequestBody body = RequestBody.create(mediaType, "{\n  \"applicationId\": \"REGISTRATION\",\n  \"data\": \"EsGmECXJucN7AH6DHoKzzGs3bwspfOftQHwhpOWHUpptyFU1MYOz_iJxi1dBcLDXKQE_OV1xrY8Jyw0XUcSDbNYW9qHr5Hfbe30kTc-hCVNKItYN0OYOSBvgq9pd6TAatzlADvW6PRbRyHuumRqoD2ZL0tddiZqe6pa_Ya3hlTYsZm-L_65IJnkGDJLmxmMVS-pqqKqqtrXnTdYMjvK2wMkuZIFz4SX6F0jxnHz7XhrKSBzY8b8O4z1ZUterB450kKPzbRsZ3fySdjlpqhwtuVXZV6gkAA_n1iACOksvSyUZ7BN5AgWKnnsUHaNyF6f-e564G6nTN4M3Fyd_Z_KzxCNLRVlfU1BMSVRURVIjcvEHI6pM3H-kRWMRBZJDyte4BHKuUj4PBtU3dJ4kb_Vmd4nFBuguSh_tFHiz62GB\",\n  \"referenceId\": \"ref123\",\n  \"timeStamp\": \"2018-12-06T12:07:44.403Z\"\n}\n");

Request request = new Request.Builder()
  .url("http://localhost:8087/cryptomanager/v1.0/decrypt")
  .post(body)
  .addHeader("content-type", "application/json")
  .build();

Response response = client.newCall(request).execute();
  ```
  
  *Response*
  
  Status:200
  
  ```
{
 "data": "VGhpcyBpcyBhIHBsYWluIHRleHQ"
}
  ```

**Sign Request**
  
  ```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");
RequestBody body = RequestBody.create(mediaType, "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"data\": \"admin\" }, \"requesttime\": \"2018-12-10T06:12:52.994Z\", \"version\": \"string\" }");
Request request = new Request.Builder()
  .url("http://localhost:8092/v1/signature/sign")
  .post(body)
  .addHeader("Content-Type", "application/json")
  .build();

Response response = client.newCall(request).execute();
  ```
  
  *Response*
  
 HTTP Status: 200 OK
  
  ```
{
    "id": null,
    "version": null,
    "responsetime": "2019-05-20T05:59:32.178Z",
    "metadata": null,
    "response": {
        "signature": "ZeNsCOsdgf0UgpXDMry82hrHS6b1ZKvS-tZ_3HBGQHleIu1fZA6LNTtx7XZPFeC8dxsyuYO_iN3mVExM4J2tPlebzsRtuxHigi9o7DI_2xGqFudzlgoH55CP_BBNUDmGm6m-lTMkRx6X61dKfKDNo2NipZdM-a_cHf6Z0aVAU4LdJhV4xWOOm8Pb8sYIc2Nf6kUJRiidEGrxonUCfXX1XlnjMAo75wu99pN8G0mc7JhOehUqbwuXwKo4sQ694ae4F_AYl70sepX24v-0k0ga9esXR4i9rKaoHbzhQFtt2hangQkxHajq9ZTrXWMhd4msTzjHCKdEPXQFsTbKrgKtDQ",
        "timestamp": "2019-05-20T05:59:31.934Z"
    },
    "errors": null
}
  ```

**The inputs which have to be provided for validate sign response by passing Response Timestamp along with the data and sign response:**

1.signature -Mandatory

2.data - Mandatory

3.timestamp -Mandatory


**The response will be Validation Successful  if request is successful, else throw exception Validation Unsuccessful** 


**Signature Validate**
  
*Request*
  
  ```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");

RequestBody body = RequestBody.create(mediaType, "{ \"id\": \"string\", \"metadata\": {}, \"request\": { \"signature\": \"DrgkF2vm4WvBe04UNe-RePRcrg77uQpsH3GENRcglBsid-K0UDReeeZVKwimOdwV7Ht1j-_D1BFf2sCrM8ni7ztE5Xc_3TEaniOAnOgZDRSI0GG-uSqjH51AwTSl1PYdStfXtOn6HEfEU68JG7TdAliDI5C7thJ1YNmPnHusIsZzX6sW_VfvSpLeA_RzCqnUDH_VaEzZt_5zRYiQv9van4wt0P7HTfIBlQ5zaeO3wXOc3Pogct3ssKwqdaMmZdc7QTDOFqDZZVceMTIXKyiH-ZVs_u3QXRysiLVdXoz7d7yXHdWxQtzsfMjY7alMJNgbmu4X26LYNRemn65Mmn6ixA\", \"data\": \"test\", \"timestamp\": \"2019-05-20T07:28:04.269Z\" }, \"requesttime\": \"2018-12-10T06:12:52.994Z\", \"version\": \"string\" }");



Request request = new Request.Builder()
  .url("http://localhost:8092/v1/signature/validate")
  .post(body)
  .addHeader("Content-Type", "application/json")
  .build();

Response response = client.newCall(request).execute();
  ```
  
  *Response*
  
 HTTP Status: 200 OK
  
  ```
{
    "id": null,
    "version": null,
    "responsetime": "2019-05-20T07:16:40.794Z",
    "metadata": null,
    "response": {
        "status": "success",
        "message": "Validation Successful"
    },
    "errors": null
}
  ```

  **Token ID Generation**
  
  
```
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
  .url("http://localhost:8097/v1/tokenidgenerator/7394829283/PC001")
  .get()
  .build();

Response response = client.newCall(request).execute();
```


  *Response:*
  
  HttpStatus: 200 OK
  
```
{
	"id": "mosip.kernel.tokenid.generate",
	"version": "1.0",
	"metadata": {},
	"responsetime": "2019-04-04T05:03:18.287Z",
	"response": {
                  "tokenID": "268177021248100621690339355202974361"
                },
        "errors": []
}
```

 
 **License Key Generation :**
 
 ```
{ 
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");
RequestBody body = RequestBody.create(mediaType, "{\"tspId\":\"TSPID1\",\"licenseExpiryTime\":\"2019-02-07T05:35:53.476Z\"}");
Request request = new Request.Builder()
  .url("http://localhost:8080/v1.0/license/generate")
  .post(body)
  .addHeader("content-type", "application/json")
  .addHeader("cache-control", "no-cache")
  .addHeader("postman-token", "7d3b19f4-5a6c-d926-4975-1f228f8caa3e")
  .build();

Response response = client.newCall(request).execute();
}
 ```
 
*License Generation Responses :*
Successful Generation :

HttpStatus : 200 OK

```
{
    "licenseKey": "rAx2TRvemovtZ0to"
}
```

**License Key Mapping:**
 
```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");
RequestBody body = RequestBody.create(mediaType, "{ \"lkey\": \"rAx2TRvemovtZ0to\",\"permissions\": [\"OTP Trigger\",\"OTP Authentication\"],\"tspId\": \"TSPID1\"}");
Request request = new Request.Builder()
  .url("http://localhost:8080/v1.0/license/map")
  .post(body)
  .addHeader("content-type", "application/json")
  .addHeader("cache-control", "no-cache")
  .addHeader("postman-token", "86230d1c-f33d-0ab1-6726-8f7f6ade6072")
  .build();

Response response = client.newCall(request).execute();

```
*License Mapping Responses:*

 HttpStatus : 200 OK
 

 ```
{
    "status": "Mapped License with the permissions"
}
 ```

**License Key Fetch:**
 
```
 OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
  .url("http://localhost:8080/v1.0/license/fetch?licenseKey=rAx2TRvemovtZ0to&tspId=TSPID1")
  .get()
  .addHeader("cache-control", "no-cache")
  .addHeader("postman-token", "ac4daf24-2cef-f5f5-50f4-32b0d1938177")
  .build();

```

*License Fetch Responses:*

 HttpStatus : 200 OK

 ```
{
    "mappedPermissions": [
        "OTP Trigger",
        "OTP Authentication"
    ]
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








