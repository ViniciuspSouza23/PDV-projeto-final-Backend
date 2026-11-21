package br.gov.sp.cps.fatec.pdv.model.service;

import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class VendaService {

    private final VendaRepository repository;

    public VendaService(VendaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "VendaRepository não pode ser nulo.");
    }

    public Venda finalizarVenda(String clienteNome, FormaPagamento formaPagamento,
            Map<Long, Integer> quantidadesPorProduto) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Selecione uma forma de pagamento.");
        }
        if (quantidadesPorProduto == null || quantidadesPorProduto.isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um produto à venda.");
        }

        Map<Long, Integer> itensValidados = new TreeMap<>();
        for (Map.Entry<Long, Integer> item : quantidadesPorProduto.entrySet()) {
            if (item.getKey() == null) {
                throw new IllegalArgumentException("Há um produto inválido na venda.");
            }
            if (item.getValue() == null || item.getValue() <= 0) {
                throw new IllegalArgumentException("A quantidade de cada produto deve ser maior que zero.");
            }
            itensValidados.put(item.getKey(), item.getValue());
        }

        String nomeNormalizado = clienteNome == null || clienteNome.trim().isEmpty()
                ? null
                : clienteNome.trim();
        if (nomeNormalizado != null && nomeNormalizado.length() > 120) {
            throw new IllegalArgumentException("O nome do cliente deve ter no máximo 120 caracteres.");
        }

        Venda venda = new Venda(nomeNormalizado, formaPagamento);
        return repository.finalizar(venda, itensValidados);
    }

    public List<Venda> listarTodas() {
        return repository.listarTodas();
    }
}
