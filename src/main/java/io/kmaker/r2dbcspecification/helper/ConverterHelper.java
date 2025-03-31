package io.kmaker.r2dbcspecification.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public final class ConverterHelper {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER.copy();
    }

    public static <T> T convert(final Object fromValue,
                                final Class<T> clazz) {
        return OBJECT_MAPPER.convertValue(fromValue, clazz);
    }

    public static <T> T convert(final Object fromValue,
                                final TypeReference<T> typeReference) {
        return OBJECT_MAPPER.convertValue(fromValue, typeReference);
    }
}
