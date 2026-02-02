package ec.edu.espe.buildtestci.service;

import ec.edu.espe.buildtestci.dto.WalletResponse;
import ec.edu.espe.buildtestci.model.Wallet;
import ec.edu.espe.buildtestci.repository.WalletRepository;

import java.util.Optional;

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

    //Depositar dinero
    public double deposit(String walletId, double amount) {
        if (amount<0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        Optional<Wallet> found = walletRepository.findById(walletId);
        if(found.isEmpty()) {
            throw new IllegalStateException("Wallet not found");
        }
        Wallet wallet = found.get();
        wallet.deposit(amount);

        //DPersistimos  el nuevo saldo
        return wallet.getBalance();
    }
}
