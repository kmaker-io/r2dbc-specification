# R2DBC Generic Specification

This repository provides a generic specification for querying entities reactively using Spring Data R2DBC. 
It supports filtering, projection, pagination, and relational data fetching.

## Adding This Library

To use this library in your project, add the following dependency to your `pom.xml` (for Maven):

```xml
<dependency>
    <groupId>io.kmaker</groupId>
    <artifactId>r2dbc-specification</artifactId>
    <version>1.0.0</version>
</dependency>
```

Or if you are using Gradle:

```gradle
dependencies {
    implementation 'io.kmaker:r2dbc-generic-specification:1.0.0'
}
```

## Example Entity

### Book Entity

```java
import org.springframework.data.relational.core.mapping.Column;

@Table("book")
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
    // Getters and Setters
}
```

### Author Entity
```java
@Table("Author")
public class Author {
    @Id
    private Long id;
    private String name;
    @Column("birth_year")
    private int birthYear;
    // Getters and Setters
}
```

### Member Entity
```java
@Table("member")
public class Member {
    @Id
    private Integer id;
    private String fullName;
    private String email;
    private String phoneNumber;
    // Getters and Setters
}
```

## Example DTO for Projection

### BookInfoDto
```java
public class BookInfoDto {
    private Integer id;
    private String title;
    private String genre;
    private Integer publicationYear;
    // Getters and Setters
}
```

### BookDetailDto (With Author Information)
```java
public class BookDetailDto {
    private Long id;
    private String title;
    private String genre;
    private int publicationYear;
    private int availableCopies;
    @FetchRelatedEntity(
            relatedEntity = Author.class,
            foreignKey = "author_id",
            dto = AuthorDto.class,
            joinType = FetchRelatedEntity.JOIN_TYPE.JOIN,
            type = FetchRelatedEntity.RelationType.ONE_TO_ONE
    )
    private AuthorDto authorDto;
    // Getters and Setters
}
```

### BorrowingInfoDto (With Book and Member Relations)
```java
public class BorrowingInfoDto {
    private Long id;
    @FetchRelatedEntity(
            relatedEntity = Book.class,
            dto = BookWithIdAndTitle.class,
            foreignKey = "book_id",
            joinType = FetchRelatedEntity.JOIN_TYPE.JOIN,
            type = FetchRelatedEntity.RelationType.ONE_TO_ONE
    )
    private BookWithIdAndTitle book;
    @FetchRelatedEntity(
            relatedEntity = Member.class,
            dto = MemberIdAndFullname.class,
            foreignKey = "member_id",
            joinType = FetchRelatedEntity.JOIN_TYPE.JOIN,
            type = FetchRelatedEntity.RelationType.ONE_TO_ONE
    )
    private MemberIdAndFullname member;
    private LocalDate borrowDate;
    private LocalDate returnDate;
    // Getters and Setters
}
```

## Example Code

### Finding an Entity by Specification
```java
var idSpec = Criteria.where("id").is(2);
var bookMono = r2dbcGenericSpecification.findOneBySpec(idSpec, Book.class, BookInfoDto.class);
```

### Finding an Entity by Specification with Relations
```java
var idSpec = Criteria.where("book.id").is(10);
var bookDetailMono = r2dbcGenericSpecification.findOneBySpecWithRel(idSpec, Book.class, BookDetailDto.class);
```

### Paginated Query with Relations
```java
var emptySpec = Criteria.empty();
var pageable = PageRequest.of(0, 3, Sort.Direction.ASC, "borrowing.id");
var borrowingPageMono = r2dbcGenericSpecification.getPageBySpecWithRel(emptySpec, pageable, Borrowing.class, BorrowingInfoDto.class);
```

### Paginated Query Without Relations
```java
var pageable = PageRequest.of(0, 30, Sort.Direction.DESC, "member.id");
var memberSpec = Criteria.empty();
var memberPageMono = r2dbcGenericSpecification.getPageBySpec(memberSpec, pageable, Member.class, Member.class);
```

Learn more about [`Criteria` method](https://docs.spring.io/spring-data/relational/reference/r2dbc/entity-persistence.html#r2dbc.datbaseclient.fluent-api.criteria)

## Ignore Field Mapping
In some cases, we may have fields in a DTO (Data Transfer Object) class that do not exist in the database. 
To prevent errors when mapping the fields to an SQL query, we can use the `@IgnoreMapping` annotation. 
This annotation tells the system to ignore the field during the mapping process.

Example:

```java
import io.kmaker.r2dbcspecification.annotation.IgnoreMapping;

public class BookInfoDto {
    private Integer id;
    private String title;
    private String genre;
    private Integer publicationYear;

    // // This field does not exist in the database, so we mark it to be ignored during mapping
    @IgnoreMapping
    private String doesNotExist;
    // Getters and Setters
}
```

## Running Tests
To run the test cases, execute:
```sh
./mvnw test
```
