package com.yoga.mborasystem.model.entidade;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Keep;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
@Keep
@Entity(tableName = "cliente")
public class Cliente implements Parcelable {

    public Cliente() {
    }

    @PrimaryKey
    private long id;

    private String nome;
    private String sobrenome;
    private String email;
    private String telefone;
    private String nifbi;
    private boolean master;
    private String senha;
    private String nomeEmpresa;

    private String telefonealternativo;
    private String provincia;
    private String municipio;
    private String bairro;
    private String rua;
    private String imei;
    @Ignore private String codigoEquipa;
    @Ignore private String data_cria;
    @Ignore private boolean visualizado;
    @Ignore private String latitude;
    @Ignore private String longitude;
    @Ignore private String uid;
    @Ignore private String fotoPefilUrl;
    @Ignore private String fotoCapaUrl;
    @Ignore private String codigoPlus;

    protected Cliente(Parcel in) {
        id = in.readLong();
        nome = in.readString();
        sobrenome = in.readString();
        email = in.readString();
        telefone = in.readString();
        nifbi = in.readString();
        master = in.readByte() != 0;
        senha = in.readString();
        telefonealternativo = in.readString();
        provincia = in.readString();
        municipio = in.readString();
        bairro = in.readString();
        rua = in.readString();
        nomeEmpresa = in.readString();
        imei = in.readString();
    }

    public static final Creator<Cliente> CREATOR = new Creator<Cliente>() {
        @Override
        public Cliente createFromParcel(Parcel in) {
            return new Cliente(in);
        }

        @Override
        public Cliente[] newArray(int size) {
            return new Cliente[size];
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public boolean isMaster() {
        return master;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public String getNifbi() {
        return nifbi;
    }

    public void setNifbi(String nifbi) {
        this.nifbi = nifbi;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getTelefonealternativo() {
        return telefonealternativo;
    }

    public void setTelefonealternativo(String telefonealternativo) {
        this.telefonealternativo = telefonealternativo;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNomeEmpresa() {
        return nomeEmpresa;
    }

    public void setNomeEmpresa(String nomeEmpresa) {
        this.nomeEmpresa = nomeEmpresa;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getCodigoEquipa() {
        return codigoEquipa;
    }

    public void setCodigoEquipa(String codigoEquipa) {
        this.codigoEquipa = codigoEquipa;
    }

    public String getData_cria() {
        return data_cria;
    }

    public void setData_cria(String data_cria) {
        this.data_cria = data_cria;
    }

    public boolean isVisualizado() {
        return visualizado;
    }

    public void setVisualizado(boolean visualizado) {
        this.visualizado = visualizado;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFotoPefilUrl() {
        return fotoPefilUrl;
    }

    public void setFotoPefilUrl(String fotoPefilUrl) {
        this.fotoPefilUrl = fotoPefilUrl;
    }

    public String getFotoCapaUrl() {
        return fotoCapaUrl;
    }

    public void setFotoCapaUrl(String fotoCapaUrl) {
        this.fotoCapaUrl = fotoCapaUrl;
    }

    public String getCodigoPlus() {
        return codigoPlus;
    }

    public void setCodigoPlus(String codigoPlus) {
        this.codigoPlus = codigoPlus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(nome);
        dest.writeString(sobrenome);
        dest.writeString(email);
        dest.writeString(telefone);
        dest.writeString(nifbi);
        dest.writeByte((byte) (master ? 1 : 0));
        dest.writeString(senha);
        dest.writeString(telefonealternativo);
        dest.writeString(provincia);
        dest.writeString(municipio);
        dest.writeString(bairro);
        dest.writeString(rua);
        dest.writeString(nomeEmpresa);
        dest.writeString(imei);
    }
}