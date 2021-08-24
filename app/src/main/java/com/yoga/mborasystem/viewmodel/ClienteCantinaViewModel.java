package com.yoga.mborasystem.viewmodel;

import android.app.Application;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.repository.ClienteCantinaRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ClienteCantinaViewModel extends AndroidViewModel {

    private Disposable disposable;
    private ClienteCantina clienteCantina;
    private CompositeDisposable compositeDisposable;
    private ClienteCantinaRepository clienteCantinaRepository;

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
        validarCliente(Ultilitario.Operacao.CRIAR, nomeCliente, telefone, dialog);
    }

    private MutableLiveData<List<ClienteCantina>> listaClientesCantina;

    public MutableLiveData<List<ClienteCantina>> getListaClientesCantina() {
        if (listaClientesCantina == null) {
            listaClientesCantina = new MutableLiveData<>();
        }
        return listaClientesCantina;
    }

    private void validarCliente(Ultilitario.Operacao operacao, TextInputEditText nomeCliente, TextInputEditText telefone, AlertDialog dialog) {
        if (isCampoVazio(nomeCliente.getText().toString()) || Ultilitario.letras.matcher(nomeCliente.getText().toString()).find()) {
            nomeCliente.requestFocus();
            nomeCliente.setError(getApplication().getString(R.string.nome_invalido));
        } else if ((!isCampoVazio(telefone.getText().toString()) && isNumeroValido(telefone.getText().toString())) || (!isCampoVazio(telefone.getText().toString()) && telefone.length() < 9)) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_invalido));
        } else {
            clienteCantina.setNome(nomeCliente.getText().toString());
            clienteCantina.setTelefone(telefone.getText().toString());
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                clienteCantina.setId(0);
                clienteCantina.setEstado(1);
                clienteCantina.setData_cria(Ultilitario.getDateCurrent());
                criarClienteCantina(clienteCantina, dialog);
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {
//                clienteCantina.setId(id);
                clienteCantina.setData_modifica(Ultilitario.getDateCurrent());
//                actualizarUsuario(usuario, false, dialog);
            }
        }
    }

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
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cliente_criado), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.cliente_nao_criado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void consultarClientesCantina() {
        compositeDisposable.add(clienteCantinaRepository.getClientesCantina()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(clientesCantina -> {
                    getListaClientesCantina().setValue(clientesCantina);
                }, throwable -> {
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_usuario) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                }));
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
