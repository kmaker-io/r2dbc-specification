package io.kmaker.r2dbcspecification.spec;

import io.kmaker.r2dbcspecification.R2dbcTestConfig;
import io.kmaker.r2dbcspecification.config.R2dbcSpecificationAutoConfiguration;
import io.kmaker.r2dbcspecification.dto.BookDetailDto;
import io.kmaker.r2dbcspecification.dto.BookInfoDto;
import io.kmaker.r2dbcspecification.dto.BorrowingInfoDto;
import io.kmaker.r2dbcspecification.entity.Book;
import io.kmaker.r2dbcspecification.entity.Borrowing;
import io.kmaker.r2dbcspecification.entity.Member;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@Import(value = {R2dbcTestConfig.class, R2dbcSpecificationAutoConfiguration.class})
class R2dbcGenericSpecificationTest {

    @Autowired
    private R2dbcGenericSpecification r2dbcGenericSpecification;

    @BeforeAll
    static void setup(@Autowired final R2dbcEntityTemplate r2dbcEntityTemplate) {
        R2dbcTestConfig.initData(r2dbcEntityTemplate);
    }

    @Test
    void testFindOneBySpec() {
        // Book Id = 2 -> 'Harry Potter and the Chamber of Secrets', 'Fantasy', 1998, 1, 4
        final var idSpec = Criteria.where("id").is(2);
        final var bookId2Mono = r2dbcGenericSpecification.findOneBySpec(idSpec, Book.class, BookInfoDto.class);

        StepVerifier.create(bookId2Mono)
                .expectNextMatches(bookInfo -> {
                    assertEquals(2, bookInfo.getId());
                    assertEquals("Harry Potter and the Chamber of Secrets", bookInfo.getTitle());
                    assertEquals("Fantasy", bookInfo.getGenre());
                    assertEquals(1998, bookInfo.getPublicationYear());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testFindOneBySpecWithRel() {
        // Book id = 10 -> 'And Then There Were None', 'Mystery', 1939, 5, 2
        // Author id = 5 -> 'Agatha Christie', 1890
        final var idSpec = Criteria.where("book.id").is(10);
        final var bookId10Mono = r2dbcGenericSpecification.findOneBySpecWithRel(idSpec, Book.class, BookDetailDto.class);

        StepVerifier.create(bookId10Mono)
                .expectNextMatches(bookDetailDto -> {
                    assertEquals(10, bookDetailDto.getId());
                    assertEquals("And Then There Were None", bookDetailDto.getTitle());
                    assertEquals("Mystery", bookDetailDto.getGenre());
                    assertEquals(1939, bookDetailDto.getPublicationYear());
                    assertEquals(2, bookDetailDto.getAvailableCopies());

                    final var author = bookDetailDto.getAuthorDto();
                    assertEquals(5, author.getId());
                    assertEquals("Agatha Christie", author.getName());
                    assertEquals(1890, author.getBirthYear());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testGetPageBySpecWithRel() {
        final var empty = Criteria.empty();
        var pageable = PageRequest.of(0, 3, Sort.Direction.ASC, "borrowing.id");
        var borrowingMono = r2dbcGenericSpecification.getPageBySpecWithRel(empty, pageable, Borrowing.class, BorrowingInfoDto.class);

        StepVerifier.create(borrowingMono)
                .expectNextMatches(page -> {
                    assertEquals(20, page.getTotalElements());
                    assertEquals(3, page.getNumberOfElements());
                    assertEquals(0, page.getNumber());

                    final var content = page.getContent();

                    final var first = content.getFirst();
                    final var third = content.getLast();
                    assertEquals(1, first.getId());
                    assertEquals(2, content.get(1).getId());
                    assertEquals(3, third.getId());

                    // borrowing id = 1 -> 1, 1, '2025-03-01', NULL
                    // book id = 1 -> 'Harry Potter and the Sorcerer''s Stone', 'Fantasy', 1997, 1, 3
                    // member id = 1 -> 'Alice Johnson', 'alice.johnson@example.com', '123-456-7890'
                    assertEquals(LocalDate.of(2025, 3, 1), first.getBorrowDate());
                    assertNull(first.getReturnDate());

                    var book = first.getBook();
                    assertEquals(1, book.getId());
                    assertEquals("Harry Potter and the Sorcerer's Stone", book.getTitle());

                    var member = first.getMember();
                    assertEquals(1, member.getId());
                    assertEquals("Alice Johnson", member.getFullName());

                    // borrowing id = 3 -> 3, 2, '2025-03-02', '2025-03-10'
                    // book id = 3 -> '1984', 'Dystopian', 1949, 2, 2
                    // member id = 2 -> 'Bob Smith', 'bob.smith@example.com', '987-654-3210'
                    book = third.getBook();
                    assertEquals(3, book.getId());
                    assertEquals("1984", book.getTitle());

                    member = third.getMember();
                    assertEquals(2, member.getId());
                    assertEquals("Bob Smith", member.getFullName());

                    return true;
                })
                .verifyComplete();

        pageable = PageRequest.of(3, 3, Sort.Direction.ASC, "borrowing.id");
        borrowingMono = r2dbcGenericSpecification.getPageBySpecWithRel(empty, pageable, Borrowing.class, BorrowingInfoDto.class);

        StepVerifier.create(borrowingMono)
                .expectNextMatches(page -> {
                    assertEquals(3, page.getNumber());

                    final var content = page.getContent();
                    assertEquals(10, content.getFirst().getId());
                    assertEquals(12, content.getLast().getId());
                    return true;
                })
                .verifyComplete();

        pageable = PageRequest.of(10, 3, Sort.Direction.ASC, "borrowing.id");
        borrowingMono = r2dbcGenericSpecification.getPageBySpecWithRel(empty, pageable, Borrowing.class, BorrowingInfoDto.class);

        StepVerifier.create(borrowingMono)
                .expectNextMatches(page -> {
                    final var content = page.getContent();
                    assertEquals(0, content.size());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testGetPageBySpec() {
        final var pageable = PageRequest.of(0, 30, Sort.Direction.DESC, "member.id");
        final var memberSpec = Criteria.empty();
        final var pageMono = r2dbcGenericSpecification.getPageBySpec(memberSpec, pageable, Member.class, Member.class);

        StepVerifier.create(pageMono)
                .expectNextMatches(page -> {
                    assertEquals(15, page.getTotalElements());
                    final var content = page.getContent();
                    assertEquals(15, content.getFirst().getId());
                    assertEquals(1, content.getLast().getId());

                    assertEquals("Oliver Thomas", content.getFirst().getFullName());
                    assertEquals("Alice Johnson", content.getLast().getFullName());
                    return true;
                })
                .verifyComplete();
    }
}