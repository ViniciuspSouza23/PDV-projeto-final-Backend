package br.gov.sp.cps.fatec.pdv.controller;

import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.model.observer.ServicoObserver;
import br.gov.sp.cps.fatec.pdv.model.service.ServicoService;
import br.gov.sp.cps.fatec.pdv.view.MainFrame;
import br.gov.sp.cps.fatec.pdv.view.ServicoListView;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ServicoController implements ServicoObserver {

    private final MainFrame mainFrame;
    private final ServicoListView view;
    private final ServicoService service;

    public ServicoController(MainFrame mainFrame, ServicoListView view, ServicoService service) {
        this.mainFrame = Objects.requireNonNull(mainFrame, "MainFrame não pode ser nulo.");
        this.view = Objects.requireNonNull(view, "ServicoListView não pode ser nula.");
        this.service = Objects.requireNonNull(service, "ServicoService não pode ser nulo.");
        vincularEventos();
    }

    public void inicializar() {
        this.service.addObserver(this);
        this.service.notificarObservadores();
    }

    public void recarregar() {
        this.service.notificarObservadores();
    }

    private void vincularEventos() {
        view.getBtnNovo().addActionListener(e -> onNovoServico());
        view.getBtnEditar().addActionListener(e -> onEditarServico());
        view.getBtnExcluir().addActionListener(e -> onExcluirServico());
        view.getBtnRecarregar().addActionListener(e -> service.notificarObservadores());
    }

    private void onNovoServico() {
        JTextField txtNome = new JTextField();
        JTextField txtPreco = new JTextField();
        JCheckBox chkAjustaPorte = new JCheckBox("Ajustar preço conforme porte do animal (Strategy)", true);
        JTextField txtDescricao = new JTextField();

        JPanel painel = new JPanel(new GridLayout(4, 2, 8, 8));
        painel.add(new JLabel("Nome do Serviço (*):"));
        painel.add(txtNome);
        painel.add(new JLabel("Preço Base R$ (*):"));
        painel.add(txtPreco);
        painel.add(new JLabel("Regra de Porte:"));
        painel.add(chkAjustaPorte);
        painel.add(new JLabel("Descrição:"));
        painel.add(txtDescricao);

        int resultado = JOptionPane.showConfirmDialog(mainFrame, painel, "Cadastrar Novo Serviço",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                BigDecimal preco = new BigDecimal(txtPreco.getText().trim().replace(",", "."));
                Servico servico = new Servico(txtNome.getText().trim(), preco, chkAjustaPorte.isSelected(),
                        txtDescricao.getText().trim());
                service.salvar(servico);
                JOptionPane.showMessageDialog(mainFrame, "Serviço cadastrado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Preço inválido. Digite um número decimal (ex: 50.00).",
                        "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void onEditarServico() {
        Servico selecionado = view.getServicoSelecionado();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione um serviço na tabela para editar.", "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtNome = new JTextField(selecionado.getNome());
        JTextField txtPreco = new JTextField(selecionado.getPrecoBase().toPlainString());
        JCheckBox chkAjustaPorte = new JCheckBox("Ajustar preço conforme porte do animal (Strategy)",
                selecionado.isPermiteMultiplicadorPorte());
        JTextField txtDescricao = new JTextField(selecionado.getDescricao() != null ? selecionado.getDescricao() : "");

        JPanel painel = new JPanel(new GridLayout(4, 2, 8, 8));
        painel.add(new JLabel("Nome do Serviço (*):"));
        painel.add(txtNome);
        painel.add(new JLabel("Preço Base R$ (*):"));
        painel.add(txtPreco);
        painel.add(new JLabel("Regra de Porte:"));
        painel.add(chkAjustaPorte);
        painel.add(new JLabel("Descrição:"));
        painel.add(txtDescricao);

        int resultado = JOptionPane.showConfirmDialog(mainFrame, painel, "Editar Serviço #" + selecionado.getId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                BigDecimal preco = new BigDecimal(txtPreco.getText().trim().replace(",", "."));
                selecionado.setNome(txtNome.getText().trim());
                selecionado.setPrecoBase(preco);
                selecionado.setPermiteMultiplicadorPorte(chkAjustaPorte.isSelected());
                selecionado.setDescricao(txtDescricao.getText().trim());
                service.atualizar(selecionado);
                JOptionPane.showMessageDialog(mainFrame, "Serviço atualizado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(mainFrame, "Preço inválido. Digite um número decimal.",
                        "Validação", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Validação", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void onExcluirServico() {
        Servico selecionado = view.getServicoSelecionado();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione um serviço na tabela para excluir.", "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(mainFrame,
                "Deseja realmente excluir o serviço '" + selecionado.getNome() + "'?",
                "Confirmar Exclusão de Serviço", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                service.excluir(selecionado.getId());
                JOptionPane.showMessageDialog(mainFrame, "Serviço excluído com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Erro ao excluir serviço: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onServicosChanged(List<Servico> servicos) {
        view.setServicos(servicos);
    }
}
