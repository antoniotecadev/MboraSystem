package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.repository.ClienteCantinaRepository;
import com.yoga.mborasystem.repository.VendaRepository;
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
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ClienteCantinaViewModel extends AndroidViewModel {

    public boolean crud;
    private Disposable disposable;
    private final ClienteCantina clienteCantina;
    private final VendaRepository vendaRepository;
    private final CompositeDisposable compositeDisposable;
    private final ClienteCantinaRepository clienteCantinaRepository;

    public ClienteCantinaViewModel(Application application) {
        super(application);
        clienteCantina = new ClienteCantina();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        vendaRepository = new VendaRepository(getApplication());
        clienteCantinaRepository = new ClienteCantinaRepository(getApplication());
    }

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    private boolean isEmailValido(String email) {
        return (!isCampoVazio(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private boolean isNumeroValido(String numero) {
        return !Patterns.PHONE.matcher(numero).matches();
    }

    public static Pattern letraNumero = Pattern.compile("[^a-zA-Z0-9]");

    public void criarCliente(TextInputEditText nif, TextInputEditText nomeCliente, TextInputEditText telefone, TextInputEditText email, TextInputEditText endereco, AlertDialog dialog) {
        validarCliente(0, "", "", Ultilitario.Operacao.CRIAR, nif, nomeCliente, telefone, email, endereco, dialog);
    }

    public void actualizarCliente(long idcliente, String nifbi, String nome, TextInputEditText nif, TextInputEditText nomeCliente, TextInputEditText telefone, TextInputEditText email, TextInputEditText endereco, AlertDialog dialog) {
        validarCliente(idcliente, nifbi, nome, Ultilitario.Operacao.ACTUALIZAR, nif, nomeCliente, telefone, email, endereco, dialog);
    }

    private MutableLiveData<Event<Boolean>> isElimina;

    public MutableLiveData<Event<Boolean>> getBooleanMutableLiveData() {
        if (isElimina == null)
            isElimina = new MutableLiveData<>();
        return isElimina;
    }

    private MutableLiveData<PagingData<ClienteCantina>> listaClientesCantina;

    public MutableLiveData<PagingData<ClienteCantina>> getListaClientesCantina() {
        if (listaClientesCantina == null)
            listaClientesCantina = new MutableLiveData<>();
        return listaClientesCantina;
    }

    private MutableLiveData<List<ClienteCantina>> cliente;

    public MutableLiveData<List<ClienteCantina>> getCliente() {
        if (cliente == null)
            cliente = new MutableLiveData<>();
        return cliente;
    }

    private MutableLiveData<Event<List<ClienteCantina>>> listaClienteExport;

    public MutableLiveData<Event<List<ClienteCantina>>> getListaClientesExport() {
        if (listaClienteExport == null)
            listaClienteExport = new MutableLiveData<>();
        return listaClienteExport;
    }

    private void validarCliente(long idcliente, String nifbi, String nome, Ultilitario.Operacao operacao, TextInputEditText nif, TextInputEditText nomeCliente, TextInputEditText telefone, TextInputEditText email, TextInputEditText endereco, AlertDialog dialog) {
        if (!isCampoVazio(Objects.requireNonNull(nif.getText()).toString()) && ((letraNumero.matcher(Objects.requireNonNull(nif.getText()).toString()).find()) || Objects.requireNonNull(nif.getText()).toString().length() > 14 || Objects.requireNonNull(nif.getText()).toString().length() < 10)) {
            nif.requestFocus();
            nif.setError(getApplication().getString(R.string.nifbi_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(nomeCliente.getText()).toString()) || Ultilitario.letras.matcher(nomeCliente.getText().toString()).find()) {
            nomeCliente.requestFocus();
            nomeCliente.setError(getApplication().getString(R.string.nome_invalido));
        } else if ((!isCampoVazio(Objects.requireNonNull(telefone.getText()).toString()) && isNumeroValido(telefone.getText().toString())) || (!isCampoVazio(telefone.getText().toString()) && telefone.length() < 9)) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_invalido));
        } else if (isEmailValido(Objects.requireNonNull(email.getText()).toString())) {
            email.requestFocus();
            email.setError(getApplication().getString(R.string.email_invalido));
        } else if (!isCampoVazio(Objects.requireNonNull(endereco.getText()).toString()) && Ultilitario.letraNumero.matcher(endereco.getText().toString()).find()) {
            endereco.requestFocus();
            endereco.setError(getApplication().getString(R.string.endereco_invalido));
        } else {
            MainActivity.getProgressBar();
            clienteCantina.setNif(nif.getText().toString().trim());
            clienteCantina.setNome(nomeCliente.getText().toString());
            clienteCantina.setTelefone(telefone.getText().toString().trim());
            clienteCantina.setEmail(email.getText().toString().trim());
            clienteCantina.setEndereco(endereco.getText().toString().trim());
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                clienteCantina.setId(0);
                clienteCantina.setEstado(Ultilitario.UM);
                clienteCantina.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                nifBiExiste(clienteCantina, dialog, true);
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {
                clienteCantina.setId(idcliente);
                clienteCantina.setEstado(Ultilitario.DOIS);
                clienteCantina.setData_modifica(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                if ((clienteCantina.getNif().equals(nifbi) || nifbi.isEmpty() || nifbi.equals("999999999")) && clienteCantina.getNome().equals(nome))
                    actualizarCliente(clienteCantina, dialog);
                else
                    verificarCompraCliente(clienteCantina, dialog, false, nifbi);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void criarClienteCantina(ClienteCantina clienteCantina, AlertDialog dialog) {
        Completable.fromAction(() -> clienteCantinaRepository.insert(clienteCantina))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
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

    public void consultarClientesCantina(LifecycleOwner lifecycleOwner, boolean isSearch, String cliente) {
        Flowable<PagingData<ClienteCantina>> flowable = PagingRx.getFlowable(new Pager<>(new PagingConfig(20), () -> clienteCantinaRepository.getClientesCantina(isSearch, cliente)));
        PagingRx.cachedIn(flowable, ViewModelKt.getViewModelScope(this));
        flowable.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
                .subscribe(clientes -> {
                    if (crud)
                        getListaClientesCantina().postValue(clientes);
                    else
                        getListaClientesCantina().setValue(clientes);
                }, e -> new Handler(Looper.getMainLooper()).post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_clientes) + "\n" + e.getMessage(), R.drawable.ic_toast_erro)));
    }

    public void consultarClienteCantina(String cliente, boolean isForDocumentSaft, List<Long> idcliente) {
        disposable = clienteCantinaRepository.getClienteCantina(cliente, isForDocumentSaft, idcliente)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(clienteCantinas -> getCliente().setValue(clienteCantinas), e -> {
                    MainActivity.dismissProgressBar();
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro);
                });
    }

    public LiveData<Long> getQuantidadeCliente() {
        return clienteCantinaRepository.getQuantidadeCliente();
    }

    @SuppressLint("CheckResult")
    public void actualizarCliente(ClienteCantina clienteCantina, AlertDialog dialog) {
        Completable.fromAction(() -> clienteCantinaRepository.update(clienteCantina))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
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

    public void verificarCompraCliente(ClienteCantina ct, AlertDialog dg, boolean isElimina, String nifbi) {
        vendaRepository.verificarCompras(clienteCantina.getId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<List<Venda>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(@io.reactivex.rxjava3.annotations.NonNull List<Venda> vendas) {
                        if (vendas.isEmpty()) {
                            if (isElimina)
                                eliminarCliente(ct, dg);
                            else
                                nifBiExiste(ct, dg, false);
                        } else {
                            if (isElimina)
                                getBooleanMutableLiveData().setValue(new Event<>(isElimina));
                            else {
                                boolean contain = false;
                                for (Venda venda : vendas)
                                    if (TextUtils.split(venda.getNome_cliente(), "-")[2].equals(nifbi))
                                        contain = true;
                                if (contain)
                                    getBooleanMutableLiveData().setValue(new Event<>(isElimina));
                                else
                                    nifBiExiste(ct, dg, false);
                            }
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void eliminarCliente(ClienteCantina clienteCantina, AlertDialog dg) {
        MainActivity.getProgressBar();
        Completable.fromAction(() -> clienteCantinaRepository.delete(clienteCantina))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        if (dg != null)
                            dg.dismiss();

                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.cli_elim), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.cli_n_elim) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });

    }

    private void nifBiExiste(ClienteCantina ct, AlertDialog dialog, boolean isCriar) {
        compositeDisposable.add(clienteCantinaRepository.nifBiExiste(ct.getNif())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cliente -> {
                    if (cliente.isEmpty())
                        if (isCriar)
                            criarClienteCantina(ct, dialog);
                        else
                            actualizarCliente(ct, dialog);
                    else {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.cli_ja_exi), R.drawable.ic_toast_erro);
                    }
                }, throwable -> {
                    MainActivity.dismissProgressBar();
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), throwable.getMessage(), R.drawable.ic_toast_erro);
                }));
    }

    public void exportarClientes() throws Exception {
        getListaClientesExport().postValue(new Event<>(clienteCantinaRepository.getClientesExport()));
    }

    public void importarClientes(List<String> clientes, Handler handler) {
        clienteCantinaRepository.importarClientes(clientes, getApplication(), handler);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable.isDisposed())
            disposable.dispose();

        if (compositeDisposable.isDisposed())
            compositeDisposable.dispose();

    }

}
