package io.kmaker.r2dbcspecification.spec;

import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import io.kmaker.r2dbcspecification.helper.ConverterHelper;
import io.kmaker.r2dbcspecification.helper.MapHelper;
import io.kmaker.r2dbcspecification.helper.R2dbcHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.util.Pair;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;

@RequiredArgsConstructor
public class R2dbcGenericSpecificationImpl implements R2dbcGenericSpecification {
    private final R2dbcEntityTemplate template;

    public <T, R> Mono<R> findOneBySpec(final CriteriaDefinition criteria,
                                        final Class<T> entityClass,
                                        final Class<R> dtoClass) {
        return Mono.defer(() -> {
            final var query = Query.query(criteria);
            return template.select(entityClass)
                    .as(dtoClass)
                    .matching(query)
                    .first();
        });
    }

    @Override
    public <T, R> Flux<R> findBySpec(final CriteriaDefinition criteria,
                                     final Class<T> entityClass,
                                     final Class<R> dtoClass) {
        return findBySpec(criteria, Sort.unsorted(), entityClass, dtoClass);
    }

    @Override
    public <T, R> Flux<R> findBySpec(final CriteriaDefinition criteria,
                                     final Sort sort,
                                     final Class<T> entityClass,
                                     final Class<R> dtoClass) {
        return findBySpec(criteria, Pageable.unpaged(sort), entityClass, dtoClass);
    }

    @Override
    public <T, R> Flux<R> findBySpec(final CriteriaDefinition criteria,
                                     final Pageable pageable,
                                     final Class<T> entityClass,
                                     final Class<R> dtoClass) {
        return Flux.defer(() -> {
            var query = Query.query(criteria);
            if (Objects.nonNull(pageable)) {
                query = query.with(pageable);
            }
            return template.select(entityClass)
                    .as(dtoClass)
                    .matching(query)
                    .all();
        });
    }

    @Override
    public <T, R> Mono<Page<R>> getPageBySpec(final CriteriaDefinition criteria,
                                              final Pageable pageable,
                                              final Class<T> entityClass,
                                              final Class<R> dtoClass) {
        return Mono.defer(() -> {
            final var query = Query.query(criteria)
                    .with(pageable);
            final var count = template.count(query, entityClass);
            final var dataFlux = template.select(entityClass)
                    .as(dtoClass)
                    .matching(query)
                    .all()
                    .collectList();
            return dataFlux.zipWith(count)
                    .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
        });
    }

    @Override
    public <T, R> Mono<R> findOneBySpecWithRel(final CriteriaDefinition criteria,
                                               final Class<T> entityClass,
                                               final Class<R> dtoClass) {
        return findBySpecWithRel(criteria, entityClass, dtoClass).next();
    }

    @Override
    public <T, R> Flux<R> findBySpecWithRel(final CriteriaDefinition criteria,
                                            final Class<T> entityClass,
                                            final Class<R> dtoClass) {
        return Flux.defer(() -> {
            final var dbClient = template.getDatabaseClient();
            final var sqlMetadata = R2dbcHelper.buildSqlMetadata(criteria, entityClass, dtoClass);
            return dbClient.sql(sqlMetadata.toSql())
                    .fetch()
                    .all()
                    .collectMultimap(map -> map.get(sqlMetadata.getAliasPrimaryKey()), Function.identity(), LinkedHashMap::new)
                    .flatMapMany(map -> Flux.fromIterable(map.entrySet()).map(entry -> mapProperties(dtoClass, entry, sqlMetadata)));
        });
    }

    @Override
    public <T, R> Flux<R> findBySpecWithRel(final CriteriaDefinition criteria,
                                            final Sort sort,
                                            final Class<T> entityClass,
                                            final Class<R> dtoClass) {
        return findBySpecWithRel(criteria, Pageable.unpaged(sort), entityClass, dtoClass);
    }

