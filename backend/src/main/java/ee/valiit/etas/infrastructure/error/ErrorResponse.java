package ee.valiit.etas.infrastructure.error;

import lombok.Getter;

@Getter
// Auth        → 1xx
// Seller      → 2xx
// Contact     → 3xx  (sh Region 32x)
// Commission  → 4xx
// Excel       → 5xx
// Invoice     → 6xx
// User        → 7xx
// VAT         → 8xx
// ProductType → 9xx

public enum ErrorResponse {

    // Auth
    INCORRECT_CREDENTIALS("Vale e-mail või parool", 111),
    ACCESS_DENIED("Teil pole selleks õigust", 115),

    // Seller
    SELLER_NOT_FOUND("Edasimüüjat ei leitud", 211),
    SELLER_ALREADY_INACTIVE("Edasimüüja on juba deaktiveeritud", 212),
    SELLER_ORG_ID_ALREADY_EXISTS("Selle ettevõtte ID-ga edasimüüja on juba olemas", 213),
    SELLER_ALREADY_ACTIVE("Edasimüüja on juba aktiivne", 214),

    // Contact
    CONTACT_NOT_FOUND("Kontakti ei leitud", 311),
    ROLE_NOT_FOUND("Rolli ei leitud", 312),

    // Region
    SELLER_REGION_NOT_FOUND("Piirkonda ei leitud", 321),
    SELLER_REGION_ALREADY_EXISTS("See piirkond on juba lisatud", 322),


    // Commission rate
    COMMISSION_RATE_NOT_FOUND("Teenustasu määra ei leitud", 411),
    COMMISSION_RATE_IN_USE("Teenustasu on kasutuses ja seda ei saa kustutada", 412),
    COMMISSION_RATE_ALREADY_EXISTS("Sellel tootegrupil on juba kehtiv teenustasu", 413),

    // Excel import
    IMPORT_SELLER_NOT_FOUND("Impordifailis on tundmatu edasimüüja", 511),
    IMPORT_PRODUCT_GROUP_UNKNOWN("Impordifailis on tundmatu tootegrupp", 512),
    IMPORT_PERIOD_ALREADY_EXISTS("Sellel perioodil on aruanne juba olemas", 513),
    IMPORT_INVALID_HEADER("Laetav raport ei vasta header tingimustele", 514),
    REPORT_NOT_FOUND("Aruannet ei leitud", 516),

    // Invoice
    INVOICE_NOT_FOUND("Arvet ei leitud", 611),
    INVOICE_ALREADY_EXISTS("Sellel perioodil on arve juba sisestatud", 612),

    // User
    USER_NOT_FOUND("Kasutajat ei leitud", 711),
    USER_EMAIL_ALREADY_EXISTS("Selle e-mailiga kasutaja on juba olemas", 712),
    USER_ALREADY_INACTIVE("Kasutaja on juba deaktiveeritud", 713),

    // ProductType
    PRODUCT_TYPE_NOT_FOUND("Tootegruppi ei leitud", 911),

    // VAT
    VAT_SETTING_NOT_FOUND("KM määra ei leitud", 811),
    ;

    private final String message;
    private final Integer errorCode;

    ErrorResponse(String message, Integer errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }
}