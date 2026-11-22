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
            validarPetsDoTutor(em, venda.getCliente(), itens);

            // Ordena produtos por ID para prevenir deadlocks em concorrência
            List<ItemVenda> produtosItens = itens.stream()
                    .filter(i -> i.getTipoItem() == TipoItemVenda.PRODUTO)
                    .sorted(Comparator.comparing(ItemVenda::getProdutoId))
                    .toList();

            // Valida e decrementa estoque agrupando por produto
            Map<Long, Integer> qtdPorProduto = new TreeMap<>();
            for (ItemVenda itemProduto : produtosItens) {
                qtdPorProduto.merge(itemProduto.getProdutoId(), itemProduto.getQuantidade(), Integer::sum);
            }

            for (Map.Entry<Long, Integer> entry : qtdPorProduto.entrySet()) {
                Produto produto = em.find(Produto.class, entry.getKey(), LockModeType.PESSIMISTIC_WRITE);
                if (produto == null) {
                    throw new IllegalArgumentException("Produto não encontrado: " + entry.getKey());
                }

                int totalSolicitado = entry.getValue();
                if (produto.getQuantidadeEstoque() < totalSolicitado) {
                    throw new IllegalArgumentException("Estoque insuficiente para '" + produto.getNome()
                            + "'. Solicitado: " + totalSolicitado + ", Disponível: " + produto.getQuantidadeEstoque() + ".");
                }

                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - totalSolicitado);
            }

            for (ItemVenda itemProduto : produtosItens) {
                Produto produto = em.find(Produto.class, itemProduto.getProdutoId());
                Pet pet = itemProduto.getPetId() == null ? null : em.find(Pet.class, itemProduto.getPetId());
                // Cria entidade gerenciada nova e transiente para a persistência mantendo vínculo com o Pet se houver
                ItemVenda novoItem = ItemVenda.criarItemProduto(produto.getId(),
                        produto.getNome(), itemProduto.getPetId(), pet != null ? pet.getNome() : null,
                        produto.getPreco(), itemProduto.getQuantidade());
                if (itemProduto.getProdutoNome() != null && !itemProduto.getProdutoNome().isBlank()) {
                    novoItem.setProdutoNome(itemProduto.getProdutoNome());
                }
                venda.adicionarItem(novoItem);
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

                String petNome = null;
                String nomeDescritivo = servico.getNome();
                if (itemServico.getPetId() != null) {
                    Pet pet = em.find(Pet.class, itemServico.getPetId());
                    if (pet != null) {
                        petNome = pet.getNome();
                        nomeDescritivo = servico.getNome() + " [Pet: " + pet.getNome() + " (" + pet.getPorte().getDescricao() + ")]";
                    }
                }

                // Cria entidade gerenciada nova e transiente para a persistência
                ItemVenda novoItem = ItemVenda.criarItemServico(servico.getId(), servico.getNome(), itemServico.getPetId(),
                        petNome, itemServico.getPrecoUnitario(), itemServico.getQuantidade());
                novoItem.setProdutoNome(nomeDescritivo);
                venda.adicionarItem(novoItem);
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

    private void validarPetsDoTutor(EntityManager em, Cliente tutor, List<ItemVenda> itens) {
        if (tutor == null || tutor.getId() == null) {
            return;
        }

        for (ItemVenda item : itens) {
            if (item.getPetId() == null) {
                continue;
            }

            Pet pet = em.find(Pet.class, item.getPetId());
            if (pet == null) {
                throw new IllegalArgumentException("Pet não encontrado: " + item.getPetId());
            }
            if (!Objects.equals(tutor.getId(), pet.getTutor().getId())) {
                throw new IllegalArgumentException(
                        "O pet '" + pet.getNome() + "' não pertence ao tutor selecionado.");
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
