package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ChaveAppDao;
import com.yoga.mborasystem.model.entidade.ChaveApp;

import java.lang.ref.WeakReference;

import io.reactivex.Single;

public class RepositoryChaveApp {

    ChaveAppDao chaveAppDao ;
    WeakReference<Context> contextWeakReference;

    public RepositoryChaveApp(Context context) {
        contextWeakReference = new WeakReference<>(context);
        AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
        chaveAppDao = appDataBase.chaveAppDao();
    }

    public Single<ChaveApp> chaveAppExiste() {
        return chaveAppDao.chaveAppExiste();
    }

}
