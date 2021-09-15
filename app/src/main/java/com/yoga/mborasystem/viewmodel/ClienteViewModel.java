package com.yoga.mborasystem.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.repository.ClienteRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ClienteViewModel extends AndroidViewModel {

    private Cliente cliente;
    private Disposable disposable;
    private ClienteRepository clienteRepository;

    public ClienteViewModel(@NonNull Application application) {
        super(application);
        cliente = new Cliente();
        disposable = new CompositeDisposable();
        clienteRepository = new ClienteRepository(application);
        clienteExiste(false, null);
    }

    MutableLiveData<Cliente> clienteMutableLiveData;

    public MutableLiveData<Cliente> getClienteMutableLiveData() {
        if (clienteMutableLiveData == null) {
            clienteMutableLiveData = new MutableLiveData<>();
        }
        return clienteMutableLiveData;
    }

    private Pattern letrasNIFBI = Pattern.compile("[^a-zA-Z0-9 ]");
    private Pattern letras = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û ]");
    private Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û0-9 ]");

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    private boolean isEmailValido(String email) {
        return (!isCampoVazio(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private boolean isNumeroValido(String numero) {
        return (isCampoVazio(numero) || !Patterns.PHONE.matcher(numero).matches());
    }

    public void validarCliente(Ultilitario.Operacao operacao, TextInputEditText nome, TextInputEditText sobreNome, TextInputEditText nif, TextInputEditText telefone, TextInputEditText telefoneAlternativo, TextInputEditText email, TextInputEditText nomeEmpresa, AppCompatSpinner provincia, TextInputEditText municipio, TextInputEditText bairro, TextInputEditText rua, TextInputEditText senha, TextInputEditText senhaNovamente) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (isCampoVazio(nome.getText().toString()) || letras.matcher(nome.getText().toString()).find()) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_invalido));
        } else if (nome.length() < 3) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_curto));
        } else if (isCampoVazio(sobreNome.getText().toString()) || letras.matcher(sobreNome.getText().toString()).find()) {
            sobreNome.requestFocus();
            sobreNome.setError(getApplication().getString(R.string.sobrenome_invalido));
        } else if (sobreNome.length() < 3) {
            sobreNome.requestFocus();
            sobreNome.setError(getApplication().getString(R.string.sobrenome_curto));
        } else if (isCampoVazio(nif.getText().toString()) || letrasNIFBI.matcher(nif.getText().toString()).find()) {
            nif.requestFocus();
            nif.setError(getApplication().getString(R.string.nifbi_invalido));
        } else if (nif.length() < 14) {
            nif.requestFocus();
            nif.setError(getApplication().getString(R.string.nifbi_incompleto));
        } else if (isNumeroValido(telefone.getText().toString())) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_invalido));
        } else if (telefone.length() < 9) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_incompleto));
        } else if (isNumeroValido(telefoneAlternativo.getText().toString())) {
            telefoneAlternativo.requestFocus();
            telefoneAlternativo.setError(getApplication().getString(R.string.numero_invalido));
        } else if (telefoneAlternativo.length() < 9) {
            telefoneAlternativo.requestFocus();
            telefoneAlternativo.setError(getApplication().getString(R.string.numero_incompleto));
        } else if (isEmailValido(email.getText().toString())) {
            email.requestFocus();
            email.setError(getApplication().getString(R.string.email_invalido));
        } else if (isCampoVazio(nomeEmpresa.getText().toString()) || letras.matcher(nomeEmpresa.getText().toString()).find()) {
            nomeEmpresa.requestFocus();
            nomeEmpresa.setError(getApplication().getString(R.string.nome_invalido));
        } else if (nomeEmpresa.length() < 5) {
            nomeEmpresa.requestFocus();
            nomeEmpresa.setError(getApplication().getString(R.string.nome_curto));
        } else if (isCampoVazio(municipio.getText().toString()) || letraNumero.matcher(municipio.getText().toString()).find()) {
            municipio.requestFocus();
            municipio.setError(getApplication().getString(R.string.municipio_invalido));
        } else if (isCampoVazio(bairro.getText().toString()) || letraNumero.matcher(bairro.getText().toString()).find()) {
            bairro.requestFocus();
            bairro.setError(getApplication().getString(R.string.bairro_invalido));
        } else if (isCampoVazio(rua.getText().toString()) || letraNumero.matcher(rua.getText().toString()).find()) {
            rua.requestFocus();
            rua.setError(getApplication().getString(R.string.rua_invalida));
        } else if (isCampoVazio(senha.getText().toString()) || letraNumero.matcher(senha.getText().toString()).find()) {
            senha.requestFocus();
            senha.setError(getApplication().getString(R.string.senha_invalida));
        } else if (isCampoVazio(senhaNovamente.getText().toString()) || letraNumero.matcher(senhaNovamente.getText().toString()).find()) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.senha_invalida));
        } else if (!senha.getText().toString().equalsIgnoreCase(senhaNovamente.getText().toString())) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.pin_diferente));
        } else {
            cliente.setNome(nome.getText().toString());
            cliente.setSobrenome(sobreNome.getText().toString());
            cliente.setNifbi(nif.getText().toString());
            cliente.setMaster(true);
            cliente.setTelefone(telefone.getText().toString());
            cliente.setTelefonealternativo(telefoneAlternativo.getText().toString());
            cliente.setEmail(email.getText().toString());
            cliente.setNomeEmpresa(nomeEmpresa.getText().toString());
            cliente.setProvincia(provincia.getSelectedItem().toString());
            cliente.setMunicipio(municipio.getText().toString());
            cliente.setBairro(bairro.getText().toString());
            cliente.setRua(rua.getText().toString());
            cliente.setSenha(Ultilitario.generateKey(senha.getText().toString().toCharArray()));
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                clienteExiste(true, cliente);
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {

            }
        }

    }

    public void clienteExiste(boolean limitCadastro, Cliente c) {
        clienteRepository.clienteExiste().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Cliente>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Cliente cliente) {
                        if (limitCadastro) {
                            Ultilitario.dialogConta(getApplication().getString(R.string.conta_cliente_ja_existe), getApplication());
                        } else {
                            Ultilitario.getExisteMutableLiveData().setValue(Ultilitario.Existe.SIM);
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (limitCadastro) {
                            cadastrarCliente(c);
                        } else {
                            Ultilitario.getExisteMutableLiveData().setValue(Ultilitario.Existe.NAO);
                        }
                    }
                });
    }

    public void cadastrarCliente(Cliente cliente) {
        Completable.fromAction(() -> clienteRepository.insert(cliente))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.getValido().setValue(Ultilitario.Operacao.CRIAR);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
//                        Toast.makeText(getApplication(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void logar(TextInputEditText senha) {
        clienteRepository.clienteExiste()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Cliente>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(@io.reactivex.annotations.NonNull Cliente cliente) {
                        try {
                            if (Ultilitario.validateSenhaPin(senha.getText().toString(), cliente.getSenha())) {
                                getClienteMutableLiveData().setValue(cliente);
                            } else {
                                MainActivity.dismissProgressBar();
                                senha.setError(getApplication().getString(R.string.senha_incorreta));
                            }
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                            senha.setError(e.getMessage());
                        } catch (InvalidKeySpecException e) {
                            e.printStackTrace();
                            senha.setError(e.getMessage());
                        } finally {
                            MainActivity.dismissProgressBar();
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        senha.setError("Erro: " + e.getMessage());
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null || !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
