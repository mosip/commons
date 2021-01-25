## kernel-idgenerator-service

[Background & Design of VID](../../docs/design/kernel/kernel-vidgenerator.md)
[Background & Design of UIN](../../docs/design/kernel/kernel-uingenerator.md)

[Api Documentation for VID](https://github.com/mosip/mosip/wiki/Kernel-APIs#vid)
[Api Documentation for uin](https://github.com/mosip/mosip/wiki/Kernel-APIs#uin)

Default Port and Context Path

```
server.port=8080
server.servlet.path=v1/idgenerator

```

** Properties to be added in parent Spring Application environment **

```
javax.persistence.jdbc.driver=org.postgresql.Driver
hibernate.dialect=org.hibernate.dialect.PostgreSQL95Dialect
hibernate.jdbc.lob.non_contextual_creation=true
hibernate.hbm2ddl.auto=none
hibernate.show_sql=false
hibernate.format_sql=false
hibernate.connection.charSet=utf8
hibernate.cache.use_second_level_cache=false
hibernate.cache.use_query_cache=false
hibernate.cache.use_structured_entries=false
hibernate.generate_statistics=false
spring.datasource.initialization-mode=always
hibernate.current_session_context_class=org.springframework.orm.hibernate5.SpringSessionContext




auth.server.validate.url=https://<host>:<port>/v1/authmanager/authorize/validateToken



#-----------------------------VID Properties--------------------------------------
# length of the vid
mosip.kernel.vid.length=16

# Upper bound of number of digits in sequence allowed in id. For example if
# limit is 3, then 12 is allowed but 123 is not allowed in id (in both
# ascending and descending order)
# to disable sequence limit validation assign 0 or negative value
mosip.kernel.vid.length.sequence-limit=3

# Number of digits in repeating block allowed in id. For example if limit is 2,
# then 4xxx4 is allowed but 48xxx48 is not allowed in id (x is any digit)
# to disable repeating block validation assign 0 or negative value
mosip.kernel.vid.length.repeating-block-limit=2


# Lower bound of number of digits allowed in between two repeating digits in
# id. For example if limit is 2, then 11 and 1x1 is not allowed in id (x is any digit)
# to disable repeating limit validation, assign 0  or negative value
mosip.kernel.vid.length.repeating-limit=2

# list of number that id should not be start with
# to disable null
mosip.kernel.vid.not-start-with=0,1

#restricted numbers for vid
mosip.kernel.vid.restricted-numbers=786,666



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
#Keygenerator symmetric algorithm name
mosip.kernel.keygenerator.symmetric-algorithm-name=AES
# keygenerator asymmetric algorithm name
mosip.kernel.keygenerator.asymmetric-algorithm-name=RSA
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




#minimum threshold of unused vid
mosip.kernel.vid.min-unused-threshold=100000
#number of vids to generate
mosip.kernel.vid.vids-to-generate=200000
#time to release after expiry(in days)
mosip.kernel.vid.time-to-release-after-expiry=5
#for genaration on init vids timeout 
mosip.kernel.vid.pool-population-timeout=10000000



kernel.vid.revoke-scheduler-type=cron
#schedular seconds configuration
kernel.vid.revoke-scheduler-seconds=0
#schedular minutes configuration
kernel.vid.revoke-scheduler-minutes=0
#schedular hours configuration
kernel.vid.revoke-scheduler-hours=23
#schedular days configuration
kernel.vid.revoke-scheduler-days_of_month=*
#schedular months configuration
kernel.vid.revoke-scheduler-months=*
#schedular weeks configuration
kernel.vid.revoke-scheduler-days_of_week=*





id_database_url=jdbc:postgresql://<host>:<port>/mosip_kernel
id_database_username=<username>
id_database_password=<password>

#-----------------------------UIN Properties--------------------------------------
#length of the uin
mosip.kernel.uin.length=10
#minimun threshold of uin
mosip.kernel.uin.min-unused-threshold=100000
#number of uins to generate
mosip.kernel.uin.uins-to-generate=200000
#restricted numbers for uin
mosip.kernel.uin.restricted-numbers=786,666
#sequence limit for uin filter
#to disable validation assign zero or negative value
mosip.kernel.uin.length.sequence-limit=3
#repeating block limit for uin filter
#to disable validation assign zero or negative value
mosip.kernel.uin.length.repeating-block-limit=2
#repeating limit for uin filter
#to disable validation assign zero or negative value
mosip.kernel.uin.length.repeating-limit=2
#reverse group digit limit for uin filter
mosip.kernel.uin.length.reverse-digits-limit=5
#group digit limit for uin filter
mosip.kernel.uin.length.digits-limit=5
#should not start with
mosip.kernel.uin.not-start-with=0,1
#adjacent even digit limit for uin filter
mosip.kernel.uin.length.conjugative-even-digits-limit=3



```
** NOTE: Once configured amount of vids are generated then http server will start, There is no conditions on amount of uins**


**Usage Sample:**

  *VID GET Request:*
  
```
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
  .url("http://localhost:8080/v1/idgenerator/vid")
  .get()
  .build();

Response response = client.newCall(request).execute();
```


  *Response:*
  
  HttpStatus: 200 OK
  
```
{
    "id": null,
    "version": null,
    "responsetime": "2020-04-27T11:15:07.240Z",
    "metadata": null,
    "response": {
        "vid": "5714920358054016"
    },
    "errors": null
}
```


  *GET Request:*
  
```
OkHttpClient client = new OkHttpClient();

Request request = new Request.Builder()
  .url("http://localhost:8080/v1/idgenerator/uin")
  .get()
  .build();

Response response = client.newCall(request).execute();
```


  *Response:*
  
  HttpStatus: 200 OK
  
```
{
    "id": null,
    "version": null,
    "responsetime": "2020-04-24T11:13:21.921Z",
    "metadata": null,
    "response": {
        "uin": "6936894708"
    },
    "errors": null
}
```


 *PUT Request:*


```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");
RequestBody body = RequestBody.create(mediaType, "{\"uin\" : \"8251728314\",\"status\" : \"ASSIGNED\ "}");
Request request = new Request.Builder()
  .url("http://localhost:8080/v1/idgenerator/uin")
  .put(body)
  .addHeader("content-type", "application/json")
  .build();

Response response = client.newCall(request).execute();
```

*Response:*
  
  HttpStatus: 200 OK
  
```
{
    "id": null,
    "version": null,
    "responsetime": "2020-04-24T11:13:21.921Z",
    "metadata": null,
    "response": {
 		"uin":"8251728314",
 		"status":"ASSIGNED"
},
    "errors": null
}
```

