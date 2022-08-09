package com.yoga.mborasystem.repository;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.CategoriaDao;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.util.Ultilitario;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.rxjava3.core.Maybe;

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

    public PagingSource<Integer, Categoria> getCategorias(boolean isLixeira, boolean isSearch, String produto) {
        if (isLixeira) {
            if (isSearch) {
                return categoriaDao.searchCategoriasLixeira(produto);
            } else {
                return categoriaDao.getCategoriasLixeira();
            }
        } else {
            if (isSearch) {
                return categoriaDao.searchCategorias(produto);
            } else {
                return categoriaDao.getCategorias();
            }
        }
    }

    public LiveData<Long> getQuantidadeCategoria(boolean isLixeira) {
        if (isLixeira) {
            return categoriaDao.getQuantidadeCategoriaLixeira();
        } else {
            return categoriaDao.getQuantidadeCategoria();
        }
    }

    public Maybe<List<Categoria>> categoriasSpinner(boolean isFactura) {
        return categoriaDao.getCategoriasSpinner(1, isFactura ? 1 : 2);
    }

    public void importarCategorias(List<String> categorias, Context context, Handler handler, AlertDialog dialog) {

        Categoria categoria = new Categoria();
        try {
            for (String ct : categorias) {
                String[] cat = ct.split(",");
                categoria.setId(0);
                categoria.setCategoria(cat[0]);
                categoria.setDescricao(cat[1]);
                categoria.setEstado(Integer.parseInt(cat[2]));
                categoria.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                categoriaDao.insert(categoria);
            }
            dialog.dismiss();
            handler.post(() -> Ultilitario.showToast(context, Color.rgb(102, 153, 0), context.getString(R.string.cats_impo), R.drawable.ic_toast_feito));
        } catch (Exception e) {
            handler.post(() -> Ultilitario.showToast(context, Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro));
        }
    }
}
