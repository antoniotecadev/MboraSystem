package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Usuario;
import com.yoga.mborasystem.repository.ClienteRepository;
import com.yoga.mborasystem.repository.UsuarioRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;


public class LoginViewModel extends AndroidViewModel {

    private int contar = 0;
    private final Bundle bundle;
    private final Disposable disposable;
    private ExecutorService executor;
    private MutableLiveData<String> infoPin;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        bundle = new Bundle();
        disposable = new CompositeDisposable();
        clienteRepository = new ClienteRepository(application);
        usuarioRepository = new UsuarioRepository(application);
    }

    public MutableLiveData<String> getinfoPin() {
        if (infoPin == null)
            infoPin = new MutableLiveData<>();
        return infoPin;
    }

    MutableLiveData<Usuario> usuarioMutableLiveData;

    public MutableLiveData<Usuario> getUsuarioMutableLiveData() {
        if (usuarioMutableLiveData == null)
            usuarioMutableLiveData = new MutableLiveData<>();
        return usuarioMutableLiveData;
    }

    private void contarIntroducaoPin() {
        ++contar;
        if (contar == Ultilitario.TRES)
            infoPin.postValue(getApplication().getString(R.string.infoAviso1));
        else if (contar == Ultilitario.QUATRO)
            infoPin.postValue(getApplication().getString(R.string.infoAviso2));
        else if (contar > Ultilitario.QUATRO)
            infoPin.postValue(String.valueOf(Ultilitario.QUATRO));
    }

    @SuppressLint("CheckResult")
    public void logar(String cp, Context context, View view) {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                List<Usuario> usuario = usuarioRepository.confirmarCodigoPin(Ultilitario.gerarHash(cp));
                if (usuario.isEmpty()) {
                    if (Ultilitario.getBooleanValue(context, "actpin") && PreferenceManager.getDefaultSharedPreferences(context).getString("pinadmin", "0").equals(cp))
                        logar(view, handler);
                    else {
                        infoPin.postValue(getApplication().getString(R.string.infoPinIncorreto));
                        contarIntroducaoPin();
                    }
                } else {
                    if (usuario.get(0).getEstado() == 2)
                        infoPin.postValue(getApplication().getString(R.string.usuario_bloqueado) + "\n" + getApplication().getString(R.string.info_usuario_bloqueado));
                    else
                        getUsuarioMutableLiveData().postValue(usuario.get(0));
                }
                MainActivity.dismissProgressBar();
            } catch (Exception e) {
                handler.post(() -> {
                    MainActivity.dismissProgressBar();
                    Toast.makeText(getApplication().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public void logar(View view, Handler handler) throws Exception {
        Ultilitario.setValueUsuarioMaster(bundle, clienteRepository.clienteExiste(), getApplication().getApplicationContext());
        handler.post(() -> Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_navigation, bundle));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable.isDisposed())
            disposable.dispose();
        if (executor != null)
            executor.shutdownNow();
        if (bundle != null)
            bundle.clear();
    }

}