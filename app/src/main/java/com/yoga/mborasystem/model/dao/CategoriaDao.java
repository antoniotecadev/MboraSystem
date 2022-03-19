package com.yoga.mborasystem.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yoga.mborasystem.model.entidade.Categoria;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface CategoriaDao {

    @Insert
    void insert(Categoria categoria);

    @Query("UPDATE categorias SET categoria = :nome, descricao = :desc, estado = :est, data_modifica = :data WHERE id = :id")
    void update(String nome, String desc, int est, String data, long id);

    @Delete
    void delete(Categoria categoria);

    @Query("UPDATE categorias SET estado = :est, data_elimina = :data WHERE id = :id")
    void deleteLixeira(int est, String data, long id);

    @Query("DELETE FROM categorias WHERE estado = :estado")
    public void deleteAllCategoriaLixeira(int estado);

    @Query("SELECT * FROM categorias WHERE estado != 3 ORDER BY id DESC")
    Flowable<List<Categoria>> getCategorias();

    @Query("SELECT * FROM categorias WHERE estado = 3 ORDER BY id DESC")
    Flowable<List<Categoria>> getCategoriasLixeira();

    @Query("SELECT * FROM categorias WHERE estado != 3 AND categoria LIKE '%' || :search || '%'")
    Flowable<List<Categoria>> searchCategorias(String search);

    @Query("SELECT * FROM categorias WHERE estado = 3 AND categoria LIKE '%' || :search || '%'")
    Flowable<List<Categoria>> searchCategoriasLixeira(String search);

    @Query("UPDATE categorias SET estado = :est WHERE id = :id")
    void restaurarCategoria(int est, long id);

}
