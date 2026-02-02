package ec.edu.espe.buildtestci;

import ec.edu.espe.buildtestci.dto.WalletResponse;
import ec.edu.espe.buildtestci.model.Wallet;
import ec.edu.espe.buildtestci.repository.WalletRepository;
import ec.edu.espe.buildtestci.service.RiskClient;
import ec.edu.espe.buildtestci.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private WalletRepository walletRepository;
    private WalletService walletService;
    private RiskClient riskClient;

    @BeforeEach
    void setUp() {
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);
    }

    @Test
    void createWallet_validData_shouldSaveAndReturnResponse() {
        // Arrange
        String email = "luis@espe.edu.ec";
        double initialBalance = 100.0;

        when(walletRepository.existsByOwnerEmail(email)).thenReturn(false);
        when(riskClient.isBloqued(email)).thenReturn(false);
        when(walletRepository.save(any(Wallet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        WalletResponse response = walletService.createWallet(email, initialBalance);

        // Assert
        assertNotNull(response.getWalletId());
        assertEquals(100.0, response.getBalance());

        verify(walletRepository).existsByOwnerEmail(email);
        verify(riskClient).isBloqued(email);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void createWallet_invalidEmail_shouldThrow_andNotCallDependencies() {
        // Arrange
        String invalidEmail = "luis-espe.edu.ec";

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> walletService.createWallet(invalidEmail, 50.0));

        verifyNoInteractions(walletRepository, riskClient);
    }

    @Test
    void deposit_walletNotFound_shouldThrow() {
        // Arrange
        String walletId = "no-exist-wallet";
        when(walletRepository.findById(walletId)).thenReturn(Optional.empty());

        // Act + Assert
        IllegalStateException exception =
                assertThrows(IllegalStateException.class,
                        () -> walletService.deposit(walletId, 60.0));

        assertEquals("Wallet not found", exception.getMessage());
        verify(walletRepository).findById(walletId);
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void deposit_validAmount_shouldUpdateBalanceAndSave() {
        // Arrange
        Wallet wallet = new Wallet("luis@espe.edu.ec", 100.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId)).thenReturn(Optional.of(wallet));

        // Act
        double newBalance = walletService.deposit(walletId, 50.0);

        // Assert
        assertEquals(150.0, newBalance);
        verify(walletRepository).save(wallet);
    }

    @Test
    void withdraw_insufficientBalance_shouldThrow() {
        // Arrange
        Wallet wallet = new Wallet("luis@espe.edu.ec", 300.0);
        String walletId = wallet.getId();

        when(walletRepository.findById(walletId))
                .thenReturn(Optional.of(wallet));

        // Act + Assert
        IllegalStateException exception =
                assertThrows(IllegalStateException.class,
                        () -> walletService.withdraw(walletId, 500.0));

        assertEquals("Insufficient Balance", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
