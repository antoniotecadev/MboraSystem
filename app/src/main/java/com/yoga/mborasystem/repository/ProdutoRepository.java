package com.yoga.mborasystem.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ProdutoDao;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.Ultilitario;

import java.lang.ref.WeakReference;
import java.util.List;

public class ProdutoRepository {
    private final ProdutoDao produtoDao;

    public ProdutoRepository(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
        produtoDao = appDataBase.produtoDao();
    }

    public void insert(Produto pd) {
        produtoDao.insert(pd);
    }

    public void update(Produto pd) {
        produtoDao.update(pd.getNome(), pd.getTipo(), pd.getUnidade(), pd.getPreco(), pd.getPrecofornecedor(), pd.getQuantidade(), pd.getCodigoBarra(), pd.isIva(), pd.getPercentagemIva(), pd.getCodigoMotivoIsencao(), pd.getEstado(), pd.isStock(), pd.getData_modifica(), pd.getId());
    }

    public void delete(Produto pd, boolean lx, boolean eliminarTodasLixeira) {
        if (eliminarTodasLixeira)
            produtoDao.deleteAllProdutoLixeira(3);
        else {
            if (lx && (pd != null))
                produtoDao.deleteLixeira(pd.getEstado(), Ultilitario.monthInglesFrances(pd.getData_elimina()), pd.getId());
            else
                produtoDao.delete(pd);
        }
    }

    public LiveData<Long> getQuantidadeProduto(long idcategoria, boolean isLixeira) {
        if (isLixeira)
            return produtoDao.getQuantidadeProdutoLixeira();
        else
            return produtoDao.getQuantidadeProduto(idcategoria);
    }

    public PagingSource<Integer, Produto> getProdutos(long idcategoria, boolean isLixeira, boolean isSearch, String produto, boolean isFactura, boolean isTodosProdutos) {
        if (isLixeira) {
            if (isSearch)
                return produtoDao.searchProdutosLixeira(produto);
            else
                return produtoDao.getProdutosLixeira();
        } else {
            if (!isFactura) {
                if (isSearch)
                    return produtoDao.searchProdutos(idcategoria, produto, 3, 3);
                else
                    return produtoDao.getProdutos(idcategoria, 3, 3);
            } else {
                if (isSearch)
                    if (isTodosProdutos)
                        return produtoDao.searchTodosProdutos(produto);
                    else
                        return produtoDao.searchProdutos(idcategoria, produto, 2, 3);
                else {
                    if (isTodosProdutos)
                        return produtoDao.getTodosProdutos();
                    else
                        return produtoDao.getProdutos(idcategoria, 2, 3);
                }
            }
        }
    }

    public PagingSource<Integer, Produto> getProdutosRascunho(List<Long> produtoRascunho) {
        return produtoDao.getProdutosRascunho(produtoRascunho);
    }

    public List<Produto> getProdutosExport(long idcat) throws Exception {
        return produtoDao.getProdutosExport(idcat);
    }

    public LiveData<Long> getProdutos() {
        return produtoDao.getProdutos();
    }

    public LiveData<List<Produto>> getPrecoFornecedor() {
        return produtoDao.getPrecoFornecedor();
    }

    public void restaurarProduto(int estado, long idproduto, boolean todosProdutoss) {
        if (todosProdutoss)
            produtoDao.restaurarTodosProdutos(estado);
        else
            produtoDao.restaurarProduto(estado, idproduto);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar, precoMin, precoMax, estadoProd);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome) {
        return produtoDao.getFilterProdutos(idcat, idprodnome);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutos(idcat, precoMin, precoMax);
    }

    public PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String codigoBar) {
        return produtoDao.getFilterProdutosCodBar(idcat, codigoBar);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, estadoProd);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar, precoMin, precoMax);
    }

    public PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String idprodnome, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutosCodBar(idcat, idprodnome, precoMin, precoMax, estadoProd);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar, estadoProd);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String codigoBar, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, codigoBar, precoMin, precoMax, estadoProd);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, precoMin, precoMax);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, String codigoBar) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, String idprodnome, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, estadoProd);
    }

    public PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String codigoBar, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutosCodBar(idcat, codigoBar, precoMin, precoMax);
    }

    public PagingSource<Integer, Produto> getFilterProdutos(long idcat, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, precoMin, precoMax, estadoProd);
    }

    public PagingSource<Integer, Produto> getFilterProdutosCodBar(long idcat, String codigoBar, int estadoProd) {
        return produtoDao.getFilterProdutosCodBar(idcat, codigoBar, estadoProd);
    }

    public void importarProdutos(List<String> produtos, boolean vemCat, Long idcategoria) {
        Produto produto = new Produto();
        for (String pt : produtos) {
            String[] prod = pt.split(",");
            produto.setId(0);
            produto.setNome(prod[0]);
            produto.setTipo(prod[1]);
            produto.setUnidade(prod[2]);
            produto.setCodigoMotivoIsencao(prod[3]);
            produto.setPercentagemIva(Integer.valueOf(prod[4]));
            produto.setPreco(Integer.parseInt(prod[5]));
            produto.setPrecofornecedor(Integer.parseInt(prod[6]));
            produto.setQuantidade(Integer.parseInt(prod[7]));
            produto.setCodigoBarra(prod[8]);
            produto.setIva(Boolean.parseBoolean(prod[9]));
            produto.setEstado(Integer.parseInt(prod[10]));
            produto.setStock(Boolean.parseBoolean(prod[11]));
            produto.setIdcategoria(vemCat ? Long.parseLong(prod[12]) : idcategoria);
            produto.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
            produtoDao.insert(produto);
        }
    }
}
