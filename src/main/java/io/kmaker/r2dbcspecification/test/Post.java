package io.kmaker.r2dbcspecification.test;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("posts")
@Getter
@Setter
public class Post {
    @Id
    private Long id;

    @Column("user_id")
    private Long userId;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("created_at")
    private LocalDate createdAt;
}
