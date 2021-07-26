package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;

import java.util.Map;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Transaction;

@Dao
public abstract class VendaDao {

    @Insert
    abstract long insert(Venda venda);

    @Insert
    abstract void insert(ProdutoVenda produtoVenda);

    @Transaction
    public void insertVendaProduto(Venda venda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        ProdutoVenda produtoVenda = new ProdutoVenda();
        long idvenda = insert(venda);
        for (Map.Entry<Long, Produto> produto : produtos.entrySet()) {
            produtoVenda.setNome_produto(produto.getValue().getNome());
            produtoVenda.setQuantidade(precoTotalUnit.get(produto.getKey()).intValue() / produto.getValue().getPreco());
            produtoVenda.setPreco_total(precoTotalUnit.get(produto.getKey()).intValue());
            produtoVenda.setCodigo_Barra(produto.getValue().getCodigoBarra());
            produtoVenda.setIva(produto.getValue().isIva());
            produtoVenda.setIdvenda(idvenda);
            insert(produtoVenda);
        }
    }
}
