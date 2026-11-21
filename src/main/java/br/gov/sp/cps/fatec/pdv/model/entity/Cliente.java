package br.gov.sp.cps.fatec.pdv.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "clientes")
@NamedQueries({
        @NamedQuery(name = "Cliente.listarTodos", query = "SELECT c FROM Cliente c ORDER BY c.nome ASC"),
        @NamedQuery(name = "Cliente.buscarPorCpf", query = "SELECT c FROM Cliente c WHERE c.cpf = :cpf")
})
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @Column(name = "cpf", unique = true, nullable = false, length = 18)
    private String cpf;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "email", length = 100)
    private String email;

    @OneToMany(mappedBy = "tutor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("nome ASC")
    private List<Pet> pets = new ArrayList<>();

    public Cliente() {
    }

    public Cliente(String nome, String cpf, String telefone, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.telefone = telefone;
        this.email = email;
    }

    public Cliente(Long id, String nome, String cpf, String telefone, String email) {
        this(nome, cpf, telefone, email);
        this.id = id;
    }

    public void adicionarPet(Pet pet) {
        pet.setTutor(this);
        pets.add(pet);
    }

    public void removerPet(Pet pet) {
        pets.remove(pet);
        pet.setTutor(null);
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Pet> getPets() {
        return pets;
    }

    public void setPets(List<Pet> pets) {
        this.pets = pets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        if (id != null && cliente.id != null) {
            return Objects.equals(id, cliente.id);
        }
        return Objects.equals(cpf, cliente.cpf);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(cpf);
    }

    @Override
    public String toString() {
        return nome + " (" + cpf + ")";
    }
}
