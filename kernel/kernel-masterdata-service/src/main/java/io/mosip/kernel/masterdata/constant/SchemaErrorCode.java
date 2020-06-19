package io.mosip.kernel.masterdata.constant;

/**
 * 
 * @author anusha
 *
 */
public enum SchemaErrorCode {
	
	DYNAMIC_FIELD_FETCH_EXCEPTION("KER-SCH-001", "Error occurred while fetching dynamic fields"),
	DYNAMIC_FIELD_INSERT_EXCEPTION("KER-SCH-002", "Error occurred while inserting dynamic field"),
	DYNAMIC_FIELD_NOT_FOUND_EXCEPTION("KER-SCH-003", "Dynamic field not found"),
		
	SCHEMA_FETCH_EXCEPTION("KER-SCH-004", "Error occurred while fetching Identity schema"),
	SCHEMA_INSERT_EXCEPTION("KER-SCH-005", "Error occurred while inserting Identity schema"),
	SCHEMA_UPDATE_EXCEPTION("KER-SCH-006", "Error occurred while updating Identity schema"),
	SCHEMA_NOT_FOUND_EXCEPTION("KER-SCH-007", "Identity schema not found"),
	NO_PUBLISHED_SCHEMA_FOUND_EXCEPTION("KER-SCH-008", "No published Identity schema"),
	SCHEMA_ALREADY_PUBLISHED("KER-SCH-009", "Schema already published"),
	SCHEMA_EFFECTIVE_FROM_IS_OLDER("KER-SCH-010", "Schema effective from date cannot be older"),
	
	DYNAMIC_FIELD_UPDATE_EXCEPTION("KER-SCH-011", "Error occurred while updating dynamic field"),
	DYNAMIC_FIELD_ALREADY_EXISTS("KER-SCH-012", "Dynamic field already exists"),
	VALUE_PARSE_ERROR("KER-SCH-013", "Error while parsing json string"),
	SCHEMA_JSON_EXCEPTION("KER-SCH-014", "Error while constructing schema json"),
	
	SCHEMA_REQUEST_EXCEPTION("KER-SCH-015", "Bad Request Found"),
	
	DUPLICATE_FIELD_EXCEPTION("KER-SCH-016", "Duplicate fields found %s"),
	SUB_TYPE_REQUIRED_EXCEPTION("KER-SCH-017", "SubType is required for field %s"),
	BIO_ATTRIBUTES_REQUIRED_EXCEPTION("KER-SCH-018", "BioAttributes are required for field %s"),
	BIO_ATTRIBUTES_DUPLICATED_EXCEPTION("KER-SCH-019", "Same BioAttributes used in field with same SubType : %s");

	private final String errorCode;
	private final String errorMessage;

	private SchemaErrorCode(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public String getErrorCode() {
		return this.errorCode;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}
}
