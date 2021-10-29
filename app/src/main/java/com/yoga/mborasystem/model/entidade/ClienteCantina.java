package com.yoga.mborasystem.model.entidade;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clientecantina")
public class ClienteCantina {

    public ClienteCantina(long id, String nome, String telefone) {
        this.id = id;
        this.nome = nome;
        this.telefone = telefone;
    }

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nome;
    private String telefone;

    private int estado;

    private String data_cria;
    private String data_modifica;

    public ClienteCantina() { }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public String getData_cria() {
        return data_cria;
    }

    public void setData_cria(String data_cria) {
        this.data_cria = data_cria;
    }

    public String getData_modifica() {
        return data_modifica;
    }

    public void setData_modifica(String data_modifica) {
        this.data_modifica = data_modifica;
    }

}
