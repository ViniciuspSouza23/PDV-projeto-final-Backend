package br.gov.sp.cps.fatec.pdv.controller;

import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.observer.ProdutoObserver;
import br.gov.sp.cps.fatec.pdv.model.repository.ProdutoRepository;
import br.gov.sp.cps.fatec.pdv.model.service.ProdutoService;
import br.gov.sp.cps.fatec.pdv.view.MainFrame;
import br.gov.sp.cps.fatec.pdv.view.ProdutoFormView;
import br.gov.sp.cps.fatec.pdv.view.ProdutoListView;

import javax.swing.JOptionPane;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Controlador Principal do Módulo de Gestão de Produtos (Padrão MVC &
 * Mediator).
 * <p>
 * <b>Conceito Pedagógico (Padrão Controller & Inversão de Dependência):</b>
 * <ul>
 * <li><b>Orquestrador Central:</b> O Controller captura as interações do
 * usuário disparadas
 * nas telas Swing (eventos de botões), valida e converte dados de entrada e
 * aciona a camada de negócio.</li>
 * <li><b>Isolamento Completo:</b> A View nunca se comunica diretamente com o
 * Repositório ou banco de dados.</li>
 * <li><b>Inversão de Dependência (IoC):</b> O Controller recebe suas
 * dependências no construtor através
 * de interfaces e classes de serviço, desacoplando a camada de controle da
 * instanciação física.</li>
 * <li><b>Integração com o Padrão Observer:</b> O Controller implementa
 * {@link ProdutoObserver},
 * sendo notificado de forma reativa a cada persistência para atualizar a JTable
 * da tela.</li>
 * </ul>
 *
 * @author Cainã Antunes Silva
 */
public class ProdutoController implements ProdutoObserver {

    private final MainFrame mainFrame;
    private final ProdutoListView listView;
    private final ProdutoFormView formView;
    private final ProdutoService service;

    /**
     * Construtor recebendo o Repositório diretamente (Inversão de Dependência).
     *
     * @param mainFrame  Janela principal da aplicação.
     * @param repository Abstração do repositório de dados.
     */
    public ProdutoController(MainFrame mainFrame, ProdutoRepository repository) {
        this(mainFrame, new ProdutoService(repository));
    }

    /**
     * Construtor recebendo a camada de serviço.
     *
     * @param mainFrame Janela principal da aplicação.
     * @param service   Camada de serviços e regras de negócio.
     */
    public ProdutoController(MainFrame mainFrame, ProdutoService service) {
        this.mainFrame = Objects.requireNonNull(mainFrame, "MainFrame não pode ser nulo.");
        this.listView = mainFrame.getListView();
        this.formView = mainFrame.getFormView();
        this.service = Objects.requireNonNull(service, "ProdutoService não pode ser nulo.");

        vincularEventos();
    }

    /**
     * Inicializa o fluxo do controlador, conectando os observadores e carregando a
     * listagem inicial.
     */
    public void inicializar() {
        this.service.addObserver(this);
        recarregar();
    }

    /**
     * Recarrega os dados de produtos via camada de serviço.
     */
    public void recarregar() {
        SwingTaskRunner.run(() -> {
            service.notificarObservadores();
            return null;
        }, ignored -> {
        }, this::mostrarErroDePersistencia);
    }

    /**
     * Configura os ouvintes de eventos (Listeners) nos componentes visuais das
     * telas.
     */
    private void vincularEventos() {
        // --- Eventos da Tela de Listagem ---
        listView.getBtnNovo().addActionListener(e -> onNovoProduto());
        listView.getBtnEditar().addActionListener(e -> onEditarProduto());
        listView.getBtnExcluir().addActionListener(e -> onExcluirProduto());
        listView.getBtnRecarregar().addActionListener(e -> onRecarregarProdutos());

        // --- Eventos da Tela de Formulário ---
        formView.getBtnSalvar().addActionListener(e -> onSalvarProduto());
        formView.getBtnLimpar().addActionListener(e -> onLimparFormulario());
        formView.getBtnVoltar().addActionListener(e -> onVoltarParaListagem());
    }

