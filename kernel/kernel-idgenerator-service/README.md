# ID Generator Service

## Overview
MOSIP uses the cryptographically safe SecureRandom algorithm to generate UINs (Unique Identification Numbers) with high randomness. A checksum is added using the Verhoeff Algorithm to validate the UIN/VID. Generated UINs are filtered against [predefined patterns](#uin-generation-filters) as outlined below, to eliminate easily identifiable numbers and prevent repetitions or sequences. The random number seed is refreshed every 45 minutes or as configured via  `mosip.idgen.uin.secure-random-reinit-frequency` in minutes.

This service is used to generate various IDs like UIN and VID.

## UIN Generation Filters
The UIN should follow the following filters and constraints:
* Only integers with length, as specified in `mosip.kernel.uin.length` configuration.
* Minimum pregenerated UINs that should be available, as specified in `mosip.kernel.uin.min-unused-threshold` configuration. If not available then the next batch of generation would start.
* Number of UINs to generate, as specified in `mosip.kernel.uin.uins-to-generate` configuration.
* Upper bound of number of digits in sequence allowed in id, as specified in `mosip.kernel.uin.length.sequence-limit` configuration. For example if limit is 3, then 12 is allowed but 123 is not allowed in id (in both ascending and descending order).
* Number of digits in repeating block allowed in id, as specified in `mosip.kernel.uin.length.repeating-block-limit` configuration. For example if limit is 2, then 4xxx4 is allowed but 48xxx48 is not allowed in id (x is any digit).
* Lower bound of number of digits allowed in between two repeating digits in id, as specified in `mosip.kernel.uin.length.repeating-limit` configuration. For example if limit is 2, then 11 and 1x1 is not allowed in id (x is any digit).
* Number of digits to check for reverse digits group limit, as specified in `mosip.kernel.uin.length.reverse-digits-limit` configuration. For example if limit is 5 and UIN is 4345665434, then first 5 digits will be 43456, reverse 65434.
* Number of digits to check for digits group limit in id, as specified in `mosip.kernel.uin.length.digits-limit` configuration. For example if limit is 5 and UIN is 4345643456, then 5 digits group will be 43456.
* Number of even adjacent digits limit in id, as specified in `mosip.kernel.uin.length.conjugative-even-digits-limit` configuration. For example, if limit is 3 then any 3 even adjacent digits is not allowed.
* List of restricted numbers with , seperation as specified in `mosip.kernel.uin.restricted-numbers` configuration.
* List of numbers that should not be the starting digits in the id. Its a , separated list, as specified in `mosip.kernel.uin.not-start-with` configuration. For example, the number should not contain '0' or '1' as the first digit.
* No alphanumeric characters allowed.
* No cyclic numbers as mentioned below are allowed. "142857", "0588235294117647", "052631578947368421", "0434782608695652173913", "0344827586206896551724137931", "0212765957446808510638297872340425531914893617", "0169491525423728813559322033898305084745762711864406779661", "016393442622950819672131147540983606557377049180327868852459", "010309278350515463917525773195876288659793814432989690721649484536082474226804123711340206185567".

Note: Significant thought has been invested in the above design to ensure the generated numbers are both random and secure. We strongly recommend retaining the stated values to maintain the integrity and security of the same.

## Default Context-path and Port
Refer [`bootstrap.properties`](src/main/resources/bootstrap.properties)

