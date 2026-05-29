package com.bank.service;

import com.bank.model.Account;
import com.bank.model.CheckingAccount;
import com.bank.model.Customer;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import com.bank.repository.DataRepository;
import com.bank.util.HashUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class BankingService {
    private final DataRepository repository;
    private final List<Customer> customers;
    private final List<Transaction> transactions;

    public BankingService() {
        this.repository = new DataRepository();
        this.customers = repository.loadCustomers();
        this.transactions = repository.loadTransactions();
    }

    public BankingService(DataRepository repository) {
        this.repository = repository;
        this.customers = repository.loadCustomers();
        this.transactions = repository.loadTransactions();
    }

    public synchronized Customer registerCustomer(String fullName, String email, String phone, String rawPassword) {
        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Tüm alanlar doldurulmalıdır.");
        }

        for (Customer c : customers) {
            if (c.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException("Bu e-posta adresiyle kayıtlı bir kullanıcı zaten mevcut.");
            }
        }

        String passwordHash = HashUtil.hashPassword(rawPassword);
        Customer customer = new Customer(fullName, email, phone, passwordHash);
        customers.add(customer);
        repository.saveCustomers(customers);
        return customer;
    }

    public Customer loginCustomer(String email, String rawPassword) {
        if (email == null || rawPassword == null) {
            return null;
        }

        String passwordHash = HashUtil.hashPassword(rawPassword);
        for (Customer c : customers) {
            if (c.getEmail().equalsIgnoreCase(email) && c.getPasswordHash().equals(passwordHash)) {
                return c;
            }
        }
        return null;
    }

    public synchronized Account createAccount(Customer customer, Account.AccountType type, double initialBalance, double interestRate) {
        if (customer == null) {
            throw new IllegalArgumentException("Müşteri bilgisi boş olamaz.");
        }
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Başlangıç bakiyesi negatif olamaz.");
        }

        String accountNumber = generateUniqueAccountNumber();
        Account account;

        if (type == Account.AccountType.SAVINGS) {
            if (interestRate <= 0 || interestRate > 1) {
                throw new IllegalArgumentException("Faiz oranı 0 ile 1 arasında olmalıdır (örn: 0.15).");
            }
            account = new SavingsAccount(accountNumber, initialBalance, interestRate);
        } else {
            account = new CheckingAccount(accountNumber, initialBalance);
        }

        Customer systemCustomer = findCustomerById(customer.getCustomerId());
        if (systemCustomer != null) {
            systemCustomer.addAccount(account);
        } else {
            customer.addAccount(account);
        }

        repository.saveCustomers(customers);

        if (initialBalance > 0) {
            recordTransaction(null, accountNumber, initialBalance, Transaction.TransactionType.DEPOSIT, "Hesap Açılış Bakiyesi");
        }

        return account;
    }

    public synchronized void deposit(Customer customer, String accountNumber, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Yatırılacak miktar sıfırdan büyük olmalıdır.");
        }

        Customer systemCustomer = findCustomerById(customer.getCustomerId());
        if (systemCustomer == null) {
            throw new IllegalArgumentException("Müşteri bulunamadı.");
        }

        Account account = systemCustomer.findAccount(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Hesap bulunamadı veya bu müşteriye ait değil.");
        }

        if (!account.isActive()) {
            throw new IllegalArgumentException("İşlem yapılmak istenen hesap aktif değil.");
        }

        account.deposit(amount);
        repository.saveCustomers(customers);

        recordTransaction(null, accountNumber, amount, Transaction.TransactionType.DEPOSIT, "Para Yatırma");
    }

    public synchronized void withdraw(Customer customer, String accountNumber, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Çekilecek miktar sıfırdan büyük olmalıdır.");
        }

        Customer systemCustomer = findCustomerById(customer.getCustomerId());
        if (systemCustomer == null) {
            throw new IllegalArgumentException("Müşteri bulunamadı.");
        }

        Account account = systemCustomer.findAccount(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Hesap bulunamadı veya bu müşteriye ait değil.");
        }

        if (!account.isActive()) {
            throw new IllegalArgumentException("İşlem yapılmak istenen hesap aktif değil.");
        }

        account.withdraw(amount);
        repository.saveCustomers(customers);

        recordTransaction(accountNumber, null, amount, Transaction.TransactionType.WITHDRAWAL, "Para Çekme");
    }

    public synchronized void transfer(Customer customer, String sourceAccountNumber, String targetAccountNumber, double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer miktarı sıfırdan büyük olmalıdır.");
        }
        if (sourceAccountNumber.equals(targetAccountNumber)) {
            throw new IllegalArgumentException("Aynı hesaba transfer yapılamaz.");
        }

        Customer sourceCustomer = findCustomerById(customer.getCustomerId());
        if (sourceCustomer == null) {
            throw new IllegalArgumentException("Gönderen müşteri bulunamadı.");
        }

        Account sourceAccount = sourceCustomer.findAccount(sourceAccountNumber);
        if (sourceAccount == null) {
            throw new IllegalArgumentException("Gönderen hesap bulunamadı veya size ait değil.");
        }

        if (!sourceAccount.isActive()) {
            throw new IllegalArgumentException("Gönderen hesap aktif değil.");
        }

        Account targetAccount = null;
        Customer targetCustomer = null;
        for (Customer c : customers) {
            Account acc = c.findAccount(targetAccountNumber);
            if (acc != null) {
                targetAccount = acc;
                targetCustomer = c;
                break;
            }
        }

        if (targetAccount == null) {
            throw new IllegalArgumentException("Alıcı hesap bulunamadı.");
        }

        if (!targetAccount.isActive()) {
            throw new IllegalArgumentException("Alıcı hesap aktif değil.");
        }

        sourceAccount.withdraw(amount);
        targetAccount.deposit(amount);

        repository.saveCustomers(customers);

        String description = String.format("%s kişisinden %s kişisine transfer", sourceCustomer.getFullName(), targetCustomer.getFullName());
        recordTransaction(sourceAccountNumber, targetAccountNumber, amount, Transaction.TransactionType.TRANSFER, description);
    }

    public List<Transaction> getTransactionHistory(String accountNumber) {
        return transactions.stream()
                .filter(t -> accountNumber.equals(t.getSourceAccountNumber()) || accountNumber.equals(t.getTargetAccountNumber()))
                .collect(Collectors.toList());
    }

    public synchronized void applyInterest(Customer customer, String accountNumber, int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("Gün sayısı sıfırdan büyük olmalıdır.");
        }

        Customer systemCustomer = findCustomerById(customer.getCustomerId());
        if (systemCustomer == null) {
            throw new IllegalArgumentException("Müşteri bulunamadı.");
        }

        Account account = systemCustomer.findAccount(accountNumber);
        if (account == null) {
            throw new IllegalArgumentException("Hesap bulunamadı.");
        }

        if (!(account instanceof SavingsAccount)) {
            throw new IllegalArgumentException("Bu işlem sadece vadeli (Savings) hesaplar için geçerlidir.");
        }

        SavingsAccount savingsAccount = (SavingsAccount) account;
        double interest = savingsAccount.calculateInterest(days);
        if (interest > 0) {
            savingsAccount.applyInterest(days);
            repository.saveCustomers(customers);
            recordTransaction(null, accountNumber, interest, Transaction.TransactionType.DEPOSIT, days + " Günlük Faiz Getirisi");
        }
    }

    private Customer findCustomerById(String customerId) {
        for (Customer c : customers) {
            if (c.getCustomerId().equals(customerId)) {
                return c;
            }
        }
        return null;
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String number;
        boolean unique;
        do {
            StringBuilder sb = new StringBuilder("TR");
            for (int i = 0; i < 10; i++) {
                sb.append(random.nextInt(10));
            }
            number = sb.toString();
            unique = true;
            for (Customer c : customers) {
                if (c.findAccount(number) != null) {
                    unique = false;
                    break;
                }
            }
        } while (!unique);
        return number;
    }

    private void recordTransaction(String sourceAcc, String targetAcc, double amount, Transaction.TransactionType type, String description) {
        Transaction transaction = new Transaction(sourceAcc, targetAcc, amount, type, description);
        transactions.add(transaction);
        repository.saveTransactions(transactions);
    }
}
