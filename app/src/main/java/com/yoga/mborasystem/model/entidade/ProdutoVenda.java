package com.yoga.mborasystem.model.entidade;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "produtosvendas", indices = {@Index(value = {"idvenda"})},
        foreignKeys = @ForeignKey(entity = Venda.class, parentColumns = "id", childColumns = "idvenda", onDelete = CASCADE))
public class ProdutoVenda {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nome_produto;
    private String tipo;
    private String unidade;
    private String codigoMotivoIsencao;
    private int preco_total;
    private int quantidade;
    private String codigo_Barra;
    private int preco_fornecedor;
    private boolean iva;
    private Integer percentagemIva;
    private long idvenda;
    private String data_cria;


    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getNome_produto() {
        return nome_produto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getCodigoMotivoIsencao() {
        return codigoMotivoIsencao;
    }

    public void setCodigoMotivoIsencao(String codigoMotivoIsencao) {
        this.codigoMotivoIsencao = codigoMotivoIsencao;
    }

    public Integer getPercentagemIva() {
        return percentagemIva;
    }

    public void setPercentagemIva(Integer percentagemIva) {
        this.percentagemIva = percentagemIva;
    }

    public void setNome_produto(String nome_produto) {
        this.nome_produto = nome_produto;
    }

    public int getPreco_total() {
        return preco_total;
    }

    public void setPreco_total(int preco_total) {
        this.preco_total = preco_total;
    }

    public int getQuantidade() {
        return quantidade;
    }
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getCodigo_Barra() {
        return codigo_Barra;
    }

    public void setCodigo_Barra(String codigo_Barra) {
        this.codigo_Barra = codigo_Barra;
    }

    public boolean isIva() {
        return iva;
    }
    public void setIva(boolean iva) {
        this.iva = iva;
    }
    public long getIdvenda() {
        return idvenda;
    }
    public void setIdvenda(long idvenda) {
        this.idvenda = idvenda;
    }

    public int getPreco_fornecedor() {
        return preco_fornecedor;
    }

    public void setPreco_fornecedor(int preco_fornecedor) {
        this.preco_fornecedor = preco_fornecedor;
    }

    public String getData_cria() {
        return data_cria;
    }

    public void setData_cria(String data_cria) {
        this.data_cria = data_cria;
    }
}
