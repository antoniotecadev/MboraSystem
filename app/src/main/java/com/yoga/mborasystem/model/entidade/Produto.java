package com.yoga.mborasystem.model.entidade;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "produtos", indices = {@Index(value = {"idcategoria"})},
        foreignKeys = @ForeignKey(entity = Categoria.class, parentColumns = "id", childColumns = "idcategoria", onDelete = CASCADE))
public class Produto implements Parcelable {

    public Produto() {}

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nome;
    private int preco;
    private int precofornecedor;
    private int quantidade;
    private String codigoBarra;
    private boolean iva;
    private int estado;
    private long idcategoria;

    private String data_cria;
    private String data_modifica;
    private String data_elimina;

    @Ignore
    private int precoMin;
    @Ignore
    private int precoMax;

    protected Produto(Parcel in) {
        id = in.readLong();
        nome = in.readString();
        preco = in.readInt();
        precofornecedor = in.readInt();
        quantidade = in.readInt();
        codigoBarra = in.readString();
        iva = in.readByte() != 0;
        estado = in.readInt();
        idcategoria = in.readLong();
        data_cria = in.readString();
        data_modifica = in.readString();
        data_elimina = in.readString();
    }

    public static final Creator<Produto> CREATOR = new Creator<Produto>() {
        @Override
        public Produto createFromParcel(Parcel in) {
            return new Produto(in);
        }

        @Override
        public Produto[] newArray(int size) {
            return new Produto[size];
        }
    };

    public long getIdcategoria() {
        return idcategoria;
    }

    public void setIdcategoria(long idcategoria) {
        this.idcategoria = idcategoria;
    }

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

    public int getPreco() {
        return preco;
    }

    public void setPreco(int preco) {
        this.preco = preco;
    }

    public int getPrecofornecedor() {
        return precofornecedor;
    }

    public void setPrecofornecedor(int precofornecedor) {
        this.precofornecedor = precofornecedor;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public String getCodigoBarra() {
        return codigoBarra;
    }

    public void setCodigoBarra(String codigoBarra) {
        this.codigoBarra = codigoBarra;
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

    public int getPrecoMin() {
        return precoMin;
    }

    public void setPrecoMin(int precoMin) {
        this.precoMin = precoMin;
    }

    public int getPrecoMax() {
        return precoMax;
    }

    public void setPrecoMax(int precoMax) {
        this.precoMax = precoMax;
    }

    public boolean isIva() {
        return iva;
    }

    public void setIva(boolean iva) {
        this.iva = iva;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nome);
        dest.writeInt(preco);
        dest.writeInt(precofornecedor);
        dest.writeInt(quantidade);
        dest.writeString(codigoBarra);
        dest.writeByte((byte) (iva ? 1 : 0));
        dest.writeInt(estado);
        dest.writeLong(idcategoria);
        dest.writeString(data_cria);
        dest.writeString(data_modifica);
        dest.writeString(data_elimina);
    }

}
