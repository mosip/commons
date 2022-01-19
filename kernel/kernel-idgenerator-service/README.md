# ID Generator Service

## About
This service is used to generates various IDs like UIN, APPID, RID, VID

## UIN generation logic
UIN should have the following properties:
* Only integers with length as specified in `mosip.kernel.uin.length` configuration in [application properties](https://github.com/mosip/mosip-config/blob/develop3-v3/application-default.properties)
* No alphanumeric characters
* No repeating numbers for 2 or more than 2 digits
* No sequential number for 3 or more than 3 digits
* Should not be generated sequentially
* Should not have repeated block of numbers for 2 or more than 2 digits
* The last digit in the number should be reserved for a checksum
* The number should not contain '0' or '1' as the first digit.
* First 5 digits should be different from the last 5 digits (example - 4345643456)
* First 5 digits should be different to the last 5 digits reversed (example - 4345665434)
* Should not be a cyclic figure (example - 4567890123, 6543210987) 
* Should be different from the repetition of the first two digits 5 times (example - 3434343434)
* Should not contain three even adjacent digits (example - 3948613752)
* Should not contain admin defined restricted number

## Default context-path and port
Refer [`bootstrap.properties`](src/main/resources/bootstrap.properties)

