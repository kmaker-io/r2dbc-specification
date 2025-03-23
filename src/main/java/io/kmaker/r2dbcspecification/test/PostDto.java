package io.kmaker.r2dbcspecification.test;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PostDto {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private LocalDate createdAt;
}
