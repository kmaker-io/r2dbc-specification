package io.kmaker.r2dbcspecification.test;

import io.kmaker.r2dbcspecification.spec.R2dbcGenericSpecificationImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final StudentRepo studentRepo;
    private final UserRepo userRepo;
    private final R2dbcGenericSpecificationImpl r2dbcGenericSpecification;

    @GetMapping("/studentdtos")
    public Flux<StudentDto> getStudentDto() {
        final var criteria = Criteria.where("age").greaterThanOrEquals(25);
        return studentRepo.findBySpec(criteria, Student.class, StudentDto.class);
    }

    @GetMapping("/students")
    public Mono<Page<StudentDto>> getStudents(
            @RequestParam("sort") final String sort,
            @RequestParam("order") final String order
    ) {
        final var direction = order.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        final var page = PageRequest.of(0, 30, direction, sort);
        final var criteria = Criteria.where("age").greaterThanOrEquals(20);
        return studentRepo.getPageBySpec(criteria, page, Student.class, StudentDto.class);
    }

    @GetMapping("/students/{studentId}")
    public Mono<StudentDto> getStudentById(
            @PathVariable("studentId") final Long studentId
    ) {
        final var criteria = Criteria.where("id").is(studentId)
                .and(Criteria.where("lastName").is("Doe")
                        .or("age").greaterThan(30));
        return studentRepo.findOneBySpec(criteria, Student.class, StudentDto.class);
    }

    @GetMapping("/users")
    public Flux<UserDto> getUsers(@RequestParam(value = "userId", required = false) final Long userId) {
        final var criteria = Objects.isNull(userId) ? Criteria.empty() : Criteria.where("users.id").is(userId);
        return r2dbcGenericSpecification.findBySpecWithRel(criteria, User.class, UserDto.class);
    }

    @GetMapping("/users/{userId}")
    public Mono<UserDto> getUser(@PathVariable("userId") final Long userId) {
        final var criteria = Criteria.where("users.id").is(userId);
        return r2dbcGenericSpecification.findOneBySpecWithRel(criteria, User.class, UserDto.class);
    }
}
