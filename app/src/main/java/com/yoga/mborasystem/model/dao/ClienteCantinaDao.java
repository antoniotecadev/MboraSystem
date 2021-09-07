package com.yoga.mborasystem.model.dao;

import com.yoga.mborasystem.model.entidade.ClienteCantina;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import io.reactivex.Flowable;

@Dao
public interface ClienteCantinaDao {

    @Insert
    void insert(ClienteCantina clienteCantina);

    @Query("SELECT * FROM clientecantina WHERE estado != 3 ORDER BY id DESC")
    Flowable<List<ClienteCantina>> getClientesCantina();

    @Query("SELECT * FROM clientecantina WHERE estado != 3 AND (nome LIKE '%' || :search || '%' OR telefone LIKE '%' || :search || '%')")
    Flowable<List<ClienteCantina>> searchCliente(String search);

    @Query("UPDATE clientecantina SET nome = :nome, telefone = :telefone, estado = :estado, data_modifica = :dataModif WHERE id = :id")
    void update(String nome, String telefone, int estado, String dataModif, long id);

    @Delete
    void delete(ClienteCantina clienteCantina);

    @Query("UPDATE clientecantina SET estado = :estado, data_elimina = :data WHERE id = :id")
    void deleteLixeira(int estado, String data, long id);

}
