package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PetRepositoryImpl implements PetRepository {

    private final EntityManagerFactory emf;

    public PetRepositoryImpl() {
        this(JPAUtil.getEntityManagerFactory());
    }

    public PetRepositoryImpl(EntityManagerFactory emf) {
        this.emf = Objects.requireNonNull(emf, "EntityManagerFactory não pode ser nulo.");
    }

    @Override
    public Pet salvar(Pet pet) {
        Objects.requireNonNull(pet, "Pet não pode ser nulo.");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(pet);
            tx.commit();
            return pet;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao salvar pet: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Pet atualizar(Pet pet) {
        Objects.requireNonNull(pet, "Pet não pode ser nulo.");
        if (pet.getId() == null) {
            throw new IllegalArgumentException("Pet a ser atualizado deve possuir um ID.");
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Pet mesclado = em.merge(pet);
            tx.commit();
            return mesclado;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao atualizar pet: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public void excluir(Long id) {
        Objects.requireNonNull(id, "ID do pet não pode ser nulo.");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Pet pet = em.find(Pet.class, id);
            if (pet == null) {
                throw new IllegalArgumentException("Pet não encontrado para o ID: " + id);
            }
            em.remove(pet);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao excluir pet ID " + id + ": " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Pet> buscarPorId(Long id) {
        if (id == null) return Optional.empty();
        EntityManager em = emf.createEntityManager();
        try {
            Pet pet = em.find(Pet.class, id);
            return Optional.ofNullable(pet);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Pet> listarPorTutor(Long tutorId) {
        if (tutorId == null) return List.of();
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Pet> query = em.createNamedQuery("Pet.listarPorTutor", Pet.class);
            query.setParameter("tutorId", tutorId);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}
