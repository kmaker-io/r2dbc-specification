package io.kmaker.r2dbcspecification.spec;

import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
}
