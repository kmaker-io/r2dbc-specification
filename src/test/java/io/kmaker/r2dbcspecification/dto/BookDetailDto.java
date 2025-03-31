package io.kmaker.r2dbcspecification.dto;

import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import io.kmaker.r2dbcspecification.entity.Author;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDetailDto {
    private Long id;
    private String title;
    private String genre;
    private int publicationYear;
    private int availableCopies;
    @FetchRelatedEntity(
            relatedEntity = Author.class,
            foreignKey = "author_id",
            dto = AuthorDto.class,
            joinType = FetchRelatedEntity.JOIN_TYPE.JOIN,
            type = FetchRelatedEntity.RelationType.ONE_TO_ONE
    )
    private AuthorDto authorDto;
}
