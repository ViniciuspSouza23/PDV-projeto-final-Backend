package br.gov.sp.cps.fatec.pdv.model.service;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.Especie;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Porte;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepositoryImpl;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClienteServiceTest {

    private static EntityManagerFactory emf;
    private ClienteService clienteService;
    private ClienteRepository clienteRepository;

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
        clienteRepository = new ClienteRepositoryImpl(emf);
        PetRepository petRepository = new PetRepositoryImpl(emf);
        clienteService = new ClienteService(clienteRepository, petRepository);
    }

    @Test
    @DisplayName("Deve cadastrar tutor válido e vincular pets com sucesso")
    void deveCadastrarTutorEVincularPets() {
        Cliente tutor = new Cliente("Roberto Firmino", "123.456.789-09", "11988887777", "roberto@email.com");
        Cliente salvo = clienteService.salvar(tutor);

        assertNotNull(salvo.getId());
        assertEquals("Roberto Firmino", salvo.getNome());
        assertEquals("123.456.789-09", salvo.getCpf());

        Pet pet1 = new Pet("Buddy", Especie.CACHORRO, "Golden Retriever", Porte.GRANDE, "Muito brincalhão");
        Pet petSalvo = clienteService.adicionarPet(salvo.getId(), pet1);

        assertNotNull(petSalvo.getId());
        List<Pet> petsDoTutor = clienteService.listarPetsPorTutor(salvo.getId());
        assertEquals(1, petsDoTutor.size());
        assertEquals("Buddy", petsDoTutor.get(0).getNome());
    }

    @Test
    @DisplayName("Deve rejeitar tutor com CPF duplicado")
    void deveRejeitarCpfDuplicado() {
        clienteRepository.salvar(new Cliente("Tutor 1", "24681357928", "11999990000", null));

        Cliente c2 = new Cliente("Tutor 2", "246.813.579-28", "11988881111", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(c2));
        assertTrue(ex.getMessage().contains("Já existe um tutor cadastrado"));
    }

    @Test
    @DisplayName("Deve aceitar CPF válido sem pontuação e armazená-lo formatado")
    void deveNormalizarCpfSemPontuacao() {
        Cliente tutor = new Cliente("Tutor", "52998224725", "11988887777", null);

        assertTrue(clienteService.buscarPorCpf(null).isEmpty());
        assertTrue(clienteService.buscarPorCpf("  ").isEmpty());

        Cliente salvo = clienteService.salvar(tutor);

        assertEquals("529.982.247-25", salvo.getCpf());
    }

    @Test
    @DisplayName("Deve rejeitar CPF com dígitos verificadores inválidos ou sequência repetida")
    void deveRejeitarCpfInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> clienteService.salvar(new Cliente("Tutor", "123.456.789-08", "11988887777", null)));
        assertThrows(IllegalArgumentException.class,
                () -> clienteService.salvar(new Cliente("Tutor", "111.111.111-11", "11988887777", null)));
        assertThrows(IllegalArgumentException.class,
                () -> clienteService.salvar(new Cliente("Tutor", "321.654.987-9A", "11988887777", null)));
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com dados obrigatórios ausentes")
    void deveRejeitarDadosObrigatoriosAusentes() {
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(null));
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(new Cliente("", "123", "119", null)));
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(new Cliente("Nome", "", "119", null)));
        assertThrows(IllegalArgumentException.class,
                () -> clienteService.salvar(new Cliente("Nome", "123.456.789-09", "", null)));
    }
}
