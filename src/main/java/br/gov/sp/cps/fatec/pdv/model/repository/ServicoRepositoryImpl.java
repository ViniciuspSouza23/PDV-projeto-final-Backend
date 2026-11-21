package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServicoRepositoryImpl implements ServicoRepository {

    private final EntityManagerFactory emf;

    public ServicoRepositoryImpl() {
        this(JPAUtil.getEntityManagerFactory());
    }

    public ServicoRepositoryImpl(EntityManagerFactory emf) {
        this.emf = Objects.requireNonNull(emf, "EntityManagerFactory não pode ser nulo.");
    }

    @Override
    public Servico salvar(Servico servico) {
        Objects.requireNonNull(servico, "Serviço não pode ser nulo.");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(servico);
            tx.commit();
            return servico;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao salvar serviço: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Servico atualizar(Servico servico) {
        Objects.requireNonNull(servico, "Serviço não pode ser nulo.");
        if (servico.getId() == null) {
            throw new IllegalArgumentException("Serviço a ser atualizado deve possuir um ID.");
        }
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Servico mesclado = em.merge(servico);
            tx.commit();
            return mesclado;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao atualizar serviço: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public void excluir(Long id) {
        Objects.requireNonNull(id, "ID do serviço não pode ser nulo.");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Servico servico = em.find(Servico.class, id);
            if (servico == null) {
                throw new IllegalArgumentException("Serviço não encontrado para o ID: " + id);
            }
            em.remove(servico);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao excluir serviço ID " + id + ": " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public Optional<Servico> buscarPorId(Long id) {
        if (id == null) return Optional.empty();
        EntityManager em = emf.createEntityManager();
        try {
            Servico servico = em.find(Servico.class, id);
            return Optional.ofNullable(servico);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    @Override
    public List<Servico> listarTodos() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Servico> query = em.createNamedQuery("Servico.listarTodos", Servico.class);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}
