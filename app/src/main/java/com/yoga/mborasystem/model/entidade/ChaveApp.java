package com.yoga.mborasystem.model.entidade;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "chaveapp", indices = {@Index(value = {"chave"}, unique = true)})
public class ChaveApp {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private int chave;

    private boolean estado;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getChave() {
        return chave;
    }

    public void setChave(int chave) {
        this.chave = chave;
    }

    public boolean isEstado() {
        return estado;
    }

    public void setEstado(boolean estado) {
        this.estado = estado;
    }
}
