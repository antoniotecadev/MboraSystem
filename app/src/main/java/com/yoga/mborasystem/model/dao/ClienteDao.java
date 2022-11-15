package com.yoga.mborasystem.model.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.yoga.mborasystem.model.entidade.Cliente;

import java.util.List;

@Dao
public interface ClienteDao {

    @Insert
    void insert(Cliente cliente);

    @Query("SELECT * FROM cliente LIMIT 1")
    List<Cliente> clienteExiste() throws Exception;

    @Delete
    void delete(Cliente cliente);

    @Query("UPDATE cliente SET senha = :senha WHERE idcliente = :idcliente")
    void alterarSenha(long idcliente, String senha);

    @Query("UPDATE cliente SET nome = :nome, sobrenome = :sobreNome, nifbi = :nif, telefone = :telefone, telefonealternativo = :telefoneAlternativo, email = :email, nomeempresa = :nomeEmpresa, provincia = :provincia, municipio = :municipio, bairro = :bairro, rua = :rua  WHERE idcliente = :idcliente")
    void update(long idcliente, String nome, String sobreNome, String nif, String telefone, String telefoneAlternativo, String email, String nomeEmpresa, String provincia, String municipio, String bairro, String rua);
}