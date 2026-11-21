package br.gov.sp.cps.fatec.pdv.model.service;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.observer.ClienteObserver;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepository;

import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PetRepository petRepository;
    private final List<ClienteObserver> observers;

    public ClienteService(ClienteRepository clienteRepository, PetRepository petRepository) {
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository não pode ser nulo.");
        this.petRepository = Objects.requireNonNull(petRepository, "PetRepository não pode ser nulo.");
        this.observers = new ArrayList<>();
    }

    public synchronized void addObserver(ClienteObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void removeObserver(ClienteObserver observer) {
        observers.remove(observer);
    }

    public void notificarObservadores() {
        List<Cliente> clientes = clienteRepository.listarTodos();
        List<ClienteObserver> copia;
        synchronized (this) {
            copia = new ArrayList<>(observers);
        }

        SwingUtilities.invokeLater(() -> {
            for (ClienteObserver obs : copia) {
                try {
                    obs.onClientesChanged(Collections.unmodifiableList(clientes));
                } catch (Exception e) {
                    System.err.println("Erro ao notificar observador de clientes: " + e.getMessage());
                }
            }
        });
    }

    public Cliente salvar(Cliente cliente) {
        validarCliente(cliente);

        Optional<Cliente> existente = clienteRepository.buscarPorCpf(cliente.getCpf());
        if (existente.isPresent()) {
            throw new IllegalArgumentException("Já existe um tutor cadastrado com o CPF informado: " + cliente.getCpf());
        }

        Cliente salvo = clienteRepository.salvar(cliente);
        notificarObservadores();
        return salvo;
    }

    public Cliente atualizar(Cliente cliente) {
        if (cliente.getId() == null) {
            throw new IllegalArgumentException("Cliente a ser atualizado deve possuir um ID.");
        }
        validarCliente(cliente);

        Optional<Cliente> existente = clienteRepository.buscarPorCpf(cliente.getCpf());
        if (existente.isPresent() && !existente.get().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("O CPF informado já pertence a outro tutor cadastrado: " + cliente.getCpf());
        }

        Cliente atualizado = clienteRepository.atualizar(cliente);
        notificarObservadores();
        return atualizado;
    }

    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido para exclusão.");
        }
        clienteRepository.excluir(id);
        notificarObservadores();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.buscarPorId(id);
    }

    public Optional<Cliente> buscarPorCpf(String cpf) {
        return clienteRepository.buscarPorCpf(cpf);
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.listarTodos();
    }

    public Pet adicionarPet(Long tutorId, Pet pet) {
        Objects.requireNonNull(tutorId, "Tutor ID não pode ser nulo.");
        Objects.requireNonNull(pet, "Pet não pode ser nulo.");
        if (pet.getNome() == null || pet.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do Pet é obrigatório.");
        }
        if (pet.getRaca() == null || pet.getRaca().trim().isEmpty()) {
            throw new IllegalArgumentException("A raça do Pet é obrigatória.");
        }
        if (pet.getEspecie() == null) {
            throw new IllegalArgumentException("A espécie do Pet é obrigatória.");
        }
        if (pet.getPorte() == null) {
            throw new IllegalArgumentException("O porte do Pet é obrigatório.");
        }

        Cliente tutor = clienteRepository.buscarPorId(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("Tutor não encontrado para o ID: " + tutorId));

        pet.setTutor(tutor);
        Pet petSalvo = petRepository.salvar(pet);
        notificarObservadores();
        return petSalvo;
    }

    public void excluirPet(Long petId) {
        if (petId == null) {
            throw new IllegalArgumentException("ID do pet não pode ser nulo.");
        }
        petRepository.excluir(petId);
        notificarObservadores();
    }

    public List<Pet> listarPetsPorTutor(Long tutorId) {
        return petRepository.listarPorTutor(tutorId);
    }

    private void validarCliente(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Dados do tutor não podem ser nulos.");
        }
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do tutor é obrigatório.");
        }
        if (cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF do tutor é obrigatório.");
        }
        if (cliente.getTelefone() == null || cliente.getTelefone().trim().isEmpty()) {
            throw new IllegalArgumentException("O telefone do tutor é obrigatório.");
        }
    }
}
