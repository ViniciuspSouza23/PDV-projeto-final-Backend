package br.gov.sp.cps.fatec.pdv.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "servicos")
@NamedQueries({
        @NamedQuery(name = "Servico.listarTodos", query = "SELECT s FROM Servico s ORDER BY s.nome ASC")
})
public class Servico implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "preco_base", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoBase;

    @Column(name = "permite_multiplicador_porte", nullable = false)
    private boolean permiteMultiplicadorPorte = true;

    @Column(name = "descricao", length = 255)
    private String descricao;

    public Servico() {
    }

    public Servico(String nome, BigDecimal precoBase, boolean permiteMultiplicadorPorte, String descricao) {
        this.nome = nome;
        this.precoBase = precoBase;
        this.permiteMultiplicadorPorte = permiteMultiplicadorPorte;
        this.descricao = descricao;
    }

    public Servico(Long id, String nome, BigDecimal precoBase, boolean permiteMultiplicadorPorte, String descricao) {
        this(nome, precoBase, permiteMultiplicadorPorte, descricao);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPrecoBase() {
        return precoBase;
    }

    public void setPrecoBase(BigDecimal precoBase) {
        this.precoBase = precoBase;
    }

    public boolean isPermiteMultiplicadorPorte() {
        return permiteMultiplicadorPorte;
    }

    public void setPermiteMultiplicadorPorte(boolean permiteMultiplicadorPorte) {
        this.permiteMultiplicadorPorte = permiteMultiplicadorPorte;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Servico servico = (Servico) o;
        if (id != null && servico.id != null) {
            return Objects.equals(id, servico.id);
        }
        return Objects.equals(nome, servico.nome);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(nome);
    }

    @Override
    public String toString() {
        return nome;
    }
}
