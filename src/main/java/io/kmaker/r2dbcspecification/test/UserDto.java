package io.kmaker.r2dbcspecification.test;

import io.kmaker.r2dbcspecification.annotation.FetchRelatedEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate createdAt;

    @FetchRelatedEntity(
            joinType = FetchRelatedEntity.JOIN_TYPE.JOIN,
            type = FetchRelatedEntity.RelationType.ONE_TO_MANY,
            foreignKey = "user_id",
            relatedEntity = Post.class,
            dto = PostDto.class)
    private List<PostDto> posts;
}
