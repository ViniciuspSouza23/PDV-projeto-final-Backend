package br.gov.sp.cps.fatec.pdv.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_venda")
public class ItemVenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private Venda venda;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_item", length = 20)
    private TipoItemVenda tipoItem = TipoItemVenda.PRODUTO;

    @Column(name = "produto_id", nullable = true)
    private Long produtoId;

    @Column(name = "servico_id", nullable = true)
    private Long servicoId;

    @Column(name = "pet_id", nullable = true)
    private Long petId;

    @Column(name = "pet_nome", length = 80)
    private String petNome;

    @Column(name = "produto_nome", nullable = false, length = 150)
    private String produtoNome;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    public ItemVenda() {
    }

    public ItemVenda(Long produtoId, Integer quantidade) {
        this.tipoItem = TipoItemVenda.PRODUTO;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
    }

    public static ItemVenda criarItemProduto(Long produtoId, String nome, BigDecimal precoUnitario, int quantidade) {
        ItemVenda item = new ItemVenda();
        item.setTipoItem(TipoItemVenda.PRODUTO);
        item.setProdutoId(produtoId);
        item.setProdutoNome(nome);
        item.setPrecoUnitario(precoUnitario);
        item.setQuantidade(quantidade);
        item.setSubtotal(precoUnitario.multiply(BigDecimal.valueOf(quantidade)));
        return item;
    }

    public static ItemVenda criarItemServico(Long servicoId, String nomeServico, Long petId, String petNome,
                                             BigDecimal precoCalculado, int quantidade) {
        ItemVenda item = new ItemVenda();
        item.setTipoItem(TipoItemVenda.SERVICO);
        item.setServicoId(servicoId);
        item.setPetId(petId);
        item.setPetNome(petNome);
        String descricao = petNome != null && !petNome.isBlank()
                ? nomeServico + " [Pet: " + petNome + "]"
                : nomeServico;
        item.setProdutoNome(descricao);
        item.setPrecoUnitario(precoCalculado);
        item.setQuantidade(quantidade);
        item.setSubtotal(precoCalculado.multiply(BigDecimal.valueOf(quantidade)));
        return item;
    }

    public Long getId() {
        return id;
    }

    public Venda getVenda() {
        return venda;
    }

    public TipoItemVenda getTipoItem() {
        return tipoItem != null ? tipoItem : TipoItemVenda.PRODUTO;
    }

    public void setTipoItem(TipoItemVenda tipoItem) {
        this.tipoItem = tipoItem;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Long getServicoId() {
        return servicoId;
    }

    public void setServicoId(Long servicoId) {
        this.servicoId = servicoId;
    }

    public Long getPetId() {
        return petId;
    }

    public void setPetId(Long petId) {
        this.petId = petId;
    }

    public String getPetNome() {
        return petNome;
    }

    public void setPetNome(String petNome) {
        this.petNome = petNome;
    }

    public String getProdutoNome() {
        return produtoNome;
    }

    public void setProdutoNome(String produtoNome) {
        this.produtoNome = produtoNome;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }
}
