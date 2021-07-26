package com.yoga.mborasystem.model.entidade;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categorias")
public class Categoria implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String categoria;
    private String descricao;
    private int estado;

    private String data_cria;
    private String data_modifica;
    private String data_elimina;

    public Categoria() {
    }

    protected Categoria(Parcel in) {
        id = in.readLong();
        categoria = in.readString();
        descricao = in.readString();
        estado = in.readInt();
        data_cria = in.readString();
        data_modifica = in.readString();
        data_elimina = in.readString();
    }

    public static final Creator<Categoria> CREATOR = new Creator<Categoria>() {
        @Override
        public Categoria createFromParcel(Parcel in) {
            return new Categoria(in);
        }

        @Override
        public Categoria[] newArray(int size) {
            return new Categoria[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getData_elimina() {
        return data_elimina;
    }

    public void setData_elimina(String data_elimina) {
        this.data_elimina = data_elimina;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(categoria);
        dest.writeString(descricao);
        dest.writeInt(estado);
        dest.writeString(data_cria);
        dest.writeString(data_modifica);
        dest.writeString(data_elimina);
    }
}
