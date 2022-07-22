package com.yoga.mborasystem.repository;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ClienteCantinaDao;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.util.Ultilitario;

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

    public void update(String nome, String telefone, String email, String endereco, int estado, String dataModif, long id) {
        clienteCantinaDao.update(nome, telefone, email, endereco, estado, dataModif, id);
    }

    public void delete(ClienteCantina clienteCantina) {
        if (clienteCantina != null) {
            clienteCantinaDao.delete(clienteCantina);
        }
    }

    public List<ClienteCantina> getClientesExport() throws Exception {
        return clienteCantinaDao.getClientesExport();
    }

    public void importarClientes(List<String> clientes, Application context, Handler handler) {
        ClienteCantina clienteCantina = new ClienteCantina();
        try {
            for (String cl : clientes) {
                String[] cli = cl.split(",");
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
