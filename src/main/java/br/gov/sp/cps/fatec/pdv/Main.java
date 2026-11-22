package br.gov.sp.cps.fatec.pdv;

import br.gov.sp.cps.fatec.pdv.controller.ClienteController;
import br.gov.sp.cps.fatec.pdv.controller.ProdutoController;
import br.gov.sp.cps.fatec.pdv.controller.ServicoController;
import br.gov.sp.cps.fatec.pdv.controller.VendaController;
import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.Especie;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Porte;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ClienteRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.PetRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.ServicoRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.ServicoRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepository;
import br.gov.sp.cps.fatec.pdv.model.repository.VendaRepositoryImpl;
import br.gov.sp.cps.fatec.pdv.model.service.ClienteService;
import br.gov.sp.cps.fatec.pdv.model.service.ProdutoService;
import br.gov.sp.cps.fatec.pdv.model.service.ServicoService;
import br.gov.sp.cps.fatec.pdv.model.service.VendaService;
import br.gov.sp.cps.fatec.pdv.util.JPAUtil;
import br.gov.sp.cps.fatec.pdv.view.MainFrame;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Ponto de Entrada Principal (Bootstrap) da Aplicação Desktop PetPDV.
 * Aplica Inversão de Controle e Pure Dependency Injection (Composition Root).
 */
public class Main {

    public static void main(String[] args) {
        configurarLookAndFeel();
        SwingUtilities.invokeLater(Main::iniciarAplicacao);
    }

    private static void iniciarAplicacao() {
        JFrame splash = new JFrame("PetPDV");
        splash.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel painel = new JPanel(new BorderLayout(12, 12));
        painel.setBorder(javax.swing.BorderFactory.createEmptyBorder(18, 22, 18, 22));
        painel.add(new JLabel("Preparando o banco de dados e carregando o PetPDV..."), BorderLayout.NORTH);
        JProgressBar progresso = new JProgressBar();
        progresso.setIndeterminate(true);
        painel.add(progresso, BorderLayout.CENTER);
        splash.setContentPane(painel);
        splash.pack();
        splash.setLocationRelativeTo(null);
        splash.setVisible(true);

        new SwingWorker<ApplicationServices, Void>() {
            @Override
            protected ApplicationServices doInBackground() throws Exception {
                return carregarServicos();
            }

            @Override
            protected void done() {
                splash.dispose();
                try {
                    abrirInterface(get());
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarErroInicializacao(ex);
                } catch (ExecutionException ex) {
                    mostrarErroInicializacao(ex.getCause() != null ? ex.getCause() : ex);
                } catch (RuntimeException ex) {
                    mostrarErroInicializacao(ex);
                }
            }
        }.execute();
    }

    private static ApplicationServices carregarServicos() throws Exception {
        migrarEstruturaBancoSeNecessario();

        ProdutoRepository produtoRepository = new ProdutoRepositoryImpl();
        ClienteRepository clienteRepository = new ClienteRepositoryImpl();
        PetRepository petRepository = new PetRepositoryImpl();
        ServicoRepository servicoRepository = new ServicoRepositoryImpl();
        VendaRepository vendaRepository = new VendaRepositoryImpl();

        inicializarDadosDemonstracao(produtoRepository, servicoRepository, clienteRepository, petRepository);

        return new ApplicationServices(
                new ProdutoService(produtoRepository),
                new ClienteService(clienteRepository, petRepository),
                new ServicoService(servicoRepository),
                new VendaService(vendaRepository));
    }

