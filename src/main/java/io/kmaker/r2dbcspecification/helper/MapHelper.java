package io.kmaker.r2dbcspecification.helper;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public final class MapHelper {

    private MapHelper() {
    }

    public static Map<String, Object> getMapWithPrefixKey(final Map<String, Object> data,
                                                          final String prefix) {
        return getMapWithPrefixKey(data, prefix, true);
    }

    public static Map<String, Object> getMapWithPrefixKey(final Map<String, Object> data,
                                                          final String prefix,
                                                          final boolean removePrefix) {
        if (Objects.isNull(data)) {
            return null;
        }
        return data.entrySet()
                .stream()
                .filter(item -> item.getKey().startsWith(prefix) && Objects.nonNull(item.getValue()))
                .collect(Collectors.toMap(
                        km -> removePrefix ? km.getKey().replace(prefix + "_", "") : km.getKey(),
                        Map.Entry::getValue,
                        (existing, replacement) -> existing
                ));
    }
}
