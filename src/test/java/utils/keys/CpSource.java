package utils.keys;

public enum CpSource {
    SOURCE_ID("source_id"),
    NAME("name"),
    URL_1("url_1"),
    URL_2("url_2"),
    REFERRAL_URL("referral_url"),
    DOMAIN_KEY("domain_key"),
    UTM_SOURCE("utm_source"),
    UTM_CAMPAIGN("utm_campaign"),
    UTM_CONTENT(""),
    UTM_KEYWORD("utm_keyword"),
    UTM_MEDIUM("utm_medium"),
    UTM_TERM("utm_term"),
    USER_ID("user_id"),
    SPECIAL_TAG("special_tag"),
    CLIENT_NAME("client_name"),
    CITY("city"),
    STATE("state"),
    COUNTRY("country"),
    ADDED_ON("added_on"),
    LINUX_ADDED_ON("linux_added_on"),
    REGISTER_FLYER_ID("register_flyer_id"),
    FTD_CONVERT_SOURCE("std_convert_source"),
    SESSION_ID("session_id");

    private final String columnName;

    CpSource(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }


}