    private static void abrirInterface(ApplicationServices services) {
        MainFrame mainFrame = new MainFrame();

        ProdutoController produtoController = new ProdutoController(mainFrame, services.produtoService());
        produtoController.inicializar();

        ClienteController clienteController = new ClienteController(
                mainFrame, mainFrame.getClientePetView(), services.clienteService());
        clienteController.inicializar();

        ServicoController servicoController = new ServicoController(
                mainFrame, mainFrame.getServicoListView(), services.servicoService());
        servicoController.inicializar();

        VendaController vendaController = new VendaController(
                mainFrame, services.produtoService(), services.vendaService(),
                services.clienteService(), services.servicoService());
        vendaController.abrirVenda();

        mainFrame.getBtnNavVendas().addActionListener(e -> vendaController.abrirVenda());
        mainFrame.getBtnNavProdutos().addActionListener(e -> produtoController.recarregar());
        mainFrame.getBtnNavClientes().addActionListener(e -> clienteController.recarregar());
        mainFrame.getBtnNavServicos().addActionListener(e -> servicoController.recarregar());

        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                JPAUtil.close();
            }
        });

        mainFrame.setVisible(true);
    }

    private static void mostrarErroInicializacao(Throwable error) {
        System.err.println("[PetPDV] Erro fatal durante a inicialização.");
        error.printStackTrace();
        JOptionPane.showMessageDialog(null,
                "Não foi possível iniciar o PetPDV:\n" + error.getMessage(),
                "Erro de inicialização", JOptionPane.ERROR_MESSAGE);
        JPAUtil.close();
    }

    private static void migrarEstruturaBancoSeNecessario() throws Exception {
        Path arquivoBanco = Path.of("data", "pdv_db.mv.db");
        if (!Files.exists(arquivoBanco)) {
            return;
        }

        Class.forName("org.h2.Driver");
        try (Connection conn = DriverManager.getConnection(
                "jdbc:h2:./data/pdv_db;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE", "sa", "");
             Statement stmt = conn.createStatement()) {
            if (tabelaExiste(conn, "ITENS_VENDA")) {
                stmt.execute("ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS tipo_item VARCHAR(20) DEFAULT 'PRODUTO'");
                stmt.execute("ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS servico_id BIGINT");
                stmt.execute("ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS pet_id BIGINT");
                stmt.execute("ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS pet_nome VARCHAR(80)");
                stmt.execute("ALTER TABLE itens_venda ALTER COLUMN produto_id DROP NOT NULL");
            }
            if (tabelaExiste(conn, "VENDAS")) {
                stmt.execute("ALTER TABLE vendas ADD COLUMN IF NOT EXISTS cliente_id BIGINT");
            }
        }
    }

    private static boolean tabelaExiste(Connection conn, String nomeTabela) throws Exception {
        try (ResultSet tabelas = conn.getMetaData().getTables(
                null, null, nomeTabela, new String[]{"TABLE"})) {
            return tabelas.next();
        }
    }

    private record ApplicationServices(
            ProdutoService produtoService,
            ClienteService clienteService,
            ServicoService servicoService,
            VendaService vendaService) {
    }

    private static void configurarLookAndFeel() {
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("ProgressBar.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new java.awt.Insets(2, 2, 2, 2));
            UIManager.put("Table.rowHeight", 30);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
            UIManager.put("Table.selectionBackground", new Color(224, 231, 255));
            UIManager.put("Table.selectionForeground", new Color(30, 41, 59));
            UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 12));
            UIManager.put("TableHeader.height", 32);
            UIManager.put("TabbedPane.tabHeight", 36);
            UIManager.put("TabbedPane.selectedBackground", Color.WHITE);
            UIManager.put("TabbedPane.underlineColor", new Color(37, 99, 235));
            UIManager.put("TabbedPane.showTabSeparators", true);
        } catch (Throwable t) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        return;
                    }
                }
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Não foi possível configurar Look and Feel: " + ex.getMessage());
            }
        }
    }

    static void inicializarDadosDemonstracao(ProdutoRepository produtoRepo,
                                             ServicoRepository servicoRepo,
                                             ClienteRepository clienteRepo,
                                             PetRepository petRepo) {
        List<Produto> existentes = produtoRepo.listarTodos();

        if (existentes.isEmpty()) {
            popularCatalogoDemonstracao(produtoRepo);
        }

        if (servicoRepo.listarTodos().isEmpty()) {
            System.out.println("[PetPDV] Populando serviços de demonstração...");
            servicoRepo.salvar(new Servico("Banho Completo com Secagem", new BigDecimal("55.00"), true,
                    "Banho relaxante, secagem, escovação e perfume pet"));
            servicoRepo.salvar(new Servico("Tosa Higiênica e Corte de Unhas", new BigDecimal("40.00"), true,
                    "Limpeza de patas, corte de unhas e higienização"));
            servicoRepo.salvar(new Servico("Tosa Geral na Tesoura", new BigDecimal("85.00"), true,
                    "Tosa estética completa na tesoura"));
            servicoRepo.salvar(new Servico("Consulta Veterinária Geral", new BigDecimal("130.00"), false,
                    "Avaliação clínica completa por médico veterinário"));
            servicoRepo.salvar(new Servico("Vacina V10 Polivalente Importada", new BigDecimal("95.00"), false,
                    "Imunização anual com aplicação veterinária inclusa"));
        }

        if (clienteRepo.listarTodos().isEmpty()) {
            System.out.println("[PetPDV] Populando tutores e pets de demonstração...");
            Cliente tutor1 = clienteRepo.salvar(new Cliente("Carlos Eduardo Mendes", "321.654.987-91",
                    "(11) 98765-4321", "carlos.mendes@email.com"));
            petRepo.salvar(new Pet("Thor", Especie.CACHORRO, "Golden Retriever", Porte.GRANDE,
                    "Muito dócil, adora água", tutor1));
            petRepo.salvar(new Pet("Luna", Especie.CACHORRO, "Shih Tzu", Porte.PEQUENO,
                    "Pele sensível, usar shampoo hipoalergênico", tutor1));

            Cliente tutor2 = clienteRepo.salvar(new Cliente("Juliana Silveira Rocha", "987.654.321-00",
                    "(11) 91234-5678", "juliana.rocha@email.com"));
            petRepo.salvar(new Pet("Mingau", Especie.GATO, "Siamês", Porte.PEQUENO,
                    "Arredio com barulho de secador", tutor2));
            petRepo.salvar(new Pet("Bob", Especie.CACHORRO, "Bulldog Francês", Porte.MEDIO,
                    "Cuidados com focinho achatado", tutor2));
        }
    }

    private static void popularCatalogoDemonstracao(ProdutoRepository produtoRepo) {
        System.out.println("[PetPDV] Populando catálogo de produtos para cães e pets...");

        salvarProdutoSeInexistente(produtoRepo, "7891000100011",
                "Ração PremieR Formula Cães Adultos Frango 15kg", "189.90", 25);
        salvarProdutoSeInexistente(produtoRepo, "7891000100012",
                "Ração Golden Formula Cães Filhotes Frango e Arroz 15kg", "164.90", 30);
        salvarProdutoSeInexistente(produtoRepo, "7891000100013",
                "Ração Royal Canin Mini Adult Cães Pequeno Porte 7.5kg", "229.00", 20);
        salvarProdutoSeInexistente(produtoRepo, "7891000100014",
                "Ração Hills Science Diet Cães Adultos Frango 12kg", "289.90", 15);

        salvarProdutoSeInexistente(produtoRepo, "7891000200021",
                "Petisco Bifinho Keldog Carne e Cereais para Cães 500g", "22.50", 50);
        salvarProdutoSeInexistente(produtoRepo, "7891000200022",
                "Biscoito Pedigree Biscrok Cães Adultos Multi 1kg", "28.90", 40);
        salvarProdutoSeInexistente(produtoRepo, "7891000200023",
                "Osso Mastigável Defumado Dental Anti-Tártaro para Cães", "14.90", 60);

        salvarProdutoSeInexistente(produtoRepo, "7891000300031",
                "Antipulgas e Carrapatos Simparic 10 a 20kg (3 comprimidos)", "115.00", 35);
        salvarProdutoSeInexistente(produtoRepo, "7891000300032",
                "Antipulgas e Carrapatos Bravecto Cães 4.5 a 10kg (1 comp)", "219.00", 20);
        salvarProdutoSeInexistente(produtoRepo, "7891000300033",
                "Shampoo Sanol Dog Antipulgas e Carrapatos 500ml", "28.50", 45);
        salvarProdutoSeInexistente(produtoRepo, "7891000300034",
                "Vermífugo Canino Chemital Plus Cães 4 comprimidos", "39.90", 30);
        salvarProdutoSeInexistente(produtoRepo, "7891000300035",
                "Tapete Higiênico Chalesco Ultra Absorvente 30 unidades", "69.90", 25);

        salvarProdutoSeInexistente(produtoRepo, "7891000400041",
                "Guia Retrátil para Cães até 25kg com Trava 5m", "54.90", 22);
        salvarProdutoSeInexistente(produtoRepo, "7891000400042",
                "Peitoral Antipuxão Ajustável com Guia Reforçada", "49.90", 18);
        salvarProdutoSeInexistente(produtoRepo, "7891000400043",
                "Brinquedo Mordedor Osso Nylon Resistente para Cães", "29.90", 50);
        salvarProdutoSeInexistente(produtoRepo, "7891000400044",
                "Bolinha Maciça com Cravo Borracha Mordedora 65mm", "18.50", 40);
        salvarProdutoSeInexistente(produtoRepo, "7891000400045",
                "Cama Pet Almofadada Conforto Lavável Tam G para Cães", "129.90", 12);

        salvarProdutoSeInexistente(produtoRepo, "7891000500051",
                "Ração Royal Canin Gatos Castrados 7.5kg", "219.00", 18);
        salvarProdutoSeInexistente(produtoRepo, "7891000500052",
                "Areia Sanitária Pipicat Floral para Gatos 4kg", "16.90", 40);
    }

    private static void salvarProdutoSeInexistente(ProdutoRepository repo, String codBarras, String nome, String preco, int estoque) {
        if (repo.buscarPorCodigoBarras(codBarras).isEmpty()) {
            repo.salvar(new Produto(codBarras, nome, new BigDecimal(preco), estoque));
        }
    }
}
