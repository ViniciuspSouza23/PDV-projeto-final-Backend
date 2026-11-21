package br.gov.sp.cps.fatec.pdv.model.service;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class VendaService {

    private final VendaRepository repository;

    public VendaService(VendaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "VendaRepository não pode ser nulo.");
    }

    /**
     * Finaliza venda apenas com produtos (retrocompatibilidade).
     */
    public Venda finalizarVenda(String clienteNome, FormaPagamento formaPagamento,
                                Map<Long, Integer> quantidadesPorProduto) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Selecione uma forma de pagamento.");
        }
        if (quantidadesPorProduto == null || quantidadesPorProduto.isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um produto à venda.");
        }

        List<ItemVenda> itens = new ArrayList<>();
        Map<Long, Integer> itensValidados = new TreeMap<>(quantidadesPorProduto);
        for (Map.Entry<Long, Integer> item : itensValidados.entrySet()) {
            if (item.getKey() == null) {
                throw new IllegalArgumentException("Há um produto inválido na venda.");
            }
            if (item.getValue() == null || item.getValue() <= 0) {
                throw new IllegalArgumentException("A quantidade de cada produto deve ser maior que zero.");
            }
            itens.add(new ItemVenda(item.getKey(), item.getValue()));
        }

        String nomeNormalizado = clienteNome == null || clienteNome.trim().isEmpty() ? null : clienteNome.trim();
        if (nomeNormalizado != null && nomeNormalizado.length() > 120) {
            throw new IllegalArgumentException("O nome do cliente deve ter no máximo 120 caracteres.");
        }

        Venda venda = new Venda(nomeNormalizado, formaPagamento);
        return repository.finalizarVendaHibrida(venda, itens);
    }

    /**
     * Finaliza venda híbrida de Pet Shop com suporte a produtos, serviços e tutor selecionado.
     */
    public Venda finalizarVendaHibrida(Cliente tutor, String clienteNomeAvulso, FormaPagamento formaPagamento,
                                       List<ItemVenda> itens) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Selecione uma forma de pagamento.");
        }
        if (itens == null || itens.isEmpty()) {
            throw new IllegalArgumentException("Adicione pelo menos um item à venda.");
        }

        for (ItemVenda item : itens) {
            if (item.getQuantidade() == null || item.getQuantidade() <= 0) {
                throw new IllegalArgumentException("A quantidade de cada item deve ser maior que zero.");
            }
            if (item.getPrecoUnitario() == null || item.getPrecoUnitario().signum() <= 0) {
                throw new IllegalArgumentException("O preço unitário de cada item deve ser maior que zero.");
            }
        }

        Venda venda;
        if (tutor != null) {
            venda = new Venda(tutor, formaPagamento);
        } else {
            String nome = clienteNomeAvulso == null || clienteNomeAvulso.trim().isEmpty() ? null : clienteNomeAvulso.trim();
            venda = new Venda(nome, formaPagamento);
        }

        return repository.finalizarVendaHibrida(venda, itens);
    }

    public List<Venda> listarTodas() {
        return repository.listarTodas();
    }
}
