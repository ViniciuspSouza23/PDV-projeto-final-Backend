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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

/**
 * Ponto de Entrada Principal (Bootstrap) da Aplicação Desktop PetPDV.
 * Aplica Inversão de Controle e Pure Dependency Injection (Composition Root).
 */
public class Main {

    public static void main(String[] args) {
        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                // Migra de forma segura a estrutura do banco H2 caso venha de versões anteriores
                migrarEstruturaBancoSeNecessario();

                // Instanciação da camada de persistência (JPA / Hibernate / H2)
                ProdutoRepository produtoRepository = new ProdutoRepositoryImpl();
                ClienteRepository clienteRepository = new ClienteRepositoryImpl();
                PetRepository petRepository = new PetRepositoryImpl();
                ServicoRepository servicoRepository = new ServicoRepositoryImpl();
                VendaRepository vendaRepository = new VendaRepositoryImpl();

                // Carga inicial didática de produtos para cães, serviços e tutores
                inicializarDadosDemonstracao(produtoRepository, servicoRepository, clienteRepository, petRepository);

                // Instanciação da camada de serviço de negócio
                ProdutoService produtoService = new ProdutoService(produtoRepository);
                ClienteService clienteService = new ClienteService(clienteRepository, petRepository);
                ServicoService servicoService = new ServicoService(servicoRepository);
                VendaService vendaService = new VendaService(vendaRepository);

                // Instanciação da Janela Principal
                MainFrame mainFrame = new MainFrame();

                // Instanciação dos Controladores conectando View e Model (MVC)
                ProdutoController produtoController = new ProdutoController(mainFrame, produtoService);
                produtoController.inicializar();

                ClienteController clienteController = new ClienteController(mainFrame, mainFrame.getClientePetView(), clienteService);
                clienteController.inicializar();

                ServicoController servicoController = new ServicoController(mainFrame, mainFrame.getServicoListView(), servicoService);
                servicoController.inicializar();

                VendaController vendaController = new VendaController(mainFrame, produtoService, vendaService, clienteService, servicoService);
                vendaController.abrirVenda();

                // Conecta os botões da barra superior de navegação para recarregar dados atualizados ao clicar
                mainFrame.getBtnNavVendas().addActionListener(e -> vendaController.abrirVenda());
                mainFrame.getBtnNavProdutos().addActionListener(e -> produtoController.recarregar());
                mainFrame.getBtnNavClientes().addActionListener(e -> clienteController.recarregar());
                mainFrame.getBtnNavServicos().addActionListener(e -> servicoController.recarregar());

                // Encerramento limpo de conexões ao fechar janela
                mainFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        System.out.println("[PetPDV] Encerrando recursos de banco de dados JPA/H2...");
                        JPAUtil.close();
                    }
                });

                mainFrame.setVisible(true);

            } catch (Exception e) {
                System.err.println("[PetPDV] Erro fatal durante a inicialização: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static void migrarEstruturaBancoSeNecessario() {
        try {
            Class.forName("org.h2.Driver");
            try (Connection conn = DriverManager.getConnection("jdbc:h2:./data/pdv_db;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE", "sa", "");
                 Statement stmt = conn.createStatement()) {
                // Atualiza de forma segura cada coluna sem criar tabelas incompletas artificialmente
                executarSqlSilencioso(stmt, "ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS tipo_item VARCHAR(20) DEFAULT 'PRODUTO'");
                executarSqlSilencioso(stmt, "ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS servico_id BIGINT");
                executarSqlSilencioso(stmt, "ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS pet_id BIGINT");
                executarSqlSilencioso(stmt, "ALTER TABLE itens_venda ADD COLUMN IF NOT EXISTS pet_nome VARCHAR(80)");
                executarSqlSilencioso(stmt, "ALTER TABLE itens_venda ALTER COLUMN produto_id DROP NOT NULL");
                executarSqlSilencioso(stmt, "ALTER TABLE vendas ADD COLUMN IF NOT EXISTS cliente_id BIGINT");
            }
        } catch (Exception ignored) {
            // Se o banco ainda não existe, o Hibernate cria do zero normalmente com o schema completo
        }
    }

    private static void executarSqlSilencioso(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (Exception ignored) {
            // Ignora silenciosamente caso a tabela ou coluna ainda não existam nesta etapa
        }
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

    private static void inicializarDadosDemonstracao(ProdutoRepository produtoRepo,
                                                     ServicoRepository servicoRepo,
                                                     ClienteRepository clienteRepo,
                                                     PetRepository petRepo) {
        try {
            List<Produto> existentes = produtoRepo.listarTodos();

            // Remove itens de supermercado da versão anterior caso existam
            boolean temItensMercado = existentes.stream().anyMatch(p ->
                    p.getNome().toLowerCase().contains("café") ||
                    p.getNome().toLowerCase().contains("açúcar") ||
                    p.getNome().toLowerCase().contains("arroz") ||
                    p.getNome().toLowerCase().contains("feijão") ||
                    p.getNome().toLowerCase().contains("azeite"));

            if (temItensMercado) {
                System.out.println("[PetPDV] Removendo itens legados de supermercado...");
                for (Produto p : existentes) {
                    String n = p.getNome().toLowerCase();
                    if (n.contains("café") || n.contains("açúcar") || n.contains("arroz") || n.contains("feijão") || n.contains("azeite")) {
                        try {
                            produtoRepo.excluir(p.getId());
                        } catch (Exception ignored) {}
                    }
                }
                existentes = produtoRepo.listarTodos();
            }

            // 1. Produtos de Cachorro e Pet Shop
            if (existentes.isEmpty() || existentes.size() < 10) {
                System.out.println("[PetPDV] Populando catálogo de produtos para cães e pets...");

                // Rações para Cães
                salvarProdutoSeInexistente(produtoRepo, "7891000100011",
                        "Ração PremieR Formula Cães Adultos Frango 15kg", "189.90", 25);
                salvarProdutoSeInexistente(produtoRepo, "7891000100012",
                        "Ração Golden Formula Cães Filhotes Frango e Arroz 15kg", "164.90", 30);
                salvarProdutoSeInexistente(produtoRepo, "7891000100013",
                        "Ração Royal Canin Mini Adult Cães Pequeno Porte 7.5kg", "229.00", 20);
                salvarProdutoSeInexistente(produtoRepo, "7891000100014",
                        "Ração Hills Science Diet Cães Adultos Frango 12kg", "289.90", 15);

                // Petiscos e Mastigáveis para Cães
                salvarProdutoSeInexistente(produtoRepo, "7891000200021",
                        "Petisco Bifinho Keldog Carne e Cereais para Cães 500g", "22.50", 50);
                salvarProdutoSeInexistente(produtoRepo, "7891000200022",
                        "Biscoito Pedigree Biscrok Cães Adultos Multi 1kg", "28.90", 40);
                salvarProdutoSeInexistente(produtoRepo, "7891000200023",
                        "Osso Mastigável Defumado Dental Anti-Tártaro para Cães", "14.90", 60);

                // Farmácia & Higiene Canina
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

                // Acessórios e Brinquedos para Cães
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

                // Itens para Gatos
                salvarProdutoSeInexistente(produtoRepo, "7891000500051",
                        "Ração Royal Canin Gatos Castrados 7.5kg", "219.00", 18);
                salvarProdutoSeInexistente(produtoRepo, "7891000500052",
                        "Areia Sanitária Pipicat Floral para Gatos 4kg", "16.90", 40);
            }

            // Garante que produtos com estoque baixo em testes didáticos sejam reabastecidos
            for (Produto p : produtoRepo.listarTodos()) {
                if (p.getQuantidadeEstoque() < 15) {
                    p.setQuantidadeEstoque(50);
                    try {
                        produtoRepo.atualizar(p);
                    } catch (Exception ignored) {}
                }
            }

            // 2. Serviços de Pet Shop
            if (servicoRepo.listarTodos().isEmpty()) {
                System.out.println("[PetPDV] Populando serviços de demonstração...");
                servicoRepo.salvar(new Servico("Banho Completo com Secagem", new BigDecimal("55.00"), true,
                        "Banho relaxante, secagem, escovação e perfume pet"));
                servicoRepo.salvar(new Servico("Tosa Higiênica e Corte de Unhas", new BigDecimal("40.00"), true,
                        "Limpeza de patas, corte de unhas e higienização"));
                servicoRepo.salvar(new Servico("Tosa Geral na Tesoura", new BigDecimal("85.00"), true,
                        "Tosa estética completa na tesoura para cães e gatos"));
                servicoRepo.salvar(new Servico("Consulta Veterinária Geral", new BigDecimal("130.00"), false,
                        "Avaliação clínica completa por médico veterinário"));
                servicoRepo.salvar(new Servico("Vacina V10 Polivalente Importada", new BigDecimal("95.00"), false,
                        "Imunização anual com aplicação veterinária inclusa"));
            }

            // 3. Clientes (Tutores) e Pets
            if (clienteRepo.listarTodos().isEmpty()) {
                System.out.println("[PetPDV] Populando tutores e pets de demonstração...");
                Cliente tutor1 = clienteRepo.salvar(new Cliente("Carlos Eduardo Mendes", "123.456.789-00",
                        "(11) 98765-4321", "carlos.mendes@email.com"));
                petRepo.salvar(new Pet("Thor", Especie.CACHORRO, "Golden Retriever", Porte.GRANDE,
                        "Muito dócil, adora água", tutor1));
                petRepo.salvar(new Pet("Luna", Especie.CACHORRO, "Shih Tzu", Porte.PEQUENO,
                        "Pele sensível, usar shampoo hipoalergênico", tutor1));

                Cliente tutor2 = clienteRepo.salvar(new Cliente("Juliana Silveira Rocha", "987.654.321-99",
                        "(11) 91234-5678", "juliana.rocha@email.com"));
                petRepo.salvar(new Pet("Mingau", Especie.GATO, "Siamês", Porte.PEQUENO,
                        "Arredio com barulho de secador", tutor2));
                petRepo.salvar(new Pet("Bob", Especie.CACHORRO, "Bulldog Francês", Porte.MEDIO,
                        "Cuidados com focinho achatado", tutor2));
            }
        } catch (Exception e) {
            System.err.println("[PetPDV] Aviso ao popular demonstração: " + e.getMessage());
        }
    }

    private static void salvarProdutoSeInexistente(ProdutoRepository repo, String codBarras, String nome, String preco, int estoque) {
        if (repo.buscarPorCodigoBarras(codBarras).isEmpty()) {
            repo.salvar(new Produto(codBarras, nome, new BigDecimal(preco), estoque));
        }
    }
}
