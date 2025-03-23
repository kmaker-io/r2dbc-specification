package io.kmaker.r2dbcspecification.spec;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface R2dbcGenericSpecification {

    <T, R> Mono<R> findOneBySpec(CriteriaDefinition criteria,
                                 Class<T> entityClass,
                                 Class<R> dtoClass);

    <T, R> Flux<R> findBySpec(CriteriaDefinition criteria,
                              Class<T> entityClass,
                              Class<R> dtoClass);

    <T, R> Flux<R> findBySpec(CriteriaDefinition criteria,
                              Sort sort,
                              Class<T> entityClass,
                              Class<R> dtoClass);

    <T, R> Flux<R> findBySpec(CriteriaDefinition criteria,
                              Pageable pageable,
                              Class<T> entityClass,
                              Class<R> dtoClass);

    <T, R> Mono<Page<R>> getPageBySpec(CriteriaDefinition criteria,
                                       Pageable pageable,
                                       Class<T> entityClass,
                                       Class<R> dtoClas);

    // Handle relationship entity
    <T, R> Mono<R> findOneBySpecWithRel(CriteriaDefinition criteria,
                                        Class<T> entityClass,
                                        Class<R> dtoClass);

    <T, R> Flux<R> findBySpecWithRel(CriteriaDefinition criteria,
                                     Class<T> entityClass,
                                     Class<R> dtoClass);
}
