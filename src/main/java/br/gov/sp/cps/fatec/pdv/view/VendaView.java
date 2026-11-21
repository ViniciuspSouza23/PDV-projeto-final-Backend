package br.gov.sp.cps.fatec.pdv.view;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.model.entity.TipoItemVenda;
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
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VendaView extends JPanel {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Seletores do Cabeçalho da Venda
    private final JComboBox<Cliente> cbTutor = new JComboBox<>();
    private final JComboBox<Pet> cbPet = new JComboBox<>();
    private final JTextField txtClienteAvulso = new JTextField(14);
    private final JComboBox<FormaPagamento> cbPagamento = new JComboBox<>(FormaPagamento.values());

    // Seção de Produtos
    private final JComboBox<Produto> cbProduto = new JComboBox<>();
    private final JSpinner spQtdProduto = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
    private final JButton btnAdicionarProduto = new JButton("+ Adicionar Produto");

    // Seção de Serviços
    private final JComboBox<Servico> cbServico = new JComboBox<>();
    private final JLabel lblPrecoServicoAjustado = new JLabel("Preço: R$ 0,00");
    private final JSpinner spQtdServico = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JButton btnAdicionarServico = new JButton("+ Adicionar Serviço");

    // Carrinho
    private final DefaultTableModel modeloCarrinho = new DefaultTableModel(
            new Object[]{"#", "Tipo", "Item / Descrição", "Qtd", "Unitário", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabelaCarrinho = new JTable(modeloCarrinho);
    private final JLabel lblTotal = new JLabel("Total: R$ 0,00");
    private final JButton btnRemoverItem = new JButton("Remover Item");
    private final JButton btnLimparCarrinho = new JButton("Limpar");
    private final JButton btnFinalizar = new JButton("Finalizar Venda");

    // Histórico
    private final DefaultTableModel modeloHistorico = new DefaultTableModel(
            new Object[]{"Venda #", "Data e Hora", "Tutor / Cliente", "Forma Pagamento", "Total"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabelaHistorico = new JTable(modeloHistorico);
    private final JButton btnAtualizarHistorico = new JButton("Atualizar Histórico");
    private final JButton btnDetalhes = new JButton("Ver Itens da Venda");

    // Estado interno do carrinho
    private final List<ItemVenda> itensCarrinho = new ArrayList<>();
    private List<Venda> vendasExibidas = List.of();

    public VendaView() {
        setLayout(new BorderLayout(12, 12));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titulo = new JLabel("Caixa PDV - Pet Shop & Serviços");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        add(titulo, BorderLayout.NORTH);

        JTabbedPane abas = new JTabbedPane();
        abas.addTab("🛒 Nova Venda / Atendimento", criarPainelNovaVenda());
        abas.addTab("📋 Histórico de Vendas", criarPainelHistorico());
        add(abas, BorderLayout.CENTER);

        configurarRenderizadores();
        setPetsDoTutor(List.of());
        cbPet.setEnabled(false);
    }

    private JPanel criarPainelNovaVenda() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));

        // 1. Dados do Tutor e Pagamento
        JPanel painelTopo = new JPanel(new GridLayout(2, 1, 6, 6));
        painelTopo.setBorder(BorderFactory.createTitledBorder(" 1. Identificação do Tutor, Pet e Pagamento "));

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linha1.add(new JLabel("Tutor Cadastrado:"));
        cbTutor.setPreferredSize(new Dimension(220, 28));
        linha1.add(cbTutor);
        linha1.add(new JLabel("Pet Atendido:"));
        cbPet.setPreferredSize(new Dimension(180, 28));
        linha1.add(cbPet);
        linha1.add(new JLabel("Ou Nome Avulso:"));
        linha1.add(txtClienteAvulso);
        painelTopo.add(linha1);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linha2.add(new JLabel("Forma de Pagamento:"));
        cbPagamento.setPreferredSize(new Dimension(180, 28));
        linha2.add(cbPagamento);
        painelTopo.add(linha2);

        // 2. Abas de Inclusão (Produtos vs Serviços)
        JTabbedPane abasItens = new JTabbedPane();

        JPanel painelAddProduto = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        painelAddProduto.add(new JLabel("Produto (Estoque):"));
        cbProduto.setPreferredSize(new Dimension(320, 28));
        painelAddProduto.add(cbProduto);
        painelAddProduto.add(new JLabel("Qtd:"));
        painelAddProduto.add(spQtdProduto);
        btnAdicionarProduto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        painelAddProduto.add(btnAdicionarProduto);
        abasItens.addTab("📦 Adicionar Produto (Rações, Remédios, Brinquedos)", painelAddProduto);

        JPanel painelAddServico = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        painelAddServico.add(new JLabel("Serviço:"));
        cbServico.setPreferredSize(new Dimension(260, 28));
        painelAddServico.add(cbServico);
        lblPrecoServicoAjustado.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPrecoServicoAjustado.setForeground(new Color(20, 110, 40));
        painelAddServico.add(lblPrecoServicoAjustado);
        painelAddServico.add(new JLabel("Qtd:"));
        painelAddServico.add(spQtdServico);
        btnAdicionarServico.setFont(new Font("Segoe UI", Font.BOLD, 12));
        painelAddServico.add(btnAdicionarServico);
        abasItens.addTab("✂️ Adicionar Serviço (Banho, Tosa, Veterinário)", painelAddServico);

        JPanel painelEntradas = new JPanel(new BorderLayout(5, 5));
        painelEntradas.add(painelTopo, BorderLayout.NORTH);
        painelEntradas.add(abasItens, BorderLayout.CENTER);
        painel.add(painelEntradas, BorderLayout.NORTH);

        // 3. Tabela do Carrinho
        tabelaCarrinho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCarrinho.setRowHeight(26);
        tabelaCarrinho.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        painel.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        // 4. Rodapé do Carrinho
        JPanel rodape = new JPanel(new BorderLayout());
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal.setForeground(new Color(30, 90, 160));
        rodape.add(lblTotal, BorderLayout.WEST);

        JPanel botoesRodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        botoesRodape.add(btnLimparCarrinho);
        btnRemoverItem.setForeground(new Color(180, 40, 40));
        botoesRodape.add(btnRemoverItem);
        btnFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFinalizar.setPreferredSize(new Dimension(160, 36));
        botoesRodape.add(btnFinalizar);
        rodape.add(botoesRodape, BorderLayout.EAST);
        painel.add(rodape, BorderLayout.SOUTH);

        return painel;
    }

    private JPanel criarPainelHistorico() {
        JPanel painel = new JPanel(new BorderLayout(8, 8));
        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaHistorico.setRowHeight(26);
        tabelaHistorico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        painel.add(new JScrollPane(tabelaHistorico), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        botoesHistorico(rodape);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private void botoesHistorico(JPanel rodape) {
        btnDetalhes.setPreferredSize(new Dimension(150, 32));
        btnAtualizarHistorico.setPreferredSize(new Dimension(150, 32));
        rodape.add(btnDetalhes);
        rodape.add(btnAtualizarHistorico);
    }

    private void configurarRenderizadores() {
        cbTutor.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("[Consumidor Balcão / Avulso]");
                } else if (value instanceof Cliente cliente) {
                    setText(cliente.getNome() + " (" + cliente.getCpf() + ")");
                }
                return this;
            }
        });

        cbPet.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    setText("[Sem Pet selecionado]");
                } else if (value instanceof Pet pet) {
                    setText(pet.getNome() + " (" + pet.getEspecie().getDescricao() + " - " + pet.getPorte().getDescricao() + ")");
                }
                return this;
            }
        });

        cbProduto.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Produto p) {
                    if (p.getQuantidadeEstoque() <= 0) {
                        setText(p.getNome() + " [ESGOTADO / SEM ESTOQUE] | " + MOEDA.format(p.getPreco()));
                        if (!isSelected) {
                            setForeground(new Color(190, 40, 40));
                        }
                    } else {
                        setText(p.getNome() + " | Estq: " + p.getQuantidadeEstoque() + " | " + MOEDA.format(p.getPreco()));
                    }
                }
                return this;
            }
        });

        cbServico.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Servico s) {
                    String ajuste = s.isPermiteMultiplicadorPorte() ? " (ajusta p/ porte)" : "";
                    setText(s.getNome() + " | Base: " + MOEDA.format(s.getPrecoBase()) + ajuste);
                }
                return this;
            }
        });
    }

    public void setProdutos(List<Produto> produtos) {
        DefaultComboBoxModel<Produto> modelo = new DefaultComboBoxModel<>();
        if (produtos != null) {
            for (Produto p : produtos) modelo.addElement(p);
        }
        cbProduto.setModel(modelo);
        if (modelo.getSize() > 0) {
            cbProduto.setSelectedIndex(0);
        }
    }

    public void setServicos(List<Servico> servicos) {
        DefaultComboBoxModel<Servico> modelo = new DefaultComboBoxModel<>();
        if (servicos != null) {
            for (Servico s : servicos) modelo.addElement(s);
        }
        cbServico.setModel(modelo);
        if (modelo.getSize() > 0) {
            cbServico.setSelectedIndex(0);
        }
    }

    public void setTutores(List<Cliente> tutores) {
        DefaultComboBoxModel<Cliente> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(null); // Consumidor não identificado
        if (tutores != null) {
            for (Cliente c : tutores) modelo.addElement(c);
        }
        cbTutor.setModel(modelo);
        cbTutor.setSelectedIndex(0);
    }

    public void setPetsDoTutor(List<Pet> pets) {
        DefaultComboBoxModel<Pet> modelo = new DefaultComboBoxModel<>();
        modelo.addElement(null);
        if (pets != null) {
            for (Pet p : pets) modelo.addElement(p);
        }
        cbPet.setModel(modelo);
        if (pets != null && !pets.isEmpty()) {
            cbPet.setSelectedIndex(1);
        } else {
            cbPet.setSelectedIndex(0);
        }
    }

    public void adicionarItemNoCarrinho(ItemVenda item) {
        if (item.getTipoItem() == TipoItemVenda.PRODUTO) {
            for (ItemVenda existente : itensCarrinho) {
                if (existente.getTipoItem() == TipoItemVenda.PRODUTO
                        && existente.getProdutoId() != null
                        && existente.getProdutoId().equals(item.getProdutoId())) {
                    existente.setQuantidade(existente.getQuantidade() + item.getQuantidade());
                    existente.setSubtotal(existente.getPrecoUnitario().multiply(BigDecimal.valueOf(existente.getQuantidade())));
                    atualizarTabelaCarrinho();
                    return;
                }
            }
        }
        itensCarrinho.add(item);
        atualizarTabelaCarrinho();
    }

    public void removerItemSelecionado() {
        int linha = tabelaCarrinho.getSelectedRow();
        if (linha >= 0 && linha < itensCarrinho.size()) {
            itensCarrinho.remove(linha);
            atualizarTabelaCarrinho();
        }
    }

    public void limparCarrinho() {
        itensCarrinho.clear();
        atualizarTabelaCarrinho();
    }

    private void atualizarTabelaCarrinho() {
        modeloCarrinho.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        int idx = 1;
        for (ItemVenda item : itensCarrinho) {
            total = total.add(item.getSubtotal());
            modeloCarrinho.addRow(new Object[]{
                    idx++,
                    item.getTipoItem().getDescricao(),
                    item.getProdutoNome(),
                    item.getQuantidade(),
                    MOEDA.format(item.getPrecoUnitario()),
                    MOEDA.format(item.getSubtotal())
            });
        }
        lblTotal.setText("Total: " + MOEDA.format(total));
    }

    public void setHistorico(List<Venda> vendas) {
        vendasExibidas = (vendas != null) ? List.copyOf(vendas) : List.of();
        modeloHistorico.setRowCount(0);
        for (Venda v : vendasExibidas) {
            String tutorNome = v.getCliente() != null ? v.getCliente().getNome() : v.getClienteNome();
            if (tutorNome == null || tutorNome.isBlank()) tutorNome = "Consumidor não identificado";

            modeloHistorico.addRow(new Object[]{
                    v.getId(),
                    v.getDataHora().format(DATA_HORA),
                    tutorNome,
                    v.getFormaPagamento(),
                    MOEDA.format(v.getTotal())
            });
        }
    }

    public Venda getVendaSelecionada() {
        int linha = tabelaHistorico.getSelectedRow();
        return (linha >= 0 && linha < vendasExibidas.size()) ? vendasExibidas.get(linha) : null;
    }

    public void setBusy(boolean busy) {
        cbTutor.setEnabled(!busy);
        cbPet.setEnabled(!busy);
        txtClienteAvulso.setEnabled(!busy);
        cbPagamento.setEnabled(!busy);
        cbProduto.setEnabled(!busy);
        spQtdProduto.setEnabled(!busy);
        btnAdicionarProduto.setEnabled(!busy);
        cbServico.setEnabled(!busy);
        spQtdServico.setEnabled(!busy);
        btnAdicionarServico.setEnabled(!busy);
        btnRemoverItem.setEnabled(!busy);
        btnLimparCarrinho.setEnabled(!busy);
        btnFinalizar.setEnabled(!busy);
        btnAtualizarHistorico.setEnabled(!busy);
        btnDetalhes.setEnabled(!busy);
    }

    // Getters para controladores
    public JComboBox<Cliente> getCbTutor() {
        return cbTutor;
    }

    public JComboBox<Pet> getCbPet() {
        return cbPet;
    }

    public JComboBox<Produto> getCbProduto() {
        return cbProduto;
    }

    public JComboBox<Servico> getCbServico() {
        return cbServico;
    }

    public Cliente getTutorSelecionado() {
        return (Cliente) cbTutor.getSelectedItem();
    }

    public Pet getPetSelecionado() {
        return (Pet) cbPet.getSelectedItem();
    }

    public JTextField getTxtClienteAvulso() {
        return txtClienteAvulso;
    }

    public String getClienteNomeAvulso() {
        return txtClienteAvulso.getText().trim();
    }

    public FormaPagamento getFormaPagamento() {
        return (FormaPagamento) cbPagamento.getSelectedItem();
    }

    public Produto getProdutoSelecionado() {
        return (Produto) cbProduto.getSelectedItem();
    }

    public int getQuantidadeProduto() {
        return (Integer) spQtdProduto.getValue();
    }

    public Servico getServicoSelecionado() {
        return (Servico) cbServico.getSelectedItem();
    }

    public int getQuantidadeServico() {
        return (Integer) spQtdServico.getValue();
    }

    public List<ItemVenda> getItensCarrinho() {
        return new ArrayList<>(itensCarrinho);
    }

    public JLabel getLblPrecoServicoAjustado() {
        return lblPrecoServicoAjustado;
    }

    public JButton getBtnAdicionarProduto() {
        return btnAdicionarProduto;
    }

    public JButton getBtnAdicionarServico() {
        return btnAdicionarServico;
    }

    public JButton getBtnRemoverItem() {
        return btnRemoverItem;
    }

    public JButton getBtnLimparCarrinho() {
        return btnLimparCarrinho;
    }

    public JButton getBtnFinalizar() {
        return btnFinalizar;
    }

    public JButton getBtnAtualizarHistorico() {
        return btnAtualizarHistorico;
    }

    public JButton getBtnDetalhes() {
        return btnDetalhes;
    }

    // Compatibilidade com código anterior
    public Map<Long, Integer> getQuantidadesPorProduto() {
        Map<Long, Integer> map = new LinkedHashMap<>();
        for (ItemVenda i : itensCarrinho) {
            if (i.getTipoItem() == TipoItemVenda.PRODUTO) {
                map.put(i.getProdutoId(), map.getOrDefault(i.getProdutoId(), 0) + i.getQuantidade());
            }
        }
        return map;
    }

    public String getClienteNome() {
        Cliente tutor = getTutorSelecionado();
        if (tutor != null) return tutor.getNome();
        return getClienteNomeAvulso();
    }
}
