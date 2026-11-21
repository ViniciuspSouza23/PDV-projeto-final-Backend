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
import java.util.UUID;

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
        String cpf = UUID.randomUUID().toString().substring(0, 11);
        Cliente tutor = new Cliente("Roberto Firmino", cpf, "11988887777", "roberto@email.com");
        Cliente salvo = clienteService.salvar(tutor);

        assertNotNull(salvo.getId());
        assertEquals("Roberto Firmino", salvo.getNome());

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
        String cpf = UUID.randomUUID().toString().substring(0, 11);
        Cliente c1 = new Cliente("Tutor 1", cpf, "11999990000", null);
        clienteService.salvar(c1);

        Cliente c2 = new Cliente("Tutor 2", cpf, "11988881111", null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(c2));
        assertTrue(ex.getMessage().contains("Já existe um tutor cadastrado"));
    }

    @Test
    @DisplayName("Deve rejeitar cadastro com dados obrigatórios ausentes")
    void deveRejeitarDadosObrigatoriosAusentes() {
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(null));
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(new Cliente("", "123", "119", null)));
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(new Cliente("Nome", "", "119", null)));
        assertThrows(IllegalArgumentException.class, () -> clienteService.salvar(new Cliente("Nome", "123", "", null)));
    }
}
