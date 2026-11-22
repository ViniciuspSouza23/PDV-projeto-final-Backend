package br.gov.sp.cps.fatec.pdv;

import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.ServicoRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ServicoRepositoryImpl;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainDemoDataTest {

    private static EntityManagerFactory emf;

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

    @Test
    void devePreservarProdutosExistentesENaoAlterarEstoqueDuranteInicializacao() {
        ProdutoRepository produtoRepository = new ProdutoRepositoryImpl(emf);
        ClienteRepository clienteRepository = new ClienteRepositoryImpl(emf);
        PetRepository petRepository = new PetRepositoryImpl(emf);
        ServicoRepository servicoRepository = new ServicoRepositoryImpl(emf);

        String codigo = UUID.randomUUID().toString();
        Produto produtoComEstoqueBaixo = produtoRepository.salvar(
                new Produto(codigo, "Café especial para pets", new BigDecimal("19.90"), 4));

        Main.inicializarDadosDemonstracao(
                produtoRepository, servicoRepository, clienteRepository, petRepository);

        Produto preservado = produtoRepository.buscarPorId(produtoComEstoqueBaixo.getId()).orElseThrow();
        assertEquals(4, preservado.getQuantidadeEstoque());
        assertTrue(produtoRepository.buscarPorCodigoBarras(codigo).isPresent());
        assertFalse(produtoRepository.buscarPorCodigoBarras("7891000100011").isPresent());
    }
}
