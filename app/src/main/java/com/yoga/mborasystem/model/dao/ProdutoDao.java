package com.yoga.mborasystem.model.dao;

import androidx.annotation.Keep;
import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yoga.mborasystem.model.entidade.Produto;

import java.util.List;

@Dao
public interface ProdutoDao {

    @Insert
    void insert(Produto produto);

    @Query("UPDATE produtos SET nome = :nome, tipo = :tipo, unidade = :unidade, preco = :preco, precofornecedor = :precofor, quantidade = :quanti, codigoBarra = :codbar, iva = :iva, percentagemIva = :percentagemIva, codigoMotivoIsencao = :codigoMotivoIsencao, estado = :est, stock = :stock, data_modifica = :data WHERE id = :id")
    void update(String nome, String tipo, String unidade, int preco, int precofor, int quanti, String codbar, boolean iva, int percentagemIva, String codigoMotivoIsencao, int est, boolean stock, String data, long id);

    @Delete
    void delete(Produto produto);

    @Query("UPDATE produtos SET estado = :est, data_elimina = :data WHERE id = :id")
    void deleteLixeira(int est, String data, long id);

    @Query("DELETE FROM produtos WHERE estado = :estado")
    void deleteAllProdutoLixeira(int estado);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcategoria AND estado NOT IN(:priEst, :segEst) ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getProdutos(long idcategoria, int priEst, int segEst);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 ORDER BY produtos.id DESC")
    List<Produto> getProdutosExport(long idcat) throws Exception;

    @Query("SELECT * FROM produtos WHERE estado = 3 ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getProdutosLixeira();

    @Query("SELECT distinct(pdv.id), pdv.preco_fornecedor, pdv.data_cria FROM produtosvendas AS pdv" +
            " INNER JOIN vendas AS vd ON pdv.idvenda = vd.id WHERE vd.referenciaNC = ''" +
            "AND vd.data_cria  LIKE '%' || :ano GROUP BY pdv.id")
    LiveData<List<Fornecedor>> getPrecoFornecedor(String ano);

    @Keep
    class Fornecedor {
        public int id;
        public long preco_fornecedor;
    }

    @Query("SELECT COUNT(id) FROM produtos  WHERE idcategoria = :idcategoria AND estado != 3")
    LiveData<Long> getQuantidadeProduto(long idcategoria);

    @Query("SELECT COUNT(id) FROM produtos  WHERE estado = 3")
    LiveData<Long> getQuantidadeProdutoLixeira();

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcategoria AND estado NOT IN(:priEst, :segEst) AND (nome LIKE '%' || :produto || '%' OR codigoBarra LIKE '%' || :produto || '%') ")
    PagingSource<Integer, Produto> searchProdutos(long idcategoria, String produto, int priEst, int segEst);

    @Query("SELECT * FROM produtos WHERE estado = 3 AND (nome LIKE '%' || :produto || '%' OR codigoBarra LIKE '%' || :produto || '%') ")
    PagingSource<Integer, Produto> searchProdutosLixeira(String produto);

    @Query("UPDATE produtos SET estado = :est WHERE id = :id")
    void restaurarProduto(int est, long id);

    @Query("UPDATE produtos SET estado = :est")
    void restaurarTodosProdutos(int est);

    @Query("SELECT * FROM produtos WHERE estado NOT IN(2,3) ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getTodosProdutos();

    @Query("SELECT * FROM produtos WHERE estado NOT IN(2,3) AND (nome LIKE '%' || :produto || '%' OR codigoBarra LIKE '%' || :produto || '%') ")
    PagingSource<Integer, Produto> searchTodosProdutos(String produto);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND codigoBarra = :codigoBar ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String codigoBar);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String idprodnome, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String codigoBar, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String codigoBar, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutos(long idcat, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND codigoBarra = :codigoBar AND estado = :estadoProd ORDER BY produtos.id DESC")
    PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String codigoBar, int estadoProd);

    @Query("SELECT * FROM produtos WHERE id IN (:idproduto) AND estado = 1")
    PagingSource<Integer, Produto> getProdutosRascunho(List<Long> idproduto);
}
