package com.bank.model;

import java.time.LocalDateTime;

public abstract class Account {
    public enum AccountType {
        CHECKING, // Vadesiz
        SAVINGS   // Vadeli
    }

    protected final String accountNumber;
    protected double balance;
    protected final AccountType accountType;
    protected final String createdDate;
    protected boolean active;

    protected Account(String accountNumber, double initialBalance, AccountType accountType) {
        this(accountNumber, initialBalance, accountType, LocalDateTime.now().toString(), true);
    }

    protected Account(String accountNumber, double balance, AccountType accountType, String createdDate, boolean active) {
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.accountType = accountType;
        this.createdDate = createdDate;
        this.active = active;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public abstract void deposit(double amount);

    public abstract void withdraw(double amount) throws IllegalArgumentException;

    @Override
    public String toString() {
        return String.format("Hesap No: %s | Tür: %s | Bakiye: %.2f TL | Durum: %s",
                accountNumber, accountType == AccountType.CHECKING ? "Vadesiz" : "Vadeli",
                balance, active ? "Aktif" : "Pasif");
    }
}
