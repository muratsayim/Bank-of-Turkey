package com.bank.model;

public class SavingsAccount extends Account {
    private final double interestRate; // Örn: 0.15 (%15 faiz)

    public SavingsAccount(String accountNumber, double initialBalance, double interestRate) {
        super(accountNumber, initialBalance, AccountType.SAVINGS);
        this.interestRate = interestRate;
    }

    public SavingsAccount(String accountNumber, double balance, double interestRate, String createdDate, boolean active) {
        super(accountNumber, balance, AccountType.SAVINGS, createdDate, active);
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    /**
     * Calculates projected interest earned for a given number of days.
     * Formula: Balance * InterestRate * (Days / 365)
     */
    public double calculateInterest(int days) {
        return this.balance * this.interestRate * (days / 365.0);
    }

    /**
     * Applies the interest earned over a period of days to the balance.
     */
    public void applyInterest(int days) {
        double interest = calculateInterest(days);
        if (interest > 0) {
            deposit(interest);
        }
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

    @Override
    public String toString() {
        return String.format("%s | Faiz Oranı: %%.2f", super.toString(), interestRate * 100);
    }
}
