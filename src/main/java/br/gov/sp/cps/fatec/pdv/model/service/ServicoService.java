package br.gov.sp.cps.fatec.pdv.model.service;

import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.model.observer.ServicoObserver;
import br.gov.sp.cps.fatec.pdv.model.repository.ServicoRepository;
import br.gov.sp.cps.fatec.pdv.model.strategy.CalculadoraPorPorteStrategy;
import br.gov.sp.cps.fatec.pdv.model.strategy.CalculadoraPrecoServico;

import javax.swing.SwingUtilities;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServicoService {

    private final ServicoRepository repository;
    private final CalculadoraPrecoServico calculadora;
    private final List<ServicoObserver> observers;

    public ServicoService(ServicoRepository repository) {
        this(repository, new CalculadoraPorPorteStrategy());
    }

    public ServicoService(ServicoRepository repository, CalculadoraPrecoServico calculadora) {
        this.repository = Objects.requireNonNull(repository, "ServicoRepository não pode ser nulo.");
        this.calculadora = Objects.requireNonNull(calculadora, "CalculadoraPrecoServico não pode ser nula.");
        this.observers = new ArrayList<>();
    }

    public synchronized void addObserver(ServicoObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public synchronized void removeObserver(ServicoObserver observer) {
        observers.remove(observer);
    }

    public void notificarObservadores() {
        List<Servico> lista = repository.listarTodos();
        List<ServicoObserver> copia;
        synchronized (this) {
            copia = new ArrayList<>(observers);
        }

        SwingUtilities.invokeLater(() -> {
            for (ServicoObserver obs : copia) {
                try {
                    obs.onServicosChanged(Collections.unmodifiableList(lista));
                } catch (Exception e) {
                    System.err.println("Erro ao notificar observador de serviços: " + e.getMessage());
                }
            }
        });
    }

    public Servico salvar(Servico servico) {
        validarServico(servico);
        Servico salvo = repository.salvar(servico);
        notificarObservadores();
        return salvo;
    }

    public Servico atualizar(Servico servico) {
        if (servico.getId() == null) {
            throw new IllegalArgumentException("O serviço deve possuir ID para ser atualizado.");
        }
        validarServico(servico);
        Servico atualizado = repository.atualizar(servico);
        notificarObservadores();
        return atualizado;
    }

    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID inválido para exclusão.");
        }
        repository.excluir(id);
        notificarObservadores();
    }

    public Optional<Servico> buscarPorId(Long id) {
        return repository.buscarPorId(id);
    }

    public List<Servico> listarTodos() {
        return repository.listarTodos();
    }

    public BigDecimal calcularPreco(Servico servico, Pet pet) {
        return calculadora.calcularPreco(servico, pet);
    }

    private void validarServico(Servico servico) {
        if (servico == null) {
            throw new IllegalArgumentException("Dados do serviço não podem ser nulos.");
        }
        if (servico.getNome() == null || servico.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do serviço é obrigatório.");
        }
        if (servico.getPrecoBase() == null || servico.getPrecoBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O preço base do serviço deve ser maior que zero.");
        }
    }
}
