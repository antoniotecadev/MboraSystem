package com.yoga.mborasystem.model.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yoga.mborasystem.model.entidade.Categoria;

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
    void deleteAllCategoriaLixeira(int estado);

    @Query("SELECT * FROM categorias WHERE estado != 3 ORDER BY id DESC")
    PagingSource<Integer, Categoria> getCategorias();

    @Query("SELECT * FROM categorias WHERE estado = 3 ORDER BY id DESC")
    PagingSource<Integer, Categoria> getCategoriasLixeira();

    @Query("SELECT * FROM categorias WHERE estado != 3 AND categoria LIKE '%' || :categoria || '%'")
    PagingSource<Integer, Categoria> searchCategorias(String categoria);

    @Query("SELECT * FROM categorias WHERE estado = 3 AND categoria LIKE '%' || :categoria || '%'")
    PagingSource<Integer, Categoria> searchCategoriasLixeira(String categoria);

    @Query("UPDATE categorias SET estado = :est WHERE id = :id")
    void restaurarCategoria(int est, long id);

    @Query("UPDATE categorias SET estado = :est")
    void restaurarCategoria(int est);

    @Query("SELECT COUNT(id) FROM categorias  WHERE estado != 3")
    LiveData<Long> getQuantidadeCategoria();

    @Query("SELECT COUNT(id) FROM categorias  WHERE estado = 3")
    LiveData<Long> getQuantidadeCategoriaLixeira();

}
