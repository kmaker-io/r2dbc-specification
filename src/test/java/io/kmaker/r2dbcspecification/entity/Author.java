package io.kmaker.r2dbcspecification.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Setter
@Getter
@Table("author")
public class Author {
    @Id
    private Long id;
    private String name;
    @Column("birth_year")
    private int birthYear;
}
