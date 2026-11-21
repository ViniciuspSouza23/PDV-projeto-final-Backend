package br.gov.sp.cps.fatec.pdv.model.service;

import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepositoryImpl;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VendaServiceTest {

    private static EntityManagerFactory emf;
    private ProdutoRepository produtoRepository;
    private VendaService vendaService;

    @BeforeAll
    static void setUpAll() {
        emf = Persistence.createEntityManagerFactory("pdv-test-pu");
    }

    @AfterAll
    static void tearDownAll() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @BeforeEach
    void setUp() {
        produtoRepository = new ProdutoRepositoryImpl(emf);
        VendaRepository vendaRepository = new VendaRepositoryImpl(emf);
        vendaService = new VendaService(vendaRepository);
    }

    @Test
    void devePersistirVendaComItensESubtrairEstoque() {
        Produto produto = salvarProduto("Produto vendido", "3.49", 6);

        Venda venda = vendaService.finalizarVenda("Ana", FormaPagamento.PIX, Map.of(produto.getId(), 2));

        assertTrue(venda.getId() > 0);
        assertEquals(new BigDecimal("6.98"), venda.getTotal());
        assertEquals(4, produtoRepository.buscarPorId(produto.getId()).orElseThrow().getQuantidadeEstoque());

        Venda historico = vendaService.listarTodas().stream()
                .filter(item -> item.getId().equals(venda.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals("Ana", historico.getClienteNome());
        assertEquals(FormaPagamento.PIX, historico.getFormaPagamento());
        assertEquals(1, historico.getItens().size());
        assertEquals("Produto vendido", historico.getItens().get(0).getProdutoNome());
        assertEquals(new BigDecimal("3.49"), historico.getItens().get(0).getPrecoUnitario());
    }

    @Test
    void deveReverterTodosOsItensQuandoEstoqueForInsuficiente() {
        Produto suficiente = salvarProduto("Produto suficiente", "2.00", 5);
        Produto insuficiente = salvarProduto("Produto insuficiente", "4.00", 1);
        String cliente = "Rollback-" + UUID.randomUUID();
        Map<Long, Integer> itens = new LinkedHashMap<>();
        itens.put(suficiente.getId(), 2);
        itens.put(insuficiente.getId(), 2);

        IllegalArgumentException erro = assertThrows(IllegalArgumentException.class,
                () -> vendaService.finalizarVenda(cliente, FormaPagamento.DINHEIRO, itens));

        assertTrue(erro.getMessage().contains("Estoque insuficiente"));
        assertEquals(5, produtoRepository.buscarPorId(suficiente.getId()).orElseThrow().getQuantidadeEstoque());
        assertEquals(1, produtoRepository.buscarPorId(insuficiente.getId()).orElseThrow().getQuantidadeEstoque());
        assertTrue(vendaService.listarTodas().stream().noneMatch(venda -> cliente.equals(venda.getClienteNome())));
    }

    @Test
    void deveRejeitarVendaSemItensOuFormaDePagamento() {
        assertThrows(IllegalArgumentException.class,
                () -> vendaService.finalizarVenda(null, FormaPagamento.PIX, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> vendaService.finalizarVenda(null, null, Map.of(1L, 1)));
        assertThrows(IllegalArgumentException.class,
                () -> vendaService.finalizarVenda(null, FormaPagamento.PIX, Map.of(1L, 0)));
    }

    private Produto salvarProduto(String nome, String preco, int estoque) {
        String codigo = UUID.randomUUID().toString();
        return produtoRepository.salvar(new Produto(codigo, nome, new BigDecimal(preco), estoque));
    }
}
