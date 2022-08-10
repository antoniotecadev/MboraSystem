package com.yoga.mborasystem.model.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yoga.mborasystem.model.entidade.ClienteCantina;

import java.util.List;

import io.reactivex.rxjava3.core.Maybe;

@Dao
public interface ClienteCantinaDao {

    @Insert
    void insert(ClienteCantina clienteCantina);

    @Query("SELECT * FROM clientecantina WHERE estado != 3 ORDER BY id DESC")
    PagingSource<Integer, ClienteCantina> getClientesCantina();

    @Query("SELECT * FROM clientecantina WHERE estado != 3 AND (nome LIKE '%' || :cliente || '%' OR telefone LIKE '%' || :cliente || '%' OR nif LIKE '%' || :cliente || '%') ORDER BY id DESC")
    Maybe<List<ClienteCantina>> getClienteCantina(String cliente);

    @Query("SELECT * FROM clientecantina WHERE estado != 3 AND (nome LIKE '%' || :cliente || '%' OR telefone LIKE '%' || :cliente || '%' OR nif LIKE '%' || :cliente || '%')")
    PagingSource<Integer, ClienteCantina> searchCliente(String cliente);

    @Query("UPDATE clientecantina SET nome = :nome, telefone = :telefone, email = :email, endereco = :endereco, estado = :estado, data_modifica = :dataModif WHERE id = :id")
    void update(String nome, String telefone, String email, String endereco, int estado, String dataModif, long id);

    @Delete
    void delete(ClienteCantina clienteCantina);

    @Query("SELECT * FROM clientecantina ORDER BY clientecantina.id DESC")
    List<ClienteCantina> getClientesExport() throws Exception;

    @Query("SELECT COUNT(id) FROM clientecantina  WHERE estado != 3")
    LiveData<Long> getQuantidadeCliente();

}
