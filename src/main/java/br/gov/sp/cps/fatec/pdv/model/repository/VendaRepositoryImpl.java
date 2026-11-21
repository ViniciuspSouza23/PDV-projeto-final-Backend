package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.model.entity.TipoItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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

        List<ItemVenda> itens = new ArrayList<>();
        Map<Long, Integer> itensOrdenados = new TreeMap<>(quantidadesPorProduto);
        for (Map.Entry<Long, Integer> entrada : itensOrdenados.entrySet()) {
            itens.add(new ItemVenda(entrada.getKey(), entrada.getValue()));
        }
        return finalizarVendaHibrida(venda, itens);
    }

    @Override
    public Venda finalizarVendaHibrida(Venda venda, List<ItemVenda> itens) {
        Objects.requireNonNull(venda, "Venda não pode ser nula.");
        Objects.requireNonNull(itens, "Itens da venda não podem ser nulos.");
        if (itens.isEmpty()) {
            throw new IllegalArgumentException("A venda deve conter ao menos um item.");
        }

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // Se a venda possui cliente associado, anexa no contexto gerenciado
            if (venda.getCliente() != null && venda.getCliente().getId() != null) {
                Cliente clienteGerenciado = em.find(Cliente.class, venda.getCliente().getId());
                if (clienteGerenciado != null) {
                    venda.setCliente(clienteGerenciado);
                }
            }

            // Ordena produtos por ID para prevenir deadlocks em concorrência
            List<ItemVenda> produtosItens = itens.stream()
                    .filter(i -> i.getTipoItem() == TipoItemVenda.PRODUTO)
                    .sorted(Comparator.comparing(ItemVenda::getProdutoId))
                    .toList();

            for (ItemVenda itemProduto : produtosItens) {
                Produto produto = em.find(Produto.class, itemProduto.getProdutoId(), LockModeType.PESSIMISTIC_WRITE);
                if (produto == null) {
                    throw new IllegalArgumentException("Produto não encontrado: " + itemProduto.getProdutoId());
                }

                int quantidade = itemProduto.getQuantidade();
                if (produto.getQuantidadeEstoque() < quantidade) {
                    throw new IllegalArgumentException("Estoque insuficiente para '" + produto.getNome()
                            + "'. Disponível: " + produto.getQuantidadeEstoque() + ".");
                }

                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - quantidade);
                itemProduto.setProdutoNome(produto.getNome());
                itemProduto.setPrecoUnitario(produto.getPreco());
                itemProduto.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));
                venda.adicionarItem(itemProduto);
            }

            // Itens de Serviço (não baixam estoque físico)
            List<ItemVenda> servicosItens = itens.stream()
                    .filter(i -> i.getTipoItem() == TipoItemVenda.SERVICO)
                    .toList();

            for (ItemVenda itemServico : servicosItens) {
                Servico servico = em.find(Servico.class, itemServico.getServicoId());
                if (servico == null) {
                    throw new IllegalArgumentException("Serviço não encontrado: " + itemServico.getServicoId());
                }

                String nomeDescritivo = servico.getNome();
                if (itemServico.getPetId() != null) {
                    Pet pet = em.find(Pet.class, itemServico.getPetId());
                    if (pet != null) {
                        itemServico.setPetNome(pet.getNome());
                        nomeDescritivo = servico.getNome() + " [Pet: " + pet.getNome() + " (" + pet.getPorte().getDescricao() + ")]";
                    }
                }

                itemServico.setProdutoNome(nomeDescritivo);
                if (itemServico.getSubtotal() == null) {
                    itemServico.setSubtotal(itemServico.getPrecoUnitario().multiply(BigDecimal.valueOf(itemServico.getQuantidade())));
                }
                venda.adicionarItem(itemServico);
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
