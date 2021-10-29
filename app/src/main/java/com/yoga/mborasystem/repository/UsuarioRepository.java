package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.UsuarioDao;
import com.yoga.mborasystem.model.entidade.Usuario;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class UsuarioRepository {

    private UsuarioDao usuarioDao;
    private WeakReference<Context> cwr;

    public UsuarioRepository(Context c) {
        cwr = new WeakReference<>(c);
        AppDataBase adb = AppDataBase.getAppDataBase(cwr.get());
        usuarioDao = adb.usuarioDao();
    }

    public void insert(Usuario us) {
        usuarioDao.insert(us);
    }

    public Flowable<List<Usuario>> getUsuarios() {
        return usuarioDao.getUsuarios();
    }

    public void update(Usuario us, boolean isCodigoPin) {
        if (isCodigoPin) {
            usuarioDao.update(us.getCodigoPin(), us.getId());
        } else {
            usuarioDao.update(us.getNome(), us.getTelefone(), us.getEndereco(), us.getEstado(), us.getData_modifica(), us.getId());
        }
    }

    public void delete(Usuario us) {
        if (us != null) {
            usuarioDao.delete(us);
        }
    }

    public Single<Usuario> confirmarCodigoPin(String cp) {
        return usuarioDao.confirmarCodigoPin(cp);
    }
}
