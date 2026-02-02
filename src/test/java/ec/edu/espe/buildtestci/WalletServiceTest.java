package ec.edu.espe.buildtestci;

import ec.edu.espe.buildtestci.dto.WalletResponse;
import ec.edu.espe.buildtestci.model.Wallet;
import ec.edu.espe.buildtestci.repository.WalletRepository;
import ec.edu.espe.buildtestci.service.RiskClient;
import ec.edu.espe.buildtestci.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WalletServiceTest
{

    private WalletRepository walletRepository;
    private WalletService walletService;
    private RiskClient riskClient;

    @BeforeEach
    public void setUp()
    {
        walletRepository = Mockito.mock(WalletRepository.class);
        riskClient = Mockito.mock(RiskClient.class);
        walletService = new WalletService(walletRepository, riskClient);

    }

    @Test
    void createWallet_validData_shouldSaveAndReturnResponse()
    {
        //Arrange
        String email = "luis@espe.edu.ec";
        double initial = 100.00;

        when(walletRepository.existsByOwnerEmail(email)).thenReturn(Boolean.FALSE);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));

        //Act
        WalletResponse response = walletService.createWallet(email, initial);

        //Assert
        assertNotNull(response.getWalletId());
        assertEquals(100.00, response.getBalance());

        verify(riskClient).isBloqued(email);
        verify(walletRepository).save(any(Wallet.class));
        verify(walletRepository).existsByOwnerEmail(email);

    }
    @Test
void createWallet_invalidEmail_shouldThrow_andNotCallDependencies(){

        //Arrange
        String invalidEmail = "luis-espe.edu.ec";

        //Act
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.createWallet(invalidEmail, 50.0);
        });
        //No debe llamar a ninguna dependencia porq falla la validacion
        verifyNoInteractions(walletRepository, riskClient);

}

@Test
    void deposit_walletNotFound_shouldThrow(){
        //Arange
    String walleId = "no-exist-wallet";
    when(walletRepository.existsByOwnerEmail(walleId)).thenReturn(Optional.empty().isEmpty());
    //Sct + Assert
    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> walletService.deposit(walleId, 60));

    assertEquals("Wallet not found", exception.getMessage());
    verify(walletRepository).findById(walleId);
    verify(walletRepository,never()).save(any(Wallet.class));


}

    public double deposit(String walletId, double amount) {

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        wallet.deposit(amount);

        walletRepository.save(wallet); // 🔥 ESTA LÍNEA FALTABA

        return wallet.getBalance();
    }

}
