package io.kmaker.r2dbcspecification.helper;

import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import io.kmaker.r2dbcspecification.annotation.IgnoreMapping;
import io.kmaker.r2dbcspecification.spec.SqlMetadata;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import org.springframework.data.util.Pair;

import java.util.*;

public final class R2dbcHelper {

    private R2dbcHelper() {
    }

    public static String buildSql(final Criteria criteria,
                                  final Class<?> entityClass,
                                  final Class<?> dtoClass) {
        final var fromTable = getTableName(entityClass);
        final var joinFields = ReflectionHelper.getAllFields(dtoClass, f -> f.isAnnotationPresent(FetchRelatedEntity.class));

        final var joins = new StringBuilder();

        final var primaryKeyFromTable = getPrimaryKey(entityClass);
        final var columns = new ArrayList<>(selectFields(fromTable, entityClass));
        for (final var joinField : joinFields) {
            final var fetchMany = joinField.getDeclaredAnnotation(FetchRelatedEntity.class);
            final var relatedEntity = fetchMany.relatedEntity();
            final var joinTable = getTableName(relatedEntity);
            columns.addAll(selectFields(joinTable, relatedEntity));

            joins.append("\n").append("JOIN %s ON %s = %s".formatted(
                    joinTable,
                    toSqlIdentifier(fromTable, primaryKeyFromTable),
                    toSqlIdentifier(joinTable, fetchMany.foreignKey())
            ));
        }

        final var whereClause = criteria.isEmpty() ? "" : "\nWHERE %s".formatted(criteria.toString());

        return "SELECT " + String.join(", ", columns) +
                "\nFROM " + fromTable +
                joins +
                whereClause;
    }

    public static SqlMetadata buildSqlMetadata(final CriteriaDefinition criteria,
                                               final Class<?> entityClass,
                                               final Class<?> dtoClass) {
        final var fromTable = getTableName(entityClass);
        final var joinFields = ReflectionHelper.getAllFields(dtoClass, f -> f.isAnnotationPresent(FetchRelatedEntity.class));

        final var joins = new StringBuilder();
        final var relationTables = new HashMap<String, Pair<String, FetchRelatedEntity>>();

        final var primaryKeyFromTable = getPrimaryKey(entityClass);
        final var columns = new ArrayList<>(selectFields(fromTable, entityClass));
        for (final var joinField : joinFields) {
            final var fetchRelation = joinField.getDeclaredAnnotation(FetchRelatedEntity.class);
            final var relatedEntity = fetchRelation.relatedEntity();
            final var joinTable = getTableName(relatedEntity);
            columns.addAll(selectFields(joinTable, relatedEntity));

            relationTables.put(joinField.getName(), Pair.of(joinTable, fetchRelation));

            joins.append("\n").append("%s %s ON %s = %s".formatted(
                    fetchRelation.joinType().getValue(),
                    joinTable,
                    toSqlIdentifier(fromTable, primaryKeyFromTable),
                    toSqlIdentifier(joinTable, fetchRelation.foreignKey())
            ));
        }

        return new SqlMetadata()
                .setFromTable(fromTable)
                .setPrimaryKey(getAliasField(fromTable, primaryKeyFromTable))
                .setJoins(joins.toString())
                .setRelationTables(relationTables)
                .setSelectedColumns(columns)
                .setCriteria(criteria);
    }

    private static List<String> selectFields(final String tableName,
                                             final Class<?> clz) {
        final var fields = ReflectionHelper.getAllFields(clz, f -> !f.isAnnotationPresent(IgnoreMapping.class));
        return fields.stream()
                .map(f -> "%s.%s".formatted(tableName, toSnakeCase(f.getName())) + " AS %s".formatted(SqlIdentifier.quoted(tableName + "_" + f.getName())))
                .toList();
    }

    public static String getSelectDistinctIdSql(final Class<?> entityClass) {
        final var idFields = ReflectionHelper.getAllFields(entityClass, f -> f.isAnnotationPresent(Id.class));
        if (idFields.isEmpty()) {
            throw new IllegalArgumentException("Cannot find primary key annotate with @Id of class '%s'".formatted(entityClass.getSimpleName()));
        }
        final var idField = idFields.getFirst();
        final var tableName = getTableName(entityClass);
        return "SELECT %s FROM %s".formatted(idField, tableName);
    }

    public static String toSqlIdentifier(final String tableName,
                                         final String field) {
        return "%s.%s".formatted(tableName, field);
    }

    public static String getAliasPrimaryKey(final Class<?> entityClass) {
        return "%s_%s".formatted(getTableName(entityClass), getPrimaryKey(entityClass));
    }

    public static String getAliasField(final String table,
                                       final String field) {
        return "%s_%s".formatted(table, field);
    }

    public static String getPrimaryKey(final Class<?> entityClass) {
        final var idFields = ReflectionHelper.getAllFields(entityClass, f -> f.isAnnotationPresent(Id.class));
        if (idFields.isEmpty()) {
            throw new IllegalArgumentException("Cannot find primary key annotate with @Id of class '%s'".formatted(entityClass.getSimpleName()));
        }
        final var idField = idFields.getFirst();
        return idField.getName();
    }

    public static String getTableName(final Class<?> entityClass) {
        final var table = entityClass.getDeclaredAnnotation(Table.class);
        return Objects.nonNull(table) ? getTableName(table) : toSnakeCase(entityClass.getSimpleName());
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
