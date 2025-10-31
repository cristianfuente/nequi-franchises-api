package co.com.nequi.usecase.util;

import java.time.Instant;
import java.util.UUID;

public final class FunctionUtils {

    private FunctionUtils() {
    }

    public static String newId() {
        return UUID.randomUUID().toString();
    }

    public static long now() {
        return Instant.now().getEpochSecond();
    }

}
