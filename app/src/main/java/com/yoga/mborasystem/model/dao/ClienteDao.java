package com.yoga.mborasystem.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yoga.mborasystem.model.entidade.Cliente;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface ClienteDao {

    @Insert
    void insert(Cliente cliente);

    //    @Query("SELECT * FROM cliente")
//    Single<Cliente> clienteExiste();
//
    @Query("SELECT * FROM cliente LIMIT 1")
    List<Cliente> clienteExiste() throws Exception;

    @Delete
    void delete(Cliente cliente);

    @Query("UPDATE cliente SET senha = :senha WHERE id = :idcliente")
    void alterarSenha(long idcliente, String senha);

}