package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Switch;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.repository.UsuarioRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class UsuarioViewModel extends AndroidViewModel {

    private final Usuario usuario;
    private Disposable disposable;
    private final UsuarioRepository usuarioRepository;
    private final CompositeDisposable compositeDisposable;

    public UsuarioViewModel(Application application) {
        super(application);
        usuario = new Usuario();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        usuarioRepository = new UsuarioRepository(getApplication());
    }

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    private boolean isNumeroValido(String numero) {
        return !Patterns.PHONE.matcher(numero).matches();
    }

    public void criarUsuario(EditText nome, TextInputEditText telefone, TextInputEditText endereco, @SuppressLint("UseSwitchCompatOrMaterialCode") Switch estado, EditText codigoPin, EditText codigoPinNovamente, AlertDialog dialog) throws NoSuchAlgorithmException {
        validarUsuario(Ultilitario.Operacao.CRIAR, 0, nome, telefone, endereco, estado, codigoPin, codigoPinNovamente, dialog);
    }

    public void actualizarUsuario(long id, EditText nome, TextInputEditText telefone, TextInputEditText endereco, @SuppressLint("UseSwitchCompatOrMaterialCode") Switch estado, EditText codigoPin, EditText codigoPinNovamente, AlertDialog dialog) throws NoSuchAlgorithmException {
        validarUsuario(Ultilitario.Operacao.ACTUALIZAR, id, nome, telefone, endereco, estado, codigoPin, codigoPinNovamente, dialog);
    }

    private MutableLiveData<List<Usuario>> listaUsuarios;

    public MutableLiveData<List<Usuario>> getListaUsuarios() {
        if (listaUsuarios == null) {
            listaUsuarios = new MutableLiveData<>();
        }
        return listaUsuarios;
    }

    public void validarUsuario(Ultilitario.Operacao operacao, long id, EditText nome, TextInputEditText telefone, TextInputEditText endereco, @SuppressLint("UseSwitchCompatOrMaterialCode") Switch estado, EditText codigoPin, EditText codigoPinNovamente, AlertDialog dialog) throws NoSuchAlgorithmException {
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
            usuario.setEstado(estado.isChecked() ? Ultilitario.DOIS : Ultilitario.UM);
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                usuario.setId(0);
                usuario.setCodigoPin(Ultilitario.gerarHash(codigoPin.getText().toString()));
                usuario.setData_cria(Ultilitario.getDateCurrent());
                verificarCodigoPin(usuario, dialog);
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {
                usuario.setId(id);
                usuario.setData_modifica(Ultilitario.getDateCurrent());
                actualizarUsuario(usuario, false, dialog);
            }
        }
    }

    @SuppressLint("CheckResult")
    private void criarUsuario(Usuario us, AlertDialog dialog) {
        Completable.fromAction(() -> usuarioRepository.insert(us))
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
                .subscribeWith(new CompletableObserver() {
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
        usuarioRepository.confirmarCodigoPin(us.getCodigoPin())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Usuario>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Usuario usuario) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.codigopin_invalido), R.drawable.ic_toast_erro);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        criarUsuario(us, dg);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void eliminarUsuario(Usuario usuario, Dialog dg) {
        MainActivity.getProgressBar();
        Completable.fromAction(() -> usuarioRepository.delete(usuario))
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
                        if (dg != null) {
                            dg.dismiss();
                        }
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.usuario_eliminado), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.usuario_nao_eliminado) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void consultarUsuarios() {
        compositeDisposable.add(usuarioRepository.getUsuarios()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(usuarios -> getListaUsuarios().setValue(usuarios), e -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_usuario) + "\n" + e.getMessage(), R.drawable.ic_toast_erro)));
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
