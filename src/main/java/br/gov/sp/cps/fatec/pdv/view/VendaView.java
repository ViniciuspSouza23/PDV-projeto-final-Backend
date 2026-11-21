package br.gov.sp.cps.fatec.pdv.view;

import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VendaView extends JPanel {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JTextField txtCliente = new JTextField(16);
    private final JComboBox<FormaPagamento> cbPagamento = new JComboBox<>(FormaPagamento.values());
    private final JComboBox<Produto> cbProduto = new JComboBox<>();
    private final JSpinner spQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
    private final DefaultTableModel modeloCarrinho = new DefaultTableModel(
            new Object[] { "ID", "Produto", "Quantidade", "Unitário", "Subtotal" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final DefaultTableModel modeloHistorico = new DefaultTableModel(
            new Object[] { "Venda", "Data e hora", "Cliente", "Pagamento", "Total" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabelaCarrinho = new JTable(modeloCarrinho);
    private final JTable tabelaHistorico = new JTable(modeloHistorico);
    private final JLabel lblTotal = new JLabel("Total: R$ 0,00");
    private final JButton btnAdicionar = new JButton("Adicionar produto");
    private final JButton btnRemover = new JButton("Remover item");
    private final JButton btnFinalizar = new JButton("Finalizar venda");
    private final JButton btnVoltar = new JButton("Voltar aos produtos");
    private final JButton btnAtualizarHistorico = new JButton("Atualizar histórico");
    private final JButton btnDetalhes = new JButton("Ver itens da venda");
    private final Map<Long, LinhaCarrinho> carrinho = new LinkedHashMap<>();
    private List<Venda> vendasExibidas = List.of();

    public VendaView() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel titulo = new JLabel("Ponto de Venda");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("Nova venda", criarPainelVenda());
        abas.addTab("Histórico", criarPainelHistorico());
        add(abas, BorderLayout.CENTER);

        cbProduto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Produto produto) {
                    setText(produto.getNome() + " | estoque: " + produto.getQuantidadeEstoque()
                            + " | " + MOEDA.format(produto.getPreco()));
                }
                return this;
            }
        });
    }

    private JPanel criarPainelVenda() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));

        JPanel dados = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        dados.setBorder(BorderFactory.createTitledBorder("Dados da venda"));
        dados.add(new JLabel("Cliente (opcional):"));
        dados.add(txtCliente);
        dados.add(new JLabel("Pagamento:"));
        dados.add(cbPagamento);
        dados.add(cbProduto);
        dados.add(spQuantidade);
        dados.add(btnAdicionar);
        painel.add(dados, BorderLayout.NORTH);

        tabelaCarrinho.setFillsViewportHeight(true);
        painel.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new BorderLayout());
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        botoes.add(btnRemover);
        botoes.add(btnVoltar);
        botoes.add(btnFinalizar);
        rodape.add(lblTotal, BorderLayout.WEST);
        rodape.add(botoes, BorderLayout.EAST);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private JPanel criarPainelHistorico() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        tabelaHistorico.setFillsViewportHeight(true);
        painel.add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rodape.add(btnDetalhes);
        rodape.add(btnAtualizarHistorico);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    public void setProdutos(List<Produto> produtos) {
        DefaultComboBoxModel<Produto> modelo = new DefaultComboBoxModel<>();
        for (Produto produto : produtos) {
            modelo.addElement(produto);
        }
        cbProduto.setModel(modelo);
    }

    public void adicionarProdutoSelecionado() {
        Produto produto = (Produto) cbProduto.getSelectedItem();
        if (produto == null) {
            throw new IllegalArgumentException("Não há produtos disponíveis para venda.");
        }

        int quantidade = (Integer) spQuantidade.getValue();
        LinhaCarrinho atual = carrinho.get(produto.getId());
        int quantidadeAtual = atual == null ? 0 : atual.quantidade();
        if (quantidadeAtual + quantidade > produto.getQuantidadeEstoque()) {
            throw new IllegalArgumentException("A quantidade informada excede o estoque disponível.");
        }

        carrinho.put(produto.getId(), new LinhaCarrinho(produto, quantidadeAtual + quantidade));
        atualizarCarrinho();
        spQuantidade.setValue(1);
    }

    public void removerItemSelecionado() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha >= 0) {
            Long produtoId = (Long) modeloCarrinho.getValueAt(linha, 0);
            carrinho.remove(produtoId);
            atualizarCarrinho();
        }
    }

    private void atualizarCarrinho() {
        modeloCarrinho.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, LinhaCarrinho> entrada : carrinho.entrySet()) {
            Produto produto = entrada.getValue().produto();
            int quantidade = entrada.getValue().quantidade();
            BigDecimal subtotal = produto.getPreco().multiply(BigDecimal.valueOf(quantidade));
            total = total.add(subtotal);
            modeloCarrinho.addRow(new Object[] {
                    entrada.getKey(),
                    produto.getNome(),
                    quantidade,
                    MOEDA.format(produto.getPreco()),
                    MOEDA.format(subtotal)
            });
        }
        lblTotal.setText("Total: " + MOEDA.format(total));
    }

    public Map<Long, Integer> getQuantidadesPorProduto() {
        Map<Long, Integer> itens = new LinkedHashMap<>();
        for (Map.Entry<Long, LinhaCarrinho> entrada : carrinho.entrySet()) {
            itens.put(entrada.getKey(), entrada.getValue().quantidade());
        }
        return itens;
    }

    public String getClienteNome() {
        return txtCliente.getText().trim();
    }

    public FormaPagamento getFormaPagamento() {
        return (FormaPagamento) cbPagamento.getSelectedItem();
    }

    public void limparVenda() {
        carrinho.clear();
        atualizarCarrinho();
        txtCliente.setText("");
        cbPagamento.setSelectedIndex(0);
    }

    public void setHistorico(List<Venda> vendas) {
        vendasExibidas = List.copyOf(vendas);
        modeloHistorico.setRowCount(0);
        for (Venda venda : vendas) {
            modeloHistorico.addRow(new Object[] {
                    venda.getId(),
                    venda.getDataHora().format(DATA_HORA),
                    venda.getClienteNome() == null ? "Consumidor não identificado" : venda.getClienteNome(),
                    venda.getFormaPagamento(),
                    MOEDA.format(venda.getTotal())
            });
        }
    }

    public Venda getVendaSelecionada() {
        int linha = tabelaHistorico.getSelectedRow();
        return linha >= 0 && linha < vendasExibidas.size() ? vendasExibidas.get(linha) : null;
    }

    public void setBusy(boolean busy) {
        txtCliente.setEnabled(!busy);
        cbPagamento.setEnabled(!busy);
        cbProduto.setEnabled(!busy);
        spQuantidade.setEnabled(!busy);
        btnAdicionar.setEnabled(!busy);
        btnRemover.setEnabled(!busy);
        btnFinalizar.setEnabled(!busy);
        btnVoltar.setEnabled(!busy);
        btnAtualizarHistorico.setEnabled(!busy);
        btnDetalhes.setEnabled(!busy);
    }

    public JButton getBtnAdicionar() {
        return btnAdicionar;
    }

    public JButton getBtnRemover() {
        return btnRemover;
    }

    public JButton getBtnFinalizar() {
        return btnFinalizar;
    }

    public JButton getBtnVoltar() {
        return btnVoltar;
    }

    public JButton getBtnAtualizarHistorico() {
        return btnAtualizarHistorico;
    }

    public JButton getBtnDetalhes() {
        return btnDetalhes;
    }

    private record LinhaCarrinho(Produto produto, int quantidade) {
    }
}
