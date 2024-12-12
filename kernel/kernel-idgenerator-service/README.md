# ID Generator Service

## Overview
MOSIP uses the cryptographically safe SecureRandom algorithm to generate UINs (Unique Identification Numbers) with high randomness. A checksum is added using the Verhoeff Algorithm to validate the UIN/VID. Generated UINs are filtered against [predefined patterns](#uin-generation-filters) outlined below to eliminate easily identifiable numbers and prevent repetitions or sequences. The random number seed is refreshed every 45 minutes or as configured via  `mosip.idgen.uin.secure-random-reinit-frequency` in minutes.

This service is used to generate various IDs like UIN, APPID, RID (AID), VID.

## UIN Generation Filters
UIN should follow the following properties:
* Only integers with length as specified in `mosip.kernel.uin.length` configuration in [application properties](https://docs.mosip.io/1.2.0/modules/module-configuration#application-properties)
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

## Default Context-path and Port
Refer [`bootstrap.properties`](src/main/resources/bootstrap.properties)

