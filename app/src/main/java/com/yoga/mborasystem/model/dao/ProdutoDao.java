package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.Produto;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Flowable;

@Dao
public interface ProdutoDao {

    @Insert
    void insert(Produto produto);

    @Query("UPDATE produtos SET nome = :nome, preco = :preco, precofornecedor = :precofor, quantidade = :quanti, codigoBarra = :codbar, iva = :iva,estado = :est, data_modifica = :data WHERE id = :id")
    void update(String nome, int preco, int precofor, int quanti, String codbar, boolean iva, int est, String data, long id);

    @Delete
    void delete(Produto produto);

    @Query("UPDATE produtos SET estado = :est, data_elimina = :data WHERE id = :id")
    void deleteLixeira(int est, String data, long id);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getProdutos(long idcat);

    @Query("SELECT * FROM produtos WHERE estado = 3 ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getProdutosLixeira();

    @Query("SELECT count(id) FROM produtos WHERE estado != 3")
    LiveData<Long> getProdutos();

    @Query("SELECT * FROM produtos WHERE estado != 3")
    LiveData<List<Produto>> getPrecoFornecedor();

//    @Query("SELECT COUNT(id) FROM produtos  WHERE idcategoria = :idcategoria AND estado = 1")
//    LiveData<Long> getQuantidadeProduto(long idcategoria);

    @Query("SELECT * FROM produtos WHERE estado != 3 AND (nome LIKE '%' || :search || '%' OR codigoBarra LIKE '%' || :search || '%') ")
    Flowable<List<Produto>> searchProdutos(String search);

    @Query("SELECT * FROM produtos WHERE estado = 3 AND (nome LIKE '%' || :search || '%' OR codigoBarra LIKE '%' || :search || '%') ")
    Flowable<List<Produto>> searchProdutosLixeira(String search);

    @Query("UPDATE produtos SET estado = :est WHERE id = :id")
    void restaurarCategoria(int est, long id);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND codigoBarra = :codigoBar ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String codigoBar);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String idprodnome, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String codigoBar, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND codigoBarra = :codigoBar ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (id = :idprodnome OR nome LIKE '%' ||  :idprodnome || '%') AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND estado != 3 AND codigoBarra = :codigoBar AND (preco BETWEEN :precoMin AND :precoMax) ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String codigoBar, int precoMin, int precoMax);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND (preco BETWEEN :precoMin AND :precoMax) AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutos(long idcat, int precoMin, int precoMax, int estadoProd);

    @Query("SELECT * FROM produtos WHERE idcategoria = :idcat AND codigoBarra = :codigoBar AND estado = :estadoProd ORDER BY produtos.id DESC")
    Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String codigoBar, int estadoProd);
}
