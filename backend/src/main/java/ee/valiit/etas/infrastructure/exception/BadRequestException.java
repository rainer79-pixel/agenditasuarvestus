package ee.valiit.etas.infrastructure.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private final String message;
    private final Integer errorCode;

    public BadRequestException(String message, Integer errorCode) {
        super(message);
        this.message = message;
        this.errorCode = errorCode;
    }
}