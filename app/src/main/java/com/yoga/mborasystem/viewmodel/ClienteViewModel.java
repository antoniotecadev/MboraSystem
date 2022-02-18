package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Patterns;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.repository.ClienteRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;
import java.util.Random;
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

import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;

public class ClienteViewModel extends AndroidViewModel {

    private byte estado;
    private String codigo;
    private final Cliente cliente;
    private Disposable disposable;
    private final ClienteRepository clienteRepository;

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

    private final Pattern numero = Pattern.compile("[^0-9 ]");
    private final Pattern letrasNIFBI = Pattern.compile("[^a-zA-Z0-9 ]");
    private final Pattern letras = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û ]");
    private final Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û0-9 ]");

    private boolean isCampoVazio(String valor) {
        return (TextUtils.isEmpty(valor) || valor.trim().isEmpty());
    }

    private boolean isEmailValido(String email) {
        return (!isCampoVazio(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }

    private boolean isNumeroValido(String numero) {
        return (isCampoVazio(numero) || !Patterns.PHONE.matcher(numero).matches());
    }

    public void validarCliente(Ultilitario.Operacao operacao, TextInputEditText nome, TextInputEditText sobreNome, TextInputEditText nif, TextInputEditText telefone, TextInputEditText telefoneAlternativo, TextInputEditText email, TextInputEditText nomeEmpresa, AppCompatSpinner provincia, AppCompatSpinner municipio, TextInputEditText bairro, TextInputEditText rua, TextInputEditText senha, TextInputEditText senhaNovamente, TextInputEditText codigoEquipa) throws InvalidKeySpecException, NoSuchAlgorithmException {
        if (isCampoVazio(Objects.requireNonNull(nome.getText()).toString()) || letras.matcher(nome.getText().toString()).find()) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_invalido));
        } else if (nome.length() < 3) {
            nome.requestFocus();
            nome.setError(getApplication().getString(R.string.nome_curto));
        } else if (isCampoVazio(Objects.requireNonNull(sobreNome.getText()).toString()) || letras.matcher(sobreNome.getText().toString()).find()) {
            sobreNome.requestFocus();
            sobreNome.setError(getApplication().getString(R.string.sobrenome_invalido));
        } else if (sobreNome.length() < 3) {
            sobreNome.requestFocus();
            sobreNome.setError(getApplication().getString(R.string.sobrenome_curto));
        } else if (isCampoVazio(Objects.requireNonNull(nif.getText()).toString()) || letrasNIFBI.matcher(nif.getText().toString()).find()) {
            nif.requestFocus();
            nif.setError(getApplication().getString(R.string.nifbi_invalido));
        } else if (nif.length() < 14) {
            nif.requestFocus();
            nif.setError(getApplication().getString(R.string.nifbi_incompleto));
        } else if (isNumeroValido(Objects.requireNonNull(telefone.getText()).toString())) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_invalido));
        } else if (telefone.length() < 9) {
            telefone.requestFocus();
            telefone.setError(getApplication().getString(R.string.numero_incompleto));
        } else if (isNumeroValido(Objects.requireNonNull(telefoneAlternativo.getText()).toString())) {
            telefoneAlternativo.requestFocus();
            telefoneAlternativo.setError(getApplication().getString(R.string.numero_invalido));
        } else if (telefoneAlternativo.length() < 9) {
            telefoneAlternativo.requestFocus();
            telefoneAlternativo.setError(getApplication().getString(R.string.numero_incompleto));
        } else if (isEmailValido(Objects.requireNonNull(email.getText()).toString())) {
            email.requestFocus();
            email.setError(getApplication().getString(R.string.email_invalido));
        } else if (isCampoVazio(Objects.requireNonNull(nomeEmpresa.getText()).toString()) || letras.matcher(nomeEmpresa.getText().toString()).find()) {
            nomeEmpresa.requestFocus();
            nomeEmpresa.setError(getApplication().getString(R.string.nome_invalido));
        } else if (nomeEmpresa.length() < 5) {
            nomeEmpresa.requestFocus();
            nomeEmpresa.setError(getApplication().getString(R.string.nome_curto));
        } else if (isCampoVazio(Objects.requireNonNull(municipio.getSelectedItem().toString()))) {
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.municipio_invalido), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(bairro.getText()).toString()) || letraNumero.matcher(bairro.getText().toString()).find()) {
            bairro.requestFocus();
            bairro.setError(getApplication().getString(R.string.bairro_invalido));
            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.bairro_invalido), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(rua.getText()).toString()) || letraNumero.matcher(rua.getText().toString()).find()) {
            rua.requestFocus();
            rua.setError(getApplication().getString(R.string.rua_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(senha.getText()).toString()) || letraNumero.matcher(senha.getText().toString()).find()) {
            senha.requestFocus();
            senha.setError(getApplication().getString(R.string.senha_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(senhaNovamente.getText()).toString()) || letraNumero.matcher(senhaNovamente.getText().toString()).find()) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.senha_invalida));
        } else if (!senha.getText().toString().equalsIgnoreCase(senhaNovamente.getText().toString())) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.pin_diferente));
        } else if (isCampoVazio(Objects.requireNonNull(codigoEquipa.getText()).toString()) || numero.matcher(codigoEquipa.getText().toString()).find()) {
            codigoEquipa.requestFocus();
            codigoEquipa.setError(getApplication().getString(R.string.cod_eqp_inv));
        } else {
            MainActivity.getProgressBar();
            cliente.setNome(nome.getText().toString());
            cliente.setSobrenome(sobreNome.getText().toString());
            cliente.setNifbi(nif.getText().toString());
            cliente.setMaster(true);
            cliente.setTelefone(telefone.getText().toString());
            cliente.setTelefonealternativo(telefoneAlternativo.getText().toString());
            cliente.setEmail(email.getText().toString());
            cliente.setNomeEmpresa(nomeEmpresa.getText().toString());
            cliente.setProvincia(provincia.getSelectedItem().toString());
            cliente.setMunicipio(municipio.getSelectedItem().toString());
            cliente.setBairro(bairro.getText().toString());
            cliente.setRua(rua.getText().toString());
            cliente.setSenha(Ultilitario.generateKey(senha.getText().toString().toCharArray()));
            cliente.setImei(System.currentTimeMillis() / 1000 + String.valueOf(new Random().nextInt((100000 - 1) + 1) + 1));
            cliente.setCodigoEquipa(codigoEquipa.getText().toString());
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                if (verificarCodigoEquipa(codigoEquipa.getText().toString())) {
                    clienteExiste(true, cliente);
                }
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR)) {

            }
        }

    }

    @SuppressLint("CheckResult")
    public void clienteExiste(boolean limitCadastro, Cliente c) {
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
                        if (limitCadastro) {
                            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.conta_cliente_ja_existe), R.drawable.ic_toast_erro);
                        } else {
                            Ultilitario.getExisteMutableLiveData().setValue(Ultilitario.Existe.SIM);
                        }
                        MainActivity.dismissProgressBar();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        if (limitCadastro) {
                            if (isNetworkConnected(getApplication())) {
                                if (internetIsConnected()) {
                                    cadastrarCliente(c);
                                } else {
                                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.sm_int), R.drawable.ic_toast_erro);
                                    MainActivity.dismissProgressBar();
                                }
                            } else {
                                Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
                                MainActivity.dismissProgressBar();
                            }
                        } else {
                            Ultilitario.getExisteMutableLiveData().setValue(Ultilitario.Existe.NAO);
                            MainActivity.dismissProgressBar();
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
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
                        salvarParceiro(cliente);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), "Erro:" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    private void salvarParceiro(Cliente cliente) {
        String URL = "http://192.168.18.3/mborasystem-admin/public/api/contacts";
        Ion.with(getApplication().getApplicationContext())
                .load("POST", URL)
                .setBodyParameter("account_id", "1")
                .setBodyParameter("codigo_equipa", cliente.getCodigoEquipa())
                .setBodyParameter("first_name", cliente.getNome())
                .setBodyParameter("last_name", cliente.getSobrenome())
                .setBodyParameter("nif_bi", cliente.getNifbi())
                .setBodyParameter("email", cliente.getEmail())
                .setBodyParameter("phone", cliente.getTelefone())
                .setBodyParameter("alternative_phone", cliente.getTelefonealternativo())
                .setBodyParameter("cantina", cliente.getNomeEmpresa())
                .setBodyParameter("municipality", cliente.getMunicipio())
                .setBodyParameter("district", cliente.getBairro())
                .setBodyParameter("street", cliente.getRua())
                .setBodyParameter("imei", cliente.getImei())
                .asJsonObject()
                .setCallback((e, jsonObject) -> {
                    try {
                        String retorno = jsonObject.get("insert").getAsString();
                        if (retorno.equals("ok")) {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), "Parceiro salvo", R.drawable.ic_toast_feito);
                        } else if (retorno.equals("erro")) {
                            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), "Parceiro não salvo", R.drawable.ic_toast_erro);
                        }
                    } catch (Exception ex) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), "Erro:" + ex.getMessage(), R.drawable.ic_toast_erro);
                    } finally {
                        MainActivity.dismissProgressBar();
                    }
                });
    }

    @SuppressLint("CheckResult")
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
                            if (Ultilitario.validateSenhaPin(Objects.requireNonNull(senha.getText()).toString(), cliente.getSenha())) {
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

    boolean isVereficado = false;

    public boolean verificarCodigoEquipa(String codigoEquipa) {
        if (isNetworkConnected(getApplication().getApplicationContext())) {
            if (internetIsConnected()) {
                String URL = "http://192.168.18.3/mborasystem-admin/public/api/equipas/" + codigoEquipa + "/verificar";
                Ion.with(getApplication().getApplicationContext())
                        .load(URL)
                        .asJsonArray()
                        .setCallback((e, jsonElements) -> {
                            try {
                                for (int i = 0; i < jsonElements.size(); i++) {
                                    JsonObject equipa = jsonElements.get(i).getAsJsonObject();
                                    codigo = equipa.get("codigo").getAsString();
                                    estado = Byte.parseByte(equipa.get("estado").getAsString());
                                }
                                if (codigo.isEmpty() || estado == Ultilitario.ZERO) {
                                    Ultilitario.showToast(getApplication().getApplicationContext(), Color.rgb(204, 0, 0), getApplication().getString(R.string.eqp_n_enc), R.drawable.ic_toast_erro);
                                } else {
                                    isVereficado = true;
                                }
                            } catch (Exception ex) {
                                Ultilitario.showToast(getApplication().getApplicationContext(), Color.rgb(204, 0, 0), "Erro:" + ex.getMessage(), R.drawable.ic_toast_erro);
                            } finally {
                                MainActivity.dismissProgressBar();
                            }
                        });
            } else {
                Ultilitario.showToast(getApplication().getApplicationContext(), Color.rgb(204, 0, 0), getApplication().getString(R.string.sm_int), R.drawable.ic_toast_erro);
                MainActivity.dismissProgressBar();
            }
        } else {
            Ultilitario.showToast(getApplication().getApplicationContext(), Color.rgb(204, 0, 0), getApplication().getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
            MainActivity.dismissProgressBar();
        }
        return isVereficado;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null || !Objects.requireNonNull(disposable).isDisposed()) {
            disposable.dispose();
        }
    }
}
