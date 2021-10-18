package com.yoga.mborasystem.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Color;
import android.view.View;

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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class VendaViewModel extends AndroidViewModel {

    private Venda venda;
    private Disposable disposable;
    private VendaRepository vendaRepository;
    private ClienteRepository clienteRepository;
    private CompositeDisposable compositeDisposable;


    public VendaViewModel(@NonNull Application application) {
        super(application);
        venda = new Venda();
        disposable = new CompositeDisposable();
        compositeDisposable = new CompositeDisposable();
        vendaRepository = new VendaRepository(getApplication());
        clienteRepository = new ClienteRepository(getApplication());
    }

    MutableLiveData<Event<Boolean>> exportLocal;
    MutableLiveData<Boolean> imprimir, guardarPdf, selectedData;
    MutableLiveData<String> enviarWhatsApp;
    MutableLiveData<Event<String>> dataExport;

    public MutableLiveData<Boolean> getPrintLiveData() {
        if (imprimir == null) {
            imprimir = new MutableLiveData<>();
        }
        return imprimir;
    }

    public MutableLiveData<Boolean> getGuardarPdfLiveData() {
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

    MutableLiveData<Cliente> dataAdminMaster;

    public MutableLiveData<Cliente> getAdminMasterLiveData() {
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
    MutableLiveData<Event<List<Venda>>> vendas;

    public MutableLiveData<List<Venda>> getListaVendasLiveData() {
        if (listaVendas == null) {
            listaVendas = new MutableLiveData<>();
        }
        return listaVendas;
    }

    public MutableLiveData<Event<String>> getDataExportAppLiveData() {
        if (dataExport == null) {
            dataExport = new MutableLiveData<>();
        }
        return dataExport;
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

    MutableLiveData<List<ProdutoVenda>> produtosVenda;

    public MutableLiveData<List<ProdutoVenda>> getProdutosVendaLiveDta() {
        if (produtosVenda == null) {
            produtosVenda = new MutableLiveData<>();
        }
        return produtosVenda;
    }

    @SuppressLint("CheckResult")
    public void cadastrarVenda(String txtNomeCliente, TextInputEditText desconto, int quantidade, int valorBase, String codigoQr, int valorIva, String formaPagamento, int totalDesconto, int totalVenda, Map<Long, Produto> produtos, Map<Long, Integer> precoTotalUnit, int valorDivida, int valorPago, long idoperador, long idcliente, View view) {
        venda.setNome_cliente(txtNomeCliente);
        venda.setDesconto(Ultilitario.removerKZ(desconto));
        venda.setQuantidade(quantidade);
        venda.setValor_base(valorBase);
        venda.setCodigo_qr(codigoQr);
        venda.setValor_iva(valorIva);
        venda.setPagamento(formaPagamento);
        venda.setTotal_desconto(totalDesconto);
        venda.setTotal_venda(totalVenda);
        venda.setDivida(valorDivida);
        venda.setValor_pago(valorPago);
        venda.setEstado(Ultilitario.UM);
        venda.setData_cria(Ultilitario.getDateCurrent());
        venda.setIdoperador(idoperador);
        venda.setIdclicant(idcliente);
        Completable.fromAction(() -> vendaRepository.insert(venda, produtos, precoTotalUnit))
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
                        FacturaFragmentDirections.ActionFacturaFragmentToDialogVendaEfectuada action = FacturaFragmentDirections.actionFacturaFragmentToDialogVendaEfectuada().setPrecoTotal(totalVenda);
                        Navigation.findNavController(view).navigate(action);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.venda_nao_efectuada) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void getDataAdminMaster() {
        clienteRepository.clienteExiste().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new SingleObserver<Cliente>() {
                    @Override
                    public void onSubscribe(@NotNull Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onSuccess(@NotNull Cliente cliente) {
                        getAdminMasterLiveData().setValue(cliente);
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.dados_admin_nao) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    public void consultarVendas(SwipeRefreshLayout mySwipeRefreshLayout) {
        compositeDisposable.add(vendaRepository.getVendas()
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

    public void searchVendas(String codQr) {
        compositeDisposable.add(vendaRepository.getSearchVendas(codQr)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vendas -> {
                    getListaVendasLiveData().setValue(vendas);
                    Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
                }, throwable -> {
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                }));
    }

    public void getVendasPorData(String data, boolean isExport) {
        compositeDisposable.add(vendaRepository.getVendasPorData(data)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(vendas -> {
                    if (isExport) {
                        getVendasParaExportar().setValue(new Event<>(vendas));
                    } else {
                        getListaVendasLiveData().setValue(vendas);
                    }
                    Ultilitario.getValido().setValue(Ultilitario.Operacao.NENHUMA);
                }, throwable -> {
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_venda) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                }));
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

    public void getProdutosVenda(long idvenda, String codQr) {
        compositeDisposable.add(vendaRepository.getProdutosVenda(idvenda, codQr)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(produtos -> {
                    getProdutosVendaLiveDta().setValue(produtos);
                }, throwable -> {
                    Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.falha_lista_produto) + "\n" + throwable.getMessage(), R.drawable.ic_toast_erro);
                }));
    }

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

    public void eliminarVendaLixeira(int estado, String data, long idivida) {
        Completable.fromAction(() -> vendaRepository.eliminarVendaLixeira(estado, data, idivida))
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
                        Ultilitario.showToast(getApplication(), Color.rgb(102, 153, 0), getApplication().getString(R.string.vend_elim), R.drawable.ic_toast_feito);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        MainActivity.dismissProgressBar();
                        Ultilitario.showToast(getApplication(), Color.rgb(204, 0, 0), getApplication().getString(R.string.vend_n_elim) + "\n" + e.getMessage(), R.drawable.ic_toast_erro);
                    }
                });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (disposable != null || !disposable.isDisposed()) {
            disposable.dispose();
        }
        if (compositeDisposable != null || !compositeDisposable.isDisposed()) {
            compositeDisposable.clear();
        }
    }

}
