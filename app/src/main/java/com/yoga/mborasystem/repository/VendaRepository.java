package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.VendaDao;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
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

    public Flowable<List<Venda>> getVendas(long idcliente, boolean isdivida, long idusuario) {
        return vendaDao.getVendas(idcliente, isdivida, idusuario);
    }

    public Flowable<List<Venda>> getVendasPorData(String data, long idcliente, boolean isDivida, long idusuario) {
        return vendaDao.getVendas(data, idcliente, isDivida, idusuario);
    }

    public Flowable<List<Venda>> getSearchVendas(String codQr, long idcliente, boolean isDivida, long idusuario) {
        return vendaDao.getSearchVendas(codQr, idcliente, isDivida, idusuario);
    }

    public void importarVendas(List<String> vendas) {
        vendaDao.insertVenda(vendas);
    }

    public Flowable<List<ProdutoVenda>> getProdutosVenda(long idvenda, String codQr) {
        return vendaDao.getProdutosVenda(idvenda, codQr);
    }

    public void liquidarDivida(int divida, long idivida) {
        vendaDao.liquidardivida(divida, idivida);
    }

    public void eliminarVendaLixeira(int estado, String data, long idvenda){
        vendaDao.deleteLixeira(estado, data, idvenda);
    }

}
