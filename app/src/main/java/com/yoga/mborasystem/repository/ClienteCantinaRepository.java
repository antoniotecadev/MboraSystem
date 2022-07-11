package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ClienteCantinaDao;
import com.yoga.mborasystem.model.entidade.ClienteCantina;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Flowable;

public class ClienteCantinaRepository {

    private final ClienteCantinaDao clienteCantinaDao;

    public ClienteCantinaRepository(Context c) {
        WeakReference<Context> cwr = new WeakReference<>(c);
        AppDataBase adb = AppDataBase.getAppDataBase(cwr.get());
        clienteCantinaDao = adb.clienteCantinaDao();
    }

    public void insert(ClienteCantina clienteCantina) {
        clienteCantinaDao.insert(clienteCantina);
    }

    public Flowable<List<ClienteCantina>> getClientesCantina() {
        return clienteCantinaDao.getClientesCantina();
    }

    public Flowable<List<ClienteCantina>> searchCliente(String cliente) {
        return clienteCantinaDao.searchCliente(cliente);
    }

    public void update(String nome, String telefone, String email, String endereco,int estado, String dataModif, long id) {
        clienteCantinaDao.update(nome, telefone, email, endereco,estado, dataModif, id);
    }

    public void delete(ClienteCantina clienteCantina) {
        if (clienteCantina != null) {
            clienteCantinaDao.delete(clienteCantina);
        }
    }
}
