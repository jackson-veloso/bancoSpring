package tech.ada.banco.controller;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import tech.ada.banco.model.Conta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PixControllerTest extends BaseContaTest{

    private final String baseUri = "/pix";

    @Test
    void testPixSaldoPositivo() throws Exception {
        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.ONE);

        Integer numeroContaDestino =  contaDestino.getNumeroConta();

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino",numeroContaDestino.toString())
                                .param("valor", "7")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals("3", response);
        assertEquals(BigDecimal.valueOf(3), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(8), contaDestino.getSaldo());
    }

    @Test
    void testPixSaldoPositivoFracionado() throws Exception {
        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.ZERO);

        Integer numeroContaDestino =  contaDestino.getNumeroConta();

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino",numeroContaDestino.toString())
                                .param("valor", "8.1")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals("1.9", response);
        assertEquals(BigDecimal.valueOf(1.9), contaOrigem.getSaldo());
        assertEquals(BigDecimal.valueOf(8.1), contaDestino.getSaldo());
    }
    @Test
    void testPixSaldoTotal() throws Exception {
        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.ZERO);

        Integer numeroContaDestino =  contaDestino.getNumeroConta();

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino",numeroContaDestino.toString())
                                .param("valor", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn().getResponse().getContentAsString();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals("0", response);
        assertEquals(BigDecimal.ZERO, contaOrigem.getSaldo());
        assertEquals(BigDecimal.TEN, contaDestino.getSaldo());
    }
    @Test
    void testPixSaldoInsuficiente() throws Exception {
        Conta contaOrigem = criarConta(BigDecimal.ONE);
        Conta contaDestino = criarConta(BigDecimal.TEN);

        Integer numeroContaDestino =  contaDestino.getNumeroConta();

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                .param("destino",numeroContaDestino.toString())
                                .param("valor", "8")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals("Limite acima do saldo disponível!", response);
        assertEquals(BigDecimal.ONE, contaOrigem.getSaldo());
        assertEquals(BigDecimal.TEN, contaDestino.getSaldo());
    }
    @Test
    void testPixSaldoNegativo() {
        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.ZERO);

        Integer numeroContaOrigem = contaOrigem.getNumeroConta();
        Integer numeroContaDestino =  contaDestino.getNumeroConta();


        Conta finalContaOrigem = contaOrigem;
        String messageError = "Request processing failed: java.lang.IllegalArgumentException: Operação não foi realizada pois o valor da transação é negativo.";
        String message = assertThrows(ServletException.class,
                () -> {
                    mvc.perform(post(baseUri + "/" + finalContaOrigem.getNumeroConta())
                            .param("destino", numeroContaDestino.toString())
                            .param("valor", "-8")
                            .contentType(MediaType.APPLICATION_JSON));
                }).getMessage();

        assertEquals(messageError,message);

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals(BigDecimal.TEN, contaOrigem.getSaldo());
        assertEquals(BigDecimal.ZERO, contaDestino.getSaldo());
    }

    @Test
    void testPixSaldoNegativo2() {
        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.ZERO);

        Integer numeroContaOrigem = contaOrigem.getNumeroConta();
        Integer numeroContaDestino =  contaDestino.getNumeroConta();


        try {
            String response =
                    mvc.perform(post(baseUri + "/" + contaOrigem.getNumeroConta())
                                    .param("destino",numeroContaDestino.toString())
                                    .param("valor", "-8")
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andDo(print())
                            .andExpect(status().isBadRequest())
                            .andReturn().getResponse().getErrorMessage();
            Assertions.fail();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Operação não foi realizada pois o valor da transação é negativo."));
        }

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals(BigDecimal.TEN, contaOrigem.getSaldo());
        assertEquals(BigDecimal.ZERO, contaDestino.getSaldo());
    }

    @Test
    void testPixContaOrigemInvalida() throws Exception {
        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.TEN);

        Integer numeroContaDestino =  contaDestino.getNumeroConta();

        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9999);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/" + "9999")
                                .param("destino",numeroContaDestino.toString())
                                .param("valor", "3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isNotFound())
                        .andReturn().getResponse().getErrorMessage();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals(BigDecimal.TEN, contaOrigem.getSaldo());
        assertEquals(BigDecimal.TEN, contaDestino.getSaldo());
    }
    @Test
    void testPixContaDestinoInvalida() throws Exception {
        Conta contaOrigem = criarConta(BigDecimal.TEN);
        Conta contaDestino = criarConta(BigDecimal.TEN);

        Optional<Conta> contaInexistente = repository.findContaByNumeroConta(9999);
        assertTrue(contaInexistente.isEmpty());

        String response =
                mvc.perform(post(baseUri + "/" + contaOrigem)
                                .param("destino","9999")
                                .param("valor", "3")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isBadRequest())
                        .andReturn().getResponse().getErrorMessage();

        contaOrigem = obtemContaDoBanco(contaOrigem);
        contaDestino = obtemContaDoBanco(contaDestino);

        assertEquals(BigDecimal.TEN, contaOrigem.getSaldo());
        assertEquals(BigDecimal.TEN, contaDestino.getSaldo());
    }




}