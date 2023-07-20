package com.yoga.mborasystem.repository;

import android.content.Context;

import com.yoga.mborasystem.model.connectiondatabase.AppDataBase;
import com.yoga.mborasystem.model.dao.ClienteDao;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.util.Ultilitario;

import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

public class ClienteRepository {

    Context context;
    ClienteDao clienteDao;
    WeakReference<Context> contextWeakReference;

    public ClienteRepository(Context context) {
        contextWeakReference = new WeakReference<>(context);
        AppDataBase appDataBase = AppDataBase.getAppDataBase(contextWeakReference.get());
        clienteDao = appDataBase.clienteDao();
        this.context = context;
    }

    public void insert(Cliente cliente) throws NoSuchAlgorithmException, InvalidKeySpecException {
        cliente.setSenha(Ultilitario.generateKey(cliente.getSenha().toCharArray()));
        clienteDao.insert(cliente);
    }

    public List<Cliente> clienteExiste() throws Exception {
        return clienteDao.clienteExiste();
    }

    public void delete(Cliente cliente) {
        clienteDao.delete(cliente);
    }

    public void alterarSenha(Cliente cliente) {
        clienteDao.alterarSenha(cliente.getIdcliente(), cliente.getSenha());
    }

    public void update(Cliente c) {
        clienteDao.update(c.getIdcliente(), c.getNome(), c.getSobrenome(), c.getNifbi(), c.getTelefone(), c.getTelefonealternativo(), c.getEmail(), c.getNomeEmpresa(), c.getProvincia(), c.getMunicipio(), c.getBairro(), c.getRua());
    }
}
