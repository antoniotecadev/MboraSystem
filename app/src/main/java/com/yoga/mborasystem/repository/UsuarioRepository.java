package com.yoga.mborasystem.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.UsuarioDao;
import com.yoga.mborasystem.model.entidade.Usuario;

import java.lang.ref.WeakReference;
import java.util.List;

public class UsuarioRepository {

    private final UsuarioDao usuarioDao;

    public UsuarioRepository(Context c) {
        WeakReference<Context> cwr = new WeakReference<>(c);
        AppDataBase adb = AppDataBase.getAppDataBase(cwr.get());
        usuarioDao = adb.usuarioDao();
    }

    public void insert(Usuario us) {
        usuarioDao.insert(us);
    }

    public PagingSource<Integer, Usuario> getUsuarios() {
        return usuarioDao.getUsuarios();
    }

    public LiveData<Long> getQuantidadeUsuario() {
        return usuarioDao.getQuantidadeUsuario();
    }

    public void update(Usuario us, boolean isCodigoPin) {
        if (isCodigoPin)
            usuarioDao.update(us.getCodigoPin(), us.getId());
        else
            usuarioDao.update(us.getNome(), us.getTelefone(), us.getEndereco(), us.getEstado(), us.getData_modifica(), us.getId());
    }

    public void delete(Usuario us) {
        if (us != null)
            usuarioDao.delete(us);
    }

    public List<Usuario> confirmarCodigoPin(String cp) throws Exception {
        return usuarioDao.confirmarCodigoPin(cp);
    }
}
