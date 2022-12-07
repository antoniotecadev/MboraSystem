package com.yoga.mborasystem.viewmodel;

import static com.yoga.mborasystem.util.Ultilitario.formatarValor;
import static com.yoga.mborasystem.util.Ultilitario.getDataFormatMonth;
import static com.yoga.mborasystem.util.Ultilitario.getFilePathCache;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelKt;
import androidx.navigation.Navigation;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.repository.ClienteRepository;
import com.yoga.mborasystem.repository.VendaRepository;
import com.yoga.mborasystem.util.EncriptaDecriptaRSA;
import com.yoga.mborasystem.util.Event;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.view.FacturaFragmentDirections;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import autodispose2.AutoDispose;
import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class VendaViewModel extends AndroidViewModel {

    public boolean crud;
    private final Venda venda;
    private Disposable disposable;
    private ExecutorService executor;
    private final VendaRepository vendaRepository;
    private final ClienteRepository clienteRepository;
    private final CompositeDisposable compositeDisposable;

    public VendaViewModel(@NonNull Application application) {
        super(application);
        venda = new Venda();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        vendaRepository = new VendaRepository(getApplication());
        clienteRepository = new ClienteRepository(getApplication());
    }

    MutableLiveData<Long> quantidade;
    MutableLiveData<Boolean> selectedData;
    MutableLiveData<Event<Venda>> imprimirNC;
    MutableLiveData<Event<Boolean>> exportLocal;
    MutableLiveData<Event<Long>> guardarPdf, imprimir, partilhar;
    MutableLiveData<Event<String>> dataExport, dataVenda, dataDocumento, enviarWhatsApp;

    public MutableLiveData<Long> getQuantidadeVenda() {
        if (quantidade == null)
            quantidade = new MutableLiveData<>();
        return quantidade;
    }

    public MutableLiveData<Event<Long>> getPrintLiveData() {
        if (imprimir == null)
            imprimir = new MutableLiveData<>();
        return imprimir;
    }

    public MutableLiveData<Event<Venda>> getPrintNCLiveData() {
        if (imprimirNC == null)
            imprimirNC = new MutableLiveData<>();
        return imprimirNC;
    }

    public MutableLiveData<Event<Long>> getGuardarPdfLiveData() {
        if (guardarPdf == null)
            guardarPdf = new MutableLiveData<>();
        return guardarPdf;
    }

    public MutableLiveData<Event<Long>> partilharPdfLiveData() {
        if (partilhar == null)
            partilhar = new MutableLiveData<>();
        return partilhar;
    }

    public MutableLiveData<Event<String>> getEnviarWhatsAppLiveData() {
        if (enviarWhatsApp == null)
            enviarWhatsApp = new MutableLiveData<>();
        return enviarWhatsApp;
    }

    MutableLiveData<List<Cliente>> dataAdminMaster;

    public MutableLiveData<List<Cliente>> getAdminMasterLiveData() {
        if (dataAdminMaster == null)
            dataAdminMaster = new MutableLiveData<>();
        return dataAdminMaster;
    }

    MutableLiveData<Event<AlertDialog>> dialog;

    public MutableLiveData<Event<AlertDialog>> getAlertDialogLiveData() {
        if (dialog == null)
            dialog = new MutableLiveData<>();
        return dialog;
    }

    public MutableLiveData<Boolean> getSelectedDataMutableLiveData() {
        if (selectedData == null)
            selectedData = new MutableLiveData<>();
        return selectedData;
    }

    MutableLiveData<PagingData<Venda>> listaVendas;
    MutableLiveData<Event<List<Venda>>> vendas, vendasGuardarImprimir;

    public MutableLiveData<PagingData<Venda>> getListaVendasLiveData() {
        if (listaVendas == null)
            listaVendas = new MutableLiveData<>();
        return listaVendas;
    }

    public MutableLiveData<Event<List<Venda>>> getVendasGuardarImprimir() {
        if (vendasGuardarImprimir == null)
            vendasGuardarImprimir = new MutableLiveData<>();
        return vendasGuardarImprimir;
    }

    public MutableLiveData<Event<String>> getDataExportAppLiveData() {
        if (dataExport == null)
            dataExport = new MutableLiveData<>();
        return dataExport;
    }

    public MutableLiveData<Event<String>> getVendaDatatAppLiveData() {
        if (dataVenda == null)
            dataVenda = new MutableLiveData<>();
        return dataVenda;
    }

    public MutableLiveData<Event<String>> getDocumentoDatatAppLiveData() {
        if (dataDocumento == null)
            dataDocumento = new MutableLiveData<>();
        return dataDocumento;
    }

    public MutableLiveData<Event<List<Venda>>> getVendasParaExportar() {
        if (vendas == null)
            vendas = new MutableLiveData<>();
        return vendas;
    }

    public MutableLiveData<Event<Boolean>> getExportarLocalLiveData() {
        if (exportLocal == null)
            exportLocal = new MutableLiveData<>();
        return exportLocal;
    }

    MutableLiveData<Event<List<ProdutoVenda>>> produtosVenda;

    public MutableLiveData<Event<List<ProdutoVenda>>> getProdutosVendaLiveData() {
        if (produtosVenda == null)
            produtosVenda = new MutableLiveData<>();
        return produtosVenda;
    }

    private MutableLiveData<List<Venda>> vendasDashboard;

    public MutableLiveData<List<Venda>> getVendasDashboard() {
        if (vendasDashboard == null)
            vendasDashboard = new MutableLiveData<>();
        return vendasDashboard;
    }

    private long idvenda;

    @SuppressLint("CheckResult")
    public void cadastrarVenda(Context context, String txtNomeCliente, TextInputEditText desconto, int percentagemDesconto, int quantidade, int valorBase, String referenciaFactura, int valorIva, String formaPagamento, int totalDesconto, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit, int valorDivida, int valorPago, long idoperador, long idcliente, String dataEmissao, View view) {
        venda.setId(0);
        venda.setNome_cliente(txtNomeCliente);
        venda.setDesconto(Ultilitario.removerKZ(desconto));
        venda.setPercentagemDesconto(percentagemDesconto);
        venda.setQuantidade(quantidade);
        venda.setValor_base(valorBase);
        venda.setCodigo_qr(referenciaFactura);
        venda.setValor_iva(valorIva);
        venda.setPagamento(formaPagamento);
        venda.setTotal_desconto(totalDesconto);
        venda.setTotal_venda(totalVenda);
        venda.setDivida(valorDivida);
        venda.setValor_pago(valorPago);
        venda.setEstado(Ultilitario.UM);
        venda.setData_cria(dataEmissao.isEmpty() ? Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()) : dataEmissao);
        venda.setIdoperador(idoperador);
        venda.setIdclicant(idcliente);
        String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        venda.setData_cria_hora(dataEmissao.isEmpty() ? getDataFormatMonth(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent())) + "T" + hora : (getDataFormatMonth(dataEmissao) + "T" + hora).trim());
        Completable.fromAction(() -> idvenda = vendaRepository.insert(venda, produtos, precoTotalUnit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        int taxPayable = venda.getDesconto() == 0 ? venda.getValor_iva() : venda.getValor_iva() - ((venda.getValor_iva() * venda.getPercentagemDesconto()) / 100);
                        int grossTotal = venda.getDesconto() == 0 ? taxPayable + venda.getValor_base() : venda.getTotal_venda() - ((venda.getTotal_venda() * venda.getPercentagemDesconto()) / 100);
                        String hashVendaLast = Ultilitario.getValueSharedPreferences(getApplication().getApplicationContext(), "hashvenda", "");
                        String vd = getDataFormatMonth(venda.getData_cria()) + ";" + venda.getData_cria_hora() + ";" + venda.getCodigo_qr() + "/" + idvenda + ";" + formatarValor(grossTotal) + ";" + hashVendaLast;
                        try {
                            String hashVenda = EncriptaDecriptaRSA.assinar(vd, EncriptaDecriptaRSA.getPrivateKey(getFilePathCache(context, "private_key.der").getAbsolutePath()), EncriptaDecriptaRSA.getPublicKey(getFilePathCache(context, "public_key.der").getAbsolutePath()));
                            if (hashVenda == null)
                                Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.err_ass), R.drawable.ic_toast_feito);
                            else {
                                insertHashVenda(context, hashVenda.trim(), idvenda);
                            }
                        } catch (Exception e) {
                            Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_feito);
                        }
                        MainActivity.dismissProgressBar();
                        FacturaFragmentDirections.ActionFacturaFragmentToDialogVendaEfectuada action = FacturaFragmentDirections.actionFacturaFragmentToDialogVendaEfectuada(referenciaFactura).setPrecoTotal(totalVenda).setIdvenda(idvenda);
                        Navigation.findNavController(view).navigate(action);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.venda_nao_efectuada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    private void insertHashVenda(Context context, String hashVenda, long idvenda) {
        compositeDisposable.add(Completable.fromAction(() -> vendaRepository.insertHashVenda(hashVenda, idvenda))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    Ultilitario.setValueSharedPreferences(context, "hashvenda", hashVenda);
                    getGuardarPdfLiveData().setValue(new Event<>(idvenda));
                }, e -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_feito)));
    }

    @SuppressLint("CheckResult")
    public void getDataAdminMaster() {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                List<Cliente> cliente;
                cliente = clienteRepository.clienteExiste();
                if (cliente.isEmpty())
                    handler.post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.dados_admin_nao), R.drawable.ic_toast_erro));
                else
                    getAdminMasterLiveData().postValue(cliente);
            } catch (Exception e) {
                handler.post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro));
            }
        });
    }

    public void consultarVendas(LifecycleOwner lifecycleOwner, long idcliente, boolean isDivida, long idusuario, boolean isLixeira, boolean isPesquisa, String referencia, boolean isData, String data) {
        Flowable<PagingData<Venda>> flowable = PagingRx.getFlowable(new Pager<>(new PagingConfig(20), () -> vendaRepository.getVendas(idcliente, isDivida, idusuario, isLixeira, isPesquisa, referencia, isData, data)));
        PagingRx.cachedIn(flowable, ViewModelKt.getViewModelScope(this));
        flowable.to(AutoDispose.autoDisposable(AndroidLifecycleScopeProvider.from(lifecycleOwner)))
                .subscribe(vendas -> {
                    if (crud)
                        getListaVendasLiveData().postValue(vendas);
                    else
                        getListaVendasLiveData().setValue(vendas);
                }, e -> new Handler(Looper.getMainLooper()).post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + e.getMessage(), R.drawable.ic_toast_erro)));
    }

    public void consultarVendasDashboard(boolean isReport, String data) {
        compositeDisposable.add(vendaRepository.getVendasDashboard(isReport, data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vendas -> {
                    if (isReport)
                        getVendasGuardarImprimir().setValue(new Event<>(vendas));
                    else
                        getVendasDashboard().setValue(vendas);
                }, e -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + e.getMessage(), R.drawable.ic_toast_erro)));
    }

    public LiveData<Long> getQuantidadeVenda(boolean isLixeira, long idcliente, boolean isDivida, long idusuario, boolean isData, String data, LifecycleOwner lifecycleOwner) {
        vendaRepository.getQuantidadeVenda(isLixeira, idcliente, isDivida, idusuario, isData, data).observe(lifecycleOwner, quantidade -> getQuantidadeVenda().setValue(quantidade));
        return null;
    }

    public void getVendasPorDataExport(String data) {
        getVendasParaExportar().postValue(new Event<>(vendaRepository.getVendasPorDataExport(data)));
    }

    public void importarVenda(List<String> vendas) {
        Completable.fromAction(() -> vendaRepository.importarVendas(vendas))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.venda_impo), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.venda_n_impo) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void getProdutosVenda(long idvenda, String codQr, String data, boolean isGuardarImprimir, boolean isForDocumentSaft, List<Long> idvendaList) {
        compositeDisposable.add(vendaRepository.getProdutosVenda(idvenda, codQr, data, isGuardarImprimir, isForDocumentSaft, idvendaList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(produtos -> getProdutosVendaLiveData().setValue(new Event<>(produtos)), throwable -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_produto) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro)));
    }

    public void getVendaSaft(String dataInicio, String dataFim) {
        compositeDisposable.add(vendaRepository.getVendaSaft(dataInicio, dataFim)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vendas -> getVendasParaExportar().setValue(new Event<>(vendas)), throwable -> {
                    MainActivity.dismissProgressBar();
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                }));
    }

    @SuppressLint("CheckResult")
    public void liquidarDivida(int divida, long idivida) {
        Completable.fromAction(() -> vendaRepository.liquidarDivida(divida, idivida))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.div_liq), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.div_n_liq) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void eliminarVendaNotaCredito(int estado, String refNC, String data, Venda venda, boolean isLixeira, boolean eliminarTodasLixeira) {
        venda.setCodigo_qr(refNC);
        venda.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
        String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        venda.setData_cria_hora(getDataFormatMonth(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent())) + "T" + hora);
        Completable.fromAction(() -> vendaRepository.eliminarVendaNotaCredito(estado, data, venda, isLixeira, eliminarTodasLixeira))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        if (isLixeira || eliminarTodasLixeira)
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), eliminarTodasLixeira ? getApplication().getString(R.string.vends_elims) : getApplication().getString(R.string.vend_elim), R.drawable.ic_toast_feito);
                        else
                            getPrintNCLiveData().setValue(new Event<>(venda));
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.vend_n_elim) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void restaurarVenda(int estado, long idvenda, boolean todasVendas) {
        Completable.fromAction(() -> vendaRepository.restaurarVenda(estado, idvenda, todasVendas))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        if (todasVendas)
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.vends_rests), R.drawable.ic_toast_feito);
                        else
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.vend_rest), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.vend_n_rest) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public LiveData<List<ProdutoVenda>> getProdutoMaisVendido(String data) {
        return vendaRepository.produtoMaisVendido(data);
    }

    public LiveData<List<ProdutoVenda>> getProdutoMenosVendido(String data) {
        return vendaRepository.produtoMenosVendido(data);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable.isDisposed())
            disposable.dispose();

        if (compositeDisposable.isDisposed())
            compositeDisposable.dispose();

        if (executor != null)
            executor.shutdownNow();
    }

}
