package com.bank.service;

import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.Transaction;
import com.bank.repository.DataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BankingServiceTest {

    private BankingService service;
    private MockDataRepository mockRepository;

    @BeforeEach
    public void setUp() {
        mockRepository = new MockDataRepository();
        service = new BankingService(mockRepository);
    }

    @Test
    public void testRegisterAndLoginCustomer() {
        Customer registered = service.registerCustomer("Murat Yılmaz", "murat@mail.com", "05551234567", "pass123");
        assertNotNull(registered);
        assertEquals("Murat Yılmaz", registered.getFullName());
        assertEquals("murat@mail.com", registered.getEmail());

        Customer loggedIn = service.loginCustomer("murat@mail.com", "pass123");
        assertNotNull(loggedIn);
        assertEquals(registered.getCustomerId(), loggedIn.getCustomerId());

        assertNull(service.loginCustomer("murat@mail.com", "wrong_pass"));
        assertNull(service.loginCustomer("nonexistent@mail.com", "pass123"));
    }

    @Test
    public void testCreateAccount() {
        Customer c = service.registerCustomer("Ahmet Can", "ahmet@mail.com", "05559876543", "pass123");
        Account acc = service.createAccount(c, Account.AccountType.CHECKING, 100.0, 0);

        assertNotNull(acc);
        assertEquals(100.0, acc.getBalance());
        assertEquals(Account.AccountType.CHECKING, acc.getAccountType());
        assertEquals(1, c.getAccounts().size());
    }

    @Test
    public void testDepositAndWithdrawal() {
        Customer c = service.registerCustomer("Ali Veli", "ali@mail.com", "05553332211", "pass123");
        Account acc = service.createAccount(c, Account.AccountType.CHECKING, 500.0, 0);

        service.deposit(c, acc.getAccountNumber(), 250.0);
        assertEquals(750.0, acc.getBalance());

        service.withdraw(c, acc.getAccountNumber(), 150.0);
        assertEquals(600.0, acc.getBalance());

        assertThrows(IllegalArgumentException.class, () -> {
            service.withdraw(c, acc.getAccountNumber(), 1000.0);
        });
    }

    @Test
    public void testTransfer() {
        Customer sender = service.registerCustomer("Gönderici", "sender@mail.com", "05551111111", "pass123");
        Customer receiver = service.registerCustomer("Alıcı", "receiver@mail.com", "05552222222", "pass123");

        Account senderAcc = service.createAccount(sender, Account.AccountType.CHECKING, 1000.0, 0);
        Account receiverAcc = service.createAccount(receiver, Account.AccountType.CHECKING, 100.0, 0);

        service.transfer(sender, senderAcc.getAccountNumber(), receiverAcc.getAccountNumber(), 300.0);

        assertEquals(700.0, senderAcc.getBalance());
        assertEquals(400.0, receiverAcc.getBalance());

        List<Transaction> senderHistory = service.getTransactionHistory(senderAcc.getAccountNumber());
        assertFalse(senderHistory.isEmpty());

        List<Transaction> receiverHistory = service.getTransactionHistory(receiverAcc.getAccountNumber());
        assertFalse(receiverHistory.isEmpty());
    }

    @Test
    public void testSavingsInterest() {
        Customer c = service.registerCustomer("Mehmet", "mehmet@mail.com", "05555555555", "pass123");
        Account acc = service.createAccount(c, Account.AccountType.SAVINGS, 1000.0, 0.10);

        service.applyInterest(c, acc.getAccountNumber(), 365);
        assertEquals(1100.0, acc.getBalance(), 0.01);
    }

    private static class MockDataRepository extends DataRepository {
        private final List<Customer> customers = new ArrayList<>();
        private final List<Transaction> transactions = new ArrayList<>();

        @Override
        public List<Customer> loadCustomers() {
            return new ArrayList<>(customers);
        }

        @Override
        public void saveCustomers(List<Customer> customers) {
            this.customers.clear();
            this.customers.addAll(customers);
        }

        @Override
        public List<Transaction> loadTransactions() {
            return new ArrayList<>(transactions);
        }

        @Override
        public void saveTransactions(List<Transaction> transactions) {
            this.transactions.clear();
            this.transactions.addAll(transactions);
        }
    }
}
