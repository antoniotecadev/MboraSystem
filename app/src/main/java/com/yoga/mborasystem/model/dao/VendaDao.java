package com.yoga.mborasystem.model.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Transaction;

import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.reactivex.rxjava3.core.Maybe;

@Dao
public abstract class VendaDao {

    @Insert
    abstract long insert(Venda venda);

    @Insert
    abstract void insert(ProdutoVenda produtoVenda);

    @Query("SELECT * FROM vendas WHERE estado = 4 ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaVazia();

    @Query("SELECT COUNT(id) FROM vendas WHERE estado = 5 ORDER BY id DESC")
    abstract LiveData<Long> getVendaVaziaCount();

    @Query("SELECT * FROM vendas WHERE estado != 3 ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVenda();

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaDiv();

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVenda(long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND divida > 0  ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaCliDiv(long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND codigo_qr LIKE '%' || :referencia || '%'")
    abstract PagingSource<Integer, Venda> searchVenda(String referencia);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 AND codigo_qr LIKE '%' || :referencia || '%'")
    abstract PagingSource<Integer, Venda> searchVendaDiv(String referencia);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND codigo_qr LIKE '%' || :referencia || '%' AND idclicant = :idcliente")
    abstract PagingSource<Integer, Venda> searchVenda(String referencia, long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 AND codigo_qr LIKE '%' || :referencia || '%' AND idclicant = :idcliente")
    abstract PagingSource<Integer, Venda> searchVendaCliDiv(String referencia, long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND data_cria LIKE '%' || :data || '%'")
    abstract PagingSource<Integer, Venda> getVenda(String data);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND data_cria LIKE '%' || :data || '%'")
    abstract LiveData<Long> getVendaCount(String data);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3")
    abstract LiveData<Long> getVendaCount();

    @Query("SELECT * FROM vendas WHERE estado != 3 AND data_cria LIKE '%' || :data || '%'")
    public abstract List<Venda> getVendaExport(String data);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND divida > 0 AND data_cria LIKE '%' || :data || '%'")
    abstract PagingSource<Integer, Venda> getVendaDataDiv(String data);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND divida > 0 AND data_cria LIKE '%' || :data || '%'")
    abstract LiveData<Long> getVendaDataDivCount(String data);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND divida > 0")
    abstract LiveData<Long> getVendaDivCount();

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND divida > 0 AND data_cria LIKE '%' || :data || '%'")
    abstract PagingSource<Integer, Venda> getVendaDataCliDiv(String data, long idcliente);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND divida > 0 AND data_cria LIKE '%' || :data || '%'")
    abstract LiveData<Long> getVendaDataCliDivCount(String data, long idcliente);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND divida > 0")
    abstract LiveData<Long> getVendaCliDivCount(long idcliente);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND data_cria LIKE '%' || :data || '%'")
    abstract PagingSource<Integer, Venda> getVenda(String data, long idcliente);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idclicant = :idcliente AND data_cria LIKE '%' || :data || '%'")
    abstract LiveData<Long> getVendaCount(String data, long idcliente);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idclicant = :idcliente")
    abstract LiveData<Long> getVendaCount(long idcliente);

    @Query("SELECT * FROM produtosvendas WHERE idvenda = :idvenda OR codigo_Barra = :codQr")
    abstract Maybe<List<ProdutoVenda>> getProdutoVenda(long idvenda, String codQr);

    @Query("SELECT * FROM produtosvendas WHERE data_cria LIKE '%' || :data || '%'")
    public abstract Maybe<List<ProdutoVenda>> getProdutoVenda(String data);

    @Query("SELECT DISTINCT(nome_produto), * FROM produtosvendas WHERE idvenda IN (:idvenda)")
    public abstract Maybe<List<ProdutoVenda>> getProdutoVendaSaft(List<Long> idvenda);

    @Query("UPDATE vendas SET divida = :divida WHERE id = :idvenda")
    abstract void setDivida(int divida, long idvenda);

    @Query("SELECT * FROM vendas WHERE estado = 3 ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaLixeira();

    @Query("SELECT * FROM vendas WHERE estado = 3 AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    public abstract PagingSource<Integer, Venda> getVendasLixeira(String data);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado = 3 AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    public abstract LiveData<Long> getVendasLixeiraCount(String data);

    @Query("SELECT * FROM vendas WHERE estado = 3 AND codigo_qr LIKE '%' || :referencia || '%'")
    abstract PagingSource<Integer, Venda> searchVendaLixeira(String referencia);

    @Delete
    abstract void deleteVenda(Venda venda);

    @Query("UPDATE vendas SET estado = :est, data_elimina = :data WHERE id = :id")
    public abstract void deleteLixeira(int est, String data, long id);

    @Query("DELETE FROM vendas WHERE estado = :estado")
    public abstract void deleteAllVendaLixeira(int estado);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaUsuario(long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaDataUsuario(String data, long idusuario);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    abstract LiveData<Long> getVendaDataUsuarioCount(String data, long idusuario);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idoperador = :idusuario")
    abstract LiveData<Long> getVendaUsuarioCount(long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND codigo_qr LIKE '%' || :referencia || '%' ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaUsuario(String referencia, long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0  ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaDivUsuario(long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0 AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaDataDivUsuario(String data, long idusuario);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0 AND data_cria LIKE '%' || :data || '%' ORDER BY id DESC")
    abstract LiveData<Long> getVendaDataDivUsuarioCount(String data, long idusuario);

    @Query("SELECT COUNT(id) FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0")
    abstract LiveData<Long> getVendaDivUsuarioCount(long idusuario);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND idoperador = :idusuario AND divida > 0 AND codigo_qr LIKE '%' || :referencia || '%' ORDER BY id DESC")
    abstract PagingSource<Integer, Venda> getVendaDivUsuario(String referencia, long idusuario);

    @Query("UPDATE vendas SET estado = :est WHERE id = :id")
    public abstract void restaurarVendas(int est, long id);

    @Query("UPDATE vendas SET estado = :est")
    public abstract void restaurarTodasVendas(int est);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, sum(preco_total) AS preco_total" +
            ", preco_fornecedor, iva, idvenda, nome_produto, data_cria, sum(quantidade) AS quantidade FROM produtosvendas WHERE data_cria = :data GROUP BY nome_produto ORDER BY sum(quantidade) DESC LIMIT 3")
    public abstract LiveData<List<ProdutoVenda>> getProdutoMaisVendido(String data);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT id, sum(preco_total) AS preco_total" +
            ", preco_fornecedor, iva, idvenda, nome_produto, data_cria, sum(quantidade) AS quantidade FROM produtosvendas WHERE data_cria = :data GROUP BY nome_produto ORDER BY sum(quantidade) ASC LIMIT 3")
    public abstract LiveData<List<ProdutoVenda>> getProdutoMenosVendido(String data);

    @Query("UPDATE vendas SET codigo_qr = :ref WHERE id = :idvenda")
    public abstract void updateReferencia(String ref, long idvenda);

    @Query("SELECT COUNT(id) FROM vendas  WHERE estado = 3")
    public abstract LiveData<Long> getVendasLixeiraCount();

    @Query("SELECT * FROM vendas WHERE estado != 3 ORDER BY id DESC")
    public abstract Maybe<List<Venda>> getVendasDashboard();

    @Query("SELECT * FROM vendas WHERE estado != 3 AND data_cria LIKE '%' || :data || '%'")
    public abstract Maybe<List<Venda>> getVendasDashboardReport(String data);

    @Query("UPDATE produtos SET quantidade = :quantidade WHERE id = :idproduto")
    public abstract void actualizarQuantidadeProduto(int quantidade, long idproduto);

    @Query("SELECT * FROM vendas WHERE estado != 3 AND (data_cria BETWEEN :dataInicio AND :dataFim) ORDER BY id DESC")
    public abstract Maybe<List<Venda>> getVendaSaft(String dataInicio, String dataFim);

    @Transaction
    public long insertVendaProduto(Venda venda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        ProdutoVenda produtoVenda = new ProdutoVenda();
        long idvenda = insert(venda);
        updateReferencia(venda.getCodigo_qr() + "/" + idvenda, idvenda);
        for (Map.Entry<Long, Produto> produto : produtos.entrySet()) {
            int quantidade = Objects.requireNonNull(precoTotalUnit.get(produto.getKey())) / produto.getValue().getPreco();
            produtoVenda.setId(0);
            produtoVenda.setNome_produto(produto.getValue().getNome());
            produtoVenda.setTipo(produto.getValue().getTipo());
            produtoVenda.setUnidade(produto.getValue().getUnidade());
            produtoVenda.setCodigoMotivoIsencao(produto.getValue().getCodigoMotivoIsencao());
            produtoVenda.setQuantidade(quantidade);
            produtoVenda.setPreco_total(Objects.requireNonNull(precoTotalUnit.get(produto.getKey())));
            produtoVenda.setCodigo_Barra(produto.getValue().getCodigoBarra());
            produtoVenda.setPreco_fornecedor(produto.getValue().getPrecofornecedor());
            produtoVenda.setIva(produto.getValue().isIva());
            produtoVenda.setPercentagemIva(produto.getValue().getPercentagemIva());
            produtoVenda.setIdvenda(idvenda);
            produtoVenda.setData_cria(venda.getData_cria());
            insert(produtoVenda);
            if (produto.getValue().isStock())
                actualizarQuantidadeProduto(produto.getValue().getQuantidade() - quantidade, produto.getValue().getId());
        }
        return idvenda;
    }

    public PagingSource<Integer, Venda> getVendas(long idcliente, boolean isdivida, long idusuario) {
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

    public PagingSource<Integer, Venda> getVendas(String data, long idcliente, boolean isDivida, long idusuario) {
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

    public PagingSource<Integer, Venda> getSearchVendas(String referencia, long idcliente, boolean isDivida, long idusuario) {
        if (idusuario == 0) {
            if (idcliente == 0 && !isDivida) {
                return searchVenda(referencia);
            } else if (idcliente > 0 && !isDivida) {
                return searchVenda(referencia, idcliente);
            } else if (idcliente == 0) {
                return searchVendaDiv(referencia);
            } else if (idcliente > 0) {
                return searchVendaCliDiv(referencia, idcliente);
            }
        } else if (isDivida) {
            return getVendaDivUsuario(referencia, idusuario);
        } else {
            return getVendaUsuario(referencia, idusuario);
        }
        return getVendaVazia();
    }

    public LiveData<Long> getVendasCount(String data, long idcliente, boolean isDivida, long idusuario) {
        if (idusuario == 0) {
            if (idcliente == 0 && !isDivida) {
                return getVendaCount(data);
            } else if (idcliente > 0 && !isDivida) {
                return getVendaCount(data, idcliente);
            } else if (idcliente == 0) {
                return getVendaDataDivCount(data);
            } else if (idcliente > 0) {
                return getVendaDataCliDivCount(data, idcliente);
            }
        } else if (isDivida) {
            return getVendaDataDivUsuarioCount(data, idusuario);
        } else {
            return getVendaDataUsuarioCount(data, idusuario);
        }
        return getVendaVaziaCount();
    }

    public LiveData<Long> getVendasCount(long idcliente, boolean isDivida, long idusuario) {
        if (idusuario == 0) {
            if (idcliente == 0 && !isDivida) {
                return getVendaCount();
            } else if (idcliente > 0 && !isDivida) {
                return getVendaCount(idcliente);
            } else if (idcliente == 0) {
                return getVendaDivCount();
            } else if (idcliente > 0) {
                return getVendaCliDivCount(idcliente);
            }
        } else if (isDivida) {
            return getVendaDivUsuarioCount(idusuario);
        } else {
            return getVendaUsuarioCount(idusuario);
        }
        return getVendaVaziaCount();
    }

    public PagingSource<Integer, Venda> getVendasLixeira() {
        return getVendaLixeira();
    }

    public PagingSource<Integer, Venda> searchVendasLixeira(String referencia) {
        return searchVendaLixeira(referencia);
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
            venda.setId(0);
            venda.setNome_cliente(vend[0]);
            venda.setCodigo_qr(vend[1]);
            venda.setQuantidade(Integer.parseInt(vend[2]));
            venda.setTotal_venda(Integer.parseInt(vend[3]));
            venda.setDesconto(Integer.parseInt(vend[4]));
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

    public Maybe<List<ProdutoVenda>> getProdutosVenda(long idvenda, String codQr) {
        return getProdutoVenda(idvenda, codQr);
    }

    public void liquidardivida(int divida, long idvenda) {
        setDivida(divida, idvenda);
    }

}
