package br.gov.sp.cps.fatec.pdv.model.entity;

public enum TipoItemVenda {
    PRODUTO("Produto"),
    SERVICO("Serviço");

    private final String descricao;

    TipoItemVenda(String descricao) {
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
