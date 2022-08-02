package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Handler;
import android.text.TextUtils;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingSource;
import androidx.paging.rxjava3.PagingRx;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.repository.ProdutoRepository;
import com.yoga.mborasystem.util.Event;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProdutoViewModel extends AndroidViewModel {

    public boolean crud;
    private final Produto produto;
    private Disposable disposable;
    private LifecycleOwner lifecycleOwner;
    public Flowable<PagingData<Produto>> flowable;
    private final ProdutoRepository produtoRepository;
    private final CompositeDisposable compositeDisposable;

    public ProdutoViewModel(@NonNull Application application) {
        super(application);
        produto = new Produto();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        produtoRepository = new ProdutoRepository(getApplication());
    }

    Pattern numero = Pattern.compile("[^0-9]");
    Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û0-9-/,()&%@ ]");

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    private MutableLiveData<List<Produto>> listaProdutos;
    private MutableLiveData<PagingData<Produto>> listaProdutospaging;
    private MutableLiveData<Event<List<Produto>>> listaProdutosImport;

    public MutableLiveData<List<Produto>> getListaProdutos() {
        if (listaProdutos == null) {
            listaProdutos = new MutableLiveData<>();
        }
        return listaProdutos;
    }

    public MutableLiveData<PagingData<Produto>> getListaProdutosPaging() {
        if (listaProdutospaging == null) {
            listaProdutospaging = new MutableLiveData<>();
        }
        return listaProdutospaging;
    }

    public MutableLiveData<Event<List<Produto>>> getListaProdutosisExport() {
        if (listaProdutosImport == null) {
            listaProdutosImport = new MutableLiveData<>();
        }
        return listaProdutosImport;
    }

    public void validarProduto(Ultilitario.Operacao operacao, long id, EditText nome, AppCompatSpinner tipo, AppCompatSpinner unidade, String codigoMotivoIsecao, TextInputEditText preco, TextInputEditText precofornecedor, EditText quantidade, EditText codigoBarra, MaterialCheckBox checkIva, Integer taxaIva, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estado, SwitchCompat stock, AlertDialog dialog, Boolean continuar, long idcategoria) {
        if (isCampoVazio(nome.getText().toString()) || letraNumero.matcher(nome.getText().toString()).find()) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precofornecedor.getText()).toString())) {
            precofornecedor.requestFocus();
            precofornecedor.setError(getApplication().getString(R.string.preco_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(preco.getText()).toString()) || Ultilitario.removerKZ(preco) <= 0) {
            preco.requestFocus();
            preco.setError(getApplication().getString(R.string.dig_preco));
        } else if (isCampoVazio(quantidade.getText().toString()) || numero.matcher(quantidade.getText().toString()).find() || quantidade.length() > 4) {
            quantidade.requestFocus();
            quantidade.setError(getApplication().getString(R.string.quantidade_invalida));
        } else if (numero.matcher(codigoBarra.getText().toString()).find() || codigoBarra.length() > 14) {
            codigoBarra.requestFocus();
            codigoBarra.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(String.valueOf(idcategoria)) || numero.matcher(String.valueOf(idcategoria)).find()) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.idcategoria_nao_encontrado), R.drawable.ic_toast_erro);
        } else {
            produto.setNome(nome.getText().toString());
            produto.setTipo(tipo.getSelectedItem().toString());
            produto.setUnidade(unidade.getSelectedItem().toString());
            produto.setCodigoMotivoIsencao(codigoMotivoIsecao);
            produto.setPreco(Ultilitario.removerKZ(preco));
            produto.setPrecofornecedor(Ultilitario.removerKZ(precofornecedor));
            produto.setQuantidade(Integer.parseInt(quantidade.getText().toString()));
            produto.setCodigoBarra(codigoBarra.getText().toString());
            produto.setIva(checkIva.isChecked());
            produto.setEstado(estado.isChecked() ? Ultilitario.DOIS : Ultilitario.UM);
            produto.setStock(stock.isChecked());
            produto.setIdcategoria(idcategoria);
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                produto.setId(0);
                produto.setPercentagemIva(taxaIva);
                produto.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                criarProduto(produto, dialog, continuar);
                nome.setText("");
                preco.setText(getApplication().getText(R.string.preco_zero));
                precofornecedor.setText(getApplication().getText(R.string.preco_zero));
                quantidade.setText("1");
                codigoBarra.setText("");
                nome.requestFocus();
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {
                produto.setPercentagemIva(checkIva.isChecked() ? (taxaIva == 1 ? 0 : taxaIva) : 0);
                produto.setId(id);
                produto.setData_modifica(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                actualizarProduto(produto, dialog);
            }
        }
    }

    public void criarProduto(EditText nome, AppCompatSpinner tipo, AppCompatSpinner unidade, String codigoMotivoIsecao, TextInputEditText preco, TextInputEditText precofornecedor, TextInputEditText quantidade, EditText codigoBarra, MaterialCheckBox checkIva, Integer percentagemIva, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estado, SwitchCompat stock, AlertDialog dialog, boolean c, long idcategoria) {
        validarProduto(Ultilitario.Operacao.CRIAR, 0, nome, tipo, unidade, codigoMotivoIsecao, preco, precofornecedor, quantidade, codigoBarra, checkIva, percentagemIva, estado, stock, dialog, c, idcategoria);
    }

    public void actualizarProduto(long id, EditText nome, AppCompatSpinner tipo, AppCompatSpinner unidade, String codigoMotivoIsecao, TextInputEditText preco, TextInputEditText precofornecedor, EditText quantidade, EditText codigoBarra, MaterialCheckBox checkIva, Integer percentagemIva, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estado, SwitchCompat stock, long idcategoria, AlertDialog dialog) {
        validarProduto(Ultilitario.Operacao.ACTUALIZAR, id, nome, tipo, unidade, codigoMotivoIsecao, preco, precofornecedor, quantidade, codigoBarra, checkIva, percentagemIva, estado, stock, dialog, false, idcategoria);
    }

    private int contar = 0;

    @SuppressLint("CheckResult")
    private void criarProduto(Produto produto, AlertDialog dialog, Boolean continuar) {
        Completable.fromAction(() -> produtoRepository.insert(produto))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.produto_criado) + " " + ++contar, R.drawable.ic_toast_feito);
                        if (continuar && dialog != null) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.produto_nao_criado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    private void actualizarProduto(Produto produto, AlertDialog dialog) {
        Completable.fromAction(() -> produtoRepository.update(produto))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.alteracao_feita), R.drawable.ic_toast_feito);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }

                    @SuppressLint("CheckResult")
                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (Objects.requireNonNull(e.getMessage()).contains("UNIQUE")) {
                            Ultilitario.showToast(getApplication(), Color.rgb(255, 187, 51), getApplication().getString(R.string.produto_existe) + " ou " + getApplication().getString(R.string.codigobarra_existe), R.drawable.ic_toast_erro);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.produto_nao_criado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void eliminarProduto(Produto produto, boolean isLixeira, Dialog dialog, boolean eliminarTodasLixeira) {
        Completable.fromAction(() -> produtoRepository.delete(produto, isLixeira, eliminarTodasLixeira))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        if (!isLixeira || eliminarTodasLixeira) {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), eliminarTodasLixeira ? getApplication().getString(R.string.pds_elim) : getApplication().getString(R.string.produto_eliminado), R.drawable.ic_toast_feito);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.prod_env_lx), R.drawable.ic_toast_feito);
                        }
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.produto_nao_eliminado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void exportarProdutos(long idcategoria) throws Exception {
        getListaProdutosisExport().postValue(new Event<>(produtoRepository.getProdutosExport(idcategoria)));
    }

    public void consultarProdutos(long idcategoria, boolean isExport, SwipeRefreshLayout mySwipeRefreshLayout, boolean isLixeira) {

    }

    public void consultarProdutos(long idcategoria, String produtoText, boolean isLixeira, boolean isPesquisa, LifecycleOwner lifecycleOwner) {
        this.lifecycleOwner = lifecycleOwner;
        Pager<Integer, Produto> pager = new Pager<>(new PagingConfig(10), () -> produtoRepository.getProdutos(idcategoria, isLixeira, isPesquisa, produtoText));
        flowable = PagingRx.getFlowable(pager);
        PagingRx.cachedIn(flowable, ViewModelKt.getViewModelScope(this));
        Handler handler = new Handler();
        flowable.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
                .subscribe(produto -> {
                    if (crud) {
                        getListaProdutosPaging().postValue(produto);
                    } else {
                        getListaProdutosPaging().setValue(produto);
                    }
                }, throwable -> handler.post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_produto) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro)));
    }

    private void filtrar(PagingSource<Integer, Produto> listProduto, AlertDialog dialog) {
        Pager<Integer, Produto> pager = new Pager<>(new PagingConfig(15, 15, false, 20), () -> listProduto);
        flowable = PagingRx.getFlowable(pager);
        PagingRx.cachedIn(flowable, ViewModelKt.getViewModelScope(this));
        flowable.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(this.lifecycleOwner)))
                .subscribe(produto -> {
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    getListaProdutosPaging().postValue(produto);
                }, throwable -> { });
    }

    public LiveData<Long> getQuantidadeProduto(long idcategoria, boolean isLixeira) {
        return produtoRepository.getQuantidadeProduto(idcategoria, isLixeira);
    }

    public LiveData<List<Produto>> getPrecoFornecedor() {
        return produtoRepository.getPrecoFornecedor();
    }

    public LiveData<Long> consultarProdutos() {
        return produtoRepository.getProdutos();
    }

    public void validarProdutoFiltro(long idcat, TextInputEditText idprodnome, TextInputEditText codigoBar, TextInputEditText precoMin, TextInputEditText precoMax, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getCodigoBarra(), produto.getPrecoMin(), produto.getPrecoMax(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoNome(long idcat, TextInputEditText idprodnome, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else {
            produto.setNome(idprodnome.getText().toString());
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome()), dialog);
        }
    }

    public void validarProdutoPreco(long idcat, TextInputEditText precoMin, TextInputEditText precoMax, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getPrecoMin(), produto.getPrecoMax()), dialog);
        }
    }

    public void validarProdutoCodBar(long idcat, TextInputEditText codigoBar, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setCodigoBarra(codigoBar.getText().toString());
            filtrar(produtoRepository.getFilterProdutosCodBar(idcat, produto.getCodigoBarra()), dialog);
        }
    }

    public void validarProdutoEstado(long idcat, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else {
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getEstado()), dialog);
        }
    }

    public void validarProdutoNomeCodBarPreco(long idcat, TextInputEditText idprodnome, TextInputEditText codigoBar, TextInputEditText precoMin, TextInputEditText precoMax, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getCodigoBarra(), produto.getPrecoMin(), produto.getPrecoMax()), dialog);
        }
    }

    public void validarProdutoNomePrecoEstado(long idcat, TextInputEditText idprodnome, TextInputEditText precoMin, TextInputEditText precoMax, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutosCodBar(idcat, produto.getNome(), produto.getPrecoMin(), produto.getPrecoMax(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoNomeCodBarEstado(long idcat, TextInputEditText idprodnome, TextInputEditText codigoBar, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getCodigoBarra(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoCodBarPrecoEstado(long idcat, TextInputEditText codigoBar, TextInputEditText precoMin, TextInputEditText precoMax, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getCodigoBarra(), produto.getPrecoMin(), produto.getPrecoMax(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoNomePreco(long idcat, TextInputEditText idprodnome, TextInputEditText precoMin, TextInputEditText precoMax, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getPrecoMin(), produto.getPrecoMax()), dialog);
        }
    }

    public void validarProdutoNomeCodBar(long idcat, TextInputEditText idprodnome, TextInputEditText codigoBar, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setCodigoBarra(codigoBar.getText().toString());
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getCodigoBarra()), dialog);
        }
    }

    public void validarProdutoNomeEstado(long idcat, TextInputEditText idprodnome, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(idprodnome.getText()).toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoCodBarPreco(long idcat, TextInputEditText codigoBar, TextInputEditText precoMin, TextInputEditText precoMax, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            filtrar(produtoRepository.getFilterProdutosCodBar(idcat, produto.getCodigoBarra(), produto.getPrecoMin(), produto.getPrecoMax()), dialog);
        }
    }

    public void validarProdutoPrecoEstado(long idcat, TextInputEditText precoMin, TextInputEditText precoMax, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(precoMin.getText()).toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(precoMax.getText()).toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getPrecoMin(), produto.getPrecoMax(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoCodBarEstado(long idcat, TextInputEditText codigoBar, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(codigoBar.getText()).toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutosCodBar(idcat, produto.getCodigoBarra(), produto.getEstado()), dialog);
        }
    }

    @SuppressLint("SetTextI18n")
    public void codigoBarra(String result, TextInputEditText codigoBarra) {
        codigoBarra.setText(result);
    }

    @SuppressLint("CheckResult")
    public void restaurarProduto(int estado, long idproduto, boolean todosProdutos) {
        Completable.fromAction(() -> produtoRepository.restaurarProduto(estado, idproduto, todosProdutos))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        if (todosProdutos) {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.prods_rests), R.drawable.ic_toast_feito);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.prod_rest), R.drawable.ic_toast_feito);
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.prod_n_rest) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void importarProdutos(List<String> produtos, boolean vemCat, Long idcategoria) {
        Completable.fromAction(() -> produtoRepository.importarProdutos(produtos, vemCat, idcategoria))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.produto_importado), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.ficheiro_certo) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable.isDisposed()) {
            disposable.dispose();
        }
        if (compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
    }

}
