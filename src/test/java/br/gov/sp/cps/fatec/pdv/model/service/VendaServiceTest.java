package br.gov.sp.cps.fatec.pdv.model.service;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.Especie;
import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Porte;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.model.entity.TipoItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.ServicoRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ServicoRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepositoryImpl;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VendaServiceTest {

    private static EntityManagerFactory emf;
    private ProdutoRepository produtoRepository;
    private ClienteRepository clienteRepository;
    private PetRepository petRepository;
    private ServicoRepository servicoRepository;
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
        clienteRepository = new ClienteRepositoryImpl(emf);
        petRepository = new PetRepositoryImpl(emf);
        servicoRepository = new ServicoRepositoryImpl(emf);
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

    @Test
    @DisplayName("Deve finalizar venda híbrida de Pet Shop com produto e serviço associado a pet")
    void deveFinalizarVendaHibridaComProdutoEServico() {
        // 1. Prepara dados
        Produto racao = salvarProduto("Ração Super Premium 15kg", "150.00", 10);
        Servico banho = servicoRepository.salvar(new Servico("Banho & Tosa", new BigDecimal("60.00"), true, "Banho"));

        Cliente tutor = clienteRepository.salvar(new Cliente("Felipe Massa", UUID.randomUUID().toString().substring(0, 11),
                "11977776666", "felipe@email.com"));
        Pet pet = petRepository.salvar(new Pet("Max", Especie.CACHORRO, "Labrador", Porte.GRANDE, "Dócil", tutor));

        // 2. Monta itens da venda
        // Ração: 1 x 150.00 = 150.00
        ItemVenda itemProduto = ItemVenda.criarItemProduto(racao.getId(), racao.getNome(), racao.getPreco(), 1);
        // Banho para cão GRANDE (1.50x): 60.00 * 1.50 = 90.00
        BigDecimal precoAjustado = new BigDecimal("90.00");
        ItemVenda itemServico = ItemVenda.criarItemServico(banho.getId(), banho.getNome(), pet.getId(), pet.getNome(), precoAjustado, 1);

        // 3. Executa venda híbrida
        Venda venda = vendaService.finalizarVendaHibrida(tutor, null, FormaPagamento.CARTAO_CREDITO,
                List.of(itemProduto, itemServico));

        assertNotNull(venda.getId());
        // Total esperado: 150.00 + 90.00 = 240.00
        assertEquals(new BigDecimal("240.00"), venda.getTotal());
        assertEquals("Felipe Massa", venda.getClienteNome());
        assertEquals(tutor.getId(), venda.getCliente().getId());
        assertEquals(2, venda.getItens().size());

        // Baixa atômica no estoque de ração (10 - 1 = 9)
        Produto racaoAtualizada = produtoRepository.buscarPorId(racao.getId()).orElseThrow();
        assertEquals(9, racaoAtualizada.getQuantidadeEstoque());
    }

    private Produto salvarProduto(String nome, String preco, int estoque) {
        String codigo = UUID.randomUUID().toString();
        return produtoRepository.salvar(new Produto(codigo, nome, new BigDecimal(preco), estoque));
    }
}