    // ==========================================
    // TRATADORES DE EVENTOS (HANDLERS)
    // ==========================================

    private void onNovoProduto() {
        formView.configurarModoNovo();
        mainFrame.mostrarFormulario();
    }

    private void onVoltarParaListagem() {
        mainFrame.mostrarLista();
    }

    private void onLimparFormulario() {
        formView.limparCampos();
    }

    private void onRecarregarProdutos() {
        recarregar();
    }

    private void onEditarProduto() {
        Produto selecionado = listView.getProdutoSelecionado();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Selecione um produto na tabela para editar.",
                    "Atenção - Seleção Necessária",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        formView.carregarParaEdicao(selecionado);
        mainFrame.mostrarFormulario();
    }

    private void onExcluirProduto() {
        Produto selecionado = listView.getProdutoSelecionado();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    "Selecione um produto na tabela para excluir.",
                    "Atenção - Seleção Necessária",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacao = JOptionPane.showConfirmDialog(
                mainFrame,
                String.format("Deseja realmente excluir o produto abaixo?\n\nID: #%d\nNome: %s\nCódigo: %s",
                        selecionado.getId(), selecionado.getNome(), selecionado.getCodigoBarras()),
                "Confirmar Exclusão de Produto",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirmacao == JOptionPane.YES_OPTION) {
            SwingTaskRunner.run(() -> {
                service.excluir(selecionado.getId());
                return null;
            }, ignored -> {
                JOptionPane.showMessageDialog(
                        mainFrame,
                        "Produto excluído com sucesso!",
                        "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
            }, ex -> JOptionPane.showMessageDialog(
                    mainFrame,
                    "Erro ao excluir produto: " + ex.getMessage(),
                    "Erro de Persistência",
                    JOptionPane.ERROR_MESSAGE));
        }
    }

    private void onSalvarProduto() {
        Produto produto;
        boolean novo;
        try {
            String codigoBarras = formView.getCodigoBarras();
            String nome = formView.getNome();
            String precoStr = formView.getPrecoTexto().replace(",", ".");
            String estoqueStr = formView.getQuantidadeEstoqueTexto();

            // Validação de formato de números
            BigDecimal preco;
            try {
                preco = new BigDecimal(precoStr);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Preço inválido. Informe um valor numérico válido (ex.: 19.99).");
            }

            Integer quantidadeEstoque;
            try {
                quantidadeEstoque = Integer.parseInt(estoqueStr);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(
                        "Quantidade em estoque inválida. Informe um número inteiro (ex.: 10).");
            }

            Long id = formView.getProdutoIdAtual();
            novo = id == null;
            produto = novo
                    ? new Produto(codigoBarras, nome, preco, quantidadeEstoque)
                    : new Produto(id, codigoBarras, nome, preco, quantidadeEstoque);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    ex.getMessage(),
                    "Validação de Dados",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SwingTaskRunner.run(() -> {
            if (novo) {
                service.salvar(produto);
            } else {
                service.atualizar(produto);
            }
            return null;
        }, ignored -> {
            JOptionPane.showMessageDialog(
                    mainFrame,
                    novo ? "Produto cadastrado com sucesso!" : "Produto atualizado com sucesso!",
                    "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            mainFrame.mostrarLista();
        }, this::mostrarErroDePersistencia);
    }

    private void mostrarErroDePersistencia(Throwable ex) {
        boolean validacao = ex instanceof IllegalArgumentException;
        JOptionPane.showMessageDialog(
                mainFrame,
                (validacao ? "" : "Falha na operação de persistência: ") + ex.getMessage(),
                validacao ? "Validação de Dados" : "Erro no Banco de Dados",
                validacao ? JOptionPane.WARNING_MESSAGE : JOptionPane.ERROR_MESSAGE);
    }

    // ==========================================
    // IMPLEMENTAÇÃO DO OBSERVER PATTERN
    // ==========================================

    @Override
    public void onProdutosChanged(List<Produto> produtos) {
        // Atualiza a tabela na View quando o Modelo/Service sinaliza alterações
        listView.atualizarTabela(produtos);
    }
}
