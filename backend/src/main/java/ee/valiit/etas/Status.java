package ee.valiit.etas;

import lombok.Getter;

@Getter
public enum Status {
    ACTIVE("A", "ACTIVE"),
    SOFT_DELETED("D", "INACTIVE");

    private final String code;
    private final String apiValue;

    Status(String code, String apiValue) {
        this.code = code;
        this.apiValue = apiValue;
    }

    public static String toApiValue(String code) {
        for (Status s : values()) {
            if (s.code.equals(code)) return s.apiValue;
        }
        return code;
    }

    public static Status fromApiValue(String apiValue) {
        for (Status s : values()) {
            if (s.apiValue.equals(apiValue)) return s;
        }
        throw new IllegalArgumentException("Tundmatu staatus: " + apiValue);
    }

}