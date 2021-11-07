package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.repository.ClienteCantinaRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ClienteCantinaViewModel extends AndroidViewModel {

    private Disposable disposable;
    private final ClienteCantina clienteCantina;
    private final CompositeDisposable compositeDisposable;
    private final ClienteCantinaRepository clienteCantinaRepository;

    public ClienteCantinaViewModel(Application application) {
        super(application);
        clienteCantina = new ClienteCantina();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        clienteCantinaRepository = new ClienteCantinaRepository(getApplication());
    }

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    private boolean isNumeroValido(String numero) {
        return !Patterns.PHONE.matcher(numero).matches();
    }

    public void criarCliente(TextInputEditText nomeCliente, TextInputEditText telefone, AlertDialog dialog) {
        validarCliente(0, Ultilitario.Operacao.CRIAR, nomeCliente, telefone, dialog);
    }

    public void actualizarCliente(long idcliente, TextInputEditText nomeCliente, TextInputEditText telefone, AlertDialog dialog) {
        validarCliente(idcliente, Ultilitario.Operacao.ACTUALIZAR, nomeCliente, telefone, dialog);
    }

    private MutableLiveData<List<ClienteCantina>> listaClientesCantina;

    public MutableLiveData<List<ClienteCantina>> getListaClientesCantina() {
        if (listaClientesCantina == null) {
            listaClientesCantina = new MutableLiveData<>();
        }
        return listaClientesCantina;
    }

    private void validarCliente(long idcliente, Ultilitario.Operacao operacao, TextInputEditText nomeCliente, TextInputEditText telefone, AlertDialog dialog) {
        if (isCampoVazio(Objects.requireNonNull(nomeCliente.getText()).toString()) || Ultilitario.letras.matcher(nomeCliente.getText().toString()).find()) {
            nomeCliente.requestFocus();
            nomeCliente.setError(getApplication().getString(R.string.nome_invalido));
        } else if ((!isCampoVazio(Objects.requireNonNull(telefone.getText()).toString()) && isNumeroValido(telefone.getText().toString())) || (!isCampoVazio(telefone.getText().toString()) && telefone.length() < 9)) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_invalido));
        } else {
            MainActivity.getProgressBar();
            clienteCantina.setNome(nomeCliente.getText().toString());
            clienteCantina.setTelefone(telefone.getText().toString());
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                clienteCantina.setId(Ultilitario.ZERO);
                clienteCantina.setEstado(Ultilitario.UM);
                clienteCantina.setData_cria(Ultilitario.getDateCurrent());
                criarClienteCantina(clienteCantina, dialog);
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {
                clienteCantina.setId(idcliente);
                clienteCantina.setEstado(Ultilitario.DOIS);
                clienteCantina.setNome(nomeCliente.getText().toString());
                clienteCantina.setTelefone(telefone.getText().toString());
                clienteCantina.setData_modifica(Ultilitario.getDateCurrent());
                actualizarCliente(clienteCantina, dialog);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void criarClienteCantina(ClienteCantina clienteCantina, AlertDialog dialog) {
        Completable.fromAction(() -> clienteCantinaRepository.insert(clienteCantina))
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
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cliente_criado), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.cliente_nao_criado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void consultarClientesCantina(SwipeRefreshLayout mySwipeRefreshLayout) {
        compositeDisposable.add(clienteCantinaRepository.getClientesCantina()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(clientesCantina -> {
                    getListaClientesCantina().setValue(clientesCantina);
                    Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
                    Ultilitario.swipeRefreshLayout(mySwipeRefreshLayout);
                }, throwable -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_usuario) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro)));
    }

    public void searchCliente(String cliente) {
        compositeDisposable.add(clienteCantinaRepository.searchCliente(cliente)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(clientes -> {
                    getListaClientesCantina().setValue(clientes);
                    Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
                }, e -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_clientes) + "\n" + e.getMessage(), R.drawable.ic_toast_erro)));
    }

    @SuppressLint("CheckResult")
    public void actualizarCliente(ClienteCantina clienteCantina, AlertDialog dialog) {
        Completable.fromAction(() -> clienteCantinaRepository.update(clienteCantina.getNome(), clienteCantina.getTelefone(), clienteCantina.getEstado(), clienteCantina.getData_modifica(), clienteCantina.getId()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.alteracao_feita), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.alteracao_nao_feita) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void eliminarCliente(ClienteCantina clienteCantina, Dialog dg) {
        MainActivity.getProgressBar();
        Completable.fromAction(() -> clienteCantinaRepository.delete(clienteCantina))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        if (dg != null) {
                            dg.dismiss();
                        }
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cli_elim), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.cli_n_elim) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null || !Objects.requireNonNull(disposable).isDisposed()) {
            disposable.dispose();
        }
        if (compositeDisposable != null || !Objects.requireNonNull(compositeDisposable).isDisposed()) {
            compositeDisposable.clear();
        }
    }

}
