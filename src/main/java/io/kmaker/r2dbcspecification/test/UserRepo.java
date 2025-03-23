package io.kmaker.r2dbcspecification.test;

import io.kmaker.r2dbcspecification.spec.R2dbcGenericSpecification;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

public interface UserRepo extends R2dbcRepository<User, Long>, R2dbcGenericSpecification {
}
