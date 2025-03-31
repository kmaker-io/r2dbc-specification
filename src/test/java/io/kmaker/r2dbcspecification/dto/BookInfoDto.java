package io.kmaker.r2dbcspecification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookInfoDto {
    private Long id;
    private String title;
    private String genre;
    private int publicationYear;
}
