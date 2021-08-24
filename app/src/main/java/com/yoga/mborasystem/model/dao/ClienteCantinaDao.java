package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.ClienteCantina;

import androidx.room.Dao;
import androidx.room.Insert;

@Dao
public interface ClienteCantinaDao {

    @Insert
    void insert(ClienteCantina clienteCantina);


}
