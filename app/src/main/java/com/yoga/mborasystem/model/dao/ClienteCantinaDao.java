package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.ClienteCantina;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Flowable;

@Dao
public interface ClienteCantinaDao {

    @Insert
    void insert(ClienteCantina clienteCantina);

    @Query("SELECT * FROM clientecantina WHERE estado != 3 ORDER BY id DESC")
    Flowable<List<ClienteCantina>> getClientesCantina();

}
