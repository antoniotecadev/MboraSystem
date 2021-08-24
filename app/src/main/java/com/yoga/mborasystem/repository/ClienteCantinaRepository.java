package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ClienteCantinaDao;
import com.yoga.mborasystem.model.entidade.ClienteCantina;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Flowable;

public class ClienteCantinaRepository {

    private ClienteCantinaDao clienteCantinaDao;
    private WeakReference<Context> cwr;

    public ClienteCantinaRepository(Context c) {
        cwr = new WeakReference<>(c);
        AppDataBase adb = AppDataBase.getAppDataBase(cwr.get());
        clienteCantinaDao = adb.clienteCantinaDao();
    }

    public void insert(ClienteCantina clienteCantina) {
        clienteCantinaDao.insert(clienteCantina);
    }

    public Flowable<List<ClienteCantina>> getClientesCantina() {
        return clienteCantinaDao.getClientesCantina();
    }
}
