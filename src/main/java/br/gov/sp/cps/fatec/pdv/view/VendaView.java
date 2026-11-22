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
    private final JButton btnRecarregarEstoque = new JButton("🔄 Recarregar");

    // Seção de Serviços
    private final JComboBox<Servico> cbServico = new JComboBox<>();
    private final JLabel lblPrecoServicoAjustado = new JLabel("Preço: R$ 0,00");
    private final JSpinner spQtdServico = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
    private final JButton btnAdicionarServico = new JButton("+ Adicionar Serviço");

    // Carrinho
    private final DefaultTableModel modeloCarrinho = new DefaultTableModel(
            new Object[]{"#", "Tipo", "Item / Descrição", "Pet Atendido", "Qtd", "Unitário", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabelaCarrinho = new JTable(modeloCarrinho);
    private final JLabel lblTotal = new JLabel("Total: R$ 0,00");
    private final JButton btnRemoverItem = new JButton("Remover Item");
    private final JButton btnLimparCarrinho = new JButton("🗑️ Limpar / Novo Atendimento");
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
        abas.addTab("Nova Venda / Atendimento", criarPainelNovaVenda());
        abas.addTab("Histórico de Vendas", criarPainelHistorico());
        add(abas, BorderLayout.CENTER);

        configurarRenderizadores();
        setPetsDoTutor(List.of());
        cbPet.setEnabled(false);
    }

    private JPanel criarPainelNovaVenda() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));

        // PAINEL ESQUERDO: ENTRADA DE DADOS E CATÁLOGO (PRODUTOS & SERVIÇOS)
        JPanel painelEsquerdo = new JPanel(new BorderLayout(10, 10));

        // 1. Card Identificação
        JPanel painelTopo = new JPanel(new GridLayout(2, 1, 8, 8));
        painelTopo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" 1. Identificação do Tutor, Pet e Pagamento "),
                BorderFactory.createEmptyBorder(6, 10, 8, 10)
        ));

        JPanel linha1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linha1.add(new JLabel("Tutor:"));
        cbTutor.setPreferredSize(new Dimension(210, 30));
        linha1.add(cbTutor);
        linha1.add(new JLabel("Pet:"));
        cbPet.setPreferredSize(new Dimension(180, 30));
        linha1.add(cbPet);
        linha1.add(new JLabel("Avulso:"));
        txtClienteAvulso.setPreferredSize(new Dimension(130, 30));
        linha1.add(txtClienteAvulso);
        painelTopo.add(linha1);

        JPanel linha2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        linha2.add(new JLabel("Pagamento:"));
        cbPagamento.setPreferredSize(new Dimension(190, 30));
        linha2.add(cbPagamento);
        painelTopo.add(linha2);

        painelEsquerdo.add(painelTopo, BorderLayout.NORTH);

        // 2. Abas de Inclusão (Produtos vs Serviços)
        JTabbedPane abasItens = new JTabbedPane();

        // --- ABA PRODUTOS ---
        JPanel tabProdutos = new JPanel(new BorderLayout(10, 10));
        tabProdutos.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel formProdutos = new JPanel();
        formProdutos.setLayout(new javax.swing.BoxLayout(formProdutos, javax.swing.BoxLayout.Y_AXIS));

        // Linha 1: Produto (Estoque)
        JPanel linhaProd1 = new JPanel(new BorderLayout(8, 0));
        linhaProd1.setOpaque(false);
        linhaProd1.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        JLabel lblProd = new JLabel("Produto (Estoque):");
        lblProd.setPreferredSize(new Dimension(130, 32));
        linhaProd1.add(lblProd, BorderLayout.WEST);
        cbProduto.setPreferredSize(new Dimension(280, 32));
        linhaProd1.add(cbProduto, BorderLayout.CENTER);
        formProdutos.add(linhaProd1);

        formProdutos.add(javax.swing.Box.createVerticalStrut(10));

        // Linha 2: Quantidade, Botão Adicionar e Botão Recarregar
        JPanel linhaProd2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        linhaProd2.setOpaque(false);
        linhaProd2.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        linhaProd2.add(new JLabel("Qtd:"));
        spQtdProduto.setPreferredSize(new Dimension(65, 32));
        linhaProd2.add(spQtdProduto);

        btnAdicionarProduto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdicionarProduto.setPreferredSize(new Dimension(175, 34));
        btnAdicionarProduto.setBackground(new Color(37, 99, 235));
        btnAdicionarProduto.setForeground(Color.WHITE);
        linhaProd2.add(btnAdicionarProduto);

        btnRecarregarEstoque.setPreferredSize(new Dimension(110, 34));
        btnRecarregarEstoque.setText("Recarregar");
        linhaProd2.add(btnRecarregarEstoque);
        formProdutos.add(linhaProd2);

        tabProdutos.add(formProdutos, BorderLayout.NORTH);

        // Card informativo abaixo na aba Produtos
        JPanel infoProd = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        infoProd.setBackground(new Color(248, 250, 252));
        infoProd.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true));
        JLabel lblInfoProd = new JLabel("Dica: O estoque e a disponibilidade são validados em tempo real no banco H2.");
        lblInfoProd.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfoProd.setForeground(new Color(100, 116, 139));
        infoProd.add(lblInfoProd);
        tabProdutos.add(infoProd, BorderLayout.CENTER);

        abasItens.addTab("Produtos (Rações, Remédios, Brinquedos)", tabProdutos);

        // --- ABA SERVIÇOS ---
        JPanel tabServicos = new JPanel(new BorderLayout(10, 10));
        tabServicos.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        JPanel formServicos = new JPanel();
        formServicos.setLayout(new javax.swing.BoxLayout(formServicos, javax.swing.BoxLayout.Y_AXIS));

        // Linha 1: Serviço (ComboBox com largura total e altura padrão de 32px)
        JPanel linhaServ1 = new JPanel(new BorderLayout(8, 0));
        linhaServ1.setOpaque(false);
        linhaServ1.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));
        JLabel lblServ = new JLabel("Serviço:");
        lblServ.setPreferredSize(new Dimension(65, 32));
        linhaServ1.add(lblServ, BorderLayout.WEST);
        cbServico.setPreferredSize(new Dimension(280, 32));
        linhaServ1.add(cbServico, BorderLayout.CENTER);
        formServicos.add(linhaServ1);

        formServicos.add(javax.swing.Box.createVerticalStrut(10));

        // Linha 2: Preço ajustado em destaque
        JPanel linhaServ2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        linhaServ2.setOpaque(false);
        linhaServ2.setMaximumSize(new Dimension(Short.MAX_VALUE, 34));

        // Badge estilizada de Preço Calculado
        JPanel badgePreco = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        badgePreco.setBackground(new Color(240, 253, 244));
        badgePreco.setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208), 1, true));
        lblPrecoServicoAjustado.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPrecoServicoAjustado.setForeground(new Color(22, 101, 52));
        badgePreco.add(lblPrecoServicoAjustado);
        linhaServ2.add(badgePreco);
        formServicos.add(linhaServ2);

        formServicos.add(javax.swing.Box.createVerticalStrut(8));

        // Linha 3: Quantidade e ação agrupadas e alinhadas à direita
        JPanel linhaServ3 = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        linhaServ3.setOpaque(false);
        linhaServ3.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));

        linhaServ3.add(new JLabel("Qtd:"));
        spQtdServico.setPreferredSize(new Dimension(60, 32));
        linhaServ3.add(spQtdServico);

        btnAdicionarServico.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdicionarServico.setPreferredSize(new Dimension(160, 34));
        btnAdicionarServico.setBackground(new Color(5, 150, 105));
        btnAdicionarServico.setForeground(Color.WHITE);
        linhaServ3.add(btnAdicionarServico);
        formServicos.add(linhaServ3);

        tabServicos.add(formServicos, BorderLayout.NORTH);

        // Card informativo abaixo na aba Serviços
        JPanel infoServ = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        infoServ.setBackground(new Color(240, 253, 244));
        infoServ.setBorder(BorderFactory.createLineBorder(new Color(187, 247, 208), 1, true));
        JLabel lblInfoServ = new JLabel("Dica: Preços de Banho e Tosa aplicam multiplicador por porte (Pequeno 1.0x, Médio 1.25x, Grande 1.5x, Gigante 1.8x).");
        lblInfoServ.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblInfoServ.setForeground(new Color(22, 101, 52));
        infoServ.add(lblInfoServ);
        tabServicos.add(infoServ, BorderLayout.CENTER);

        abasItens.addTab("Serviços (Banho, Tosa, Veterinário)", tabServicos);

        painelEsquerdo.add(abasItens, BorderLayout.CENTER);

        // PAINEL DIREITO: COMANDA VIRTUAL & CHECKOUT
        JPanel painelDireito = new JPanel(new BorderLayout(8, 8));
        painelDireito.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(" Comanda de Atendimento / Carrinho "),
                BorderFactory.createEmptyBorder(4, 8, 8, 8)
        ));

        // Tabela do Carrinho
        tabelaCarrinho.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaCarrinho.setRowHeight(30);
        tabelaCarrinho.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        if (tabelaCarrinho.getColumnModel().getColumnCount() >= 7) {
            tabelaCarrinho.getColumnModel().getColumn(0).setPreferredWidth(35);
            tabelaCarrinho.getColumnModel().getColumn(1).setPreferredWidth(70);
            tabelaCarrinho.getColumnModel().getColumn(2).setPreferredWidth(210);
            tabelaCarrinho.getColumnModel().getColumn(3).setPreferredWidth(120);
            tabelaCarrinho.getColumnModel().getColumn(4).setPreferredWidth(45);
            tabelaCarrinho.getColumnModel().getColumn(5).setPreferredWidth(90);
            tabelaCarrinho.getColumnModel().getColumn(6).setPreferredWidth(90);
        }
        painelDireito.add(new JScrollPane(tabelaCarrinho), BorderLayout.CENTER);

        // Rodapé do Carrinho com Totalizador em Destaque e Ações Limpas
        JPanel painelCheckout = new JPanel(new BorderLayout(8, 8));
        painelCheckout.setBackground(new Color(240, 253, 244));
        painelCheckout.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(187, 247, 208), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Linha 1: Total do Atendimento
        JPanel linhaTotal = new JPanel(new BorderLayout());
        linhaTotal.setOpaque(false);
        JLabel lblTextoTotal = new JLabel("TOTAL DO ATENDIMENTO:");
        lblTextoTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTextoTotal.setForeground(new Color(22, 101, 52));
        linhaTotal.add(lblTextoTotal, BorderLayout.WEST);

        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotal.setForeground(new Color(21, 128, 61));
        linhaTotal.add(lblTotal, BorderLayout.EAST);
        painelCheckout.add(linhaTotal, BorderLayout.NORTH);

        // Linha 2: 3 Botões de Ação Perfeitamente Distribuídos (sem sobreposição)
        JPanel linhaBotoes = new JPanel(new GridLayout(1, 3, 8, 0));
        linhaBotoes.setOpaque(false);

        btnLimparCarrinho.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnLimparCarrinho.setText("Limpar Carrinho");
        btnLimparCarrinho.setPreferredSize(new Dimension(0, 38));

        btnRemoverItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnRemoverItem.setText("Remover Item");
        btnRemoverItem.setForeground(new Color(220, 38, 38));
        btnRemoverItem.setPreferredSize(new Dimension(0, 38));

        btnFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnFinalizar.setText("Finalizar Venda");
        btnFinalizar.setBackground(new Color(22, 163, 74));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setPreferredSize(new Dimension(0, 38));

        linhaBotoes.add(btnLimparCarrinho);
        linhaBotoes.add(btnRemoverItem);
        linhaBotoes.add(btnFinalizar);
        painelCheckout.add(linhaBotoes, BorderLayout.SOUTH);

        painelDireito.add(painelCheckout, BorderLayout.SOUTH);

        // JSplitPane dividindo Esquerda e Direita de forma responsiva
        javax.swing.JSplitPane split = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, painelEsquerdo, painelDireito);
        split.setResizeWeight(0.48);
        split.setDividerLocation(520);
        split.setDividerSize(6);
        split.setBorder(null);

        painel.add(split, BorderLayout.CENTER);
        return painel;
    }

    private JPanel criarPainelHistorico() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        tabelaHistorico.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaHistorico.setRowHeight(30);
        tabelaHistorico.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(tabelaHistorico);
        scroll.setBorder(BorderFactory.createTitledBorder(" Vendas Concluídas no Caixa "));
        painel.add(scroll, BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        botoesHistorico(rodape);
        painel.add(rodape, BorderLayout.SOUTH);
        return painel;
    }

    private void botoesHistorico(JPanel rodape) {
        btnDetalhes.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDetalhes.setPreferredSize(new Dimension(170, 36));
        btnDetalhes.setBackground(new Color(37, 99, 235));
        btnDetalhes.setForeground(Color.WHITE);

        btnAtualizarHistorico.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnAtualizarHistorico.setPreferredSize(new Dimension(170, 36));

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
                        && java.util.Objects.equals(existente.getProdutoId(), item.getProdutoId())
                        && java.util.Objects.equals(existente.getPetId(), item.getPetId())) {
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
            String petTexto = (item.getPetNome() != null && !item.getPetNome().isBlank())
                    ? item.getPetNome()
                    : "Geral / Balcão";

            // Se o nome do item já contém a tag [Pet: ...], removemos para exibir limpo na coluna Descrição
            String descItem = item.getProdutoNome();
            if (descItem != null && descItem.contains(" [Pet: ")) {
                descItem = descItem.substring(0, descItem.indexOf(" [Pet: "));
            }

            modeloCarrinho.addRow(new Object[]{
                    idx++,
                    item.getTipoItem().getDescricao(),
                    descItem,
                    petTexto,
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
            String tutorNome = v.getClienteNome();
            if (tutorNome == null || tutorNome.isBlank()) {
                try {
                    tutorNome = v.getCliente() != null ? v.getCliente().getNome() : null;
                } catch (Exception ignored) {
                }
            }
            if (tutorNome == null || tutorNome.isBlank()) {
                tutorNome = "Consumidor não identificado";
            }

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
        btnRecarregarEstoque.setEnabled(!busy);
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

    public JButton getBtnRecarregarEstoque() {
        return btnRecarregarEstoque;
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
