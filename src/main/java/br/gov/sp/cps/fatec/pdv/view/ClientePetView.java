package br.gov.sp.cps.fatec.pdv.view;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.Especie;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Porte;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

public class ClientePetView extends JPanel {

    private final DefaultTableModel modeloClientes = new DefaultTableModel(
            new Object[]{"ID", "Nome do Tutor", "CPF", "Telefone", "E-mail", "Qtd. Pets"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabelaClientes = new JTable(modeloClientes);

    private final DefaultTableModel modeloPets = new DefaultTableModel(
            new Object[]{"ID", "Nome do Pet", "Espécie", "Raça", "Porte", "Observações"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable tabelaPets = new JTable(modeloPets);

    private final JButton btnNovoCliente = new JButton("+ Novo Tutor");
    private final JButton btnEditarCliente = new JButton("Editar Tutor");
    private final JButton btnExcluirCliente = new JButton("Excluir Tutor");
    private final JButton btnAdicionarPet = new JButton("+ Adicionar Pet");
    private final JButton btnExcluirPet = new JButton("Remover Pet");
    private final JButton btnRecarregar = new JButton("Recarregar");

    private List<Cliente> clientesExibidos = new ArrayList<>();

    public ClientePetView() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Cabeçalho
        JPanel painelCabecalho = new JPanel();
        painelCabecalho.setLayout(new BoxLayout(painelCabecalho, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel("Gestão de Tutores & Pets");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel lblSubtitulo = new JLabel("Cadastre clientes (tutores) e vincule seus animais de estimação para atendimento.");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(Color.GRAY);

        painelCabecalho.add(lblTitulo);
        painelCabecalho.add(Box.createVerticalStrut(4));
        painelCabecalho.add(lblSubtitulo);
        add(painelCabecalho, BorderLayout.NORTH);

        // Painel Superior: Tutores
        JPanel painelTutores = new JPanel(new BorderLayout(8, 8));
        painelTutores.setBorder(BorderFactory.createTitledBorder(" 1. Tutores Cadastrados (Selecione um tutor para ver os pets) "));
        tabelaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaClientes.setRowHeight(26);
        tabelaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        painelTutores.add(new JScrollPane(tabelaClientes), BorderLayout.CENTER);

        JPanel botoesTutores = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        botoesTutores.add(btnRecarregar);
        btnExcluirCliente.setForeground(new Color(180, 40, 40));
        botoesTutores.add(btnExcluirCliente);
        botoesTutores.add(btnEditarCliente);
        btnNovoCliente.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botoesTutores.add(btnNovoCliente);
        painelTutores.add(botoesTutores, BorderLayout.SOUTH);

        // Painel Inferior: Pets do Tutor Selecionado
        JPanel painelPets = new JPanel(new BorderLayout(8, 8));
        painelPets.setBorder(BorderFactory.createTitledBorder(" 2. Pets do Tutor Selecionado "));
        tabelaPets.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabelaPets.setRowHeight(26);
        tabelaPets.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        painelPets.add(new JScrollPane(tabelaPets), BorderLayout.CENTER);

        JPanel botoesPets = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        btnExcluirPet.setForeground(new Color(180, 40, 40));
        botoesPets.add(btnExcluirPet);
        btnAdicionarPet.setFont(new Font("Segoe UI", Font.BOLD, 12));
        botoesPets.add(btnAdicionarPet);
        painelPets.add(botoesPets, BorderLayout.SOUTH);

        // Divisão Vertical com JSplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, painelTutores, painelPets);
        splitPane.setResizeWeight(0.55);
        splitPane.setContinuousLayout(true);
        add(splitPane, BorderLayout.CENTER);

        // Ao selecionar tutor, atualiza pets
        tabelaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                atualizarTabelaPetsDoTutorSelecionado();
            }
        });
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientesExibidos = (clientes != null) ? new ArrayList<>(clientes) : new ArrayList<>();
        modeloClientes.setRowCount(0);
        for (Cliente c : clientesExibidos) {
            modeloClientes.addRow(new Object[]{
                    c.getId(),
                    c.getNome(),
                    c.getCpf(),
                    c.getTelefone(),
                    c.getEmail() != null ? c.getEmail() : "-",
                    c.getPets() != null ? c.getPets().size() : 0
            });
        }
        if (!clientesExibidos.isEmpty()) {
            tabelaClientes.setRowSelectionInterval(0, 0);
        }
        atualizarTabelaPetsDoTutorSelecionado();
    }

    public Cliente getClienteSelecionado() {
        int linha = tabelaClientes.getSelectedRow();
        if (linha >= 0 && linha < clientesExibidos.size()) {
            return clientesExibidos.get(linha);
        }
        return null;
    }

    public Pet getPetSelecionado() {
        Cliente tutor = getClienteSelecionado();
        if (tutor == null) return null;
        int linhaPet = tabelaPets.getSelectedRow();
        if (linhaPet >= 0 && linhaPet < tutor.getPets().size()) {
            return tutor.getPets().get(linhaPet);
        }
        return null;
    }

    private void atualizarTabelaPetsDoTutorSelecionado() {
        modeloPets.setRowCount(0);
        Cliente tutor = getClienteSelecionado();
        if (tutor != null && tutor.getPets() != null) {
            for (Pet p : tutor.getPets()) {
                modeloPets.addRow(new Object[]{
                        p.getId(),
                        p.getNome(),
                        p.getEspecie().getDescricao(),
                        p.getRaca(),
                        p.getPorte().getDescricao(),
                        p.getObservacoes() != null ? p.getObservacoes() : "-"
                });
            }
        }
    }

    public JButton getBtnNovoCliente() {
        return btnNovoCliente;
    }

    public JButton getBtnEditarCliente() {
        return btnEditarCliente;
    }

    public JButton getBtnExcluirCliente() {
        return btnExcluirCliente;
    }

    public JButton getBtnAdicionarPet() {
        return btnAdicionarPet;
    }

    public JButton getBtnExcluirPet() {
        return btnExcluirPet;
    }

    public JButton getBtnRecarregar() {
        return btnRecarregar;
    }
}
