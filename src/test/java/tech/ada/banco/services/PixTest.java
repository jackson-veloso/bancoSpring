package tech.ada.banco.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.ada.banco.model.Conta;
import tech.ada.banco.model.ModalidadeConta;
import tech.ada.banco.repository.ContaRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PixTest {
    private final ContaRepository repository = Mockito.mock(ContaRepository.class);
    private final Pix pix = new Pix(repository);
    @Test
    void testPixSaldoPositivo() {
        Conta contaOrigem = criarConta(10D,10);
        Conta contaDestino = criarConta(0D,11);

        BigDecimal resp = pix.executar(10,11,BigDecimal.ONE);

        verify(repository, times(1)).save(contaOrigem);
        verify(repository, times(1)).save(contaDestino);
        assertEquals(BigDecimal.valueOf(9.0), resp);
        assertEquals(BigDecimal.valueOf(9.0), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(1.0), contaDestino.getSaldo());

    }

    @Test
    void testPixSaldoPositivoFracionado() {

    }

    @Test
    void testPixSaldoTotal() {

    }

    @Test
    void testPixSaldoInsuficiente() {

    }

    @Test
    void testPixSaldoNegativo() {

    }

    @Test
    void testPixContaOrigemInvalida(){

    }

    @Test
    void testPixContaDestinoInvalida(){

    }

    private Conta criarConta(double valor, int numeroConta) {
        Conta conta = new Conta(ModalidadeConta.CC, null);
        conta.deposito(BigDecimal.valueOf(valor));
        when(repository.findContaByNumeroConta(numeroConta)).thenReturn(Optional.of(conta));
        assertEquals(BigDecimal.valueOf(valor), conta.getSaldo(),
                "O saldo inicial da conta deve ser alterado para " + valor);
        return conta;
    }
}