package io.kmaker.r2dbcspecification.helper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ReflectionHelper {

    private ReflectionHelper() {
    }

    public static List<Field> getAllFields(Class<?> clz) {
        final var fields = new ArrayList<Field>();
        while (Objects.nonNull(clz)) {
            fields.addAll(Arrays.asList(clz.getDeclaredFields()));
            clz = clz.getDeclaringClass();
        }
        return fields;
    }

    public static List<Field> getAllFields(final Class<?> clz,
                                           final Predicate<Field> predicate) {
        final var fields = getAllFields(clz);
        return fields.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

}
