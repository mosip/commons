## kernel-notification-service

[Background & Design](../../docs/design/kernel/kernel-emailnotification.md)
 
[Api Documentation](https://github.com/mosip/mosip/wiki/Kernel-APIs#email-notification)

Default Port and Context Path
 
 ```
server.port=8083
server.servlet.path=/emailnotifier

 ```
 
 localhost:8083/notifier/swagger-ui.html
 
  **Application proprties**

[kernel-emailnotification-service-dev.properties](../../config/kernel-emailnotification-service-dev.properties)

```

# SMTP (Gmail-SMTP-Properties)
#host being used.
spring.mail.host=smtp.gmail.com
#user mail id, from which the mail will be sent.
spring.mail.username=username@gmail.com
#user password, password to authenticate the above mail address.
spring.mail.password=mailpwd
#port being used.
spring.mail.port=587
#protocol being used.
spring.mail.properties.mail.transport.protocol=smtp
#property to enable/disable tls.
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.starttls.enable=true
#property to enable/disable authorization.
spring.mail.properties.mail.smtp.auth=true
#property to set the mail debugging.
spring.mail.debug=false
#-------------------------------------
# MULTIPART (Multipart-Properties)
# Enable multipart uploads
spring.servlet.multipart.enabled=true
# Max file size.
spring.servlet.multipart.max-file-size=5MB 

```
 
 The mandatory required parameters in the form-data request are:
 * mailTo
 * mailSubject
 * mailContent
The optional required parameters in the form-data request are:
 - mailCc
 - attachments
 
**Usage Sample:**
 
 Usage1:
 Email Notification Request:
 
```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");

RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gWContent-Disposition: form-data; name=\"mailTo\"tmail@testmail.com------WebKitFormBoundary7MA4YWxkTrZu0gWContent-Disposition: form-data; name=\"mailSubject\" test subject------WebKitFormBoundary7MA4YWxkTrZu0gWContent-Disposition: form-data; name=\"mailContent\"test content------WebKitFormBoundary7MA4YWxkTrZu0gWContent-Disposition: form-data; name=\"mailCc\"------WebKitFormBoundary7MA4YWxkTrZu0gWContent-Disposition: form-data; name=\"attachments\"------WebKitFormBoundary7MA4YWxkTrZu0gW--");

Request request = new Request.Builder()
  .url("http://localhost:8083/emailnotifier/email")
  .post(body)
  .addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
  .build();

Response response = client.newCall(request).execute();

 ```
 
Email Notification Response :

Successful submission of request :
HttpStatus : 202 Accepted

```
{
    "status": "Success",
    "message": "Email request sent"
    
}
```

Failure in argument validations : 

If No mandatory arguments is passed and request is submitted :

HttpStatus : 406 Not Acceptable


```
{
    "errors": [
        {
            "errorCode": "KER-NOE-002",
            "errorMessage": "Subject must be valid. It can't be empty or null."
        },
        {
            "errorCode": "KER-NOE-001",
            "errorMessage": "To must be valid. It can't be empty or null."
        },
        {
            "errorCode": "KER-NOE-003",
            "errorMessage": "Content must be valid. It can't be empty or null."
        }
    ]
}
```

If Few mandatory arguments not passed and request is submitted :

HttpStatus : 406 Not Acceptable

```
{
    "errors": [
        {
            "errorCode": "KER-NOE-002",
            "errorMessage": "Subject must be valid. It can't be empty or null."
        },
        {
            "errorCode": "KER-NOE-003",
            "errorMessage": "Content must be valid. It can't be empty or null."
        }
    ]
}
```


## kernel-smsnotification-service
 
[Background & Design](../../docs/design/kernel/kernel-smsnotification.md)

[Api Documentation](https://github.com/mosip/mosip/wiki/Kernel-APIs#sms-notification)


**This service is can be configured for infobip or msg91 SMS gateway**


** Infobip Registartion**

- Register on [Infobip](https://www.infobip.com/en/get-started) and configure same username and password in properties.

- Update **mosip.kernel.sms.api** property with the api got after registration, Follow [Infobib-Api](https://dev.infobip.com/send-sms/single-sms-message) and get api from **Resource** .

- Update the mosip.kernel.sms.username and mosip.kernel.sms.password


** MSG91 Registartion**

- Register on https://control.msg91.com/signup/ as developer and get an authkey. 

- Update mosip.kernel.sms.authkey=240764AwCGPlwv5bb455b0




Default Port and Context Path

```
server.port=8083
server.servlet.path=/notifier

```

localhost:8083/notifier/swagger-ui.html


**Application Properties**

[kernel-smsnotification-service-dev.properties](../../config/kernel-smsnotification-service-dev.properties)
 
 
 ```
mosip.kernel.sms.enabled=true
mosip.kernel.sms.country.code=91
mosip.kernel.sms.number.length=10

#mosip.kernel.sms.gateway : "infobip" or "msg91"
mosip.kernel.sms.gateway=msg91

#----------infoBip gateway---------------
#mosip.kernel.sms.api=https://9qqdy.api.infobip.com/sms/2/text/single
#mosip.kernel.sms.username=MDTMOSIP
#mosip.kernel.sms.password=Start$01
#mosip.kernel.sms.sender=MOSMSG


#----------msg91 gateway---------------
mosip.kernel.sms.api=http://api.msg91.com/api/v2/sendsms
mosip.kernel.sms.authkey=282572AcvONzDY0SlY5d11eb1a
mosip.kernel.sms.route=4
mosip.kernel.sms.sender=MOSMSG
 
 ```
 
 
**Usage Sample**
 
Request

 ```
OkHttpClient client = new OkHttpClient();

MediaType mediaType = MediaType.parse("application/json");

RequestBody body = RequestBody.create(mediaType, "{\"message\": \"OTP-432467\",\"number\": \"98*****897\"}");

Request request = new Request.Builder()
  .url("http://localhost:8083/notifier/sms")
  .post(body)
  .addHeader("content-type", "application/json")
  .build();

Response response = client.newCall(request).execute();
 
 ```


Response body model for POST **/notifier/sms**

HttpStatus: 202 Accepted
  
 ```
{
  "message": "Sms Request Sent",
  "status": "success"
}
 ```
 
Exception Scenario-

1.Null or empty inputs provided-

HttpStatus : 406 Not Acceptable

```
{
    "errors": [
        {
            "errorCode": "KER-NOS-001",
            "errorMessage": "Number and message can't be empty, null"
        }
    ]
}

```

2.Invalid number present-

HttpStatus : 406 Not Acceptable


```
{
   "errors": [
        {
            "errorCode": "KER-NOS-002",
            "errorMessage": "Contact number cannot contains alphabet,special character or less than 10 digits"
        }
    ]
}

```