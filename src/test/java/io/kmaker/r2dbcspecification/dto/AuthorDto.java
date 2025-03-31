package io.kmaker.r2dbcspecification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorDto {
    private Long id;
    private String name;
    private int birthYear;
}
