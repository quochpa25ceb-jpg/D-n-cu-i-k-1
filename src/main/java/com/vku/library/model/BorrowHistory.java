package com.vku.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_history")
public class BorrowHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Thiết lập mối quan hệ Nhiều lịch sử - Một người dùng
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Thiết lập mối quan hệ Nhiều lịch sử - Một cuốn sách
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "borrow_date", nullable = false)
    private LocalDateTime borrowDate;

    public BorrowHistory() {}

    public BorrowHistory(User user, Book book, LocalDateTime borrowDate) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }
}
