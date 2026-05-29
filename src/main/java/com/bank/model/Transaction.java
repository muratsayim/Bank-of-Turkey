package com.bank.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction {
    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }

    private final String id;
    private final String sourceAccountNumber;
    private final String targetAccountNumber;
    private final double amount;
    private final TransactionType type;
    private final String timestamp;
    private final String description;

    public Transaction(String sourceAccountNumber, String targetAccountNumber, double amount, TransactionType type, String description) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = LocalDateTime.now().toString();
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getSourceAccountNumber() {
        return sourceAccountNumber;
    }

    public String getTargetAccountNumber() {
        return targetAccountNumber;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s -> %s | Tutar: %.2f TL | Açıklama: %s",
                timestamp.substring(0, 19).replace('T', ' '), type,
                sourceAccountNumber != null ? sourceAccountNumber : "DIŞARIDAN",
                targetAccountNumber != null ? targetAccountNumber : "DIŞARIYA",
                amount, description);
    }
}
