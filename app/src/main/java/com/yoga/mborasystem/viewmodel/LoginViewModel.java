package com.yoga.mborasystem.viewmodel;

import android.app.Application;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.repository.UsuarioRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.security.NoSuchAlgorithmException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends AndroidViewModel {

    private int contar = 0;
    private Disposable disposable;
    private MutableLiveData<String> infoPin;
    private UsuarioRepository usuarioRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        disposable = new CompositeDisposable();
        usuarioRepository = new UsuarioRepository(application);
    }

    public MutableLiveData<String> getinfoPin() {
        if (infoPin == null) {
            infoPin = new MutableLiveData<>();
        }
        return infoPin;
    }

    MutableLiveData<Usuario> usuarioMutableLiveData;

    public MutableLiveData<Usuario> getUsuarioMutableLiveData() {
        if (usuarioMutableLiveData == null) {
            usuarioMutableLiveData = new MutableLiveData<>();
        }
        return usuarioMutableLiveData;
    }

    private void contarIntroducaoPin() {
        ++contar;
        if (contar == Ultilitario.TRES) {
            infoPin.setValue(getApplication().getString(R.string.infoAviso1));
        } else if (contar == Ultilitario.QUATRO) {
            infoPin.setValue(getApplication().getString(R.string.infoAviso2));
        } else if (contar > Ultilitario.QUATRO) {
            infoPin.setValue(String.valueOf(Ultilitario.QUATRO));
        }
    }

    public void logar(String cp) throws NoSuchAlgorithmException {
        usuarioRepository.confirmarCodigoPin(Ultilitario.gerarHash(cp))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Usuario>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Usuario usuario) {
                        if (usuario.getEstado() == Ultilitario.DOIS) {
                            infoPin.setValue(getApplication().getString(R.string.usuario_bloqueado) + "\n" + getApplication().getString(R.string.info_usuario_bloqueado));
                        } else {
                            getUsuarioMutableLiveData().setValue(usuario);
                        }
                        MainActivity.dismissProgressBar();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        infoPin.setValue(getApplication().getString(R.string.infoPinIncorreto));
                        contarIntroducaoPin();
                        MainActivity.dismissProgressBar();
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null || !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

}