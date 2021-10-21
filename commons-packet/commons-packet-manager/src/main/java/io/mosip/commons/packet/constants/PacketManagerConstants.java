package io.mosip.commons.packet.constants;

public class PacketManagerConstants {
	
	public static final String IDENTITY_FILENAME = "ID";
	public static final String IDENTITY_FILENAME_WITH_EXT = "ID.json";
	public static final String AUDIT_FILENAME = "audit";
	public static final String AUDIT_FILENAME_WITH_EXT = "audit.json";
	public static final String PACKET_META_FILENAME = "packet_meta_info.json";
	public static final String PACKET_DATA_HASH_FILENAME = "packet_data_hash.txt";
	public static final String PACKET_OPER_HASH_FILENAME = "packet_operations_hash.txt";
	public static final String CBEFF_FILE_FORMAT = "cbeff";
	public static final double CBEFF_VERSION = 1.0;
	public static final String CBEFF_SCHEMA_FILE_PATH = "cbeff.xsd";
	public static final String CBEFF_DEFAULT_FORMAT_ORG = "Mosip";
	public static final String CBEFF_DEFAULT_FORMAT_TYPE = "257";
	public static final String CBEFF_DEFAULT_ALG_ORG = "HMAC";
	public static final String CBEFF_DEFAULT_ALG_TYPE = "SHA-256";
	public static final String CBEFF_FILENAME = "%s_bio_CBEFF";
	public static final String CBEFF_FILENAME_WITH_EXT = CBEFF_FILENAME.concat(".xml");
	public static final String CBEFF_EXT = ".xml";

	public static final String DEMOGRAPHIC_SEQ = "demographicSequence";
	public static final String BIOMETRIC_SEQ = "biometricSequence";
	public static final String OPERATIONS_SEQ = "otherFiles";
	
	public static final String OFFICER = "officer_bio_cbeff";
	public static final String SUPERVISOR = "supervisor_bio_cbeff";
	
	public static final String PROPERTIES = "properties";
	public static final String IDENTITY = "identity";

	public static final String SUBPACKET_ZIP_FILE_NAME = "%s_%s.zip";
	
	public static final String IDSCHEMA_VERSION = "IDSchemaVersion";
	public static final String REGISTRATIONID = "registrationId";
	
	public static final String SCHEMA_ID = "id";
	public static final String SCHEMA_TYPE = "type";
	public static final String SCHEMA_REF = "$ref";
	public static final String SCHEMA_CATEGORY = "fieldCategory";
	public static final String SCHEMA_VERSION_QUERY_PARAM = "schemaVersion";
	
	public static final String BIOMETRICS_TYPE = "#/definitions/biometricsType";
	public static final String DOCUMENTS_TYPE = "#/definitions/documentType";
	
	public static final String BIOMETRICS_DATATYPE = "biometricsType";
	public static final String DOCUMENTS_DATATYPE = "documentType";	
	
	public static final String FINGERPRINT_SLAB_LEFT = "FINGERPRINT_SLAB_LEFT";
	public static final String FINGERPRINT_SLAB_RIGHT = "FINGERPRINT_SLAB_RIGHT";
	public static final String FINGERPRINT_SLAB_THUMBS = "FINGERPRINT_SLAB_THUMBS";
	public static final String IRIS_DOUBLE = "IRIS_DOUBLE";
	public static final String FACE_FULLFACE = "FACE_FULL FACE";
	
	public static final String META_CREATION_DATE = "creationDate";
	public static final String META_CLIENT_VERSION = "Registration Client Version Number";
	public static final String META_LATITUDE = "geoLocLatitude";
	public static final String META_LONGITUDE = "geoLoclongitude";
	public static final String META_REGISTRATION_TYPE = "registrationType";
	public static final String META_PRE_REGISTRATION_ID = "preRegistrationId";
	public static final String META_MACHINE_ID = "machineId";
	public static final String META_DONGLE_ID ="dongleId";
	public static final String META_KEYINDEX ="keyIndex";
	public static final String META_CENTER_ID ="centerId";
	public static final String META_APPLICANT_CONSENT ="consentOfApplicant";
	
	public static final String META_OFFICER_ID ="officerId";
	public static final String META_OFFICER_BIOMETRIC_FILE ="officerBiometricFileName";
	public static final String META_SUPERVISOR_ID ="supervisorId";
	public static final String META_SUPERVISOR_BIOMETRIC_FILE ="supervisorBiometricFileName";
	public static final String META_SUPERVISOR_PWD ="supervisorPassword";
	public static final String META_OFFICER_PWD ="officerPassword";
	public static final String META_SUPERVISOR_PIN ="supervisorPIN";
	public static final String META_OFFICER_PIN ="officerPIN";
	public static final String META_SUPERVISOR_OTP ="supervisorOTPAuthentication";
	public static final String META_OFFICER_OTP ="officerOTPAuthentication";

	/** The Constant ABIS. */
	public static final String SCHEMA = "schema";

	public static final String FIELDCATEGORY = "fieldCategory";

	public static final String PVT = "pvt";
	public static final String LABEL = "label";
	public static final String VALUE = "value";
	public static final String TYPE = "type";
	public static final String FORMAT = "format";

	public static final String IDSCHEMA_URL = "IDSCHEMA";
	public static final String SCHEMA_JSON = "schemaJson";
	public static final String RESPONSE = "response";

	public static final String META_INFO_OPERATIONS_DATA = "operationsData";
	public static final String METAINFO = "metaInfo";
	public static final String AUDITS = "audits";

	public static final String OFFICER_BIOMETRIC = "officerBiometricFileName";
	public static final String SUPERVISOR_BIOMETRIC = "supervisorBiometricFileName";

	//TODO During update flow if its non-biometric update, then the captured biometrics are sent in this file
	public static final String META_AUTH_BIO_FILENAME = "authenticationBiometricFileName";

	// Packet meta info constants
	public static final String ID = "id";
	public static final String PACKET_NAME = "packetname";
	public static final String SOURCE = "source";
	public static final String PROCESS = "process";
	public static final String SCHEMA_VERSION = "schemaversion";
	public static final String SIGNATURE = "signature";
	public static final String ENCRYPTED_HASH = "encryptedhash";
	public static final String PROVIDER_NAME = "providername";
	public static final String PROVIDER_VERSION = "providerversion";
	public static final String CREATION_DATE = "creationdate";


}
