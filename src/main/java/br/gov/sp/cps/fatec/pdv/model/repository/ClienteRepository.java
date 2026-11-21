package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository {
    Cliente salvar(Cliente cliente);

    Cliente atualizar(Cliente cliente);

    void excluir(Long id);

    Optional<Cliente> buscarPorId(Long id);

    Optional<Cliente> buscarPorCpf(String cpf);

    List<Cliente> listarTodos();
}
