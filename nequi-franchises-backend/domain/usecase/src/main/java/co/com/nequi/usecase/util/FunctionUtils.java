package co.com.nequi.usecase.util;

import co.com.nequi.usecase.constant.ExceptionMessage;
import co.com.nequi.usecase.exception.ValidationException;

import java.time.Instant;
import java.util.UUID;

public final class FunctionUtils {

    private FunctionUtils() {
    }

    public static void validateNotEmptyValue(String value,
                                             ExceptionMessage exceptionMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(exceptionMessage);
        }
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static long now() {
        return Instant.now().getEpochSecond();
    }

}
