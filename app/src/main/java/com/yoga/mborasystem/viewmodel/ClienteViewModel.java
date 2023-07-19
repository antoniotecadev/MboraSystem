package com.yoga.mborasystem.viewmodel;

import static com.yoga.mborasystem.util.Ultilitario.Existe.NAO;
import static com.yoga.mborasystem.util.Ultilitario.Existe.SIM;
import static com.yoga.mborasystem.util.Ultilitario.bytesToHex;
import static com.yoga.mborasystem.util.Ultilitario.conexaoInternet;
import static com.yoga.mborasystem.util.Ultilitario.getAPN;
import static com.yoga.mborasystem.util.Ultilitario.getDeviceUniqueID;
import static com.yoga.mborasystem.util.Ultilitario.getHash;
import static com.yoga.mborasystem.util.Ultilitario.isCampoVazio;
import static com.yoga.mborasystem.util.Ultilitario.isEmailValido;
import static com.yoga.mborasystem.util.Ultilitario.isNumeroValido;
import static com.yoga.mborasystem.util.Ultilitario.setValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.setValueUsuarioMaster;
import static com.yoga.mborasystem.util.Ultilitario.showToast;
import static com.yoga.mborasystem.util.Ultilitario.validateSenhaPin;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.koushikdutta.ion.Ion;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.repository.ClienteRepository;
import com.yoga.mborasystem.util.Ultilitario;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ClienteViewModel extends AndroidViewModel {

    private byte estado;
    private String codigo;
    private final Bundle bundle;
    private final Cliente cliente;
    private Disposable disposable;
    private ExecutorService executor;
    private final ClienteRepository clienteRepository;

    public ClienteViewModel(@NonNull Application application) {
        super(application);
        bundle = new Bundle();
        cliente = new Cliente();
        disposable = new CompositeDisposable();
        clienteRepository = new ClienteRepository(application);
    }

    MutableLiveData<List<Cliente>> clienteMutableLiveData;

    public MutableLiveData<List<Cliente>> getClienteMutableLiveData() {
        if (clienteMutableLiveData == null)
            clienteMutableLiveData = new MutableLiveData<>();
        return clienteMutableLiveData;
    }

    private MutableLiveData<Ultilitario.Operacao> valido;

    public MutableLiveData<Ultilitario.Operacao> getValido() {
        if (valido == null)
            valido = new MutableLiveData<>();
        return valido;
    }

    private final Pattern numero = Pattern.compile("[^0-9 ]");
    private final Pattern letrasNIFBI = Pattern.compile("[^a-zA-Z0-9 ]");
    private final Pattern letras = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û,-/ ]");
    private final Pattern letraNumero = Pattern.compile("[^a-zA-Zá-úà-ùã-õâ-ûÁ-ÚÀ-ÙÃ-ÕÂ-Û0-9 ]");

    public void validarDadosEmpresa(Ultilitario.Operacao operacao, TextInputEditText nome, TextInputEditText sobreNome, TextInputEditText nif, TextInputEditText telefone, TextInputEditText telefoneAlternativo, TextInputEditText email, TextInputEditText nomeEmpresa, AppCompatSpinner provincia, AppCompatSpinner municipio, TextInputEditText bairro, TextInputEditText rua, TextInputEditText senha, TextInputEditText senhaNovamente, TextInputEditText codigoEquipa, String imei, String regimeIva, Activity activity) throws InvalidKeySpecException, NoSuchAlgorithmException {
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
        } else if (nif.length() < 10) {
            nif.requestFocus();
            nif.setError(getApplication().getString(R.string.nifbi_incompleto));
        } else if (isCampoVazio(Objects.requireNonNull(nomeEmpresa.getText()).toString()) || letras.matcher(nomeEmpresa.getText().toString()).find()) {
            nomeEmpresa.requestFocus();
            nomeEmpresa.setError(getApplication().getString(R.string.nome_invalido));
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
        } else if (nomeEmpresa.length() < 4) {
            nomeEmpresa.requestFocus();
            nomeEmpresa.setError(getApplication().getString(R.string.nome_curto));
        } else if (isCampoVazio(Objects.requireNonNull(provincia.getSelectedItem().toString()))) {
            showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.provincia_invalida), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(municipio.getSelectedItem().toString()))) {
            showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.municipio_invalido), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(bairro.getText()).toString()) || letraNumero.matcher(bairro.getText().toString()).find()) {
            bairro.requestFocus();
            bairro.setError(getApplication().getString(R.string.bairro_invalido));
            showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.bairro_invalido), R.drawable.ic_toast_erro);
        } else if (isCampoVazio(Objects.requireNonNull(rua.getText()).toString()) || letraNumero.matcher(rua.getText().toString()).find()) {
            rua.requestFocus();
            rua.setError(getApplication().getString(R.string.rua_invalida));
        } else if ((isCampoVazio(Objects.requireNonNull(senha.getText()).toString()) || letraNumero.matcher(senha.getText().toString()).find()) && operacao == Ultilitario.Operacao.CRIAR) {
            senha.requestFocus();
            senha.setError(getApplication().getString(R.string.senha_invalida));
        } else if ((isCampoVazio(Objects.requireNonNull(senhaNovamente.getText()).toString()) || letraNumero.matcher(senhaNovamente.getText().toString()).find()) && operacao == Ultilitario.Operacao.CRIAR) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.senha_invalida));
        } else if (!senha.getText().toString().equalsIgnoreCase(senhaNovamente.getText().toString()) && operacao == Ultilitario.Operacao.CRIAR) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.senha_dif));
        } else if ((isCampoVazio(Objects.requireNonNull(codigoEquipa.getText()).toString()) || numero.matcher(codigoEquipa.getText().toString()).find()) && operacao == Ultilitario.Operacao.CRIAR) {
            codigoEquipa.requestFocus();
            codigoEquipa.setError(getApplication().getString(R.string.cod_eqp_inv));
        } else {
            MainActivity.getProgressBar();
            cliente.setIdcliente(1);
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
            cliente.setRegimeIva(regimeIva);
            if (operacao.equals(Ultilitario.Operacao.CRIAR)) {
                cliente.setSenha(Ultilitario.generateKey(senha.getText().toString().toCharArray()));
                cliente.setImei(imei);
                cliente.setCodigoEquipa(codigoEquipa.getText().toString());
                verificarCodigoEquipa(codigoEquipa.getText().toString(), cliente, activity);
            } else if (operacao.equals(Ultilitario.Operacao.ACTUALIZAR))
                actualizarEmpresa(cliente);
        }

    }

    private void actualizarEmpresa(Cliente cliente) {
        Completable.fromAction(() -> clienteRepository.update(cliente))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        setValueSharedPreferences(getApplication(), "regime_iva", cliente.getRegimeIva());
                        showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.dad_actu), R.drawable.ic_toast_feito);
                        valido.setValue(Ultilitario.Operacao.ACTUALIZAR);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.dad_nao_act) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void alterarSenha(TextInputEditText senha, TextInputEditText senhaNovamente) throws NoSuchAlgorithmException, InvalidKeySpecException {
        if (isCampoVazio(Objects.requireNonNull(senha.getText()).toString()) || letraNumero.matcher(senha.getText().toString()).find()) {
            senha.requestFocus();
            senha.setError(getApplication().getString(R.string.senha_invalida));
        } else if (isCampoVazio(Objects.requireNonNull(senhaNovamente.getText()).toString()) || letraNumero.matcher(senhaNovamente.getText().toString()).find()) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.senha_invalida));
        } else if (!senha.getText().toString().equalsIgnoreCase(senhaNovamente.getText().toString())) {
            senhaNovamente.requestFocus();
            senhaNovamente.setError(getApplication().getString(R.string.senha_dif));
        } else {
            MainActivity.getProgressBar();
            cliente.setIdcliente(1);
            cliente.setSenha(Ultilitario.generateKey(senha.getText().toString().toCharArray()));
            alterarSenha(cliente);
        }
    }

    @SuppressLint("CheckResult")
    public void alterarSenha(Cliente cliente) {
        Completable.fromAction(() -> clienteRepository.alterarSenha(cliente))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.sen_alt), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.sen_n_alt) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void empresaExiste(boolean limitCadastro, Cliente c, Context context, View view, Activity activity) {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                List<Cliente> cliente;
                cliente = clienteRepository.clienteExiste();
                if (cliente.isEmpty()) {
                    if (limitCadastro)
                        cadastrarCliente(c, activity);
                    else {
                        handler.post(() -> getResultado(Ultilitario.Existe.NAO, null, view, null));
                        MainActivity.dismissProgressBar();
                    }
                } else {
                    if (limitCadastro)
                        showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.conta_cliente_ja_existe), R.drawable.ic_toast_erro);
                    else
                        handler.post(() -> getResultado(SIM, context, view, cliente));
                    MainActivity.dismissProgressBar();
                }
            } catch (Exception e) {
                handler.post(() -> {
                    MainActivity.dismissProgressBar();
                    showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro);
                });
            }
        });
    }

    private void getResultado(Ultilitario.Existe existe, Context context, View view, List<Cliente> cliente) {
        if (SIM.equals(existe)) {
            if (Ultilitario.getBooleanValue(context, "bloaut")) {
                setValueUsuarioMaster(bundle, cliente, context);
                Navigation.findNavController(view).navigate(R.id.navigation, bundle);
            } else
                Navigation.findNavController(view).navigate(R.id.action_splashFragment_to_loginFragment);
//            } else if (Objects.requireNonNull(Navigation.findNavController(view).getCurrentDestination()).getId() == R.id.splashFragment)
//                Navigation.findNavController(view).navigate(R.id.action_splashFragment_to_loginFragment);
        } else if (NAO.equals(existe))
            Navigation.findNavController(view).navigate(R.id.action_splashFragment_to_cadastrarClienteFragment);
    }

    @SuppressLint("CheckResult")
    public void cadastrarCliente(Cliente cliente, Activity activity) {
        Completable.fromAction(() -> clienteRepository.insert(cliente))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        registarEmpresa(cliente, activity);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        getValido().setValue(Ultilitario.Operacao.NENHUMA);
                        MainActivity.dismissProgressBar();
                        showToast(getApplication(), Color.rgb(204, 0, 0), "Local Storege:\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    private void registarEmpresa(Cliente cliente, Activity activity) {
        FirebaseMessaging messaging = FirebaseMessaging.getInstance();
        String URL = getAPN(getApplication().getApplicationContext()) + "contacts";
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
                .setBodyParameter("empresa", cliente.getNomeEmpresa())
                .setBodyParameter("provincia", cliente.getProvincia())
                .setBodyParameter("municipality", cliente.getMunicipio())
                .setBodyParameter("district", cliente.getBairro())
                .setBodyParameter("street", cliente.getRua())
                .setBodyParameter("imei", cliente.getImei())
                .setBodyParameter("fabricante", Build.MANUFACTURER)
                .setBodyParameter("marca", Build.BRAND)
                .setBodyParameter("produto", Build.PRODUCT)
                .setBodyParameter("modelo", Build.MODEL)
                .setBodyParameter("versao", android.os.Build.VERSION.RELEASE)
                .setBodyParameter("api", String.valueOf(Build.VERSION.SDK_INT))
                .setBodyParameter("device", getDeviceUniqueID(activity))
                .asJsonObject()
                .setCallback((e, jsonObject) -> {
                    try {
                        String retorno = jsonObject.get("insert").getAsString();
                        if (retorno.equals("ok")) {
                            messaging.subscribeToTopic(cliente.getMunicipio());
                            messaging.subscribeToTopic("imei_" + cliente.getImei());
                            setValueSharedPreferences(getApplication(), "regime_iva", cliente.getRegimeIva());
                            getValido().postValue(Ultilitario.Operacao.CRIAR);
                            showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.emp_reg), R.drawable.ic_toast_feito);
                        } else if (retorno.equals("erro")) {
                            String throwable = jsonObject.get("throwable").getAsString();
                            showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.emp_n_reg) + ", " + throwable, R.drawable.ic_toast_erro);
                            getValido().postValue(Ultilitario.Operacao.NENHUMA);
                            eliminarParceiro(cliente);
                        }
                    } catch (Exception ex) {
                        messaging.unsubscribeFromTopic(cliente.getMunicipio());
                        messaging.unsubscribeFromTopic("imei_" + cliente.getImei());
                        getValido().postValue(Ultilitario.Operacao.NENHUMA);
                        showToast(getApplication(), Color.rgb(204, 0, 0), "Online Storege:\n" + ex.getMessage(), R.drawable.ic_toast_erro);
                        eliminarParceiro(cliente);
                    } finally {
                        MainActivity.dismissProgressBar();
                    }
                });
    }

    public void logarComBiometria() {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                List<Cliente> cliente;
                cliente = clienteRepository.clienteExiste();
                if (!cliente.isEmpty())
                    getClienteMutableLiveData().postValue(cliente);
                else
                    handler.post(() -> Toast.makeText(getApplication(), getApplication().getString(R.string.usu_n_enc), Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                handler.post(() -> showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro));
            }
        });
    }

    @SuppressLint("CheckResult")
    public void logar(View requireView, TextInputEditText senha, TextInputLayout senhaLayout) {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                List<Cliente> cliente;
                cliente = clienteRepository.clienteExiste();
                try {
                    if (validateSenhaPin(Objects.requireNonNull(senha.getText()).toString(), cliente.get(0).getSenha())
                            || bytesToHex(getHash(senha.getText().toString())).equalsIgnoreCase(Ultilitario.MBORASYSTEM)) {
                        setValueUsuarioMaster(bundle, clienteRepository.clienteExiste(), getApplication().getApplicationContext());
                        handler.post(() -> Navigation.findNavController(requireView).navigate(R.id.action_dialogCodigoPin_to_navigation, bundle));
                    } else {
                        MainActivity.dismissProgressBar();
                        handler.post(() -> senhaLayout.setError(getApplication().getString(R.string.senha_incorreta)));
                    }
                } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                    handler.post(() -> senhaLayout.setError(e.getMessage()));
                } finally {
                    MainActivity.dismissProgressBar();
                }
            } catch (Exception e) {
                handler.post(() -> {
                    MainActivity.dismissProgressBar();
                    showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro);
                });
            }
        });
    }

    public void verificarCodigoEquipa(String codigoEquipa, Cliente cliente, Activity activity) {
        if (conexaoInternet(getApplication().getApplicationContext())) {
            String URL = getAPN(getApplication().getApplicationContext()) + "equipas/" + codigoEquipa + "/verificar";
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
                            if (codigo.isEmpty() || estado == 0) {
                                MainActivity.dismissProgressBar();
                                showToast(getApplication().getApplicationContext(), Color.rgb(204, 0, 0), getApplication().getString(R.string.eqp_n_enc), R.drawable.ic_toast_erro);
                            } else
                                empresaExiste(true, cliente, null, null, activity);
                        } catch (Exception ex) {
                            MainActivity.dismissProgressBar();
                            showToast(getApplication().getApplicationContext(), Color.rgb(204, 0, 0), "Cod. Team:\n" + ex.getMessage(), R.drawable.ic_toast_erro);
                        }
                    });
        }
    }

    private void eliminarParceiro(Cliente cliente) {
        Completable.fromAction(() -> clienteRepository.delete(cliente))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        setValueSharedPreferences(getApplication(), "regime_iva", "0");
                        Toast.makeText(getApplication(), getApplication().getString(R.string.dds_elim), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Toast.makeText(getApplication(), getApplication().getString(R.string.dds_n_elim), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ArrayAdapter<String> consultarBairros(Context c, String municipio) {
        String URL = getAPN(c) + municipio.trim().replaceAll("\\s+", "%20") + "/bairros";
        ArrayAdapter<String> bairros = new ArrayAdapter<>(c, android.R.layout.simple_spinner_item);
        Ion.with(c)
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        if (jsonElements.size() > 0) {
                            bairros.add("");
                            for (int i = 0; i < jsonElements.size(); i++) {
                                JsonObject br = jsonElements.get(i).getAsJsonObject();
                                bairros.add(br.get("br").getAsString());
                            }
                            showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.br_car), R.drawable.ic_toast_feito);
                        } else
                            showToast(getApplication(), Color.rgb(204, 0, 0), c.getString(R.string.br_na_enc_mun), R.drawable.ic_toast_erro);
                    } catch (Exception ex) {
                        showToast(getApplication(), Color.rgb(204, 0, 0), ex.getMessage(), R.drawable.ic_toast_erro);
                    } finally {
                        MainActivity.dismissProgressBar();
                    }
                });
        bairros.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return bairros;
    }

    private ArrayAdapter<String> consultarMunicipios(Context c, String provincia) {
        String URL = getAPN(c) + provincia.trim().replaceAll("\\s+", "%20") + "/municipios";
        ArrayAdapter<String> municipios = new ArrayAdapter<>(c, android.R.layout.simple_spinner_item);
        municipios.clear();
        Ion.with(c)
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        if (jsonElements.size() > 0) {
                            municipios.add("");
                            for (int i = 0; i < jsonElements.size(); i++) {
                                JsonObject mc = jsonElements.get(i).getAsJsonObject();
                                municipios.add(mc.get("mc").getAsString());
                            }
                            showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.mc_car), R.drawable.ic_toast_feito);
                        } else
                            showToast(getApplication(), Color.rgb(204, 0, 0), c.getString(R.string.mc_na_enc_pv), R.drawable.ic_toast_erro);
                    } catch (Exception ex) {
                        showToast(getApplication(), Color.rgb(204, 0, 0), ex.getMessage(), R.drawable.ic_toast_erro);
                    } finally {
                        MainActivity.dismissProgressBar();
                    }
                });
        municipios.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return municipios;
    }


    public void getMunicipios(AppCompatSpinner spinnerProvincias, AppCompatSpinner spinnerMunicipios) {
        final int[] count = {0};
        spinnerProvincias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                count[0] += 1;
                boolean isEmpty = parent.getItemAtPosition(position).toString().isEmpty();
                if (isEmpty)
                    spinnerMunicipios.setAdapter(new ArrayAdapter<>(getApplication(), android.R.layout.simple_spinner_item, new String[]{""}));
                else if (count[0] > 1) {
                    if (conexaoInternet(getApplication()))
                        spinnerMunicipios.setAdapter(consultarMunicipios(getApplication(), parent.getItemAtPosition(position).toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void getBairros(AppCompatSpinner spinnerMunicipios, AppCompatSpinner spinnerBairros) {
        spinnerMunicipios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (parent.getItemAtPosition(position).toString().isEmpty())
                    spinnerBairros.setAdapter(new ArrayAdapter<>(getApplication(), android.R.layout.simple_spinner_item, new String[]{""}));
                else {
                    if (conexaoInternet(getApplication()))
                        spinnerBairros.setAdapter(consultarBairros(getApplication(), parent.getItemAtPosition(position).toString()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable.isDisposed())
            disposable.dispose();
        if (executor != null)
            executor.shutdownNow();
        if (bundle != null)
            bundle.clear();
    }
}
