package com.yoga.mborasystem.model.entidade;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
@Keep
@Entity(tableName = "vendas", indices = {@Index(value = {"codigo_qr", "data_cria", "idclicant", "idoperador"})})
public class Venda implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String nome_cliente;
    private int desconto;
    private int quantidade;
    private int valor_base;
    private String codigo_qr;
    private int valor_iva;
    private String pagamento;
    private int total_desconto;
    private int total_venda;
    private int divida;
    private int valor_pago;
    private int estado;
    private String data_cria;
    private String data_elimina;
    private long idclicant;
    private long idoperador;
    private String data_cria_hora;
    private String hash;


    public Venda(Parcel in) {
        id = in.readLong();
        nome_cliente = in.readString();
        desconto = in.readInt();
        quantidade = in.readInt();
        valor_base = in.readInt();
        codigo_qr = in.readString();
        valor_iva = in.readInt();
        pagamento = in.readString();
        total_desconto = in.readInt();
        total_venda = in.readInt();
        divida = in.readInt();
        valor_pago = in.readInt();
        estado = in.readInt();
        data_cria = in.readString();
        data_elimina = in.readString();
        idclicant = in.readLong();
        idoperador = in.readLong();
        data_cria_hora = in.readString();
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

    public String getCodigo_qr() {
        return codigo_qr;
    }

    public void setCodigo_qr(String codigo_qr) {
        this.codigo_qr = codigo_qr;
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

    public int getDivida() {
        return divida;
    }

    public void setDivida(int divida) {
        this.divida = divida;
    }

    public int getValor_pago() {
        return valor_pago;
    }

    public void setValor_pago(int valor_pago) {
        this.valor_pago = valor_pago;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public long getIdclicant() {
        return idclicant;
    }

    public void setIdclicant(long idclicant) {
        this.idclicant = idclicant;
    }

    public long getIdoperador() {
        return idoperador;
    }

    public void setIdoperador(long idoperador) {
        this.idoperador = idoperador;
    }

    public String getData_cria_hora() {
        return data_cria_hora;
    }

    public void setData_cria_hora(String data_cria_hora) {
        this.data_cria_hora = data_cria_hora;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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
        dest.writeString(codigo_qr);
        dest.writeInt(valor_iva);
        dest.writeString(pagamento);
        dest.writeInt(total_desconto);
        dest.writeInt(total_venda);
        dest.writeInt(divida);
        dest.writeInt(valor_pago);
        dest.writeInt(estado);
        dest.writeString(data_cria);
        dest.writeString(data_elimina);
        dest.writeLong(idclicant);
        dest.writeLong(idoperador);
        dest.writeString(data_cria_hora);
    }
}
