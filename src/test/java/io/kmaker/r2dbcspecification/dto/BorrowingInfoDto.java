package io.kmaker.r2dbcspecification.dto;


import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import io.kmaker.r2dbcspecification.entity.Book;
import io.kmaker.r2dbcspecification.entity.Member;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BorrowingInfoDto {
    private Long id;
    @FetchRelatedEntity(
            relatedEntity = Book.class,
            dto = BookWithIdAndTitle.class,
            foreignKey = "book_id",
            joinType = FetchRelatedEntity.JOIN_TYPE.JOIN,
            type = FetchRelatedEntity.RelationType.ONE_TO_ONE
    )
    private BookWithIdAndTitle book;
    @FetchRelatedEntity(
            relatedEntity = Member.class,
            dto = MemberIdAndFullname.class,
            foreignKey = "member_id",
            joinType = FetchRelatedEntity.JOIN_TYPE.JOIN,
            type = FetchRelatedEntity.RelationType.ONE_TO_ONE
    )
    private MemberIdAndFullname member;
    private LocalDate borrowDate;
    private LocalDate returnDate;
}
