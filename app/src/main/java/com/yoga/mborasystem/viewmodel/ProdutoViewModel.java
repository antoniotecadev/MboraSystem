package com.yoga.mborasystem.viewmodel;

import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.zxing.integration.android.IntentResult;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.repository.ProdutoRepository;
import com.yoga.mborasystem.util.Event;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ProdutoViewModel extends AndroidViewModel {

    private Produto produto;
    private Disposable disposable;
    private ProdutoRepository produtoRepository;
    private CompositeDisposable compositeDisposable;

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
    private MutableLiveData<Event<List<Produto>>> listaProdutosImport;
    private MutableLiveData<Map<Long, Produto>> listaProdutosFactura;

    public MutableLiveData<List<Produto>> getListaProdutos() {
        if (listaProdutos == null) {
            listaProdutos = new MutableLiveData<>();
        }
        return listaProdutos;
    }

    public MutableLiveData<Event<List<Produto>>> getListaProdutosImport() {
        if (listaProdutosImport == null) {
            listaProdutosImport = new MutableLiveData<>();
        }
        return listaProdutosImport;
    }

    public MutableLiveData<Map<Long, Produto>> getListaProdutosFactura() {
        if (listaProdutosFactura == null) {
            listaProdutosFactura = new MutableLiveData<>();
        }
        return listaProdutosFactura;
    }

    public void validarProduto(Ultilitario.Operacao operacao, long id, EditText nome, TextInputEditText preco, TextInputEditText precofornecedor, EditText quantidade, EditText codigoBarra, MaterialCheckBox checkIva, Switch estado, AlertDialog dialog, Boolean continuar, long idcategoria) {
        if (isCampoVazio(nome.getText().toString()) || letraNumero.matcher(nome.getText().toString()).find()) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_invalido));
        } else if (isCampoVazio(precofornecedor.getText().toString())) {
            precofornecedor.requestFocus();
            precofornecedor.setError(getApplication().getString(R.string.preco_invalido));
        } else if (isCampoVazio(preco.getText().toString())) {
            preco.requestFocus();
            preco.setError(getApplication().getString(R.string.preco_invalido));
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
            produto.setPreco(Ultilitario.removerKZ(preco));
            produto.setPrecofornecedor(Ultilitario.removerKZ(precofornecedor));
            produto.setQuantidade(Integer.parseInt(quantidade.getText().toString()));
            produto.setCodigoBarra(codigoBarra.getText().toString());
            produto.setIva(checkIva.isChecked() ? true : false);
            produto.setEstado(estado.isChecked() ? Ultilitario.DOIS : Ultilitario.UM);
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                produto.setIdcategoria(idcategoria);
                produto.setData_cria(Ultilitario.getDateCurrent());
                criarProduto(produto, dialog, continuar);
                nome.setText("");
                preco.setText(getApplication().getText(R.string.preco_zero));
                precofornecedor.setText(getApplication().getText(R.string.preco_zero));
                quantidade.setText("1");
                codigoBarra.setText("");
                nome.requestFocus();
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {
                produto.setId(id);
                produto.setData_modifica(Ultilitario.getDateCurrent());
                actualizarProduto(produto, dialog);
            }
        }
    }

    public void criarProduto(EditText nome, TextInputEditText preco, TextInputEditText precofornecedor, TextInputEditText quantidade, EditText codigoBarra, MaterialCheckBox checkIva, Switch estado, AlertDialog dialog, boolean c, long idcategoria) {
        validarProduto(Ultilitario.Operacao.CRIAR, 0, nome, preco, precofornecedor, quantidade, codigoBarra, checkIva, estado, dialog, c, idcategoria);
    }

    public void actualizarProduto(long id, EditText nome, TextInputEditText preco, TextInputEditText precofornecedor, EditText quantidade, EditText codigoBarra, MaterialCheckBox checkIva, Switch estado, long idcategoria, AlertDialog dialog) {
        validarProduto(Ultilitario.Operacao.ACTUALIZAR, id, nome, preco, precofornecedor, quantidade, codigoBarra, checkIva, estado, dialog, false, idcategoria);
    }

    private int contar = 0;

    private void criarProduto(Produto produto, AlertDialog dialog, Boolean continuar) {
        Completable.fromAction(() -> produtoRepository.insert(produto))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.produto_criado) + " " + ++contar, R.drawable.ic_toast_feito);
                        if (continuar) {
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.produto_nao_criado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    private void actualizarProduto(Produto produto, AlertDialog dialog) {
        Completable.fromAction(() -> produtoRepository.update(produto))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.alteracao_feita), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (e.getMessage().contains("UNIQUE")) {
                            Ultilitario.showToast(getApplication(), Color.rgb(255, 187, 51), getApplication().getString(R.string.produto_existe) + " ou " + getApplication().getString(R.string.codigobarra_existe), R.drawable.ic_toast_erro);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.produto_nao_criado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                        }
                    }
                });
    }

    public void eliminarProduto(Produto produto, boolean lx, Dialog dialog) {
        Completable.fromAction(() -> produtoRepository.delete(produto, lx))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.produto_eliminado), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.produto_nao_eliminado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void consultarProdutos(long idcategoria, boolean isImport, SwipeRefreshLayout mySwipeRefreshLayout) {
        compositeDisposable.add(produtoRepository.getProdutos(idcategoria)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Produto>>() {
                    @Override
                    public void accept(List<Produto> produtos) throws Exception {
                        if (isImport) {
                            getListaProdutosImport().setValue(new Event<>(produtos));
                        } else {
                            getListaProdutos().setValue(produtos);
                            Ultilitario.swipeRefreshLayout(mySwipeRefreshLayout);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_produto) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                        Ultilitario.swipeRefreshLayout(mySwipeRefreshLayout);
                    }
                }));
    }

    public void validarProdutoFiltro(long idcat, TextInputEditText idprodnome, TextInputEditText codigoBar, TextInputEditText precoMin, TextInputEditText precoMax, Switch estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
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
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
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
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
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
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setCodigoBarra(codigoBar.getText().toString());
            filtrar(produtoRepository.getFilterProdutosCodBar(idcat, produto.getCodigoBarra()), dialog);
        }
    }

    public void validarProdutoEstado(long idcat, Switch estadoProd, AlertDialog dialog) {
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
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
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

    public void validarProdutoNomePrecoEstado(long idcat, TextInputEditText idprodnome, TextInputEditText precoMin, TextInputEditText precoMax, Switch estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
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

    public void validarProdutoNomeCodBarEstado(long idcat, TextInputEditText idprodnome, TextInputEditText codigoBar, Switch estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getCodigoBarra(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoCodBarPrecoEstado(long idcat, TextInputEditText codigoBar, TextInputEditText precoMin, TextInputEditText precoMax, Switch estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
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
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
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
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
            idprodnome.requestFocus();
            idprodnome.setError(getApplication().getString(R.string.nome_referencia_invalida));
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setNome(idprodnome.getText().toString());
            produto.setCodigoBarra(codigoBar.getText().toString());
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getNome(), produto.getCodigoBarra()), dialog);
        }
    }

    public void validarProdutoNomeEstado(long idcat, TextInputEditText idprodnome, Switch estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(idprodnome.getText().toString()) || letraNumero.matcher(idprodnome.getText().toString()).find()) {
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
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            filtrar(produtoRepository.getFilterProdutosCodBar(idcat, produto.getCodigoBarra(), produto.getPrecoMin(), produto.getPrecoMax()), dialog);
        }
    }

    public void validarProdutoPrecoEstado(long idcat, TextInputEditText precoMin, TextInputEditText precoMax, Switch estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(precoMin.getText().toString())) {
            precoMin.requestFocus();
            precoMin.setError(getApplication().getString(R.string.preco_min_invalido));
        } else if (isCampoVazio(precoMax.getText().toString())) {
            precoMax.requestFocus();
            precoMax.setError(getApplication().getString(R.string.preco_max_invalido));
        } else {
            produto.setPrecoMin(Integer.parseInt(precoMin.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setPrecoMax(Integer.parseInt(precoMax.getText().toString().replace(",", "").replace("Kz", "").replaceAll("\\s+", "")));
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutos(idcat, produto.getPrecoMin(), produto.getPrecoMax(), produto.getEstado()), dialog);
        }
    }

    public void validarProdutoCodBarEstado(long idcat, TextInputEditText codigoBar, Switch estadoProd, AlertDialog dialog) {
        if (isCampoVazio(String.valueOf(idcat))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.id_categoria_vazio), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(codigoBar.getText().toString()) || numero.matcher(codigoBar.getText().toString()).find()) {
            codigoBar.requestFocus();
            codigoBar.setError(getApplication().getString(R.string.codigobarra_invalido));
        } else {
            produto.setCodigoBarra(codigoBar.getText().toString());
            produto.setEstado(estadoProd.isChecked() ? 2 : 1);
            filtrar(produtoRepository.getFilterProdutosCodBar(idcat, produto.getCodigoBarra(), produto.getEstado()), dialog);
        }
    }

    private void filtrar(Flowable<List<Produto>> listProduto, AlertDialog dialog) {
        compositeDisposable.add(listProduto
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Produto>>() {
                    @Override
                    public void accept(List<Produto> produtos) throws Exception {
                        getListaProdutos().setValue(produtos);
                        if (!produtos.isEmpty()) {
//                            Ultilitario.showToast(getApplication(), Color.rgb(102, 50, 0), getApplication().getString(R.string.produto_nao_encontrado), R.drawable.ic_baseline_close_24);
                            dialog.dismiss();
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_produto) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                    }
                }));
    }

    public LiveData<Integer> consultarQuantidadeProduto(long idcategoria) {
        return produtoRepository.getQuantidadeProduto(idcategoria);
    }

    public void codigoBarra(IntentResult result, TextInputEditText codigoBarra) {
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getApplication(), R.string.scaner_code_bar_cancelado, Toast.LENGTH_SHORT).show();
            } else {
                codigoBarra.setText("");
                codigoBarra.setText("" + result.getContents());
            }
        }
    }

    public void searchProduto(String produto) {
        compositeDisposable.add(produtoRepository.searchProdutos(produto)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Produto>>() {
                    @Override
                    public void accept(List<Produto> produtoPagingData) throws Exception {
                        getListaProdutos().setValue(produtoPagingData);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_produto) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                    }
                }));
    }

    public void importarProdutos(List<String> produtos) {
        produtoRepository.importarProdutos(produtos, getApplication());
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
