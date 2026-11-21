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

        // Barra de Status Inferior
        JPanel barraStatus = criarBarraStatus();

        JPanel container = new JPanel(new BorderLayout());
        container.add(barraNavegacao, BorderLayout.NORTH);
        container.add(painelPrincipal, BorderLayout.CENTER);
        container.add(barraStatus, BorderLayout.SOUTH);
        setContentPane(container);

        // Configurações da Janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setPreferredSize(new Dimension(1160, 760));
        pack();
        setLocationRelativeTo(null); // Centraliza a janela na tela
        atualizarSelecaoNav(btnNavVendas);
    }

    private JPanel criarBarraNavegacao() {
        JPanel nav = new JPanel(new BorderLayout(15, 0));
        nav.setBackground(new Color(255, 255, 255));
        nav.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        // Marca / Logo à Esquerda
        JPanel painelMarca = new JPanel(new GridLayout(2, 1, 0, 1));
        painelMarca.setOpaque(false);
        JLabel lblLogo = new JLabel("🐾 PetPDV Pro");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblLogo.setForeground(new Color(15, 23, 42));

        JLabel lblSub = new JLabel("Pet Shop & Clínica Veterinária");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(100, 116, 139));

        painelMarca.add(lblLogo);
        painelMarca.add(lblSub);
        nav.add(painelMarca, BorderLayout.WEST);

        // Botões de Navegação Segmentados
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        botoes.setOpaque(false);
        botoes.add(btnNavVendas);
        botoes.add(btnNavProdutos);
        botoes.add(btnNavClientes);
        botoes.add(btnNavServicos);
        nav.add(botoes, BorderLayout.CENTER);

        return nav;
    }

    private JPanel criarBarraStatus() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBackground(new Color(248, 250, 252));
        status.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(6, 18, 6, 18)
        ));

        JLabel lblStatus = new JLabel("🟢 Sistema Pronto  •  H2 Database Conectado  •  JPA 3.1 / Hibernate 6");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(100, 116, 139));
        status.add(lblStatus, BorderLayout.WEST);

        JLabel lblAtalho = new JLabel("PetPDV v1.2.0 • Modo Caixa Ativo");
        lblAtalho.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblAtalho.setForeground(new Color(148, 163, 184));
        status.add(lblAtalho, BorderLayout.EAST);

        return status;
    }

    private JButton criarBotaoNav(String texto, Runnable acao) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(185, 36));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        btn.addActionListener(e -> {
            acao.run();
            atualizarSelecaoNav(btn);
        });
        return btn;
    }

    private void atualizarSelecaoNav(JButton botaoAtivo) {
        JButton[] botoes = {btnNavVendas, btnNavProdutos, btnNavClientes, btnNavServicos};
        for (JButton b : botoes) {
            if (b == botaoAtivo) {
                b.setBackground(new Color(37, 99, 235));
                b.setForeground(Color.WHITE);
                b.setFont(new Font("Segoe UI", Font.BOLD, 12));
            } else {
                b.setBackground(new Color(241, 245, 249));
                b.setForeground(new Color(51, 65, 85));
                b.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            }
        }
    }

    public void mostrarLista() {
        cardLayout.show(painelPrincipal, TELA_LISTA);
        atualizarSelecaoNav(btnNavProdutos);
    }

    public void mostrarFormulario() {
        cardLayout.show(painelPrincipal, TELA_FORMULARIO);
    }

    public void mostrarVenda() {
        cardLayout.show(painelPrincipal, TELA_VENDA);
        atualizarSelecaoNav(btnNavVendas);
    }

    public void mostrarClientes() {
        cardLayout.show(painelPrincipal, TELA_CLIENTES);
        atualizarSelecaoNav(btnNavClientes);
    }

    public void mostrarServicos() {
        cardLayout.show(painelPrincipal, TELA_SERVICOS);
        atualizarSelecaoNav(btnNavServicos);
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
