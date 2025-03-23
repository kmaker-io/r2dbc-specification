package io.kmaker.r2dbcspecification.annotation;

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Fetch relationship to map in dto class
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FetchRelatedEntity {

    Class<?> relatedEntity();

    Class<?> dto();

    String foreignKey();

    RelationType type();

    JOIN_TYPE joinType() default JOIN_TYPE.JOIN;

    enum RelationType {
        ONE_TO_ONE,
        ONE_TO_MANY
    }

    @Getter
    enum JOIN_TYPE {
        JOIN("JOIN"),
        LEFT_JOIN("LEFT JOIN"),
        RIGHT_JOIN("RIGHT JOIN");

        private final String value;

        JOIN_TYPE(final String value) {
            this.value = value;
        }
    }
}
