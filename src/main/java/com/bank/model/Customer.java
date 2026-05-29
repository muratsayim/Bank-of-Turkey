package com.bank.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Customer {
    private final String customerId;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    private final List<Account> accounts;

    public Customer(String fullName, String email, String phone, String passwordHash) {
        this.customerId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.accounts = new ArrayList<>();
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
    }

    public Account findAccount(String accountNumber) {
        for (Account account : accounts) {
            if (account.getAccountNumber().equals(accountNumber)) {
                return account;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Müşteri No: %s | Adı: %s | E-posta: %s | Hesap Sayısı: %d",
                customerId, fullName, email, accounts.size());
    }
}
