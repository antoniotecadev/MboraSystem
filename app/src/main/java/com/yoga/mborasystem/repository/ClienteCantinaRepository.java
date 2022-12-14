package com.yoga.mborasystem.repository;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ClienteCantinaDao;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.util.Ultilitario;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

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

    public PagingSource<Integer, ClienteCantina> getClientesCantina(boolean isSearch, String cliente) {
        if (isSearch)
            return clienteCantinaDao.searchCliente(cliente);
        else
            return clienteCantinaDao.getClientesCantina();
    }

    public Maybe<List<ClienteCantina>> getClienteCantina(String cliente, boolean isForDocumentSaft, List<Long> idcliente) {
        if (isForDocumentSaft)
            return clienteCantinaDao.getClienteCantinaSaft(idcliente);
        else
            return clienteCantinaDao.getClienteCantina(cliente);
    }

    public LiveData<Long> getQuantidadeCliente() {
        return clienteCantinaDao.getQuantidadeCliente();
    }

    public void update(ClienteCantina clienteCantina) {
        clienteCantinaDao.update(clienteCantina.getNif(), clienteCantina.getNome(), clienteCantina.getTelefone(), clienteCantina.getEmail(), clienteCantina.getEndereco(), clienteCantina.getEstado(), clienteCantina.getData_modifica(), clienteCantina.getId());
    }

    public void delete(ClienteCantina clienteCantina) {
        if (clienteCantina != null)
            clienteCantinaDao.delete(clienteCantina);
    }

    public List<ClienteCantina> getClientesExport() throws Exception {
        return clienteCantinaDao.getClientesExport();
    }

    public Single<List<ClienteCantina>> nifBiExiste(String nif) {
        return clienteCantinaDao.nifBiExiste(nif);
    }


    public void importarClientes(List<String> clientes, Application context, Handler handler) {
        ClienteCantina clienteCantina = new ClienteCantina();
        try {
            for (String cl : clientes) {
                String[] cli = cl.split(",");
                clienteCantina.setId(0);
                clienteCantina.setNome(cli[0]);
                clienteCantina.setTelefone(cli[1]);
                clienteCantina.setEmail(cli[2]);
                clienteCantina.setEndereco(cli[3]);
                clienteCantina.setNif(cli[4]);
                clienteCantina.setEstado(Ultilitario.UM);
                clienteCantina.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                clienteCantinaDao.insert(clienteCantina);
            }
            handler.post(() -> Ultilitario.showToast(context, Color.rgb(102, 153, 0), context.getString(R.string.cli_import), R.drawable.ic_toast_feito));
        } catch (Exception e) {
            handler.post(() -> Ultilitario.showToast(context, Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro));
        }
    }
}
