package com.example.bookworm;

import java.util.ArrayList;

public class Owner extends User implements BookUser {
    private Borrower borrower;

    public Owner() { }

    public Owner(Borrower borrower) {
        this.borrower = borrower;
    }

    /**
     * Gets the owner related to this borrower object
     * @return Borrower
     */
    public Borrower getBorrower() {
        return borrower;
    }

    public void addBook(Book book) {
        this.books.add(book);
    }
    public ArrayList<Book> getBooks() {
        return this.books;
    }
    public void deleteBook(Book book) {
        this.books.remove(book);
    }
}
