package io.kmaker.r2dbcspecification.test;

import io.kmaker.r2dbcspecification.annotation.MapColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class StudentDto {
    private Long id;
    @MapColumn(value = "firstName")
    private String firstName;
    private String lastName;
}
