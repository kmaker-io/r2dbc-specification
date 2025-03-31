package io.kmaker.r2dbcspecification.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("book")
@Getter
@Setter
public class Book {
    @Id
    private Long id;
    private String title;
    private String genre;
    @Column("publication_year")
    private int publicationYear;
    @Column("available_copies")
    private int availableCopies;
    @Column("author_id")
    private Long authorId;
}
