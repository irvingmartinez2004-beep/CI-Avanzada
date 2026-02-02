package ec.edu.espe.buildtestci.model;

import java.util.UUID;

public class Wallet {

    private final String id;
    private final String ownerEmail;
    private double balance;

    public Wallet(String ownerEmail, double balance) {
        this.id = UUID.randomUUID().toString();
        this.ownerEmail = ownerEmail;
        this.balance = balance;
    }

    public String getId() {
        return id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0 || amount > balance) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        this.balance -= amount;
    }
}
