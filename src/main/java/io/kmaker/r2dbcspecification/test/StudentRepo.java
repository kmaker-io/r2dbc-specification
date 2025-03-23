package io.kmaker.r2dbcspecification.test;

import io.kmaker.r2dbcspecification.spec.R2dbcGenericSpecification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface StudentRepo extends R2dbcRepository<Student, Long>, R2dbcGenericSpecification {
}
