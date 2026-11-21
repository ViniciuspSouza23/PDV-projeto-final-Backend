package br.gov.sp.cps.fatec.pdv.controller;

import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.model.service.ProdutoService;
import br.gov.sp.cps.fatec.pdv.model.service.VendaService;
import br.gov.sp.cps.fatec.pdv.view.MainFrame;
import br.gov.sp.cps.fatec.pdv.view.VendaView;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class VendaController {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final MainFrame mainFrame;
    private final VendaView vendaView;
    private final ProdutoService produtoService;
    private final VendaService vendaService;

    public VendaController(MainFrame mainFrame, ProdutoService produtoService, VendaService vendaService) {
        this.mainFrame = Objects.requireNonNull(mainFrame, "MainFrame não pode ser nulo.");
        this.vendaView = mainFrame.getVendaView();
        this.produtoService = Objects.requireNonNull(produtoService, "ProdutoService não pode ser nulo.");
        this.vendaService = Objects.requireNonNull(vendaService, "VendaService não pode ser nulo.");
        vincularEventos();
    }

    private void vincularEventos() {
        mainFrame.getListView().getBtnAbrirVenda().addActionListener(event -> abrirVenda());
        vendaView.getBtnAdicionar().addActionListener(event -> adicionarProduto());
        vendaView.getBtnRemover().addActionListener(event -> vendaView.removerItemSelecionado());
        vendaView.getBtnFinalizar().addActionListener(event -> finalizarVenda());
        vendaView.getBtnVoltar().addActionListener(event -> mainFrame.mostrarLista());
        vendaView.getBtnAtualizarHistorico().addActionListener(event -> carregarDados());
        vendaView.getBtnDetalhes().addActionListener(event -> mostrarDetalhesVenda());
    }

    private void abrirVenda() {
        mainFrame.mostrarVenda();
        carregarDados();
    }

    private void adicionarProduto() {
        try {
            vendaView.adicionarProdutoSelecionado();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Produto não adicionado",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarDetalhesVenda() {
        Venda venda = vendaView.getVendaSelecionada();
        if (venda == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione uma venda no histórico.",
                    "Nenhuma venda selecionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder detalhes = new StringBuilder("Venda #")
                .append(venda.getId()).append("\n\n");
        for (ItemVenda item : venda.getItens()) {
            detalhes.append(MessageFormat.format("{0} x {1} — {2}\n",
                    item.getQuantidade(), item.getProdutoNome(), MOEDA.format(item.getSubtotal())));
        }
        detalhes.append("\nTotal: ").append(MOEDA.format(venda.getTotal()));
        JOptionPane.showMessageDialog(mainFrame, detalhes.toString(),
                "Itens da venda", JOptionPane.INFORMATION_MESSAGE);
    }

    private void carregarDados() {
        vendaView.setBusy(true);
        new SwingWorker<DadosVenda, Void>() {
            @Override
            protected DadosVenda doInBackground() {
                return new DadosVenda(produtoService.listarTodos(), vendaService.listarTodas());
            }

            @Override
            protected void done() {
                try {
                    DadosVenda dados = get();
                    vendaView.setProdutos(dados.produtos());
                    vendaView.setHistorico(dados.vendas());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarErro("A leitura dos dados foi interrompida.", ex);
                } catch (ExecutionException ex) {
                    mostrarErro("Não foi possível carregar os dados da venda.", ex.getCause());
                } finally {
                    vendaView.setBusy(false);
                }
            }
        }.execute();
    }

    private void finalizarVenda() {
        String clienteNome = vendaView.getClienteNome();
        FormaPagamento formaPagamento = vendaView.getFormaPagamento();
        Map<Long, Integer> quantidades = vendaView.getQuantidadesPorProduto();

        try {
            vendaView.setBusy(true);
            new SwingWorker<ResultadoVenda, Void>() {
                @Override
                protected ResultadoVenda doInBackground() {
                    Venda venda = vendaService.finalizarVenda(
                            clienteNome, formaPagamento, quantidades);
                    produtoService.notificarObservadores();
                    return new ResultadoVenda(venda, produtoService.listarTodos(), vendaService.listarTodas());
                }

                @Override
                protected void done() {
                    try {
                        ResultadoVenda resultado = get();
                        vendaView.setProdutos(resultado.produtos());
                        vendaView.setHistorico(resultado.vendas());
                        vendaView.limparVenda();
                        JOptionPane.showMessageDialog(mainFrame,
                                "Venda #" + resultado.venda().getId() + " finalizada.\nTotal: "
                                        + MOEDA.format(resultado.venda().getTotal()),
                                "Venda concluída", JOptionPane.INFORMATION_MESSAGE);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        mostrarErro("A finalização da venda foi interrompida.", ex);
                    } catch (ExecutionException ex) {
                        mostrarErro("Não foi possível finalizar a venda.", ex.getCause());
                    } finally {
                        vendaView.setBusy(false);
                    }
                }
            }.execute();
        } catch (RuntimeException ex) {
            vendaView.setBusy(false);
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Venda inválida",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarErro(String mensagem, Throwable causa) {
        String detalhe = causa == null ? "Erro desconhecido." : causa.getMessage();
        JOptionPane.showMessageDialog(mainFrame, mensagem + "\n" + detalhe,
                "Erro", JOptionPane.ERROR_MESSAGE);
    }

    private record DadosVenda(List<Produto> produtos, List<Venda> vendas) {
    }

    private record ResultadoVenda(Venda venda, List<Produto> produtos, List<Venda> vendas) {
    }
}
