package br.gov.sp.cps.fatec.pdv.model.entity;

import java.math.BigDecimal;

public enum Porte {
    PEQUENO("Pequeno (até 10kg)", new BigDecimal("1.00")),
    MEDIO("Médio (11 a 25kg)", new BigDecimal("1.25")),
    GRANDE("Grande (26 a 45kg)", new BigDecimal("1.50")),
    GIGANTE("Gigante (mais de 45kg)", new BigDecimal("1.80"));

    private final String descricao;
    private final BigDecimal multiplicador;

    Porte(String descricao, BigDecimal multiplicador) {
        this.descricao = descricao;
        this.multiplicador = multiplicador;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getMultiplicador() {
        return multiplicador;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
