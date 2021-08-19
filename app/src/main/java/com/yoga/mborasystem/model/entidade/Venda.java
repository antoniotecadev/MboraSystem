package com.yoga.mborasystem.model.entidade;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vendas")
public class Venda implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nome_cliente;
    private int desconto;
    private int quantidade;
    private int valor_base;
    private String codigo_Barra;
    private int valor_iva;
    private String pagamento;
    private int total_desconto;
    private int total_venda;
    private String data_cria;
    private String data_elimina;
    private long idoperador;

    public Venda(Parcel in) {
        id = in.readLong();
        nome_cliente = in.readString();
        desconto = in.readInt();
        quantidade = in.readInt();
        valor_base = in.readInt();
        codigo_Barra = in.readString();
        valor_iva = in.readInt();
        pagamento = in.readString();
        total_desconto = in.readInt();
        total_venda = in.readInt();
        data_cria = in.readString();
        data_elimina = in.readString();
        idoperador = in.readLong();
    }

    public static final Creator<Venda> CREATOR = new Creator<Venda>() {
        @Override
        public Venda createFromParcel(Parcel in) {
            return new Venda(in);
        }

        @Override
        public Venda[] newArray(int size) {
            return new Venda[size];
        }
    };

    public Venda() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome_cliente() {
        return nome_cliente;
    }

    public void setNome_cliente(String nome_cliente) {
        this.nome_cliente = nome_cliente;
    }

    public String getData_cria() {
        return data_cria;
    }

    public void setData_cria(String data_cria) {
        this.data_cria = data_cria;
    }

    public String getData_elimina() {
        return data_elimina;
    }

    public void setData_elimina(String data_elimina) {
        this.data_elimina = data_elimina;
    }

    public int getDesconto() {
        return desconto;
    }

    public void setDesconto(int desconto) {
        this.desconto = desconto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public int getValor_base() {
        return valor_base;
    }

    public void setValor_base(int valor_base) {
        this.valor_base = valor_base;
    }

    public String getCodigo_Barra() {
        return codigo_Barra;
    }

    public void setCodigo_Barra(String codigo_Barra) {
        this.codigo_Barra = codigo_Barra;
    }

    public int getValor_iva() {
        return valor_iva;
    }

    public void setValor_iva(int valor_iva) {
        this.valor_iva = valor_iva;
    }

    public String getPagamento() {
        return pagamento;
    }

    public void setPagamento(String pagamento) {
        this.pagamento = pagamento;
    }

    public int getTotal_desconto() {
        return total_desconto;
    }

    public void setTotal_desconto(int total_desconto) {
        this.total_desconto = total_desconto;
    }

    public int getTotal_venda() {
        return total_venda;
    }

    public void setTotal_venda(int total_venda) {
        this.total_venda = total_venda;
    }

    public long getIdoperador() {
        return idoperador;
    }

    public void setIdoperador(long idoperador) {
        this.idoperador = idoperador;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nome_cliente);
        dest.writeInt(desconto);
        dest.writeInt(quantidade);
        dest.writeInt(valor_base);
        dest.writeString(codigo_Barra);
        dest.writeInt(valor_iva);
        dest.writeString(pagamento);
        dest.writeInt(total_desconto);
        dest.writeInt(total_venda);
        dest.writeString(data_cria);
        dest.writeString(data_elimina);
        dest.writeLong(idoperador);
    }
}
