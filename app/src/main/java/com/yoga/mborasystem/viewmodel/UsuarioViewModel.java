package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
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
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.repository.UsuarioRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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


public class UsuarioViewModel extends AndroidViewModel {

    public boolean crud;
    private final Usuario usuario;
    private Disposable disposable;
    private ExecutorService executor;
    private final UsuarioRepository usuarioRepository;

    public UsuarioViewModel(Application application) {
        super(application);
        usuario = new Usuario();
        disposable = new CompositeDisposable();
        usuarioRepository = new UsuarioRepository(getApplication());
    }

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    private boolean isNumeroValido(String numero) {
        return !Patterns.PHONE.matcher(numero).matches();
    }

    public void criarUsuario(EditText nome, TextInputEditText telefone, TextInputEditText endereco, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estado, EditText codigoPin, EditText codigoPinNovamente, AlertDialog dialog) throws NoSuchAlgorithmException {
        validarUsuario(Ultilitario.Operacao.CRIAR, 0, nome, telefone, endereco, estado, codigoPin, codigoPinNovamente, dialog);
    }

    public void actualizarUsuario(long id, EditText nome, TextInputEditText telefone, TextInputEditText endereco, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estado, EditText codigoPin, EditText codigoPinNovamente, AlertDialog dialog) throws NoSuchAlgorithmException {
        validarUsuario(Ultilitario.Operacao.ACTUALIZAR, id, nome, telefone, endereco, estado, codigoPin, codigoPinNovamente, dialog);
    }

    private MutableLiveData<PagingData<Usuario>> listaUsuarios;

    public MutableLiveData<PagingData<Usuario>> getListaUsuarios() {
        if (listaUsuarios == null)
            listaUsuarios = new MutableLiveData<>();
        return listaUsuarios;
    }

    public void validarUsuario(Ultilitario.Operacao operacao, long id, EditText nome, TextInputEditText telefone, TextInputEditText endereco, @SuppressLint("UseSwitchCompatOrMaterialCode") SwitchCompat estado, EditText codigoPin, EditText codigoPinNovamente, AlertDialog dialog) throws NoSuchAlgorithmException {
        if (isCampoVazio(nome.getText().toString()) || Ultilitario.letras.matcher(nome.getText().toString()).find()) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_invalido));
        } else if (nome.length() < 5) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_curto));
        } else if ((!isCampoVazio(Objects.requireNonNull(telefone.getText()).toString()) && isNumeroValido(telefone.getText().toString())) || (!isCampoVazio(telefone.getText().toString()) && telefone.length() < 9)) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_invalido));
        } else if (!isCampoVazio(Objects.requireNonNull(endereco.getText()).toString()) && Ultilitario.letraNumero.matcher(endereco.getText().toString()).find()) {
            endereco.requestFocus();
            endereco.setError(getApplication().getString(R.string.endereco_invalido));
        } else if (isCampoVazio(codigoPin.getText().toString())) {
            codigoPin.requestFocus();
            codigoPin.setError(getApplication().getString(R.string.codigopin_vazio));
        } else if (codigoPin.length() > 6 || codigoPin.length() < 6) {
            codigoPin.requestFocus();
            codigoPin.setError(getApplication().getString(R.string.codigopin_incompleto));
        } else if (isCampoVazio(codigoPinNovamente.getText().toString())) {
            codigoPinNovamente.requestFocus();
            codigoPinNovamente.setError(getApplication().getString(R.string.codigopin_vazio));
        } else if (codigoPinNovamente.length() > 6 || codigoPinNovamente.length() < 6) {
            codigoPinNovamente.requestFocus();
            codigoPinNovamente.setError(getApplication().getString(R.string.codigopin_incompleto));
        } else if (!codigoPin.getText().toString().equals(codigoPinNovamente.getText().toString())) {
            codigoPinNovamente.requestFocus();
            codigoPinNovamente.setError(getApplication().getString(R.string.pin_diferente));
        } else {
            MainActivity.getProgressBar();
            usuario.setNome(nome.getText().toString());
            usuario.setTelefone(telefone.getText().toString());
            usuario.setEndereco(endereco.getText().toString());
            usuario.setEstado(estado.isChecked() ? 2 : 1);
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                usuario.setId(0);
                usuario.setCodigoPin(Ultilitario.gerarHash(codigoPin.getText().toString()));
                usuario.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()) + "/T");
                verificarCodigoPin(usuario, dialog);
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {
                usuario.setId(id);
                usuario.setData_modifica(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
                actualizarUsuario(usuario, false, dialog);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void criarUsuario(Usuario us, AlertDialog dialog) {
        Completable.fromAction(() -> usuarioRepository.insert(us))
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
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.usuario_criado), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.usuario_nao_criado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void actualizarUsuario(Usuario usuario, boolean isCodigoPin, AlertDialog dialog) {
        Completable.fromAction(() -> usuarioRepository.update(usuario, isCodigoPin))
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
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.alteracao_feita), R.drawable.ic_toast_feito);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.alteracao_nao_feita) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void verificarCodigoPin(Usuario us, AlertDialog dg) {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                if (usuarioRepository.confirmarCodigoPin(us.getCodigoPin()).isEmpty())
                    criarUsuario(us, dg);
                else {
                    handler.post(() -> {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.codigopin_invalido), R.drawable.ic_toast_erro);
                    });
                }
            } catch (Exception e) {
                handler.post(() -> {
                    MainActivity.dismissProgressBar();
                    Toast.makeText(getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @SuppressLint("CheckResult")
    public void eliminarUsuario(Usuario usuario, Dialog dg) {
        MainActivity.getProgressBar();
        Completable.fromAction(() -> usuarioRepository.delete(usuario))
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
                        if (dg != null)
                            dg.dismiss();

                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.usuario_eliminado), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.usuario_nao_eliminado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void consultarUsuarios(LifecycleOwner lifecycleOwner) {
        Flowable<PagingData<Usuario>> flowable = PagingRx.getFlowable(new Pager<>(new PagingConfig(20), usuarioRepository::getUsuarios));
        PagingRx.cachedIn(flowable, ViewModelKt.getViewModelScope(this));
        flowable.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
                .subscribe(categorias -> {
                    if (crud)
                        getListaUsuarios().postValue(categorias);
                    else
                        getListaUsuarios().setValue(categorias);
                }, e -> new Handler(Looper.getMainLooper()).post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_usuario) + "\n" + e.getMessage(), R.drawable.ic_toast_erro)));
    }

    public LiveData<Long> getQuantidadeUsuario() {
        return usuarioRepository.getQuantidadeUsuario();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable.isDisposed())
            disposable.dispose();

        if (executor != null)
            executor.shutdownNow();
    }
}
