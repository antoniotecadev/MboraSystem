package com.yoga.mborasystem.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.Flowable;

@Dao
public abstract class VendaDao {

    @Insert
    abstract long insert(Venda venda);

    @Insert
    abstract void insert(ProdutoVenda produtoVenda);

    @Query("SELECT * FROM vendas WHERE estado = 4 ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaVazia();

    @Query("SELECT * FROM vendas WHERE estado != 3 ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVenda();

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaDiv();

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVenda(long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND divida > 0  ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaCliDiv(long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND codigo_qr LIKE '%' || :codQr || '%'")
    abstract Flowable<List<Venda>> searchVenda(String codQr);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 AND codigo_qr LIKE '%' || :codQr || '%'")
    abstract Flowable<List<Venda>> searchVendaDiv(String codQr);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND codigo_qr LIKE '%' || :codQr || '%' AND idclicant = :idcliente")
    abstract Flowable<List<Venda>> searchVenda(String codQr, long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 AND codigo_qr LIKE '%' || :codQr || '%' AND idclicant = :idcliente")
    abstract Flowable<List<Venda>> searchVendaCliDiv(String codQr, long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND data_cria LIKE '%' || :data || '%'")
    abstract Flowable<List<Venda>> getVenda(String data);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 AND data_cria LIKE '%' || :data || '%'")
    abstract Flowable<List<Venda>> getVendaDataDiv(String data);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND divida > 0 AND data_cria LIKE '%' || :data || '%'")
    abstract Flowable<List<Venda>> getVendaDataCliDiv(String data, long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND data_cria LIKE '%' || :data || '%'")
    abstract Flowable<List<Venda>> getVenda(String data, long idcliente);

    @Query("SELECT * FROM produtosvendas WHERE idvenda = :idvenda OR codigo_Barra = :codQr")
    abstract Flowable<List<ProdutoVenda>> getProdutoVenda(long idvenda, String codQr);

    @Query("UPDATE vendas SET divida = :divida WHERE id = :idvenda")
    abstract void setDivida(int divida, long idvenda);

    @Query("SELECT * FROM vendas WHERE estado = 3 ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaLixeira();

    @Query("SELECT * FROM vendas WHERE estado = 3 AND codigo_qr LIKE '%' || :codQr || '%'")
    abstract Flowable<List<Venda>> searchVendaLixeira(String codQr);

    @Delete
    abstract void deleteVenda(Venda venda);

    @Query("UPDATE vendas SET estado = :est, data_elimina = :data WHERE id = :id")
    public abstract void deleteLixeira(int est, String data, long id);

    @Query("DELETE FROM vendas WHERE estado = :estado")
    public abstract void deleteAllVendaLixeira(int estado);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaUsuario(long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaDataUsuario(String data, long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND codigo_qr LIKE '%' || :codQr || '%' ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaUsuario(String codQr, long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0  ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaDivUsuario(long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0 AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaDataDivUsuario(String data, long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0 AND codigo_qr LIKE '%' || :codQr || '%' ORDER BY id DESC")
    abstract Flowable<List<Venda>> getVendaDivUsuario(String codQr, long idusuario);

    @Query("UPDATE vendas SET estado = :est WHERE id = :id")
    public abstract void restaurarVendas(int est, long id);

    @Query("SELECT id, sum(preco_total) AS preco_total" +
            ", preco_fornecedor, iva, idvenda, nome_produto, data_cria, sum(quantidade) AS quantidade FROM produtosvendas WHERE data_cria = :data GROUP BY nome_produto ORDER BY sum(quantidade) DESC LIMIT 3")
    public abstract LiveData<List<ProdutoVenda>> getProdutoMaisVendido(String data);

    @Query("SELECT id, sum(preco_total) AS preco_total" +
            ", preco_fornecedor, iva, idvenda, nome_produto, data_cria, sum(quantidade) AS quantidade FROM produtosvendas WHERE data_cria = :data GROUP BY nome_produto ORDER BY sum(quantidade) ASC LIMIT 3")
    public abstract LiveData<List<ProdutoVenda>> getProdutoMenosVendido(String data);

    @Transaction
    public void insertVendaProduto(Venda venda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        ProdutoVenda produtoVenda = new ProdutoVenda();
        long idvenda = insert(venda);
        for (Map.Entry<Long, Produto> produto : produtos.entrySet()) {
            produtoVenda.setNome_produto(produto.getValue().getNome());
            produtoVenda.setQuantidade(Objects.requireNonNull(precoTotalUnit.get(produto.getKey())) / produto.getValue().getPreco());
            produtoVenda.setPreco_total(Objects.requireNonNull(precoTotalUnit.get(produto.getKey())));
            produtoVenda.setCodigo_Barra(venda.getCodigo_qr());
            produtoVenda.setPreco_fornecedor(produto.getValue().getPrecofornecedor());
            produtoVenda.setIva(produto.getValue().isIva());
            produtoVenda.setIdvenda(idvenda);
            produtoVenda.setData_cria(venda.getData_cria());
            insert(produtoVenda);
        }
    }

    public Flowable<List<Venda>> getVendas(long idcliente, boolean isdivida, long idusuario) {
        if (idusuario == 0) {
            if (idcliente == 0 && !isdivida) {
                return getVenda();
            } else if (idcliente > 0 && !isdivida) {
                return getVenda(idcliente);
            } else if (idcliente == 0) {
                return getVendaDiv();
            } else if (idcliente > 0) {
                return getVendaCliDiv(idcliente);
            }
        } else if (isdivida) {
            return getVendaDivUsuario(idusuario);
        } else {
            return getVendaUsuario(idusuario);
        }
        return getVendaVazia();
    }

    public Flowable<List<Venda>> getVendas(String data, long idcliente, boolean isDivida, long idusuario) {
        if (idusuario == 0) {
            if (idcliente == 0 && !isDivida) {
                return getVenda(data);
            } else if (idcliente > 0 && !isDivida) {
                return getVenda(data, idcliente);
            } else if (idcliente == 0) {
                return getVendaDataDiv(data);
            } else if (idcliente > 0) {
                return getVendaDataCliDiv(data, idcliente);
            }
        } else if (isDivida) {
            return getVendaDataDivUsuario(data, idusuario);
        } else {
            return getVendaDataUsuario(data, idusuario);
        }
        return getVendaVazia();
    }

    public Flowable<List<Venda>> getSearchVendas(String codQr, long idcliente, boolean isDivida, long idusuario) {
        if (idusuario == 0) {
            if (idcliente == 0 && !isDivida) {
                return searchVenda(codQr);
            } else if (idcliente > 0 && !isDivida) {
                return searchVenda(codQr, idcliente);
            } else if (idcliente == 0) {
                return searchVendaDiv(codQr);
            } else if (idcliente > 0) {
                return searchVendaCliDiv(codQr, idcliente);
            }
        } else if (isDivida) {
            return getVendaDivUsuario(codQr, idusuario);
        } else {
            return getVendaUsuario(codQr, idusuario);
        }
        return getVendaVazia();
    }

    public Flowable<List<Venda>> getVendasLixeira() {
        return getVendaLixeira();
    }

    public Flowable<List<Venda>> searchVendasLixeira(String codQr) {
        return searchVendaLixeira(codQr);
    }

    public void deleteVendas(Venda venda) {
        deleteVenda(venda);
    }

    public void restaurarVenda(int estado, long idvenda) {
        restaurarVendas(estado, idvenda);
    }

    @Transaction
    public void insertVenda(List<String> vendas) {
        Venda venda = new Venda();
        for (String vd : vendas) {
            String[] vend = vd.split(",");
            venda.setNome_cliente(vend[0]);
            venda.setCodigo_qr(vend[1]);
            venda.setQuantidade(Integer.parseInt(vend[2]));
            venda.setTotal_venda(Integer.parseInt(vend[3]));
            venda.setTotal_desconto(Integer.parseInt(vend[4]));
            venda.setTotal_desconto(Integer.parseInt(vend[5]));
            venda.setValor_pago(Integer.parseInt(vend[6]));
            venda.setDivida(Integer.parseInt(vend[7]));
            venda.setValor_base(Integer.parseInt(vend[8]));
            venda.setValor_iva(Integer.parseInt(vend[9]));
            venda.setPagamento(vend[10]);
            venda.setData_cria(vend[11]);
            venda.setIdoperador(Integer.parseInt(vend[12]));
            venda.setIdclicant(Integer.parseInt(vend[13]));
            venda.setData_elimina(vend[14]);
            venda.setEstado(Integer.parseInt(vend[15]));
            insert(venda);
        }
    }

    public Flowable<List<ProdutoVenda>> getProdutosVenda(long idvenda, String codQr) {
        return getProdutoVenda(idvenda, codQr);
    }

    public void liquidardivida(int divida, long idvenda) {
        setDivida(divida, idvenda);
    }

}
