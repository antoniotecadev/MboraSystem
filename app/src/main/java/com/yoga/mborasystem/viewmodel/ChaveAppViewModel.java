package com.yoga.mborasystem.viewmodel;

import android.app.Application;

import com.yoga.mborasystem.model.entidade.ChaveApp;
import com.yoga.mborasystem.repository.RepositoryChaveApp;
import com.yoga.mborasystem.util.Ultilitario;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ChaveAppViewModel extends AndroidViewModel {

    Disposable disposable;
    RepositoryChaveApp repositoryChaveApp;

    public ChaveAppViewModel(@NonNull Application application) {
        super(application);
        disposable = new CompositeDisposable();
        repositoryChaveApp = new RepositoryChaveApp(application);
        chaveAppExiste();
    }

    private MutableLiveData<Ultilitario.Existe> existeMutableLiveData;

    public MutableLiveData<Ultilitario.Existe> getExisteMutableLiveData() {
        if (existeMutableLiveData == null) {
            existeMutableLiveData = new MutableLiveData<>();
        }
        return existeMutableLiveData;
    }

    public void chaveAppExiste() {
        repositoryChaveApp.chaveAppExiste().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<ChaveApp>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull ChaveApp chaveApp) {
                        getExisteMutableLiveData().setValue(Ultilitario.Existe.SIM);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        getExisteMutableLiveData().setValue(Ultilitario.Existe.NAO);
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
