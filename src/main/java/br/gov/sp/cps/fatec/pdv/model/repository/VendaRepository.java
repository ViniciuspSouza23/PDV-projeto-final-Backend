package br.gov.sp.cps.fatec.pdv.model.repository;

import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;

import java.util.List;
import java.util.Map;

public interface VendaRepository {

    /**
     * Finaliza venda apenas com produtos (mantido para compatibilidade).
     */
    Venda finalizar(Venda venda, Map<Long, Integer> quantidadesPorProduto);

    /**
     * Finaliza venda mista (produtos com baixa atômica de estoque e serviços de pet shop).
     */
    Venda finalizarVendaHibrida(Venda venda, List<ItemVenda> itens);

    List<Venda> listarTodas();
}
