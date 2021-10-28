package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.Usuario;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface UsuarioDao {

    @Insert
    void insert(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE estado != 3 ORDER BY id DESC")
    Flowable<List<Usuario>> getUsuarios();

    @Query("SELECT * FROM usuarios WHERE estado = 3 ORDER BY id DESC")
    Flowable<List<Usuario>> getUsuariosLixeira();

    @Query("UPDATE usuarios SET nome = :nome, telefone = :tel, endereco = :end, estado = :est, data_modifica = :data WHERE id = :id")
    void update(String nome, String tel, String end, int est, String data, long id);

    @Query("UPDATE usuarios SET codigo_pin = :cp WHERE id = :id")
    void update(String cp, long id);

    @Query("UPDATE usuarios SET estado = :est WHERE id = :id")
    void restaurarUsuario(int est, long id);

    @Delete
    void delete(Usuario usuario);

    @Query("UPDATE usuarios SET estado = :est, data_elimina = :data WHERE id = :id")
    void deleteLixeira(int est, String data, long id);

    @Query("SELECT * FROM usuarios WHERE estado != 3 AND codigo_pin =:cp")
    Single<Usuario> confirmarCodigoPin(String cp);

}