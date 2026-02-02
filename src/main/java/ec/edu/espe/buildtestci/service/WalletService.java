package ec.edu.espe.buildtestci.service;

import ec.edu.espe.buildtestci.dto.WalletResponse;
import ec.edu.espe.buildtestci.model.Wallet;
import ec.edu.espe.buildtestci.repository.WalletRepository;

public class WalletService {

    private final WalletRepository walletRepository;
    private final RiskClient riskClient;

    public WalletService(WalletRepository walletRepository, RiskClient riskClient) {
        this.walletRepository = walletRepository;
        this.riskClient = riskClient;
    }

    // Crear una cuenta si cumple con las reglas del negocio
    public WalletResponse createWallet(String ownerEmail, double balance) {

        if (ownerEmail == null || ownerEmail.isEmpty() || !ownerEmail.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }

        if (balance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }

        if (riskClient.isBloqued(ownerEmail)) {
            throw new IllegalArgumentException("User blocked");
        }

        if (walletRepository.existsByOwnerEmail(ownerEmail)) {
            throw new IllegalArgumentException("Wallet already exists");
        }

        Wallet wallet = new Wallet(ownerEmail, balance);
        Wallet saved = walletRepository.save(wallet);

        return new WalletResponse(saved.getId(), saved.getBalance());
    }

    // Depositar dinero
    public double deposit(String walletId, double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        wallet.deposit(amount);
        walletRepository.save(wallet);

        return wallet.getBalance();
    }

    // Retiro de dinero
    public double withdraw(String walletId, double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Insufficient Balance");
        }

        wallet.withdraw(amount);
        walletRepository.save(wallet);

        return wallet.getBalance();
    }
}
