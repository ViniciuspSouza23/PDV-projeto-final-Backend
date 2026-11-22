package br.gov.sp.cps.fatec.pdv.controller;

import br.gov.sp.cps.fatec.pdv.model.entity.Cliente;
import br.gov.sp.cps.fatec.pdv.model.entity.FormaPagamento;
import br.gov.sp.cps.fatec.pdv.model.entity.ItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Pet;
import br.gov.sp.cps.fatec.pdv.model.entity.Produto;
import br.gov.sp.cps.fatec.pdv.model.entity.Servico;
import br.gov.sp.cps.fatec.pdv.model.entity.TipoItemVenda;
import br.gov.sp.cps.fatec.pdv.model.entity.Venda;
import br.gov.sp.cps.fatec.pdv.model.observer.ClienteObserver;
import br.gov.sp.cps.fatec.pdv.model.observer.ProdutoObserver;
import br.gov.sp.cps.fatec.pdv.model.observer.ServicoObserver;
import br.gov.sp.cps.fatec.pdv.model.service.ClienteService;
import br.gov.sp.cps.fatec.pdv.model.service.ProdutoService;
import br.gov.sp.cps.fatec.pdv.model.service.ServicoService;
import br.gov.sp.cps.fatec.pdv.model.service.VendaService;
import br.gov.sp.cps.fatec.pdv.view.MainFrame;
import br.gov.sp.cps.fatec.pdv.view.VendaView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class VendaController implements ProdutoObserver, ClienteObserver, ServicoObserver {

    private static final NumberFormat MOEDA = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    private final MainFrame mainFrame;
    private final VendaView vendaView;
    private final ProdutoService produtoService;
    private final VendaService vendaService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public VendaController(MainFrame mainFrame, ProdutoService produtoService, VendaService vendaService) {
        this(mainFrame, produtoService, vendaService, null, null);
    }

    public VendaController(MainFrame mainFrame, ProdutoService produtoService, VendaService vendaService,
                           ClienteService clienteService, ServicoService servicoService) {
        this.mainFrame = Objects.requireNonNull(mainFrame, "MainFrame não pode ser nulo.");
        this.vendaView = mainFrame.getVendaView();
        this.produtoService = Objects.requireNonNull(produtoService, "ProdutoService não pode ser nulo.");
        this.vendaService = Objects.requireNonNull(vendaService, "VendaService não pode ser nulo.");
        this.clienteService = clienteService;
        this.servicoService = servicoService;

        vincularEventos();
        registrarObservadores();
    }

    private void registrarObservadores() {
        this.produtoService.addObserver(this);
        if (this.clienteService != null) {
            this.clienteService.addObserver(this);
        }
        if (this.servicoService != null) {
            this.servicoService.addObserver(this);
        }
    }

    private void vincularEventos() {
        mainFrame.getListView().getBtnAbrirVenda().addActionListener(event -> abrirVenda());
        vendaView.getBtnAdicionarProduto().addActionListener(event -> adicionarProduto());
        vendaView.getBtnRecarregarEstoque().addActionListener(event -> carregarDados());
        vendaView.getBtnAdicionarServico().addActionListener(event -> adicionarServico());
        vendaView.getBtnRemoverItem().addActionListener(event -> vendaView.removerItemSelecionado());
        vendaView.getBtnLimparCarrinho().addActionListener(event -> vendaView.limparCarrinho());
        vendaView.getBtnFinalizar().addActionListener(event -> finalizarVenda());
        vendaView.getBtnAtualizarHistorico().addActionListener(event -> carregarDados());
        vendaView.getBtnDetalhes().addActionListener(event -> mostrarDetalhesVenda());

        // Atualização reativa de pets quando muda o tutor
        vendaView.getCbTutor().addActionListener(event -> onTutorMudou());

        // Atualização do preview de preço do serviço e verificação de pet
        vendaView.getCbServico().addActionListener(event -> atualizarPrecoServicoPreview());
        vendaView.getCbPet().addActionListener(event -> onPetMudou());
    }

    public void abrirVenda() {
        mainFrame.mostrarVenda();
        carregarDados();
    }

    private void onTutorMudou() {
        if (!vendaView.getItensCarrinho().isEmpty()) {
            int resposta = JOptionPane.showConfirmDialog(mainFrame,
                    "Você alterou o tutor e o carrinho possui " + vendaView.getItensCarrinho().size()
                            + " item(ns) do atendimento anterior.\n\n"
                            + "Deseja limpar o carrinho para iniciar um novo atendimento para este tutor?",
                    "Troca de Tutor / Novo Atendimento",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (resposta == JOptionPane.YES_OPTION) {
                vendaView.limparCarrinho();
            }
        }

        Cliente tutor = vendaView.getTutorSelecionado();
        if (tutor != null && tutor.getPets() != null && !tutor.getPets().isEmpty()) {
            vendaView.setPetsDoTutor(tutor.getPets());
            vendaView.getCbPet().setEnabled(true);
            vendaView.getTxtClienteAvulso().setText("");
            vendaView.getTxtClienteAvulso().setEnabled(false);
        } else if (tutor != null) {
            vendaView.setPetsDoTutor(List.of());
            vendaView.getCbPet().setEnabled(false);
            vendaView.getTxtClienteAvulso().setText("");
            vendaView.getTxtClienteAvulso().setEnabled(false);
        } else {
            vendaView.setPetsDoTutor(List.of());
            vendaView.getCbPet().setEnabled(false);
            vendaView.getTxtClienteAvulso().setEnabled(true);
        }
        atualizarPrecoServicoPreview();
    }

    private void onPetMudou() {
        atualizarPrecoServicoPreview();
        Pet petAtual = vendaView.getPetSelecionado();
        List<ItemVenda> itens = vendaView.getItensCarrinho();
        if (petAtual != null && !itens.isEmpty()) {
            boolean temItensDeOutroPet = itens.stream()
                    .anyMatch(i -> i.getPetId() != null && !i.getPetId().equals(petAtual.getId()));
            boolean temItemDestePet = itens.stream()
                    .anyMatch(i -> i.getPetId() != null && i.getPetId().equals(petAtual.getId()));

            if (temItensDeOutroPet && !temItemDestePet) {
                int opc = JOptionPane.showConfirmDialog(mainFrame,
                        "Você selecionou o pet '" + petAtual.getNome() + "', mas o carrinho contém "
                                + itens.size() + " item(ns) de outro pet.\n\n"
                                + "Deseja LIMPAR o carrinho para iniciar um atendimento novo para " + petAtual.getNome() + "?\n"
                                + "(Selecione 'Não' se desejar manter os itens e registrar múltiplos pets juntos)",
                        "Troca de Pet Atendido",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if (opc == JOptionPane.YES_OPTION) {
                    vendaView.limparCarrinho();
                }
            }
        }
    }

    private void atualizarPrecoServicoPreview() {
        Servico servico = vendaView.getServicoSelecionado();
        if (servico == null) {
            vendaView.getLblPrecoServicoAjustado().setText("Preço: R$ 0,00");
            return;
        }

        Pet pet = vendaView.getPetSelecionado();
        BigDecimal preco;
        if (servicoService != null) {
            preco = servicoService.calcularPreco(servico, pet);
        } else {
            preco = servico.getPrecoBase();
        }

        String complemento = "";
        if (servico.isPermiteMultiplicadorPorte() && pet != null) {
            complemento = " (" + pet.getPorte().getDescricao() + " " + pet.getPorte().getMultiplicador() + "x)";
        }
        vendaView.getLblPrecoServicoAjustado().setText("Preço: " + MOEDA.format(preco) + complemento);
    }

    private void adicionarProduto() {
        Produto produtoSelecionado = vendaView.getProdutoSelecionado();
        if (produtoSelecionado == null) {
            JOptionPane.showMessageDialog(mainFrame, "Nenhum produto selecionado.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade = vendaView.getQuantidadeProduto();
        Pet pet = vendaView.getPetSelecionado();
        Long petId = pet != null ? pet.getId() : null;
        String petNome = pet != null ? pet.getNome() : null;
        vendaView.getBtnAdicionarProduto().setEnabled(false);

        SwingTaskRunner.run(() -> produtoService.buscarPorId(produtoSelecionado.getId())
                        .orElseThrow(() -> new IllegalArgumentException("O produto selecionado não existe mais.")),
                produto -> {
                    try {
                        if (produto.getQuantidadeEstoque() <= 0) {
                            JOptionPane.showMessageDialog(mainFrame,
                                    "O produto '" + produto.getNome()
                                            + "' está sem estoque. Atualize a quantidade na tela de Produtos.",
                                    "Produto sem estoque", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        int jaNoCarrinho = vendaView.getItensCarrinho().stream()
                                .filter(i -> i.getTipoItem() == TipoItemVenda.PRODUTO
                                        && produto.getId().equals(i.getProdutoId()))
                                .mapToInt(ItemVenda::getQuantidade)
                                .sum();
                        if (jaNoCarrinho + quantidade > produto.getQuantidadeEstoque()) {
                            JOptionPane.showMessageDialog(mainFrame,
                                    "Quantidade total solicitada: " + (jaNoCarrinho + quantidade)
                                            + ". Disponível: " + produto.getQuantidadeEstoque() + ".",
                                    "Estoque insuficiente", JOptionPane.WARNING_MESSAGE);
                            return;
                        }

                        ItemVenda item = ItemVenda.criarItemProduto(produto.getId(), produto.getNome(),
                                petId, petNome, produto.getPreco(), quantidade);
                        vendaView.adicionarItemNoCarrinho(item);
                    } finally {
                        vendaView.getBtnAdicionarProduto().setEnabled(true);
                    }
                }, error -> {
                    vendaView.getBtnAdicionarProduto().setEnabled(true);
                    JOptionPane.showMessageDialog(mainFrame, error.getMessage(),
                            "Não foi possível adicionar o produto", JOptionPane.ERROR_MESSAGE);
                });
    }

    private void adicionarServico() {
        Servico servico = vendaView.getServicoSelecionado();
        if (servico == null) {
            JOptionPane.showMessageDialog(mainFrame, "Nenhum serviço selecionado.", "Atenção", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pet pet = vendaView.getPetSelecionado();
        BigDecimal precoCalculado = (servicoService != null)
                ? servicoService.calcularPreco(servico, pet)
                : servico.getPrecoBase();

        int quantidade = vendaView.getQuantidadeServico();
        Long petId = pet != null ? pet.getId() : null;
        String petNome = pet != null ? pet.getNome() : null;

        ItemVenda item = ItemVenda.criarItemServico(servico.getId(), servico.getNome(), petId, petNome,
                precoCalculado, quantidade);
        vendaView.adicionarItemNoCarrinho(item);
    }

    private void mostrarDetalhesVenda() {
        Venda venda = vendaView.getVendaSelecionada();
        if (venda == null) {
            JOptionPane.showMessageDialog(mainFrame, "Selecione uma venda no histórico.",
                    "Nenhuma venda selecionada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder detalhes = new StringBuilder("Venda #").append(venda.getId()).append("\n\n");
        String cliente = venda.getClienteNome();
        if (cliente == null || cliente.isBlank()) {
            try {
                cliente = venda.getCliente() != null ? venda.getCliente().getNome() : null;
            } catch (Exception ignored) {
            }
        }
        detalhes.append("Tutor/Cliente: ").append(cliente != null ? cliente : "Consumidor não identificado").append("\n");
        String pets = venda.getPetsResumo();
        detalhes.append("Pet(s): ").append(pets.isBlank() ? "Não informado" : pets).append("\n");
        detalhes.append("Pagamento: ").append(venda.getFormaPagamento()).append("\n\nItens:\n");

        for (ItemVenda item : venda.getItens()) {
            String tipoTag = item.getTipoItem() == TipoItemVenda.SERVICO ? "[SERVIÇO] " : "[PRODUTO] ";
            detalhes.append(MessageFormat.format("• {0}{1} x {2} — {3}\n",
                    tipoTag, item.getQuantidade(), item.getProdutoNome(), MOEDA.format(item.getSubtotal())));
        }
        detalhes.append("\nTotal: ").append(MOEDA.format(venda.getTotal()));
        JOptionPane.showMessageDialog(mainFrame, detalhes.toString(), "Itens da Venda #" + venda.getId(),
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void carregarDados() {
        vendaView.setBusy(true);
        new SwingWorker<DadosCompletosVenda, Void>() {
            @Override
            protected DadosCompletosVenda doInBackground() {
                List<Produto> produtos = List.of();
                List<Venda> vendas = List.of();
                List<Cliente> clientes = List.of();
                List<Servico> servicos = List.of();

                try {
                    produtos = produtoService.listarTodos();
                } catch (Exception e) {
                    System.err.println("[PetPDV] Erro ao carregar produtos: " + e.getMessage());
                }

                try {
                    vendas = vendaService.listarTodas();
                } catch (Exception e) {
                    System.err.println("[PetPDV] Erro ao carregar vendas: " + e.getMessage());
                }

                try {
                    if (clienteService != null) {
                        clientes = clienteService.listarTodos();
                    }
                } catch (Exception e) {
                    System.err.println("[PetPDV] Erro ao carregar clientes: " + e.getMessage());
                }

                try {
                    if (servicoService != null) {
                        servicos = servicoService.listarTodos();
                    }
                } catch (Exception e) {
                    System.err.println("[PetPDV] Erro ao carregar serviços: " + e.getMessage());
                }

                return new DadosCompletosVenda(produtos, vendas, clientes, servicos);
            }

            @Override
            protected void done() {
                try {
                    DadosCompletosVenda dados = get();
                    vendaView.setProdutos(dados.produtos());
                    vendaView.setHistorico(dados.vendas());
                    vendaView.setTutores(dados.clientes());
                    vendaView.setServicos(dados.servicos());
                    onTutorMudou();
                    atualizarPrecoServicoPreview();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    mostrarErro("A leitura dos dados foi interrompida.", ex);
                } catch (ExecutionException ex) {
                    mostrarErro("Não foi possível carregar os dados da venda.", ex.getCause());
                } finally {
                    vendaView.setBusy(false);
                }
            }
        }.execute();
    }

    private void finalizarVenda() {
        Cliente tutor = vendaView.getTutorSelecionado();
        String nomeAvulso = vendaView.getClienteNomeAvulso();
        FormaPagamento formaPagamento = vendaView.getFormaPagamento();
        List<ItemVenda> itens = vendaView.getItensCarrinho();

        if (itens.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame, "O carrinho está vazio. Adicione produtos ou serviços antes de finalizar.",
                    "Carrinho Vazio", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            vendaView.setBusy(true);
            new SwingWorker<ResultadoVendaHibrida, Void>() {
                @Override
                protected ResultadoVendaHibrida doInBackground() {
                    Venda venda = vendaService.finalizarVendaHibrida(tutor, nomeAvulso, formaPagamento, itens);
                    produtoService.notificarObservadores();
                    List<Produto> produtos = produtoService.listarTodos();
                    List<Venda> vendas = vendaService.listarTodas();
                    return new ResultadoVendaHibrida(venda, produtos, vendas);
                }

                @Override
                protected void done() {
                    try {
                        ResultadoVendaHibrida resultado = get();
                        vendaView.limparCarrinho();
                        vendaView.setProdutos(resultado.produtos());
                        vendaView.setHistorico(resultado.vendas());
                        JOptionPane.showMessageDialog(mainFrame,
                                "Venda #" + resultado.venda().getId() + " finalizada com sucesso!\nTotal: "
                                        + MOEDA.format(resultado.venda().getTotal()),
                                "Venda Concluída", JOptionPane.INFORMATION_MESSAGE);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        mostrarErro("A finalização da venda foi interrompida.", ex);
                    } catch (ExecutionException ex) {
                        Throwable causa = ex.getCause();
                        String msgErro = causa != null ? causa.getMessage() : "Erro desconhecido.";

                        if (msgErro != null && msgErro.contains("Estoque insuficiente")) {
                            JOptionPane.showMessageDialog(mainFrame,
                                    msgErro + "\n\nAjuste o estoque no cadastro de produtos e tente novamente.",
                                    "Estoque insuficiente", JOptionPane.WARNING_MESSAGE);
                            SwingUtilities.invokeLater(VendaController.this::carregarDados);
                        } else {
                            mostrarErro("Não foi possível finalizar a venda.", causa);
                        }
                    } catch (Exception ex) {
                        mostrarErro("Erro ao atualizar interface após a venda.", ex);
                    } finally {
                        vendaView.setBusy(false);
                    }
                }
            }.execute();
        } catch (RuntimeException ex) {
            vendaView.setBusy(false);
            JOptionPane.showMessageDialog(mainFrame, ex.getMessage(), "Venda Inválida", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void mostrarErro(String mensagem, Throwable causa) {
        String detalhe = causa == null ? "Erro desconhecido." : causa.getMessage();
        JOptionPane.showMessageDialog(mainFrame, mensagem + "\n" + detalhe, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void onProdutosChanged(List<Produto> produtos) {
        vendaView.setProdutos(produtos);
    }

    @Override
    public void onClientesChanged(List<Cliente> clientes) {
        vendaView.setTutores(clientes);
        onTutorMudou();
    }

    @Override
    public void onServicosChanged(List<Servico> servicos) {
        vendaView.setServicos(servicos);
        atualizarPrecoServicoPreview();
    }

    private record DadosCompletosVenda(List<Produto> produtos, List<Venda> vendas,
                                      List<Cliente> clientes, List<Servico> servicos) {
    }

    private record ResultadoVendaHibrida(Venda venda, List<Produto> produtos, List<Venda> vendas) {
    }
}
