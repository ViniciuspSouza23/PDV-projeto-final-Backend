package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.Venda;

import java.util.List;
import java.util.Map;

public interface VendaRepository {
    Venda finalizar(Venda venda, Map<Long, Integer> quantidadesPorProduto);

    List<Venda> listarTodas();
}
