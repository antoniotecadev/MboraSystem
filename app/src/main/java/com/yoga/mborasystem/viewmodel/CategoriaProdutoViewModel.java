package com.yoga.mborasystem.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Switch;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.repository.CategoriaRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class CategoriaProdutoViewModel extends AndroidViewModel {

    private Categoria categoria;
    private Disposable disposable;
    private CompositeDisposable compositeDisposable;
    private CategoriaRepository categoriaRepository;

    public CategoriaProdutoViewModel(Application application) {
        super(application);
        categoria = new Categoria();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        categoriaRepository = new CategoriaRepository(application);
        consultarCategorias(null);
    }

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    public void validarCategoria(Ultilitario.Operacao operacao, EditText nome, EditText descricao, Switch estado, AlertDialog dialog, long idcategoria) {
        if (isCampoVazio(nome.getText().toString()) || Ultilitario.letras.matcher(nome.getText().toString()).find()) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_invalido));
        } else if (isCampoVazio(descricao.getText().toString()) || Ultilitario.letras.matcher(descricao.getText().toString()).find()) {
            descricao.requestFocus();
            descricao.setError(getApplication().getString(R.string.descricao_invalida));
        } else {
            MainActivity.getProgressBar();
            categoria.setCategoria(nome.getText().toString());
            categoria.setDescricao(descricao.getText().toString());
            categoria.setEstado(estado.isChecked() ? Ultilitario.DOIS : Ultilitario.UM);
            switch (operacao) {
                case CRIAR:
                    categoria.setData_cria(Ultilitario.getDateCurrent());
                    criarCategoria(categoria, dialog);
                    break;
                case ACTUALIZAR:
                    categoria.setId(idcategoria);
                    categoria.setData_modifica(Ultilitario.getDateCurrent());
                    renomearCategoria(categoria, dialog);
                    break;
                default:
                    break;
            }
        }
    }

    private MutableLiveData<List<Categoria>> listaCategorias;

    public MutableLiveData<List<Categoria>> getListaCategorias() {
        if (listaCategorias == null) {
            listaCategorias = new MutableLiveData<>();
        }
        return listaCategorias;
    }

    private void criarCategoria(Categoria categoria, AlertDialog dialog) {
        Completable.fromAction(() -> categoriaRepository.insert(categoria))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.categoria_criada), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        if (e.getMessage().contains("UNIQUE")) {
                            Ultilitario.showToast(getApplication(), Color.rgb(255, 187, 51), getApplication().getString(R.string.categoria_existe), R.drawable.ic_toast_erro);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.categoria_nao_criada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                        }
                    }
                });
    }

    public void consultarCategorias(SwipeRefreshLayout mySwipeRefreshLayout) {
        compositeDisposable.add(categoriaRepository.getCategorias()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categorias -> {
                    getListaCategorias().setValue(categorias);
                    Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
                    Ultilitario.swipeRefreshLayout(mySwipeRefreshLayout);
                    MainActivity.dismissProgressBar();
                }, e -> {
                    MainActivity.dismissProgressBar();
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_categoria) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    Ultilitario.swipeRefreshLayout(mySwipeRefreshLayout);
                }));
    }

    public void searchCategoria(String categoria) {
        compositeDisposable.add(categoriaRepository.searchCategorias(categoria)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categorias -> {
                    getListaCategorias().setValue(categorias);
                    Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
                }, e -> {
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_categoria) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                }));
    }

    public void renomearCategoria(Categoria categoria, AlertDialog dialog) {
        Completable.fromAction(() -> categoriaRepository.update(categoria))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.categoria_renomeada), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        if (e.getMessage().contains("UNIQUE")) {
                            Ultilitario.showToast(getApplication(), Color.rgb(255, 187, 51), getApplication().getString(R.string.categoria_existe), R.drawable.ic_toast_erro);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.categoria_nao_renomeada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                        }
                    }
                });
    }

    public void eliminarCategoria(Categoria cat, boolean lx) {
        MainActivity.getProgressBar();
        Completable.fromAction(() -> categoriaRepository.delete(cat, lx))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.categoria_eliminada), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.categoria_nao_eliminada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void importarCategorias(Map<String, String> categorias) {
        categoriaRepository.importarCategorias(categorias, getApplication());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null || !disposable.isDisposed()) {
            disposable.dispose();
        }
        if (compositeDisposable != null || !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }
}
