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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        ClienteCantina cc = new ClienteCantina();
        try {
            Map<Long, String> clientList = new HashMap<>();
            for (ClienteCantina clienteCantina : clienteCantinaDao.getClientesExport())
                clientList.put(clienteCantina.getId(), clienteCantina.getNif());
            for (String cl : clientes) {
                String[] cli = cl.split(",");
                if (!clientList.containsKey(Long.parseLong(cli[0])) && !clientList.containsValue(cli[5])) {
                    cc.setId(0);
                    cc.setNome(cli[1]);
                    cc.setTelefone(cli[2]);
                    cc.setEmail(cli[3]);
                    cc.setEndereco(cli[4]);
                    cc.setNif(cli[5]);
                    cc.setEstado(Ultilitario.UM);
                    cc.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                    clienteCantinaDao.insert(cc);
                }
            }
            handler.post(() -> Ultilitario.showToast(context, Color.rgb(102, 153, 0), context.getString(R.string.cli_import), R.drawable.ic_toast_feito));
        } catch (Exception e) {
            handler.post(() -> Ultilitario.showToast(context, Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro));
        }
    }
}
