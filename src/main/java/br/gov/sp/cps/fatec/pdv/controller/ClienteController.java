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
        recarregar();
    }

    public void recarregar() {
        SwingTaskRunner.run(() -> {
            service.notificarObservadores();
            return null;
        }, ignored -> {
        }, this::mostrarErroAoCarregar);
    }

    private void vincularEventos() {
        view.getBtnNovoCliente().addActionListener(e -> onNovoCliente());
        view.getBtnEditarCliente().addActionListener(e -> onEditarCliente());
        view.getBtnExcluirCliente().addActionListener(e -> onExcluirCliente());
        view.getBtnAdicionarPet().addActionListener(e -> onAdicionarPet());
        view.getBtnExcluirPet().addActionListener(e -> onExcluirPet());
        view.getBtnRecarregar().addActionListener(e -> recarregar());
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
            Cliente novo = new Cliente(txtNome.getText().trim(), txtCpf.getText().trim(),
                    txtTelefone.getText().trim(), txtEmail.getText().trim());
            executarOperacao(() -> service.salvar(novo), "Tutor cadastrado com sucesso!",
                    "Não foi possível cadastrar o tutor: ");
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
            selecionado.setNome(txtNome.getText().trim());
            selecionado.setCpf(txtCpf.getText().trim());
            selecionado.setTelefone(txtTelefone.getText().trim());
            selecionado.setEmail(txtEmail.getText().trim());
            executarOperacao(() -> service.atualizar(selecionado), "Tutor atualizado com sucesso!",
                    "Não foi possível atualizar o tutor: ");
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
            executarOperacao(() -> service.excluir(selecionado.getId()), "Tutor removido com sucesso!",
                    "Não foi possível excluir o tutor: ");
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
            Pet novoPet = new Pet(txtNome.getText().trim(), (Especie) cbEspecie.getSelectedItem(),
                    txtRaca.getText().trim(), (Porte) cbPorte.getSelectedItem(), txtObs.getText().trim());
            executarOperacao(() -> service.adicionarPet(tutor.getId(), novoPet), "Pet adicionado com sucesso!",
                    "Não foi possível adicionar o pet: ");
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
            executarOperacao(() -> service.excluirPet(pet.getId()), "Pet removido com sucesso!",
                    "Erro ao remover pet: ");
        }
    }

    private void executarOperacao(Runnable operacao, String mensagemSucesso, String mensagemErro) {
        SwingTaskRunner.run(() -> {
            operacao.run();
            return null;
        }, ignored -> JOptionPane.showMessageDialog(mainFrame, mensagemSucesso, "Sucesso",
                JOptionPane.INFORMATION_MESSAGE), ex -> {
            boolean validacao = ex instanceof IllegalArgumentException;
            JOptionPane.showMessageDialog(mainFrame, mensagemErro + ex.getMessage(),
                    validacao ? "Validação" : "Erro",
                    validacao ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
        });
    }

    private void mostrarErroAoCarregar(Throwable error) {
        JOptionPane.showMessageDialog(mainFrame, "Não foi possível carregar tutores e pets:\n" + error.getMessage(),
                "Erro de persistência", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onClientesChanged(List<Cliente> clientes) {
        view.setClientes(clientes);
    }
}
