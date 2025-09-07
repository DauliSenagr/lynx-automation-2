package utils.db;

import lombok.Getter;

@Getter
public enum KycStatus {

    DOC_VALIDATE("doc_validate"),
    DOC_SUCCESS("doc_success"),
    STATUS_VERIFIED("verified"),
    STATUS_INITIATED("initiated"),
    STATUS_REJECTED("rejected");

    private final String status;

    KycStatus(String status) {
        this.status = status;
    }

}