    @Override
    public <T, R> Flux<R> findBySpecWithRel(final CriteriaDefinition criteria,
                                            final Pageable pageable,
                                            final Class<T> entityClass,
                                            final Class<R> dtoClass) {
        return Flux.defer(() -> {
            final var dbClient = template.getDatabaseClient();
            final var sqlMetadata = R2dbcHelper.buildSqlMetadata(criteria, entityClass, dtoClass);
            final var primaryKeys = dbClient.sql(sqlMetadata.toSqlDistinctPrimaryKey(pageable))
                    .map((row, metadata) -> row.get(0))
                    .all()
                    .collectList();
            return primaryKeys.flatMapMany(pks -> {
                        if (pks.isEmpty()) {
                            return Flux.empty();
                        }
                        return dbClient.sql(sqlMetadata.toSqlWhereInPrimaryKeys(pks, pageable))
                                .fetch()
                                .all()
                                .collectMultimap(map -> map.get(sqlMetadata.getAliasPrimaryKey()), Function.identity(), LinkedHashMap::new)
                                .flatMapMany(map -> Flux.fromIterable(map.entrySet()).map(entry -> mapProperties(dtoClass, entry, sqlMetadata)));
                    }
            );
        });
    }

    @Override
    public <T, R> Mono<Page<R>> getPageBySpecWithRel(final CriteriaDefinition criteria,
                                                     final Pageable pageable,
                                                     final Class<T> entityClass,
                                                     final Class<R> dtoClass) {
        return Mono.defer(() -> {
            final var dbClient = template.getDatabaseClient();
            final var sqlMetadata = R2dbcHelper.buildSqlMetadata(criteria, entityClass, dtoClass);

            final var primaryKeys = dbClient.sql(sqlMetadata.toSqlDistinctPrimaryKey(pageable))
                    .map((row, metadata) -> row.get(0))
                    .all()
                    .collectList();

            final var count = dbClient.sql(sqlMetadata.toSqlCount())
                    .mapValue(Long.class)
                    .one();

            return primaryKeys
                    .flatMap(pks -> {
                        if (pks.isEmpty()) {
                            return Mono.just(new PageImpl<>(List.of(), pageable, 0));
                        }
                        return dbClient.sql(sqlMetadata.toSqlWhereInPrimaryKeys(pks, pageable))
                                .fetch()
                                .all()
                                .collectMultimap(map -> map.get(sqlMetadata.getAliasPrimaryKey()), Function.identity(), LinkedHashMap::new)
                                .flatMapMany(map -> Flux.fromIterable(map.entrySet()).map(entry -> mapProperties(dtoClass, entry, sqlMetadata)))
                                .collectList()
                                .zipWith(count)
                                .map(tuple2 -> new PageImpl<>(tuple2.getT1(), pageable, tuple2.getT2()));
                    });
        });
    }

    private static <R> R mapProperties(final Class<R> dtoClass,
                                       final Map.Entry<Object, Collection<Map<String, Object>>> entry,
                                       final SqlMetadata sqlMetadata) {
        final var rows = entry.getValue();
        final var mainData = MapHelper.getMapWithPrefixKey(rows.iterator().next(), sqlMetadata.getFromTable());
        final var mainEntity = ConverterHelper.convert(mainData, dtoClass);
        final var beanWrapper = new BeanWrapperImpl(mainEntity);
        for (final var relation : sqlMetadata.getRelationTables().entrySet()) {
            final var propertyName = relation.getKey();
            final var value = mapNestProperties(relation, rows);
            if (Objects.nonNull(value)) {
                beanWrapper.setPropertyValue(propertyName, value);
            }
        }
        return mainEntity;
    }

    private static Object mapNestProperties(final Map.Entry<String, Pair<String, FetchRelatedEntity>> relation,
                                            final Collection<Map<String, Object>> rows) {
        final var prefix = relation.getValue().getFirst();
        final var fetchRelated = relation.getValue().getSecond();
        final var valueStream = rows.stream()
                .map(d -> MapHelper.getMapWithPrefixKey(d, prefix))
                .map(d -> ConverterHelper.convert(d, fetchRelated.dto()));
        return fetchRelated.type().equals(FetchRelatedEntity.RelationType.ONE_TO_MANY) ?
                valueStream.toList() :
                valueStream.findFirst().orElse(null);
    }
}
