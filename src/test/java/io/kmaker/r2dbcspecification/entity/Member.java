package io.kmaker.r2dbcspecification.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(name = "member")
@Getter
@Setter
public class Member {
    @Id
    private Long id;
    @Column("full_name")
    private String fullName;
    private String email;
    @Column("membership_date")
    private LocalDate membershipDate;
}
