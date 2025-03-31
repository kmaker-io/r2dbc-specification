package io.kmaker.r2dbcspecification.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("borrowing")
@Getter
@Setter
public class Borrowing {
    @Id
    private Long id;
    @Column("book_id")
    private Long bookId;
    @Column("member_id")
    private Long memberId;
    @Column("borrow_date")
    private LocalDate borrowDate;
    @Column("return_date")
    private LocalDate returnDate;
}
