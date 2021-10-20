package io.mosip.commons.packetmanager.constant;

public enum DefaultStrategy {
    EXCEPTION("exception"), DEFAULT_PRIORITY("defaultPriority"), LAST_MODIFIED("byLastModified");

    private String value;

    DefaultStrategy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
