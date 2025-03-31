package io.kmaker.r2dbcspecification.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import io.kmaker.r2dbcspecification.annotation.IgnoreMapping;
import io.kmaker.r2dbcspecification.spec.SqlMetadata;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class R2dbcHelper {

    private R2dbcHelper() {
    }

    private static final Set<Integer> IGNORE_MODIFIERS = Set.of(
            Modifier.STATIC,
            Modifier.TRANSIENT
    );
    private static final Cache<String, Object> LOCAL_CACHE = Caffeine.newBuilder()
            .maximumSize(1024)
            .expireAfterAccess(Duration.ofMinutes(10))
            .build();

    public static SqlMetadata buildSqlMetadata(final CriteriaDefinition criteria,
                                               final Class<?> entityClass,
                                               final Class<?> dtoClass) {
        final var fromTable = getTableName(entityClass);
        final var joinFields = ReflectionHelper.getAllFields(dtoClass, f -> f.isAnnotationPresent(FetchRelatedEntity.class));

        final var joins = new StringBuilder();
        final var relationTables = new HashMap<String, Pair<String, FetchRelatedEntity>>();

        final var primaryKeyFromTable = getPrimaryKey(entityClass);
        final var mainFields = selectFields(fromTable, dtoClass, f -> joinFields.isEmpty() || !joinFields.contains(f));
        final var columns = new ArrayList<>(mainFields);

        // Always select primary table if it is not selected from dto
        final var selectPrimaryKeyField = getSelectFieldWithAlias(fromTable, primaryKeyFromTable);
        if (!columns.contains(selectPrimaryKeyField)) {
            columns.addFirst(selectPrimaryKeyField);
        }

        final var fromTableFields = ReflectionHelper.getAllFields(entityClass)
                .stream()
                .map(field -> {
                    final var column = field.getDeclaredAnnotation(Column.class);
                    return Objects.nonNull(column) ? column.value() : field.getName();
                })
                .collect(Collectors.toSet());

        for (final var joinField : joinFields) {
            final var fetchRelation = joinField.getDeclaredAnnotation(FetchRelatedEntity.class);
            final var relatedEntity = fetchRelation.relatedEntity();
            final var joinTable = getTableName(relatedEntity);
            columns.addAll(selectFields(joinTable, relatedEntity));
            final var primaryJoinTable = getPrimaryKey(relatedEntity);

            relationTables.put(joinField.getName(), Pair.of(joinTable, fetchRelation));

            // if A has many B, it means A.id join B.a_id
            // We can fetch A from B or B from A, But it still A.id join B.a_id in terms of SQL join

            final var isForeignKeyOfFromTable = fromTableFields.contains(fetchRelation.foreignKey());

            final var leftSide = isForeignKeyOfFromTable ?
                    toSqlIdentifier(fromTable, fetchRelation.foreignKey()) : toSqlIdentifier(fromTable, primaryKeyFromTable);
            final var rightSide = isForeignKeyOfFromTable ?
                    toSqlIdentifier(joinTable, primaryJoinTable) : toSqlIdentifier(joinTable, fetchRelation.foreignKey());;

            joins.append("\n").append("%s %s ON %s = %s".formatted(
                    fetchRelation.joinType().getValue(),
                    joinTable,
                    leftSide,
                    rightSide
            ));
        }

        return new SqlMetadata()
                .setFromTable(fromTable)
                .setPrimaryKey(toSqlIdentifier(fromTable, primaryKeyFromTable))
                .setAliasPrimaryKey(getAliasField(fromTable, primaryKeyFromTable))
                .setJoins(joins.toString())
                .setRelationTables(relationTables)
                .setSelectedColumns(columns)
                .setCriteria(criteria);
    }

    private static List<String> selectFields(final String tableName,
                                             final Class<?> clz) {
        return selectFields(tableName, clz, s -> true);
    }

    @SuppressWarnings("unchecked")
    private static List<String> selectFields(final String tableName,
                                             final Class<?> clz,
                                             final Predicate<Field> predicate) {
        final var key = "selectFields-" + tableName + "-" + clz.getName();
        return (List<String>) LOCAL_CACHE.get(key, k -> {
            final var fields = ReflectionHelper.getAllFields(clz, R2dbcHelper::ignoreField);
            return fields.stream()
                    .filter(predicate)
                    .map(f -> getSelectFieldWithAlias(tableName, f.getName()))
                    .toList();
        });
    }

    private static boolean ignoreField(final Field field) {
        return !field.isAnnotationPresent(IgnoreMapping.class) ||
                IGNORE_MODIFIERS.contains(field.getModifiers());
    }

    private static String getSelectFieldWithAlias(final String tableName,
                                                  final String fieldName) {
        return "%s.%s".formatted(tableName, toSnakeCase(fieldName)) + " AS %s".formatted(SqlIdentifier.quoted(tableName + "_" + fieldName));
    }

    public static String toSqlIdentifier(final String tableName,
                                         final String field) {
        return "%s.%s".formatted(tableName, field);
    }

    public static String getAliasField(final String table,
                                       final String field) {
        return "%s_%s".formatted(table, field);
    }

    public static String getPrimaryKey(final Class<?> entityClass) {
        final var key = "getPrimaryKey-" + entityClass.getName();
        return (String) LOCAL_CACHE.get(key, k -> {
            final var idFields = ReflectionHelper.getAllFields(entityClass, f -> f.isAnnotationPresent(Id.class));
            if (idFields.isEmpty()) {
                throw new IllegalArgumentException("Cannot find primary key annotate with @Id of class '%s'".formatted(entityClass.getSimpleName()));
            }
            final var idField = idFields.getFirst();
            final var idColumn = idField.getDeclaredAnnotation(Column.class);
            return Objects.nonNull(idColumn) ? idColumn.value() : toSnakeCase(idField.getName());
        });
    }

    public static String getTableName(final Class<?> entityClass) {
        final var key = "getTableName-" + entityClass.getName();
        return (String) LOCAL_CACHE.get(key, k -> {
            final var table = entityClass.getDeclaredAnnotation(Table.class);
            return Objects.nonNull(table) ? getTableName(table) : toSnakeCase(entityClass.getSimpleName());
        });
    }

    public static String getTableName(final Table table) {
        final var name = StringUtils.isBlank(table.name()) ? table.value() : table.name();
        final var schema = table.schema();
        return StringUtils.isBlank(schema) ? name : "%s.%s".formatted(schema, name);
    }

    public static String toSnakeCase(final String camelCase) {
        final var tokens = StringUtils.splitByCharacterTypeCamelCase(camelCase);
        return String.join("_", tokens);
    }

}
