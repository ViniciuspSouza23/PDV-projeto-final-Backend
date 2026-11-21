package br.gov.sp.cps.fatec.pdv.controller;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.Especie;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Porte;
import br.gov.sp.cps.fatec.pdv.model.observer.ClienteObserver;
import br.gov.sp.cps.fatec.pdv.model.service.ClienteService;
import br.gov.sp.cps.fatec.pdv.view.ClientePetView;
import br.gov.sp.cps.fatec.pdv.view.MainFrame;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.util.List;
import java.util.Objects;

public class ClienteController implements ClienteObserver {

    private final MainFrame mainFrame;
    private final ClientePetView view;
    private final ClienteService service;

    public ClienteController(MainFrame mainFrame, ClientePetView view, ClienteService service) {
        this.mainFrame = Objects.requireNonNull(mainFrame, "MainFrame não pode ser nulo.");
        this.view = Objects.requireNonNull(view, "ClientePetView não pode ser nula.");
        this.service = Objects.requireNonNull(service, "ClienteService não pode ser nulo.");
        vincularEventos();
    }

    public void inicializar() {
        this.service.addObserver(this);
        this.service.notificarObservadores();
    }

    private void vincularEventos() {
        view.getBtnNovoCliente().addActionListener(e -> onNovoCliente());
        view.getBtnEditarCliente().addActionListener(e -> onEditarCliente());
        view.getBtnExcluirCliente().addActionListener(e -> onExcluirCliente());
        view.getBtnAdicionarPet().addActionListener(e -> onAdicionarPet());
        view.getBtnExcluirPet().addActionListener(e -> onExcluirPet());
        view.getBtnRecarregar().addActionListener(e -> service.notificarObservadores());
    }

    private void onNovoCliente() {
        JTextField txtNome = new JTextField();
        JTextField txtCpf = new JTextField();
        JTextField txtTelefone = new JTextField();
        JTextField txtEmail = new JTextField();

        JPanel painel = new JPanel(new GridLayout(4, 2, 8, 8));
        painel.add(new JLabel("Nome Completo (*):"));
        painel.add(txtNome);
        painel.add(new JLabel("CPF (*):"));
        painel.add(txtCpf);
        painel.add(new JLabel("Telefone/WhatsApp (*):"));
        painel.add(txtTelefone);
        painel.add(new JLabel("E-mail:"));
        painel.add(txtEmail);

        int resultado = JOptionPane.showConfirmDialog(mainFrame, painel, "Cadastrar Novo Tutor",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                Cliente novo = new Cliente(txtNome.getText().trim(), txtCpf.getText().trim(),
                        txtTelefone.getText().trim(), txtEmail.getText().trim());
                service.salvar(novo);
                JOptionPane.showMessageDialog(mainFrame, "Tutor cadastrado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Validação de Tutor",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void onEditarCliente() {
        Cliente selecionado = view.getClienteSelecionado();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione um tutor na tabela para editar.", "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtNome = new JTextField(selecionado.getNome());
        JTextField txtCpf = new JTextField(selecionado.getCpf());
        JTextField txtTelefone = new JTextField(selecionado.getTelefone());
        JTextField txtEmail = new JTextField(selecionado.getEmail() != null ? selecionado.getEmail() : "");

        JPanel painel = new JPanel(new GridLayout(4, 2, 8, 8));
        painel.add(new JLabel("Nome Completo (*):"));
        painel.add(txtNome);
        painel.add(new JLabel("CPF (*):"));
        painel.add(txtCpf);
        painel.add(new JLabel("Telefone/WhatsApp (*):"));
        painel.add(txtTelefone);
        painel.add(new JLabel("E-mail:"));
        painel.add(txtEmail);

        int resultado = JOptionPane.showConfirmDialog(mainFrame, painel, "Editar Tutor #" + selecionado.getId(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                selecionado.setNome(txtNome.getText().trim());
                selecionado.setCpf(txtCpf.getText().trim());
                selecionado.setTelefone(txtTelefone.getText().trim());
                selecionado.setEmail(txtEmail.getText().trim());
                service.atualizar(selecionado);
                JOptionPane.showMessageDialog(mainFrame, "Tutor atualizado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Erro ao Atualizar",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void onExcluirCliente() {
        Cliente selecionado = view.getClienteSelecionado();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione um tutor na tabela para excluir.", "Atenção",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(mainFrame,
                "Deseja realmente excluir o tutor '" + selecionado.getNome()
                        + "' e todos os seus pets vinculados?",
                "Confirmar Exclusão de Tutor", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                service.excluir(selecionado.getId());
                JOptionPane.showMessageDialog(mainFrame, "Tutor removido com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Não foi possível excluir o tutor: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void onAdicionarPet() {
        Cliente tutor = view.getClienteSelecionado();
        if (tutor == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione primeiro um tutor na tabela para adicionar um Pet.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JTextField txtNome = new JTextField();
        JComboBox<Especie> cbEspecie = new JComboBox<>(Especie.values());
        JTextField txtRaca = new JTextField();
        JComboBox<Porte> cbPorte = new JComboBox<>(Porte.values());
        JTextField txtObs = new JTextField();

        JPanel painel = new JPanel(new GridLayout(5, 2, 8, 8));
        painel.add(new JLabel("Nome do Pet (*):"));
        painel.add(txtNome);
        painel.add(new JLabel("Espécie (*):"));
        painel.add(cbEspecie);
        painel.add(new JLabel("Raça (*):"));
        painel.add(txtRaca);
        painel.add(new JLabel("Porte (*):"));
        painel.add(cbPorte);
        painel.add(new JLabel("Observações/Cuidados:"));
        painel.add(txtObs);

        int resultado = JOptionPane.showConfirmDialog(mainFrame, painel,
                "Adicionar Pet para " + tutor.getNome(), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (resultado == JOptionPane.OK_OPTION) {
            try {
                Pet novoPet = new Pet(txtNome.getText().trim(), (Especie) cbEspecie.getSelectedItem(),
                        txtRaca.getText().trim(), (Porte) cbPorte.getSelectedItem(), txtObs.getText().trim());
                service.adicionarPet(tutor.getId(), novoPet);
                JOptionPane.showMessageDialog(mainFrame, "Pet adicionado com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Validação do Pet",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void onExcluirPet() {
        Pet pet = view.getPetSelecionado();
        if (pet == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione um pet na tabela inferior para remover.",
                    "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int resp = JOptionPane.showConfirmDialog(mainFrame,
                "Deseja realmente remover o pet '" + pet.getNome() + "'?",
                "Confirmar Remoção de Pet", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (resp == JOptionPane.YES_OPTION) {
            try {
                service.excluirPet(pet.getId());
                JOptionPane.showMessageDialog(mainFrame, "Pet removido com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Erro ao remover pet: " + ex.getMessage(),
                        "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public void onClientesChanged(List<Cliente> clientes) {
        view.setClientes(clientes);
    }
}
