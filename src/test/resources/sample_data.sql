-- Create Author Table
CREATE TABLE author (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    birth_year INT
);

-- Create Book Table
CREATE TABLE book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    publication_year INT,
    author_id BIGINT,
    available_copies INT DEFAULT 1,
    FOREIGN KEY (author_id) REFERENCES author(id)
);

-- Create Member Table
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    membership_date DATE DEFAULT CURRENT_DATE
);

-- Create Borrowing Table
CREATE TABLE borrowing (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT,
    member_id BIGINT,
    borrow_date DATE DEFAULT CURRENT_DATE,
    return_date DATE,
    FOREIGN KEY (book_id) REFERENCES book(id),
    FOREIGN KEY (member_id) REFERENCES member(id)
);


-- Insert Authors
INSERT INTO author (name, birth_year) VALUES
('J.K. Rowling', 1965),
('George Orwell', 1903),
('J.R.R. Tolkien', 1892),
('Isaac Asimov', 1920),
('Agatha Christie', 1890),
('Mark Twain', 1835),
('Ernest Hemingway', 1899),
('Stephen King', 1947),
('Jane Austen', 1775),
('Leo Tolstoy', 1828);

-- Insert Books
INSERT INTO book (title, genre, publication_year, author_id, available_copies) VALUES
('Harry Potter and the Sorcerer''s Stone', 'Fantasy', 1997, 1, 3),
('Harry Potter and the Chamber of Secrets', 'Fantasy', 1998, 1, 4),
('1984', 'Dystopian', 1949, 2, 2),
('Animal Farm', 'Political Satire', 1945, 2, 1),
('The Hobbit', 'Fantasy', 1937, 3, 5),
('The Lord of the Rings', 'Fantasy', 1954, 3, 3),
('Foundation', 'Science Fiction', 1951, 4, 2),
('I, Robot', 'Science Fiction', 1950, 4, 3),
('Murder on the Orient Express', 'Mystery', 1934, 5, 4),
('And Then There Were None', 'Mystery', 1939, 5, 2),
('Adventures of Huckleberry Finn', 'Adventure', 1885, 6, 2),
('The Old Man and the Sea', 'Fiction', 1952, 7, 3),
('The Shining', 'Horror', 1977, 8, 5),
('Pride and Prejudice', 'Romance', 1813, 9, 3),
('Emma', 'Romance', 1815, 9, 2),
('War and Peace', 'Historical', 1869, 10, 1),
('Anna Karenina', 'Drama', 1877, 10, 2),
('Dune', 'Science Fiction', 1965, 4, 4),
('Misery', 'Horror', 1987, 8, 2),
('Carrie', 'Horror', 1974, 8, 3);

-- Insert Members
INSERT INTO member (full_name, email, phone) VALUES
('Alice Johnson', 'alice.johnson@example.com', '123-456-7890'),
('Bob Smith', 'bob.smith@example.com', '987-654-3210'),
('Charlie Brown', 'charlie.brown@example.com', '555-123-4567'),
('David White', 'david.white@example.com', '111-222-3333'),
('Eve Black', 'eve.black@example.com', '444-555-6666'),
('Frank Green', 'frank.green@example.com', '777-888-9999'),
('Grace Hall', 'grace.hall@example.com', '222-333-4444'),
('Henry Adams', 'henry.adams@example.com', '333-444-5555'),
('Ivy Clark', 'ivy.clark@example.com', '888-999-0000'),
('Jack Wilson', 'jack.wilson@example.com', '999-000-1111'),
('Kevin Lee', 'kevin.lee@example.com', '555-666-7777'),
('Laura Scott', 'laura.scott@example.com', '666-777-8888'),
('Mike Brown', 'mike.brown@example.com', '777-888-9999'),
('Nancy Davis', 'nancy.davis@example.com', '111-222-3333'),
('Oliver Thomas', 'oliver.thomas@example.com', '333-444-5555');

-- Insert Borrowing Records
INSERT INTO borrowing (book_id, member_id, borrow_date, return_date) VALUES
(1, 1, '2025-03-01', NULL),
(2, 1, '2025-03-05', NULL),
(3, 2, '2025-03-02', '2025-03-10'),
(4, 3, '2025-03-06', NULL),
(5, 4, '2025-03-08', NULL),
(6, 5, '2025-03-10', NULL),
(7, 6, '2025-03-12', '2025-03-20'),
(8, 7, '2025-03-14', NULL),
(9, 8, '2025-03-15', NULL),
(10, 9, '2025-03-16', NULL),
(11, 10, '2025-03-18', NULL),
(12, 11, '2025-03-20', '2025-03-25'),
(13, 12, '2025-03-22', NULL),
(14, 13, '2025-03-24', NULL),
(15, 14, '2025-03-26', NULL),
(16, 15, '2025-03-28', NULL),
(17, 1, '2025-03-30', NULL),
(18, 2, '2025-03-31', NULL),
(19, 3, '2025-04-01', NULL),
(20, 4, '2025-04-02', NULL);
