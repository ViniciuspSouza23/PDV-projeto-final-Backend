package br.gov.sp.cps.fatec.pdv.view;

import br.gov.sp.cps.fatec.pdv.model.entity.Servico;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ServicoListView extends JPanel {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final DefaultTableModel modeloTabela = new DefaultTableModel(
            new Object[]{"ID", "Nome do Serviço", "Preço Base", "Ajusta por Porte?", "Descrição"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabela = new JTable(modeloTabela);

    private final JButton btnNovo = new JButton("+ Novo Serviço");
    private final JButton btnEditar = new JButton("Editar");
    private final JButton btnExcluir = new JButton("Excluir");
    private final JButton btnRecarregar = new JButton("Recarregar");

    private List<Servico> servicosExibidos = new ArrayList<>();

    public ServicoListView() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel cabecalho = new JPanel();
        cabecalho.setLayout(new BoxLayout(cabecalho, BoxLayout.Y_AXIS));

        JLabel titulo = new JLabel("Catálogo de Serviços de Pet Shop");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel subtitulo = new JLabel("Gerencie os serviços de banho, tosa, hidratação e consultas veterinárias.");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(Color.GRAY);

        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(4));
        cabecalho.add(subtitulo);
        add(cabecalho, BorderLayout.NORTH);

        tabela.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabela.setRowHeight(28);
        tabela.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        add(new JScrollPane(tabela), BorderLayout.CENTER);

        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnRecarregar.setPreferredSize(new Dimension(110, 35));
        btnExcluir.setPreferredSize(new Dimension(110, 35));
        btnExcluir.setForeground(new Color(180, 40, 40));
        btnEditar.setPreferredSize(new Dimension(110, 35));
        btnNovo.setPreferredSize(new Dimension(150, 35));
        btnNovo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        rodape.add(btnRecarregar);
        rodape.add(btnExcluir);
        rodape.add(btnEditar);
        rodape.add(btnNovo);
        add(rodape, BorderLayout.SOUTH);
    }

    public void setServicos(List<Servico> servicos) {
        this.servicosExibidos = (servicos != null) ? new ArrayList<>(servicos) : new ArrayList<>();
        modeloTabela.setRowCount(0);
        for (Servico s : servicosExibidos) {
            modeloTabela.addRow(new Object[]{
                    s.getId(),
                    s.getNome(),
                    MOEDA.format(s.getPrecoBase() != null ? s.getPrecoBase() : BigDecimal.ZERO),
                    s.isPermiteMultiplicadorPorte() ? "Sim (Strategy Porte)" : "Não (Preço Fixo)",
                    s.getDescricao() != null ? s.getDescricao() : "-"
            });
        }
    }

    public Servico getServicoSelecionado() {
        int linha = tabela.getSelectedRow();
        if (linha >= 0 && linha < servicosExibidos.size()) {
            return servicosExibidos.get(linha);
        }
        return null;
    }

    public JButton getBtnNovo() {
        return btnNovo;
    }

    public JButton getBtnEditar() {
        return btnEditar;
    }

    public JButton getBtnExcluir() {
        return btnExcluir;
    }

    public JButton getBtnRecarregar() {
        return btnRecarregar;
    }
}
