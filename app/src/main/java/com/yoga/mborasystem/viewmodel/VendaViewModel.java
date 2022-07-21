package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.model.entidade.ProdutoVenda;
import com.yoga.mborasystem.model.entidade.Venda;
import com.yoga.mborasystem.repository.ClienteRepository;
import com.yoga.mborasystem.repository.VendaRepository;
import com.yoga.mborasystem.util.Event;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.view.FacturaFragmentDirections;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VendaViewModel extends AndroidViewModel {

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

    MutableLiveData<Long> guardarPdf, imprimir;
    MutableLiveData<Event<Boolean>> exportLocal;
    MutableLiveData<Boolean> selectedData;
    MutableLiveData<String> enviarWhatsApp;
    MutableLiveData<Event<String>> dataExport, dataVenda, dataDocumento;

    public MutableLiveData<Long> getPrintLiveData() {
        if (imprimir == null) {
            imprimir = new MutableLiveData<>();
        }
        return imprimir;
    }

    public MutableLiveData<Long> getGuardarPdfLiveData() {
        if (guardarPdf == null) {
            guardarPdf = new MutableLiveData<>();
        }
        return guardarPdf;
    }

    public MutableLiveData<String> getEnviarWhatsAppLiveData() {
        if (enviarWhatsApp == null) {
            enviarWhatsApp = new MutableLiveData<>();
        }
        return enviarWhatsApp;
    }

    MutableLiveData<List<Cliente>> dataAdminMaster;

    public MutableLiveData<List<Cliente>> getAdminMasterLiveData() {
        if (dataAdminMaster == null) {
            dataAdminMaster = new MutableLiveData<>();
        }
        return dataAdminMaster;
    }

    MutableLiveData<AlertDialog> dialog;

    public MutableLiveData<AlertDialog> getAlertDialogLiveData() {
        if (dialog == null) {
            dialog = new MutableLiveData<>();
        }
        return dialog;
    }

    public MutableLiveData<Boolean> getSelectedDataMutableLiveData() {
        if (selectedData == null) {
            selectedData = new MutableLiveData<>();
        }
        return selectedData;
    }

    MutableLiveData<List<Venda>> listaVendas;
    MutableLiveData<Event<List<Venda>>> vendas, vendasGuardarImprimir;

    public MutableLiveData<List<Venda>> getListaVendasLiveData() {
        if (listaVendas == null) {
            listaVendas = new MutableLiveData<>();
        }
        return listaVendas;
    }

    public MutableLiveData<Event<List<Venda>>> getVendasGuardarImprimir() {
        if (vendasGuardarImprimir == null) {
            vendasGuardarImprimir = new MutableLiveData<>();
        }
        return vendasGuardarImprimir;
    }

    public MutableLiveData<Event<String>> getDataExportAppLiveData() {
        if (dataExport == null) {
            dataExport = new MutableLiveData<>();
        }
        return dataExport;
    }

    public MutableLiveData<Event<String>> getVendaDatatAppLiveData() {
        if (dataVenda == null) {
            dataVenda = new MutableLiveData<>();
        }
        return dataVenda;
    }

    public MutableLiveData<Event<String>> getDocumentoDatatAppLiveData() {
        if (dataDocumento == null) {
            dataDocumento = new MutableLiveData<>();
        }
        return dataDocumento;
    }

    public MutableLiveData<Event<List<Venda>>> getVendasParaExportar() {
        if (vendas == null) {
            vendas = new MutableLiveData<>();
        }
        return vendas;
    }

    public MutableLiveData<Event<Boolean>> getExportarLocalLiveData() {
        if (exportLocal == null) {
            exportLocal = new MutableLiveData<>();
        }
        return exportLocal;
    }

    MutableLiveData<Event<List<ProdutoVenda>>> produtosVenda;

    public MutableLiveData<Event<List<ProdutoVenda>>> getProdutosVendaLiveData() {
        if (produtosVenda == null) {
            produtosVenda = new MutableLiveData<>();
        }
        return produtosVenda;
    }

    private MutableLiveData<Ultilitario.Operacao> valido;

    public MutableLiveData<Ultilitario.Operacao> getValido() {
        if (valido == null) {
            valido = new MutableLiveData<>();
        }
        return valido;
    }

    private long idvenda;

    @SuppressLint("CheckResult")
    public long cadastrarVenda(String txtNomeCliente, TextInputEditText desconto, int quantidade, int valorBase, String ReferenciaFactura, int valorIva, String formaPagamento, int totalDesconto, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit, int valorDivida, int valorPago, long idoperador, long idcliente, View view) {
        venda.setNome_cliente(txtNomeCliente);
        venda.setDesconto(Ultilitario.removerKZ(desconto));
        venda.setQuantidade(quantidade);
        venda.setValor_base(valorBase);
        venda.setCodigo_qr(ReferenciaFactura);
        venda.setValor_iva(valorIva);
        venda.setPagamento(formaPagamento);
        venda.setTotal_desconto(totalDesconto);
        venda.setTotal_venda(totalVenda);
        venda.setDivida(valorDivida);
        venda.setValor_pago(valorPago);
        venda.setEstado(Ultilitario.UM);
        venda.setData_cria(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent()));
        venda.setIdoperador(idoperador);
        venda.setIdclicant(idcliente);
        Completable.fromAction(() -> {
            idvenda = vendaRepository.insert(venda, produtos, precoTotalUnit);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        FacturaFragmentDirections.ActionFacturaFragmentToDialogVendaEfectuada action = FacturaFragmentDirections.actionFacturaFragmentToDialogVendaEfectuada().setPrecoTotal(totalVenda).setIdvenda(idvenda);
                        Navigation.findNavController(view).navigate(action);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.venda_nao_efectuada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
        return idvenda;
    }

    @SuppressLint("CheckResult")
    public void getDataAdminMaster() {
        executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                List<Cliente> cliente;
                cliente = clienteRepository.clienteExiste();
                if (cliente.isEmpty()) {
                    handler.post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.dados_admin_nao), R.drawable.ic_toast_erro));
                } else {
                    getAdminMasterLiveData().postValue(cliente);
                }
            } catch (Exception e) {
                handler.post(() -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), e.getMessage(), R.drawable.ic_toast_erro));
            }
        });
    }

    public void consultarVendas(SwipeRefreshLayout mySwipeRefreshLayout, long idcliente, boolean isdivida, long idusuario, boolean isLixeira) {
        compositeDisposable.add(vendaRepository.getVendas(idcliente, isdivida, idusuario, isLixeira)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vendas -> {
                    getListaVendasLiveData().setValue(vendas);
                    Ultilitario.swipeRefreshLayout(mySwipeRefreshLayout);
                    MainActivity.dismissProgressBar();
                }, e -> {
                    MainActivity.dismissProgressBar();
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                }));
    }

    public void searchVendas(String codQr, long idcliente, boolean isDivida, long idusuario, boolean isLixeira) {
        compositeDisposable.add(vendaRepository.getSearchVendas(codQr, idcliente, isDivida, idusuario, isLixeira)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vendas -> {
                    getListaVendasLiveData().setValue(vendas);
                    getValido().setValue(Ultilitario.Operacao.NENHUMA);
                }, throwable -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro)));
    }

    public void getVendasPorDataExport(String data) {
        getVendasParaExportar().postValue(new Event<>(vendaRepository.getVendasPorDataExport(data)));
    }

    public void getVendasPorData(String data, boolean isExport, long idcliente, boolean isDivida, long idusuario, boolean isVenda) {
        compositeDisposable.add(vendaRepository.getVendasPorData(data, idcliente, isDivida, idusuario)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vendas -> {
                    if (isExport) {
                        getVendasParaExportar().setValue(new Event<>(vendas));
                    } else {
                        if (isVenda) {
                            getListaVendasLiveData().setValue(vendas);
                        } else {
                            getVendasGuardarImprimir().setValue(new Event<>(vendas));
                        }
                    }
                    getValido().setValue(Ultilitario.Operacao.NENHUMA);
                }, throwable -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro)));
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

    public void getProdutosVenda(long idvenda, String codQr, String data, boolean isGuardarImprimir) {
        compositeDisposable.add(vendaRepository.getProdutosVenda(idvenda, codQr, data, isGuardarImprimir)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(produtos -> getProdutosVendaLiveData().setValue(new Event<>(produtos)), throwable -> Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_produto) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro)));
    }

    @SuppressLint("CheckResult")
    public void liquidarDivida(int divida, long idivida) {
        Completable.fromAction(() -> vendaRepository.liquidarDivida(divida, idivida))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.div_liq), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.div_n_liq) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void eliminarVendaLixeira(int estado, String data, Venda venda, boolean isLixeira, boolean eliminarTodasLixeira) {
        Completable.fromAction(() -> vendaRepository.eliminarVendaLixeira(estado, data, venda, isLixeira, eliminarTodasLixeira))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        if (isLixeira || eliminarTodasLixeira) {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), eliminarTodasLixeira ? getApplication().getString(R.string.vends_elims) : getApplication().getString(R.string.vend_elim), R.drawable.ic_toast_feito);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.vend_env_lx), R.drawable.ic_toast_feito);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.vend_n_elim) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void restaurarVenda(int estado, long idvenda, boolean todasVendas) {
        MainActivity.getProgressBar();
        Completable.fromAction(() -> vendaRepository.restaurarVenda(estado, idvenda, todasVendas))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onComplete() {
                        MainActivity.dismissProgressBar();
                        if (todasVendas) {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.vends_rests), R.drawable.ic_toast_feito);
                            consultarVendas(null, 0, false, 0, true);
                        } else {
                            Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.vend_rest), R.drawable.ic_toast_feito);
                            consultarVendas(null, 0, false, 0, true);
                        }
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.vend_n_rest) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                        consultarVendas(null, 0, false, 0, true);
                        MainActivity.dismissProgressBar();
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
        if (disposable != null || !Objects.requireNonNull(disposable).isDisposed()) {
            disposable.dispose();
        }
        if (compositeDisposable != null || !Objects.requireNonNull(compositeDisposable).isDisposed()) {
            compositeDisposable.clear();
        }
        if (executor != null)
            executor.shutdownNow();
    }

}
