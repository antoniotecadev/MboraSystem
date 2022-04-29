package com.yoga.mborasystem.repository;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;

import androidx.lifecycle.LiveData;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ProdutoDao;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.Ultilitario;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Flowable;

public class ProdutoRepository {
    private boolean isErro;
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
        produtoDao.update(pd.getNome(), pd.getPreco(), pd.getPrecofornecedor(), pd.getQuantidade(), pd.getCodigoBarra(), pd.isIva(), pd.getEstado(), pd.getData_modifica(), pd.getId());
    }

    public void delete(Produto pd, boolean lx, boolean eliminarTodasLixeira) {
        if (eliminarTodasLixeira) {
            produtoDao.deleteAllProdutoLixeira(3);
        } else {
            if (lx && (pd != null)) {
                produtoDao.deleteLixeira(pd.getEstado(), Ultilitario.monthInglesFrances(pd.getData_elimina()), pd.getId());
            } else {
                produtoDao.delete(pd);
            }
        }
    }

    public Flowable<List<Produto>> getProdutos(long idcat, boolean isLixeira) {
        if (isLixeira) {
            return produtoDao.getProdutosLixeira();
        } else {
            return produtoDao.getProdutos(idcat);
        }
    }

    public LiveData<Long> getProdutos() {
        return produtoDao.getProdutos();
    }

    public LiveData<List<Produto>> getPrecoFornecedor() {
        return produtoDao.getPrecoFornecedor();
    }

    public Flowable<List<Produto>> searchProdutos(String produto, boolean isLixeira) {
        if (isLixeira) {
            return produtoDao.searchProdutosLixeira(produto);
        } else {
            return produtoDao.searchProdutos(produto);
        }
    }

    public void restaurarProduto(int estado, long idproduto, boolean todosProdutoss) {
        if (todosProdutoss) {
            produtoDao.restaurarTodosProdutos(estado);
        } else {
            produtoDao.restaurarProduto(estado, idproduto);
        }
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar, precoMin, precoMax, estadoProd);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome) {
        return produtoDao.getFilterProdutos(idcat, idprodnome);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutos(idcat, precoMin, precoMax);
    }

    public Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String codigoBar) {
        return produtoDao.getFilterProdutosCodBar(idcat, codigoBar);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, estadoProd);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar, precoMin, precoMax);
    }

    public Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String idprodnome, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutosCodBar(idcat, idprodnome, precoMin, precoMax, estadoProd);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar, estadoProd);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String codigoBar, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, codigoBar, precoMin, precoMax, estadoProd);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, precoMin, precoMax);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, String codigoBar) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, codigoBar);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, String idprodnome, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, idprodnome, estadoProd);
    }

    public Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String codigoBar, int precoMin, int precoMax) {
        return produtoDao.getFilterProdutosCodBar(idcat, codigoBar, precoMin, precoMax);
    }

    public Flowable<List<Produto>> getFilterProdutos(long idcat, int precoMin, int precoMax, int estadoProd) {
        return produtoDao.getFilterProdutos(idcat, precoMin, precoMax, estadoProd);
    }

    public Flowable<List<Produto>> getFilterProdutosCodBar(long idcat, String codigoBar, int estadoProd) {
        return produtoDao.getFilterProdutosCodBar(idcat, codigoBar, estadoProd);
    }

    public void importarProdutos(List<String> produtos, Application context, Handler handler) {
        Produto produto = new Produto();
        for (String pt : produtos) {
            try {
                String[] prod = pt.split(",");
                produto.setNome(prod[0]);
                produto.setPreco(Integer.parseInt(prod[1]));
                produto.setPrecofornecedor(Integer.parseInt(prod[2]));
                produto.setQuantidade(Integer.parseInt(prod[3]));
                produto.setCodigoBarra(prod[4]);
                produto.setIva(Boolean.parseBoolean(prod[5]));
                produto.setEstado(Integer.parseInt(prod[6]));
                produto.setIdcategoria(Long.parseLong(prod[7]));
                produto.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                produtoDao.insert(produto);
            } catch (Exception e) {
                isErro = true;
            }
            handler.post(() -> {
                if (isErro) {
                    Ultilitario.showToast(context, Color.rgb(204, 0, 0), context.getString(R.string.ficheiro_certo), R.drawable.ic_toast_erro);
                } else {
                    Ultilitario.showToast(context, Color.rgb(102, 153, 0), context.getString(R.string.produto_importado), R.drawable.ic_toast_feito);
                }
            });
        }
    }
}
