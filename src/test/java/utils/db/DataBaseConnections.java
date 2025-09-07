package utils.db;

import lombok.Getter;

@Getter
public enum DataBaseConnections {

    APPLICATION("app"),
    KYC("kyc"),
    PSQL("psql");

    private final String resource;

    DataBaseConnections(String resource) {
        this.resource = resource;
    }


}
