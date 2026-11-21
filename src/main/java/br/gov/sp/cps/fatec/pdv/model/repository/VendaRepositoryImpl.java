package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class VendaRepositoryImpl implements VendaRepository {

    private final EntityManagerFactory emf;

    public VendaRepositoryImpl() {
        this(JPAUtil.getEntityManagerFactory());
    }

    public VendaRepositoryImpl(EntityManagerFactory emf) {
        this.emf = Objects.requireNonNull(emf, "EntityManagerFactory não pode ser nulo.");
    }

    @Override
    public Venda finalizar(Venda venda, Map<Long, Integer> quantidadesPorProduto) {
        Objects.requireNonNull(venda, "Venda não pode ser nula.");
        Objects.requireNonNull(quantidadesPorProduto, "Itens da venda não podem ser nulos.");

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Map<Long, Integer> itensOrdenados = new TreeMap<>(quantidadesPorProduto);

            for (Map.Entry<Long, Integer> entrada : itensOrdenados.entrySet()) {
                Produto produto = em.find(Produto.class, entrada.getKey(), LockModeType.PESSIMISTIC_WRITE);
                if (produto == null) {
                    throw new IllegalArgumentException("Produto não encontrado: " + entrada.getKey());
                }

                int quantidade = entrada.getValue();
                if (produto.getQuantidadeEstoque() < quantidade) {
                    throw new IllegalArgumentException("Estoque insuficiente para '" + produto.getNome()
                            + "'. Disponível: " + produto.getQuantidadeEstoque() + ".");
                }

                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
                ItemVenda item = new ItemVenda(produto.getId(), quantidade);
                item.setProdutoNome(produto.getNome());
                item.setPrecoUnitario(produto.getPreco());
                item.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));
                venda.adicionarItem(item);
            }

            venda.calcularTotal();
            em.persist(venda);
            tx.commit();
            return venda;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            if (e instanceof IllegalArgumentException) {
                throw e;
            }
            throw new RuntimeException("Erro ao finalizar venda: " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    @Override
    public List<Venda> listarTodas() {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Venda> query = em.createNamedQuery("Venda.listarTodas", Venda.class);
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }
}
