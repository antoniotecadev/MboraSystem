package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.ChaveApp;

import androidx.room.Dao;
import androidx.room.Query;

import io.reactivex.rxjava3.core.Single;

@Dao
public interface ChaveAppDao {

    @Query("SELECT * FROM chaveapp")
    Single<ChaveApp> chaveAppExiste();
}