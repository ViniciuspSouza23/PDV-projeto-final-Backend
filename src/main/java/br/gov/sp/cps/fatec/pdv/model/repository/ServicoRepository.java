package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.Servico;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository {
    Servico salvar(Servico servico);

    Servico atualizar(Servico servico);

    void excluir(Long id);

    Optional<Servico> buscarPorId(Long id);

    List<Servico> listarTodos();
}
