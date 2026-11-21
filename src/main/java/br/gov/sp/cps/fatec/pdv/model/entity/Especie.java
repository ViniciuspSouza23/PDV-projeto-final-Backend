package br.gov.sp.cps.fatec.pdv.model.entity;

public enum Especie {
    CACHORRO("Cachorro"),
    GATO("Gato"),
    AVE("Ave"),
    ROEDOR("Roedor"),
    OUTRO("Outro");

    private final String descricao;

    Especie(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}
