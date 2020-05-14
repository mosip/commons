package io.mosip.kernel.packetmanager.constants;

public class PacketManagerConstants {
	
	public static final String IDENTITY_FILENAME = "ID.json";
	public static final String AUDIT_FILENAME = "audit.json";
	public static final String PACKET_META_FILENAME = "packet_meta_info.json";
	public static final String PACKET_DATA_HASH_FILENAME = "packet_data_hash.txt";
	public static final String PACKET_OPER_HASH_FILENAME = "packet_operations_hash.txt";
		
	public static final String VERSION_FIELD_NAME = "IDSchemaVersion";
	
	public static final String CBEFF_FILE_FORMAT = "cbeff";
	public static final double CBEFF_VERSION = 1.0;
	public static final String CBEFF_SCHEMA_FILE_PATH = "cbeff.xsd";
	public static final String CBEFF_DEFAULT_FORMAT_ORG = "Mosip";
	public static final String CBEFF_DEFAULT_FORMAT_TYPE = "257";
	public static final String CBEFF_DEFAULT_ALG_ORG = "HMAC";
	public static final String CBEFF_DEFAULT_ALG_TYPE = "SHA-256";
	public static final String CBEFF_FILENAME = "%s_bio_CBEFF";
	public static final String CBEFF_FILENAME_WITH_EXT = CBEFF_FILENAME.concat(".xml");
	
	public static final String DEMOGRAPHIC_SEQ = "demographicSequence";
	public static final String BIOMETRIC_SEQ = "biometricSequence";
	public static final String OPERATIONS_SEQ = "otherFiles";
	
	public static final String OFFICER = "officer";
	public static final String SUPERVISOR = "supervisor";
	
	public static final String PROPERTIES = "properties";
	public static final String IDENTITY = "identity";

	public static final String SUBPACKET_ZIP_FILE_NAME = "%s_%s.zip";
	
	public static final String IDSCHEMA_VERSION = "IDSchemaVersion";
	public static final String REGISTRATIONID = "registrationId";
	
	public static final String SCHEMA_ID = "id";
	public static final String SCHEMA_TYPE = "type";
	public static final String SCHEMA_REF = "$ref";
	public static final String SCHEMA_CATEGORY = "fieldCategory";
	
	public static final String BIOMETRICS_TYPE = "#/definitions/biometricsType";
	public static final String DOCUMENTS_TYPE = "#/definitions/documentType";
	
	public static final String BIOMETRICS_DATATYPE = "biometricsType";
	public static final String DOCUMENTS_DATATYPE = "documentType";	
	
	public static final String FINGERPRINT_SLAB_LEFT = "FINGERPRINT_SLAB_LEFT";
	public static final String FINGERPRINT_SLAB_RIGHT = "FINGERPRINT_SLAB_RIGHT";
	public static final String FINGERPRINT_SLAB_THUMBS = "FINGERPRINT_SLAB_THUMBS";
	public static final String IRIS_DOUBLE = "IRIS_DOUBLE";
	public static final String FACE_FULLFACE = "FACE_FULL FACE";

}
