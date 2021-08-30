package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.VendaDao;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.Venda;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public class VendaRepository {

    private VendaDao vendaDao;
    private WeakReference<Context> contextWeakReference;

    public VendaRepository(Context context) {
        contextWeakReference = new WeakReference<>(context);
        AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
        vendaDao = appDataBase.vendaDao();
    }

    public void insert(Venda venda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        vendaDao.insertVendaProduto(venda, produtos, precoTotalUnit);
    }

    public Flowable<List<Venda>> getVendas() {
        return vendaDao.getVendas();
    }

    public Flowable<List<Venda>> getVendasPorData(String data) {
        return vendaDao.getVendas(data);
    }

    public Flowable<List<Venda>> getSearchVendas(String codQr) {
        return vendaDao.getSearchVendas(codQr);
    }

}
