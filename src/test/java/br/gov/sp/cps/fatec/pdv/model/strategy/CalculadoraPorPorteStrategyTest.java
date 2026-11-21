package br.gov.sp.cps.fatec.pdv.model.strategy;

import br.gov.sp.cps.fatec.pdv.model.entity.Especie;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Porte;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CalculadoraPorPorteStrategyTest {

    private CalculadoraPrecoServico calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraPorPorteStrategy();
    }

    @Test
    @DisplayName("Deve calcular valor ajustado para serviço de banho conforme porte do pet")
    void deveAjustarPrecoConformePorte() {
        Servico banho = new Servico("Banho", new BigDecimal("50.00"), true, "Banho pet");

        Pet pequeno = new Pet("Pipoca", Especie.CACHORRO, "Pinscher", Porte.PEQUENO, null);
        Pet medio = new Pet("Mel", Especie.CACHORRO, "Beagle", Porte.MEDIO, null);
        Pet grande = new Pet("Thor", Especie.CACHORRO, "Golden", Porte.GRANDE, null);
        Pet gigante = new Pet("Hulk", Especie.CACHORRO, "Dogue Alemão", Porte.GIGANTE, null);

        // 50.00 * 1.00 = 50.00
        assertEquals(new BigDecimal("50.00"), calculadora.calcularPreco(banho, pequeno));
        // 50.00 * 1.25 = 62.50
        assertEquals(new BigDecimal("62.50"), calculadora.calcularPreco(banho, medio));
        // 50.00 * 1.50 = 75.00
        assertEquals(new BigDecimal("75.00"), calculadora.calcularPreco(banho, grande));
        // 50.00 * 1.80 = 90.00
        assertEquals(new BigDecimal("90.00"), calculadora.calcularPreco(banho, gigante));
    }

    @Test
    @DisplayName("Não deve alterar preço se serviço tiver multiplicador de porte desativado")
    void naoDeveAlterarPrecoServicoFixo() {
        Servico consulta = new Servico("Consulta Veterinária", new BigDecimal("120.00"), false, "Consulta fixa");
        Pet grande = new Pet("Thor", Especie.CACHORRO, "Golden", Porte.GRANDE, null);

        assertEquals(new BigDecimal("120.00"), calculadora.calcularPreco(consulta, grande));
    }

    @Test
    @DisplayName("Deve manter preço base se pet for nulo")
    void deveManterPrecoBaseQuandoPetNulo() {
        Servico banho = new Servico("Banho", new BigDecimal("50.00"), true, "Banho pet");
        assertEquals(new BigDecimal("50.00"), calculadora.calcularPreco(banho, null));
    }

    @Test
    @DisplayName("Deve lançar exceção se serviço for nulo")
    void deveLancarExcecaoQuandoServicoNulo() {
        assertThrows(IllegalArgumentException.class, () -> calculadora.calcularPreco(null, null));
    }
}
