package io.kmaker.r2dbcspecification.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookWithIdAndTitle {
    private Long id;
    private String title;
}
