package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.navigation.Navigation;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.repository.CategoriaRepository;
import com.yoga.mborasystem.util.Event;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CategoriaProdutoViewModel extends AndroidViewModel {

    public boolean crud;
    private final Bundle bundle;
    private Disposable disposable;
    private final Categoria categoria;
    private ArrayList<String> categoriaList, descricaoList;
    private final CategoriaRepository categoriaRepository;

    public CategoriaProdutoViewModel(Application application) {
        super(application);
        bundle = new Bundle();
        categoria = new Categoria();
        disposable = new CompositeDisposable();
        categoriaRepository = new CategoriaRepository(application);
    }

    private MutableLiveData<PagingData<Categoria>> listaCategorias;

    public MutableLiveData<PagingData<Categoria>> getListaCategorias() {
        if (listaCategorias == null) {
            listaCategorias = new MutableLiveData<>();
        }
        return listaCategorias;
    }

    private MutableLiveData<Event<List<Categoria>>> listaCategoriasSpinner;

    public MutableLiveData<Event<List<Categoria>>> getListaCategoriasSpinner() {
        if (listaCategoriasSpinner == null) {
            listaCategoriasSpinner = new MutableLiveData<>();
        }
        return listaCategoriasSpinner;
    }

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    public void validarCategoria(Ultilitario.Operacao operacao, EditText nome, EditText descricao, @SuppressLint("UseSwitchCompatOrMaterialCode") Switch estado, AlertDialog dialog, long idcategoria) {
        if (isCampoVazio(nome.getText().toString())) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_invalido));
        } else if (isCampoVazio(descricao.getText().toString())) {
            descricao.requestFocus();
            descricao.setError(getApplication().getString(R.string.descricao_invalida));
        } else {
            categoria.setCategoria(nome.getText().toString());
            categoria.setDescricao(descricao.getText().toString());
            categoria.setEstado(estado.isChecked() ? Ultilitario.DOIS : Ultilitario.UM);
            switch (operacao) {
                case CRIAR:
                    categoria.setId(0);
                    categoria.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                    criarCategoria(categoria, dialog);
                    break;
                case ACTUALIZAR:
                    categoria.setId(idcategoria);
                    categoria.setData_modifica(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                    renomearCategoria(categoria, dialog);
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressLint("CheckResult")
    private void criarCategoria(Categoria categoria, AlertDialog dialog) {
        Completable.fromAction(() -> categoriaRepository.insert(categoria))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.categoria_criada), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.categoria_nao_criada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void consultarCategorias(String categoria, boolean isLixeira, boolean isPesquisa, LifecycleOwner lifecycleOwner) {
        Flowable<PagingData<Categoria>> flowable = PagingRx.getFlowable(new Pager<>(new PagingConfig(20), () -> categoriaRepository.getCategorias(isLixeira, isPesquisa, categoria)));
        PagingRx.cachedIn(flowable, ViewModelKt.getViewModelScope(this));
        flowable.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
                .subscribe(categorias -> {
                    if (crud)
                        getListaCategorias().postValue(categorias);
                    else
                        getListaCategorias().setValue(categorias);
                }, e -> new Handler().post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_categoria) + "\n" + e.getMessage(), R.drawable.ic_toast_erro)));
    }

    public void categoriasSpinner(boolean isFactura, int operacao, View view) {
        disposable = categoriaRepository.categoriasSpinner(isFactura)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(categorias -> {
                    if (isFactura)
                        getListaCategoriasSpinner().setValue(new Event<>(categorias));
                    else {
                        categoriaList = new ArrayList<>();
                        descricaoList = new ArrayList<>();
                        for (Categoria c : categorias) {
                            categoriaList.add(c.getId() + "-" + c.getCategoria());
                            descricaoList.add(c.getDescricao());
                        }
                        bundle.putInt("typeoperation", operacao);
                        bundle.putStringArrayList("categorias", categoriaList);
                        bundle.putStringArrayList("descricao", descricaoList);
                        Navigation.findNavController(view).navigate(R.id.action_categoriaProdutoFragment_to_dialogExportarImportar, bundle);
                    }
                }, e -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_categoria) + "\n" + e.getMessage(), R.drawable.ic_toast_erro));
    }

    public LiveData<Long> getQuantidadeCategoria(boolean isLixeira) {
        return categoriaRepository.getQuantidadeCategoria(isLixeira);
    }

    @SuppressLint("CheckResult")
    public void renomearCategoria(Categoria categoria, AlertDialog dialog) {
        Completable.fromAction(() -> categoriaRepository.update(categoria))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cat_alter), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.categoria_nao_renomeada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void restaurarCategoria(int estado, long idcategoria, boolean todasCategorias) {
        Completable.fromAction(() -> categoriaRepository.restaurarCategoria(estado, idcategoria, todasCategorias))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        if (todasCategorias) {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cats_rests), R.drawable.ic_toast_feito);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cat_rest), R.drawable.ic_toast_feito);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.cat_n_rest) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void eliminarCategoria(Categoria cat, boolean isLixeira, boolean eliminarTodasLixeira) {
        Completable.fromAction(() -> categoriaRepository.delete(cat, isLixeira, eliminarTodasLixeira))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        if (!isLixeira || eliminarTodasLixeira) {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), eliminarTodasLixeira ? getApplication().getString(R.string.cts_elims) : getApplication().getString(R.string.categoria_eliminada), R.drawable.ic_toast_feito);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cat_env_lx), R.drawable.ic_toast_feito);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.categoria_nao_eliminada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void importarCategorias(Map<String, String> categorias, Handler handler) {
        categoriaRepository.importarCategorias(categorias, getApplication(), handler);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable.isDisposed())
            disposable.dispose();

        if (bundle != null)
            bundle.clear();
    }
}
