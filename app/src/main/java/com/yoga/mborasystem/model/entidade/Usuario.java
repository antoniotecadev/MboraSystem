package com.yoga.mborasystem.model.entidade;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "usuarios")
public class Usuario implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String nome;
    private String telefone;
    private String endereco;

    @ColumnInfo(name = "codigo_pin")
    private String codigoPin;

    private int estado;

    private String data_cria;
    private String data_modifica;
    private String data_elimina;

    public Usuario() {
        super();
    }

    protected Usuario(Parcel in) {
        id = in.readInt();
        nome = in.readString();
        telefone = in.readString();
        endereco = in.readString();
        codigoPin = in.readString();
        estado = in.readInt();
        data_cria = in.readString();
        data_modifica = in.readString();
        data_elimina = in.readString();
    }

    public static final Creator<Usuario> CREATOR = new Creator<Usuario>() {
        @Override
        public Usuario createFromParcel(Parcel in) {
            return new Usuario(in);
        }

        @Override
        public Usuario[] newArray(int size) {
            return new Usuario[size];
        }
    };

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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCodigoPin() {
        return codigoPin;
    }

    public void setCodigoPin(String codigoPin) {
        this.codigoPin = codigoPin;
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
        dest.writeString(nome);
        dest.writeString(telefone);
        dest.writeString(endereco);
        dest.writeString(codigoPin);
        dest.writeInt(estado);
        dest.writeString(data_cria);
        dest.writeString(data_modifica);
        dest.writeString(data_elimina);
    }
}
