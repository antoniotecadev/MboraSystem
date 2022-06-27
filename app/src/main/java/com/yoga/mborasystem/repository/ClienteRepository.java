package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ClienteDao;
import com.yoga.mborasystem.model.entidade.Cliente;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class ClienteRepository {

    Context context;
    ClienteDao clienteDao;
    WeakReference<Context> contextWeakReference;

    public ClienteRepository(Context context) {
        contextWeakReference = new WeakReference<>(context);
        AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
        clienteDao = appDataBase.clienteDao();
        this.context = context;
    }

    public void insert(Cliente cliente) {
        clienteDao.insert(cliente);
    }

    public List<Cliente> clienteExiste() throws Exception {
        return clienteDao.clienteExiste();
    }

    public void delete(Cliente cliente) {
        clienteDao.delete(cliente);
    }

    public void alterarSenha(Cliente cliente) {
        clienteDao.alterarSenha(cliente.getId(), cliente.getSenha());
    }
}
