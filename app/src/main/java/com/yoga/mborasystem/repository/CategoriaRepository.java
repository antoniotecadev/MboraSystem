package com.yoga.mborasystem.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.CategoriaDao;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.util.Ultilitario;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public class CategoriaRepository {

    private final CategoriaDao categoriaDao;

    public CategoriaRepository(Context context) {
        WeakReference<Context> contextWeakReference = new WeakReference<>(context);
        AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
        categoriaDao = appDataBase.categoriaDao();
    }

    public void insert(Categoria cat) {
        categoriaDao.insert(cat);
    }

    public void update(Categoria cat) {
        categoriaDao.update(cat.getCategoria(), cat.getDescricao(), cat.getEstado(), cat.getData_modifica(), cat.getId());
    }

    public void delete(Categoria cat, boolean lx, boolean eliminarTodasLixeira) {
        if (eliminarTodasLixeira) {
            categoriaDao.deleteAllCategoriaLixeira(3);
        } else {
            if (lx && (cat != null)) {
                categoriaDao.deleteLixeira(cat.getEstado(), cat.getData_elimina(), cat.getId());
            } else {
                categoriaDao.delete(cat);
            }
        }
    }

    public void restaurarCategoria(int estado, long idcategoria, boolean todasCategorias) {
        if (todasCategorias) {
            categoriaDao.restaurarCategoria(estado);
        } else {
            categoriaDao.restaurarCategoria(estado, idcategoria);
        }
    }

    public Flowable<List<Categoria>> getCategorias(boolean isLixeira) {
        if (isLixeira) {
            return categoriaDao.getCategoriasLixeira();
        } else {
            return categoriaDao.getCategorias();
        }
    }

    public Flowable<List<Categoria>> searchCategorias(String categoria, boolean isLixeira) {
        if (isLixeira) {
            return categoriaDao.searchCategoriasLixeira(categoria);
        } else {
            return categoriaDao.searchCategorias(categoria);
        }
    }

    public void importarCategorias(Map<String, String> categorias, Context context) {
        new CategoriaIm(categorias, context).execute();
    }

    @SuppressLint("StaticFieldLeak")
    class CategoriaIm extends AsyncTask<Void, Void, Void> {

        Context context;
        Map<String, String> categorias;

        CategoriaIm(Map<String, String> categorias, Context context) {
            this.context = context;
            this.categorias = categorias;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Categoria categoria = new Categoria();

            for (String ct : categorias.keySet()) {
                categoria.setCategoria(ct);
                categoria.setDescricao(categorias.get(ct));
                categoria.setEstado(1);
                categoria.setData_cria(Ultilitario.getDateCurrent());
                categoriaDao.insert(categoria);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(context, R.string.cats_impo, Toast.LENGTH_LONG).show();
        }
    }

}
