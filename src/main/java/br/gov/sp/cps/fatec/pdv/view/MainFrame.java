package br.gov.sp.cps.fatec.pdv.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Janela Principal da Aplicação Swing Pet Shop ({@link JFrame}).
 * <p>
 * Utiliza {@link CardLayout} para gerenciar a alternância limpa entre telas:
 * Vendas/Caixa, Catálogo de Produtos, Gestão de Tutores/Pets e Serviços.
 */
public class MainFrame extends JFrame {

    public static final String TELA_LISTA = "CARD_LISTA";
    public static final String TELA_FORMULARIO = "CARD_FORMULARIO";
    public static final String TELA_VENDA = "CARD_VENDA";
    public static final String TELA_CLIENTES = "CARD_CLIENTES";
    public static final String TELA_SERVICOS = "CARD_SERVICOS";

    private final CardLayout cardLayout;
    private final JPanel painelPrincipal;

    private final ProdutoListView listView;
    private final ProdutoFormView formView;
    private final VendaView vendaView;
    private final ClientePetView clientePetView;
    private final ServicoListView servicoListView;

    private final JButton btnNavVendas;
    private final JButton btnNavProdutos;
    private final JButton btnNavClientes;
    private final JButton btnNavServicos;

    public MainFrame() {
        super("PetPDV - Sistema Integrado de Pet Shop & Clínica");

        this.cardLayout = new CardLayout();
        this.painelPrincipal = new JPanel(cardLayout);

        this.listView = new ProdutoListView();
        this.formView = new ProdutoFormView();
        this.vendaView = new VendaView();
        this.clientePetView = new ClientePetView();
        this.servicoListView = new ServicoListView();

        this.btnNavVendas = criarBotaoNav("🛒 Caixa / Vendas", () -> mostrarVenda());
        this.btnNavProdutos = criarBotaoNav("📦 Produtos (Estoque)", () -> mostrarLista());
        this.btnNavClientes = criarBotaoNav("🐾 Tutores & Pets", () -> mostrarClientes());
        this.btnNavServicos = criarBotaoNav("✂️ Serviços (Banho & Tosa)", () -> mostrarServicos());

        // Adiciona as telas ao container gerenciado por CardLayout
        this.painelPrincipal.add(vendaView, TELA_VENDA);
        this.painelPrincipal.add(listView, TELA_LISTA);
        this.painelPrincipal.add(formView, TELA_FORMULARIO);
        this.painelPrincipal.add(clientePetView, TELA_CLIENTES);
        this.painelPrincipal.add(servicoListView, TELA_SERVICOS);

        // Barra Superior de Navegação Rápida
        JPanel barraNavegacao = criarBarraNavegacao();

        JPanel container = new JPanel(new BorderLayout());
        container.add(barraNavegacao, BorderLayout.NORTH);
        container.add(painelPrincipal, BorderLayout.CENTER);
        setContentPane(container);

        // Configurações da Janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(950, 680));
        setPreferredSize(new Dimension(1050, 720));
        pack();
        setLocationRelativeTo(null); // Centraliza a janela na tela
    }

    private JPanel criarBarraNavegacao() {
        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        nav.setBackground(new Color(245, 247, 250));
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        nav.add(btnNavVendas);
        nav.add(btnNavProdutos);
        nav.add(btnNavClientes);
        nav.add(btnNavServicos);
        return nav;
    }

    private JButton criarBotaoNav(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(190, 36));
        btn.addActionListener(e -> acao.run());
        return btn;
    }

    public void mostrarLista() {
        cardLayout.show(painelPrincipal, TELA_LISTA);
    }

    public void mostrarFormulario() {
        cardLayout.show(painelPrincipal, TELA_FORMULARIO);
    }

    public void mostrarVenda() {
        cardLayout.show(painelPrincipal, TELA_VENDA);
    }

    public void mostrarClientes() {
        cardLayout.show(painelPrincipal, TELA_CLIENTES);
    }

    public void mostrarServicos() {
        cardLayout.show(painelPrincipal, TELA_SERVICOS);
    }

    public ProdutoListView getListView() {
        return listView;
    }

    public ProdutoFormView getFormView() {
        return formView;
    }

    public VendaView getVendaView() {
        return vendaView;
    }

    public ClientePetView getClientePetView() {
        return clientePetView;
    }

    public ServicoListView getServicoListView() {
        return servicoListView;
    }

    public JButton getBtnNavVendas() {
        return btnNavVendas;
    }

    public JButton getBtnNavProdutos() {
        return btnNavProdutos;
    }

    public JButton getBtnNavClientes() {
        return btnNavClientes;
    }

    public JButton getBtnNavServicos() {
        return btnNavServicos;
    }
}
