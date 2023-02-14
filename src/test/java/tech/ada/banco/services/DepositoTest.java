package tech.ada.banco.services;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import tech.ada.banco.exceptions.ResourceNotFoundException;
import tech.ada.banco.model.Conta;
import tech.ada.banco.model.ModalidadeConta;
import tech.ada.banco.repository.ContaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class DepositoTest {
    private final ContaRepository repository = Mockito.mock(ContaRepository.class);
    private final Deposito deposito = new Deposito(repository);

    @Test
    void testDepositoSaldo() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        //conta.deposito(BigDecimal.TEN);
        when(repository.findContaByNumeroConta(10)).thenReturn(Optional.of(conta));
        //assertEquals(BigDecimal.valueOf(10), conta.getSaldo(), "O saldo inicial da conta deve ser alterado para 10");

        BigDecimal resp = deposito.executar(10, BigDecimal.TEN);

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.TEN.setScale(2), resp);
        assertEquals(BigDecimal.TEN.setScale(2), conta.getSaldo());
    }

    @Test
    void testDepositoSaldoFracionado() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.TEN);
        when(repository.findContaByNumeroConta(10)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(10), conta.getSaldo(), "O saldo inicial da conta deve ser alterado para 10");

        BigDecimal resp = deposito.executar(10, BigDecimal.valueOf(8.5));

        verify(repository, times(1)).save(conta);
        assertEquals(BigDecimal.valueOf(18.5).setScale(2), resp);
        assertEquals(BigDecimal.valueOf(18.5).setScale(2), conta.getSaldo());
    }

    @Test
    void testDepositoSaldoNegativo() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.TEN);
        when(repository.findContaByNumeroConta(10)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(10), conta.getSaldo(), "O saldo inicial da conta deve ser alterado para 10");

        String message = assertThrows(RuntimeException.class,
                () -> {
                    deposito.executar(10, BigDecimal.valueOf(-5));
                }).getMessage();

        assertTrue(message.contains("Valor informado está inválido."));

        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.TEN, conta.getSaldo());
    }

    @Test
    void testDepositoContaInvalida() {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.TEN);
        when(repository.findContaByNumeroConta(10)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(10), conta.getSaldo(), "O saldo inicial da conta deve ser alterado para 10");

        try {
            deposito.executar(1, BigDecimal.ONE);
            fail("A conta não deveria ter sido encontrada.");
        } catch (ResourceNotFoundException e) {

        }

        verify(repository, times(0)).save(any());
        assertEquals(BigDecimal.TEN, conta.getSaldo());
    }


}