package com.bank.model;

public class CheckingAccount extends Account {

    public CheckingAccount(String accountNumber, double initialBalance) {
        super(accountNumber, initialBalance, AccountType.CHECKING);
    }

    public CheckingAccount(String accountNumber, double balance, String createdDate, boolean active) {
        super(accountNumber, balance, AccountType.CHECKING, createdDate, active);
    }

    @Override
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Yatırılacak miktar sıfırdan büyük olmalıdır.");
        }
        this.balance += amount;
    }

    @Override
    public void withdraw(double amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Çekilecek miktar sıfırdan büyük olmalıdır.");
        }
        if (this.balance < amount) {
            throw new IllegalArgumentException("Yetersiz bakiye! Mevcut bakiye: " + this.balance + " TL");
        }
        this.balance -= amount;
    }
}
