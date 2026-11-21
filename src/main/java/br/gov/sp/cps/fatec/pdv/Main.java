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

/**
 * Ponto de Entrada Principal (Bootstrap) da Aplicação Desktop PetPDV.
 * Aplica Inversão de Controle e Pure Dependency Injection (Composition Root).
 */
public class Main {

    public static void main(String[] args) {
        configurarLookAndFeel();

        SwingUtilities.invokeLater(() -> {
            try {
                // Instanciação da camada de persistência (JPA / Hibernate / H2)
                ProdutoRepository produtoRepository = new ProdutoRepositoryImpl();
                ClienteRepository clienteRepository = new ClienteRepositoryImpl();
                PetRepository petRepository = new PetRepositoryImpl();
                ServicoRepository servicoRepository = new ServicoRepositoryImpl();
                VendaRepository vendaRepository = new VendaRepositoryImpl();

                // Carga inicial didática se o banco estiver vazio
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

    private static void configurarLookAndFeel() {
        try {
            FlatLightLaf.setup();
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
            // 1. Produtos de Pet Shop
            if (produtoRepo.listarTodos().isEmpty()) {
                System.out.println("[PetPDV] Populando produtos de demonstração...");
                produtoRepo.salvar(new Produto("7891000100011", "Ração Golden Formula Cães Adultos Frango 15kg",
                        new BigDecimal("169.90"), 25));
                produtoRepo.salvar(new Produto("7891000200022", "Ração Royal Canin Gatos Castrados 7.5kg",
                        new BigDecimal("219.00"), 18));
                produtoRepo.salvar(new Produto("7891000300033", "Antipulgas e Carrapatos Simparic 10 a 20kg",
                        new BigDecimal("109.90"), 40));
                produtoRepo.salvar(new Produto("7891000400044", "Shampoo Pet Neutro Brilho e Maciez 500ml",
                        new BigDecimal("34.50"), 30));
                produtoRepo.salvar(new Produto("7891000500055", "Brinquedo Mordedor Osso Nylon Resistente",
                        new BigDecimal("29.90"), 50));
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
}
