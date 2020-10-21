package com.example.bookworm;

public class Owner {
    private Borrower borrower;

    public Owner(){}

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

    public void setBorrower(Borrower borrower){ this.borrower = borrower; }
}
