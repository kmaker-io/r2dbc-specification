package io.kmaker.r2dbcspecification.spec;

import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.domain.Pageable;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class SqlMetadata {
    private String fromTable;
    private String primaryKey;
    private String aliasPrimaryKey;
    private Map<String, Pair<String, FetchRelatedEntity>> relationTables;
    private List<String> selectedColumns;
    private CriteriaDefinition criteria;
    private String joins;

    public String toSql() {
        final var whereClause = criteria.isEmpty() ? "" : "\nWHERE %s".formatted(criteria.toString());
        return "SELECT " + String.join(", ", selectedColumns) +
                "\nFROM " + fromTable +
                joins +
                whereClause;
    }

    /**
     * Get primary key for handle limit and offset with join relationship.
     */
    public String toSqlDistinctPrimaryKey(final Pageable pageable) {
        final var whereClause = criteria.isEmpty() ? "" : "\nWHERE %s".formatted(criteria.toString());
        return "SELECT DISTINCT " + primaryKey +
                "\nFROM " + fromTable +
                joins +
                whereClause +
                getOrderBy(pageable) +
                "\nLIMIT %s OFFSET %s".formatted(pageable.getPageSize(), pageable.getOffset());
    }

    /**
     * Get all records of join query
     */
    public String toSqlWhereInPrimaryKeys(final List<Object> primaryKeys,
                                          final Pageable pageable) {
        final var whereClause = "\nWHERE %s".formatted(Criteria.where(primaryKey).in(primaryKeys).toString());
        return "SELECT " + String.join(", ", selectedColumns) +
                "\nFROM " + fromTable +
                joins +
                whereClause +
                getOrderBy(pageable);
    }

    /**
     * Get total number of records
     */
    public String toSqlCount() {
        final var whereClause = criteria.isEmpty() ? "" : "\nWHERE %s".formatted(criteria.toString());
        return "SELECT COUNT(DISTINCT %s)".formatted(primaryKey) +
                "\nFROM " + fromTable +
                joins +
                whereClause;
    }

    private String getOrderBy(final Pageable pageable) {
        final var orders = pageable.getSort()
                .stream()
                .map(o -> o.getProperty() + " " + o.getDirection().name())
                .toList();
        return orders.isEmpty() ? "" : "\nORDER BY " + String.join(", ", orders);
    }
}
