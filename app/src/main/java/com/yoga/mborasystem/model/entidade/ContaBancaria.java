package com.yoga.mborasystem.model.entidade;

import androidx.annotation.Keep;

@Keep
public class ContaBancaria {

    private String proprietario;
    private String nome;
    private String nib;
    private String iban;

    public ContaBancaria() {
    }

    public ContaBancaria(String proprietario, String nome, String nib, String iban) {
        this.proprietario = proprietario;
        this.nome = nome;
        this.nib = nib;
        this.iban = iban;
    }

    public String getProprietario() {
        return proprietario;
    }

    public void setProprietario(String proprietario) {
        this.proprietario = proprietario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNib() {
        return nib;
    }

    public void setNib(String nib) {
        this.nib = nib;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

}
