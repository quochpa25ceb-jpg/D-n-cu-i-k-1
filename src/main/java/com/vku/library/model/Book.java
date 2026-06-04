package com.vku.library.model;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int quantity;

    public Book() {}

    public Book(String title, int quantity) {
        this.title = title;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}