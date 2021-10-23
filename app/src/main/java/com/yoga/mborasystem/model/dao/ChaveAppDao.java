package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.ChaveApp;

import androidx.room.Dao;
import androidx.room.Query;
import io.reactivex.Single;

@Dao
public interface ChaveAppDao {

    @Query("SELECT * FROM chaveapp")
    Single<ChaveApp> chaveAppExiste();
}