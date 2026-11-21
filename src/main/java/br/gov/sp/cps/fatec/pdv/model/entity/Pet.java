package br.gov.sp.cps.fatec.pdv.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "pets")
@NamedQueries({
        @NamedQuery(name = "Pet.listarPorTutor", query = "SELECT p FROM Pet p WHERE p.tutor.id = :tutorId ORDER BY p.nome ASC")
})
public class Pet implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "especie", nullable = false, length = 20)
    private Especie especie = Especie.CACHORRO;

    @Column(name = "raca", nullable = false, length = 60)
    private String raca;

    @Enumerated(EnumType.STRING)
    @Column(name = "porte", nullable = false, length = 20)
    private Porte porte = Porte.PEQUENO;

    @Column(name = "observacoes", length = 255)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Cliente tutor;

    public Pet() {
    }

    public Pet(String nome, Especie especie, String raca, Porte porte, String observacoes) {
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.porte = porte;
        this.observacoes = observacoes;
    }

    public Pet(String nome, Especie especie, String raca, Porte porte, String observacoes, Cliente tutor) {
        this(nome, especie, raca, porte, observacoes);
        this.tutor = tutor;
    }

    public Pet(Long id, String nome, Especie especie, String raca, Porte porte, String observacoes, Cliente tutor) {
        this.id = id;
        this.nome = nome;
        this.especie = especie;
        this.raca = raca;
        this.porte = porte;
        this.observacoes = observacoes;
        this.tutor = tutor;
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

    public Especie getEspecie() {
        return especie;
    }

    public void setEspecie(Especie especie) {
        this.especie = especie;
    }

    public String getRaca() {
        return raca;
    }

    public void setRaca(String raca) {
        this.raca = raca;
    }

    public Porte getPorte() {
        return porte;
    }

    public void setPorte(Porte porte) {
        this.porte = porte;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Cliente getTutor() {
        return tutor;
    }

    public void setTutor(Cliente tutor) {
        this.tutor = tutor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return Objects.equals(id, pet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nome + " (" + especie + " - " + porte.getDescricao() + ")";
    }
}
