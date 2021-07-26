package com.yoga.mborasystem.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.repository.VendaRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VendaViewModel extends AndroidViewModel {

    private Venda venda;
    private Disposable disposable;
    private VendaRepository vendaRepository;
    private CompositeDisposable compositeDisposable;


    public VendaViewModel(@NonNull Application application) {
        super(application);
        venda = new Venda();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        vendaRepository = new VendaRepository(getApplication());
    }

    public void cadastrarVenda(TextInputEditText txtNomeCliente, TextInputEditText desconto, int quantidade, int valorBase, int valorIva, String formaPagamento, int totalDesconto, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit) {
        venda.setNome_cliente(txtNomeCliente.getText().toString());
        venda.setDesconto(Ultilitario.removerKZ(desconto));
        venda.setQuantidade(quantidade);
        venda.setValor_base(valorBase);
        venda.setValor_iva(valorIva);
        venda.setPagamento(formaPagamento);
        venda.setTotal_desconto(totalDesconto);
        venda.setTotal_venda(totalVenda);
        venda.setData_cria(Ultilitario.getDateCurrent());
        Completable.fromAction(() -> vendaRepository.insert(venda, produtos,precoTotalUnit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Toast.makeText(getApplication(), "Vendido", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.venda_nao_efectuada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
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
