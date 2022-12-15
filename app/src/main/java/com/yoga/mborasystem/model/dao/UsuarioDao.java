package com.yoga.mborasystem.model.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yoga.mborasystem.model.entidade.Usuario;

import java.util.List;

@Dao
public interface UsuarioDao {

    @Insert
    void insert(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE estado != 3 ORDER BY id DESC")
    PagingSource<Integer, Usuario> getUsuarios();

    @Query("SELECT COUNT(id) FROM usuarios  WHERE estado != 3")
    LiveData<Long> getQuantidadeUsuario();

    @Query("UPDATE usuarios SET nome = :nome, telefone = :tel, endereco = :end, estado = :est, data_modifica = :data WHERE id = :id")
    void update(String nome, String tel, String end, int est, String data, long id);

    @Query("UPDATE usuarios SET codigo_pin = :cp, data_cria = :dataCria WHERE id = :id")
    void update(String cp, String dataCria, long id);

    @Delete
    void delete(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE estado != 3 AND codigo_pin =:cp")
    List<Usuario> confirmarCodigoPin(String cp) throws Exception;

}