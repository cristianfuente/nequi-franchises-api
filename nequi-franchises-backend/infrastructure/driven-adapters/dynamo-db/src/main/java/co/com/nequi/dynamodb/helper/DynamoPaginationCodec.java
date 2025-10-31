package co.com.nequi.dynamodb.helper;

import co.com.nequi.usecase.constant.ExceptionMessage;
import co.com.nequi.usecase.exception.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public final class DynamoPaginationCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private DynamoPaginationCodec() {
    }

    public static String encode(Map<String, AttributeValue> key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        Map<String, Map<String, String>> serializable = new HashMap<>();
        key.forEach((attr, value) -> {
            Map<String, String> data = new HashMap<>();
            if (value.s() != null) {
                data.put("S", value.s());
            }
            if (value.n() != null) {
                data.put("N", value.n());
            }
            if (!data.isEmpty()) {
                serializable.put(attr, data);
            }
        });
        if (serializable.isEmpty()) {
            return null;
        }
        try {
            String json = OBJECT_MAPPER.writeValueAsString(serializable);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to encode pagination key", e);
        }
    }

    public static Map<String, AttributeValue> decode(String cursor) {
        if (cursor == null) {
            return null;
        }
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            String json = new String(decoded, StandardCharsets.UTF_8);
            Map<String, Map<String, String>> data = OBJECT_MAPPER.readValue(json, new TypeReference<>() {
            });
            Map<String, AttributeValue> result = new HashMap<>();
            data.forEach((attr, valueMap) -> {
                AttributeValue.Builder builder = AttributeValue.builder();
                if (valueMap.containsKey("S")) {
                    builder.s(valueMap.get("S"));
                } else if (valueMap.containsKey("N")) {
                    builder.n(valueMap.get("N"));
                }
                result.put(attr, builder.build());
            });
            return result;
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new ValidationException(ExceptionMessage.INVALID_PAGINATION_CURSOR);
        }
    }
}
