package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.CheckingAccount;
import com.bank.model.Customer;
import com.bank.model.SavingsAccount;
import com.bank.model.Transaction;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataRepository {
    private static final String DATA_DIR = "data";
    private static final String CUSTOMERS_FILE = DATA_DIR + "/customers.json";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.json";
    private final Gson gson;

    public DataRepository() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Veri dizini oluşturulamadı: " + e.getMessage());
        }

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Account.class, new AccountAdapter())
                .setPrettyPrinting()
                .create();
    }

    public List<Customer> loadCustomers() {
        File file = new File(CUSTOMERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Customer>>() {}.getType();
            List<Customer> customers = gson.fromJson(reader, listType);
            return customers != null ? customers : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("Müşteri verileri yüklenirken hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveCustomers(List<Customer> customers) {
        try (Writer writer = new FileWriter(CUSTOMERS_FILE)) {
            gson.toJson(customers, writer);
        } catch (IOException e) {
            System.err.println("Müşteri verileri kaydedilirken hata oluştu: " + e.getMessage());
        }
    }

    public List<Transaction> loadTransactions() {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
            List<Transaction> transactions = gson.fromJson(reader, listType);
            return transactions != null ? transactions : new ArrayList<>();
        } catch (IOException e) {
            System.err.println("İşlem verileri yüklenirken hata oluştu: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void saveTransactions(List<Transaction> transactions) {
        try (Writer writer = new FileWriter(TRANSACTIONS_FILE)) {
            gson.toJson(transactions, writer);
        } catch (IOException e) {
            System.err.println("İşlem verileri kaydedilirken hata oluştu: " + e.getMessage());
        }
    }

    private static class AccountAdapter implements JsonSerializer<Account>, JsonDeserializer<Account> {
        @Override
        public JsonElement serialize(Account src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject result = new JsonObject();
            result.addProperty("accountNumber", src.getAccountNumber());
            result.addProperty("balance", src.getBalance());
            result.addProperty("accountType", src.getAccountType().name());
            result.addProperty("createdDate", src.getCreatedDate());
            result.addProperty("active", src.isActive());
            if (src instanceof SavingsAccount) {
                result.addProperty("interestRate", ((SavingsAccount) src).getInterestRate());
            }
            return result;
        }

        @Override
        public Account deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String accountNumber = jsonObject.get("accountNumber").getAsString();
            double balance = jsonObject.get("balance").getAsDouble();
            String createdDate = jsonObject.get("createdDate").getAsString();
            boolean active = jsonObject.get("active").getAsBoolean();
            String typeStr = jsonObject.get("accountType").getAsString();
            Account.AccountType type = Account.AccountType.valueOf(typeStr);

            if (type == Account.AccountType.SAVINGS) {
                double interestRate = jsonObject.get("interestRate").getAsDouble();
                return new SavingsAccount(accountNumber, balance, interestRate, createdDate, active);
            } else {
                return new CheckingAccount(accountNumber, balance, createdDate, active);
            }
        }
    }
}
