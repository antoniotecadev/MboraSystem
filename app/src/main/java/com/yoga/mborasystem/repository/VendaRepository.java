package com.yoga.mborasystem.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.VendaDao;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.util.Ultilitario;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Flowable;

public class VendaRepository {

    private final VendaDao vendaDao;

    public VendaRepository(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
        vendaDao = appDataBase.vendaDao();
    }

    public long insert(Venda venda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        return vendaDao.insertVendaProduto(venda, produtos, precoTotalUnit);
    }

    public PagingSource<Integer, Venda> getVendas(long idcliente, boolean isDivida, long idusuario, boolean isLixeira, boolean isPesquisa, String referencia, boolean isData, String data) {
        if (isData) {
            return vendaDao.getVendas(data, idcliente, isDivida, idusuario);
        } else {
            if (isLixeira) {
                if (isPesquisa)
                    return vendaDao.searchVendasLixeira(referencia);
                else
                    return vendaDao.getVendasLixeira();
            } else {
                if (isPesquisa)
                    return vendaDao.getSearchVendas(referencia, idcliente, isDivida, idusuario);
                else
                    return vendaDao.getVendas(idcliente, isDivida, idusuario);
            }
        }
    }

    public LiveData<Long> getQuantidadeVenda(boolean isLixeira) {
        if (isLixeira) {
            return vendaDao.getQuantidadeVendaLixeira();
        } else {
            return vendaDao.getQuantidadeVenda();
        }
    }

    public List<Venda> getVendasPorDataExport(String data) {
        return vendaDao.getVendaExport(data);
    }

    public void importarVendas(List<String> vendas) {
        vendaDao.insertVenda(vendas);
    }

    public Flowable<List<ProdutoVenda>> getProdutosVenda(long idvenda, String codQr, String data, boolean isGuardarImprimir) {
        if (isGuardarImprimir) {
            return vendaDao.getProdutoVenda(data);
        } else {
            return vendaDao.getProdutosVenda(idvenda, codQr);
        }
    }

    public void liquidarDivida(int divida, long idivida) {
        vendaDao.liquidardivida(divida, idivida);
    }

    public void eliminarVendaLixeira(int estado, String data, Venda venda, boolean isLixeira, boolean eliminarTodasLixeira) {
        if (eliminarTodasLixeira) {
            vendaDao.deleteAllVendaLixeira(3);
        } else {
            if (isLixeira) {
                vendaDao.deleteVendas(venda);
            } else {
                vendaDao.deleteLixeira(estado, Ultilitario.monthInglesFrances(data), venda.getId());
            }
        }
    }

    public void restaurarVenda(int estado, long idvenda, boolean todasVendas) {
        if (todasVendas) {
            vendaDao.restaurarTodasVendas(estado);
        } else {
            vendaDao.restaurarVenda(estado, idvenda);
        }
    }

    public LiveData<List<ProdutoVenda>> produtoMaisVendido(String data) {
        return vendaDao.getProdutoMaisVendido(data);
    }

    public LiveData<List<ProdutoVenda>> produtoMenosVendido(String data) {
        return vendaDao.getProdutoMenosVendido(data);
    }

}
