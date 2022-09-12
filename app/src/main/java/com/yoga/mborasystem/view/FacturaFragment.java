package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;
import static com.yoga.mborasystem.util.Ultilitario.monthInglesFrances;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.paging.PagingDataAdapter;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
import com.koushikdutta.ion.Ion;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.GroupieViewHolder;
import com.xwray.groupie.Item;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentFacturaBinding;
import com.yoga.mborasystem.databinding.FragmentListaProdutoBinding;
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.AutoCompleteClienteCantinaAdapter;
import com.yoga.mborasystem.util.CriarFactura;
import com.yoga.mborasystem.util.EventObserver;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public class FacturaFragment extends Fragment {

    private Gson gson;
    private Bundle bundle;
    private Cliente cliente;
    private long idc, idcliente;
    private BeepManager beepManager;
    private Map<Long, View> itemView;
    private Map<Long, Boolean> estado;
    private Map<Long, Produto> produtos;
    private GroupAdapter adapterFactura;
    private VendaViewModel vendaViewModel;
    private List<Long> idprodutoRascunho;
    private FragmentFacturaBinding binding;
    private ArrayList<String> listaCategoria;
    private ProdutoViewModel produtoViewModel;
    private ProdutoFacturaAdapter pagingAdapter;
    private SharedPreferences sharedPreferences;
    private boolean addScaner, load, addRascunho;
    private String resultCodeBar, referenciaFactura = "", facturaPath, dataEmissao = "";
    private ClienteCantinaViewModel clienteCantinaViewModel;
    @SuppressLint("StaticFieldLeak")
    private static DecoratedBarcodeView barcodeView;
    private ArrayAdapter<String> listCategoriaAdapter;
    private int total, totaldesconto, valorBase, valorIva, desconto, troco, valorPago, valorDivida;
    private Map<Long, Integer> precoTotal, iva, valor, posicao;
    private CategoriaProdutoViewModel categoriaProdutoViewModel;

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText().equals(resultCodeBar)) {
                barcodeView.setStatusText(getString(R.string.ja_scaneado) + " (" + result.getText() + ")");
            } else {
                addScaner = true;
                resultCodeBar = result.getText();
                barcodeView.setStatusText(result.getText() + "  " + getString(R.string.ja_scaneado));
                beepManager.playBeepSoundAndVibrate();
                consultarProdutos(idc, true, resultCodeBar, true, false, null);
                Ultilitario.showToastOrAlertDialogQrCode(requireContext(), result.getBitmapWithResultPoints(Color.YELLOW), false, null, "", "", "");
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new Gson();
        iva = new HashMap<>();
        bundle = new Bundle();
        valor = new HashMap<>();
        cliente = new Cliente();
        estado = new HashMap<>();
        posicao = new HashMap<>();
        produtos = new HashMap<>();
        itemView = new HashMap<>();
        precoTotal = new HashMap<>();
        listaCategoria = new ArrayList<>();
        adapterFactura = new GroupAdapter();
        idprodutoRascunho = new ArrayList<>();
        pagingAdapter = new ProdutoFacturaAdapter(new ProdutoFacturaComparator());
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);
        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
        sharedPreferences = requireContext().getSharedPreferences("PRODUTO_RASCUNHO", Context.MODE_PRIVATE);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFacturaBinding.inflate(inflater, container, false);
        clienteCantinaViewModel.getCliente().observe(getViewLifecycleOwner(), clientesCantina -> {
            if (!clientesCantina.isEmpty()) {
                binding.txtNomeCliente.setAdapter(new AutoCompleteClienteCantinaAdapter(requireContext(), clientesCantina));
            }
        });
        binding.txtNomeCliente.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!charSequence.toString().isEmpty()) {
                    clienteCantinaViewModel.consultarClienteCantina(charSequence.toString(), false, null);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        barcodeView = binding.viewStub.inflate().findViewById(R.id.barcode_scanner);
        binding.viewStub.setVisibility(View.GONE);
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setCameraId(0);
        integrator.setPrompt(getString(R.string.alinhar_codigo_barra));
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));
        barcodeView.initializeFromIntent(integrator.createScanIntent());
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(requireActivity());

        barcodeView.setTorchListener(new DecoratedBarcodeView.TorchListener() {
            @Override
            public void onTorchOn() {
                binding.switchFlashlightButton.setText(R.string.turn_off_flashlight);
            }

            @Override
            public void onTorchOff() {
                binding.switchFlashlightButton.setText(R.string.turn_on_flashlight);
            }
        });

        if (!hasFlash()) {
            binding.switchFlashlightButton.setVisibility(View.GONE);
        }

        binding.switchFlashlightButton.setOnClickListener(v -> {
            if (getString(R.string.turn_on_flashlight).contentEquals(binding.switchFlashlightButton.getText())) {
                barcodeView.setTorchOn();
            } else {
                barcodeView.setTorchOff();
            }
        });

        binding.buttonFechar.setOnClickListener(v -> fecharCamera());

        binding.btnCriarCliente.setOnClickListener(v -> {
            Navigation.findNavController(requireView()).navigate(FacturaFragmentDirections.actionFacturaFragmentToDialogCriarClienteCantina(binding.txtNomeCliente.getText().toString(), "", 0, "", "", ""));
            binding.txtNomeCliente.setText("");
        });

        binding.btnCleaNameClient.setOnClickListener(v -> {
            binding.txtNomeCliente.setEnabled(true);
            binding.txtNomeCliente.setText("");
            binding.txtNomeCliente.requestFocus();
        });

        binding.txtNomeCliente.setOnItemClickListener((parent, view, position, id) -> binding.txtNomeCliente.setEnabled(false));
        binding.btnScannerBack.setOnClickListener(v -> abrirCamera());
        binding.btnClose.setOnClickListener(v -> fecharCamera());
        binding.btnScannerFront.setOnClickListener(v -> abrirCamera());
        binding.recyclerViewFacturaProduto.setAdapter(pagingAdapter);
        binding.recyclerViewFacturaProduto.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFactura.setAdapter(adapterFactura);
        binding.recyclerViewFactura.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriaProdutoViewModel.categoriasSpinner(true, 0, null);
        categoriaProdutoViewModel.getListaCategoriasSpinner().observe(getViewLifecycleOwner(), new EventObserver<>(categorias -> {
            if (!categorias.isEmpty() && listaCategoria.isEmpty()) {
                for (Categoria categoria : categorias) {
                    listaCategoria.add(categoria.getId() + " - " + categoria.getCategoria());
                }
                listCategoriaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listaCategoria);
                binding.spinnerCategorias.setAdapter(listCategoriaAdapter);
            }
        }));
        binding.spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] idcategoria = TextUtils.split(parent.getItemAtPosition(position).toString(), "-");
                idc = Long.parseLong(idcategoria[0].trim());
                if (load)
                    consultarProdutos(idc, false, null, false, false, null);
                load = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        binding.checkboxTodosProdutos.setChecked(Ultilitario.getBooleanPreference(requireContext(), "checkboxTodosProdutos"));
        binding.checkboxTodosProdutos.setOnCheckedChangeListener((compoundButton, b) -> {
            Ultilitario.setBooleanPreference(requireContext(), b, "checkboxTodosProdutos");
            consultarProdutos(idc, false, null, false, false, null);
        });
        consultarProdutos(idc, false, null, false, false, null);
        produtoViewModel.getListaProdutosPaging().observe(getViewLifecycleOwner(), produtoPagingData -> {
            pagingAdapter.submitData(getLifecycle(), produtoPagingData);
            Ultilitario.swipeRefreshLayout(binding.mySwipeRefreshLayout);
        });
        binding.mySwipeRefreshLayout.setOnRefreshListener(() -> {
            consultarProdutos(idc, false, null, false, false, null);
            pagingAdapter.notifyDataSetChanged();
        });
        binding.textTaxa.setText(Ultilitario.getTaxaIva(requireActivity()) + "%");
        Ultilitario.precoFormat(getContext(), binding.textDesconto);
        binding.btnLimpar.setOnClickListener(v -> Ultilitario.zerarPreco(binding.textDesconto));
        binding.textDesconto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("Kz")) {
                    desconto = Ultilitario.removerKZ(binding.textDesconto);
                    if (total >= desconto) {
                        totaldesconto = total - desconto;
                        binding.totalDesconto.setText(getText(R.string.total_desconto) + ": " + Ultilitario.formatPreco(String.valueOf(totaldesconto)));

                        if (valorPago >= (total - desconto)) {
                            troco = valorPago - totaldesconto;
                            binding.troco.setText(getText(R.string.troco) + ": " + Ultilitario.formatPreco(String.valueOf(troco)));
                        }
                        if (desconto == 0) {
                            totaldesconto = 0;
                            binding.totalDesconto.setText(getText(R.string.total_desconto) + ": " + Ultilitario.formatPreco("0"));
                        }
                    } else {
                        binding.textDesconto.requestFocus();
                        binding.textDesconto.setError(getString(R.string.desconto_maior));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Ultilitario.precoFormat(getContext(), binding.textValorPago);

        binding.btnLimparValorPago.setOnClickListener(v -> Ultilitario.zerarPreco(binding.textValorPago));

        binding.textValorPago.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("Kz")) {
                    valorPago = Ultilitario.removerKZ(binding.textValorPago);
                    if ((total - desconto) <= valorPago) {
                        troco = valorPago - (total - desconto);
                        binding.troco.setText(getText(R.string.troco) + ": " + Ultilitario.formatPreco(String.valueOf(troco)));
                    } else {
                        troco = 0;
                        binding.troco.setText(getText(R.string.troco) + ": " + Ultilitario.formatPreco("0"));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.checkboxDivida.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (valorPago < total) {
                    binding.textValorDivida.setText("" + ((total - desconto) - valorPago));
                } else {
                    buttonView.setChecked(false);
                    Ultilitario.alertDialog(getString(R.string.dvd), getString(R.string.no_pos_apl_div), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                }
            } else {
                binding.switchEdit.setChecked(false);
                binding.checkboxSemValorPago.setChecked(false);
                binding.textValorDivida.setText(Ultilitario.formatPreco("0"));
                binding.textValorDivida.setEnabled(false);
            }
        });

        Ultilitario.precoFormat(getContext(), binding.textValorDivida);

        binding.checkboxSemValorPago.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (binding.checkboxDivida.isChecked()) {
                    binding.textValorPago.setEnabled(false);
                    binding.textValorPago.setText(Ultilitario.formatPreco("0"));
                    binding.textValorPago.setHint(getString(R.string.se_val_pag));
                } else {
                    buttonView.setChecked(false);
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.check_dvd), R.drawable.ic_toast_erro);
                }
            } else {
                binding.textValorPago.setEnabled(true);
                binding.textValorPago.setText(Ultilitario.formatPreco("0"));
            }
        });

        binding.switchEdit.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (binding.checkboxDivida.isChecked()) {
                    binding.textValorDivida.setEnabled(true);
                } else {
                    binding.switchEdit.setChecked(false);
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.check_dvd), R.drawable.ic_toast_erro);
                }
            } else {
                binding.textValorDivida.setEnabled(false);
            }
        });

        binding.btnLimparValorDivida.setOnClickListener(v -> Ultilitario.zerarPreco(binding.textValorDivida));

        binding.textValorDivida.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains("Kz")) {
                    valorDivida = Ultilitario.removerKZ(binding.textValorDivida);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Ultilitario.precoFormat(getContext(), binding.dinheiroValorPago);
        Ultilitario.precoFormat(getContext(), binding.cartaoValorPago);
        Ultilitario.precoFormat(getContext(), binding.depValorPago);
        Ultilitario.precoFormat(getContext(), binding.transfValorPago);

        checkValorFormaPagamento(binding.checkboxNumerario, binding.dinheiroValorPago);
        checkValorFormaPagamento(binding.checkboxCartaoMulticaixa, binding.cartaoValorPago);
        checkValorFormaPagamento(binding.checkboxDepositoBancario, binding.depValorPago);
        checkValorFormaPagamento(binding.checkboxTransferenciaBancario, binding.transfValorPago);

        vendaViewModel.getDocumentoDatatAppLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(data -> {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy");
            try {
                Date date1 = sdf.parse(Ultilitario.getDateCurrent());
                Date date2 = sdf.parse(data);
                if (date1 != null) {
                    if (date1.compareTo(date2) >= 0) {
                        dataEmissao = data;
                        Ultilitario.showToast(getContext(), Color.rgb(102, 153, 0), getString(R.string.dat_ems) + ": " + data, R.drawable.ic_toast_feito);
                    } else {
                        dataEmissao = "";
                        Ultilitario.alertDialog(getString(R.string.dat_nao_sel), getString(R.string.dat_ems_nao_dat_act), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    }
                }
            } catch (ParseException e) {
                dataEmissao = "";
                Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }));

        binding.btnEfectuarVenda.setOnClickListener(v -> {
            facturaPath = "";
            if (isCheckedFormaPagamento()) {
                if (valorPago > 0 || binding.checkboxSemValorPago.isChecked()) {
                    String[] nomeIDcliente;
                    if (binding.txtNomeCliente.getText().toString().trim().isEmpty()) {
                        nomeIDcliente = TextUtils.split(getString(R.string.csm_fnl), "-");
                    } else {
                        nomeIDcliente = TextUtils.split(binding.txtNomeCliente.getText().toString(), "-");
                    }
                    referenciaFactura = "FR 00V" + (dataEmissao.isEmpty() ? TextUtils.split(Ultilitario.getDateCurrent(), "-")[2].trim() : TextUtils.split(dataEmissao, "-")[2].trim());
                    if (binding.checkboxDivida.isChecked()) {
                        if (valorDivida > 0) {
                            if (nomeIDcliente.length == 3 && Long.parseLong(nomeIDcliente[1].trim()) > 0) {
                                dialogVerificarVenda(nomeIDcliente);
                            } else {
                                binding.txtNomeCliente.requestFocus();
                                binding.txtNomeCliente.setError(getString(R.string.dvd_atri_cl_cad));
                            }
                        } else {
                            binding.textValorDivida.requestFocus();
                            binding.textValorDivida.setError(getString(R.string.dt_vl_dv));
                        }
                    } else {
                        dialogVerificarVenda(nomeIDcliente);
                    }
                } else {
                    binding.textValorPago.requestFocus();
                    binding.textValorPago.setError(getString(R.string.digite_valor_pago));
                }
            } else {
                Ultilitario.showToast(getContext(), Color.rgb(250, 170, 5), getString(R.string.selecciona_forma_pagamento), R.drawable.ic_toast_erro);
            }
        });

        vendaViewModel.getDataAdminMaster();
        vendaViewModel.getAdminMasterLiveData().observe(getViewLifecycleOwner(), cliente -> this.cliente = cliente.get(0));

        vendaViewModel.getGuardarPdfLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(idvenda -> {
            if (idvenda > 0) {
                if (!referenciaFactura.isEmpty()) {
                    facturaPath = referenciaFactura + "_" + idvenda + ".pdf";
                    CriarFactura.getPemissionAcessStoregeExternal(true, getActivity(), getContext(), facturaPath, cliente, requireArguments().getLong("idoperador", 0), binding.txtNomeCliente, binding.textDesconto, valorBase, referenciaFactura, valorIva, getFormaPamento(binding), totaldesconto, valorPago, troco, total, produtos, precoTotal, dataEmissao, referenciaFactura + "/" + idvenda);
                } else {
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.venda_vazia), R.drawable.ic_toast_erro);
                }
            }
        }));

        vendaViewModel.getPrintLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(idvenda -> {
            if (idvenda > 0) {
                if (!referenciaFactura.isEmpty()) {
                    facturaPath = referenciaFactura + "_" + idvenda + ".pdf";
                    CriarFactura.getPemissionAcessStoregeExternal(false, getActivity(), getContext(), facturaPath, cliente, requireArguments().getLong("idoperador", 0), binding.txtNomeCliente, binding.textDesconto, valorBase, referenciaFactura, valorIva, getFormaPamento(binding), totaldesconto, valorPago, troco, total, produtos, precoTotal, dataEmissao, referenciaFactura + "/" + idvenda);
                } else {
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.venda_vazia), R.drawable.ic_toast_erro);
                }
            }
        }));

        vendaViewModel.getEnviarWhatsAppLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(numero -> {
            if (!numero.isEmpty()) {
                if (facturaPath.isEmpty()) {
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.enviar_w_primeiro), R.drawable.ic_toast_erro);
                } else {
                    Ultilitario.openWhatsApp(getActivity(), numero);
                }
            }
        }));
        vendaViewModel.getAlertDialogLiveData().observe(getViewLifecycleOwner(), new EventObserver<>(alertDialog -> {
            if (alertDialog != null && !Ultilitario.getNaoMostrarNovamente(requireActivity())) {
                if (facturaPath.isEmpty()) {
                    LinearLayoutCompat layout = new LinearLayoutCompat(requireContext());
                    MaterialCheckBox check = new MaterialCheckBox(requireContext());
                    check.setText(getString(R.string.n_most_nov));
                    layout.setPadding(45, 0, 0, 0);
                    layout.addView(check);
                    check.setOnCheckedChangeListener((compoundButton, b) -> Ultilitario.setNaoMostrarNovamente(requireActivity(), b));
                    new AlertDialog.Builder(requireContext())
                            .setIcon(R.drawable.ic_baseline_store_24)
                            .setTitle(R.string.fechar)
                            .setMessage(R.string.tem_cert_fech)
                            .setView(layout)
                            .setNegativeButton(R.string.nao, (dialogInterface, i) -> {
                                Ultilitario.setNaoMostrarNovamente(requireActivity(), false);
                                dialogInterface.dismiss();
                            })
                            .setPositiveButton(R.string.sim, (dialogInterface, i) -> fecharAlertDialog(alertDialog)).show();
                } else {
                    fecharAlertDialog(alertDialog);
                }
            } else {
                if (alertDialog != null)
                    fecharAlertDialog(alertDialog);

            }
        }));
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_factura, menu);
                if (getProdutoRascunho().isEmpty())
                    menu.findItem(R.id.itemEliminarRascunho).setVisible(false);
                else
                    requireActivity().setTitle(getString(R.string.rasc));
                SearchManager searchManager = (SearchManager) requireActivity().getSystemService(Context.SEARCH_SERVICE);
                MenuItem menuItem = menu.findItem(R.id.app_bar_search);
                SearchView searchView = (SearchView) menuItem.getActionView();
                searchView.setQueryHint(getString(R.string.prod) + " " + getString(R.string.ou) + " " + getString(R.string.codigo_bar));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));
                searchView.onActionViewExpanded();
                menuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        return true;
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        consultarProdutos(idc, false, null, false, false, null);
                        return true;
                    }
                });
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (newText.isEmpty()) {
                            consultarProdutos(idc, false, null, false, false, null);
                        } else {
                            consultarProdutos(idc, true, newText, true, false, null);
                        }
                        return false;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                int itemId = menuItem.getItemId();
                if (itemId == R.id.calculadoraFragmentItem) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_facturaFragment_to_calculadoraFragment);
                } else if (itemId == R.id.itemData) {
                    FacturaFragmentDirections.ActionFacturaFragmentToDatePickerFragment direction = FacturaFragmentDirections.actionFacturaFragmentToDatePickerFragment(false).setIdcliente(1).setIsDivida(false).setIdusuario(1).setIsPesquisa(true);
                    Navigation.findNavController(requireView()).navigate(direction);
                } else if (itemId == R.id.itemEliminarRascunho) {
                    eliminarRascunho();
                    Snackbar.make(binding.myCoordinatorLayout, getText(R.string.rasc_elm), Snackbar.LENGTH_LONG).show();
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        consultarRascunho();
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private void consultarRascunho() {
        if (PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("activarrascunho", false))
            if (!getProdutoRascunho().isEmpty()) {
                binding.checkboxTodosProdutos.setChecked(false);
                Ultilitario.setBooleanPreference(requireContext(), false, "checkboxTodosProdutos");
                consultarProdutos(0, true, null, false, true, getProdutoRascunho());
            }
    }

    private void consultarProdutos(long idcategoria, boolean iScrud, String produto, boolean isPesquisa, boolean isRascunho, List<Long> produtoRascunho) {
        addRascunho = isRascunho;
        produtoViewModel.crud = iScrud;
        produtoViewModel.consultarProdutos(idcategoria, produto, false, isPesquisa, getViewLifecycleOwner(), true, Ultilitario.getBooleanPreference(requireContext(), "checkboxTodosProdutos"), isRascunho, produtoRascunho);
    }

    private void fecharAlertDialog(AlertDialog alertDialog) {
        restaurar();
        alertDialog.dismiss();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void restaurar() {
        eliminarRascunho();
        binding.txtNomeCliente.setText("");
        estado.clear();
        produtos.clear();
        adapterFactura.clear();
        itemView.clear();
        precoTotal.clear();
        valor.clear();
        iva.clear();
        posicao.clear();
        total = 0;
        totaldesconto = 0;
        valorBase = 0;
        valorIva = 0;
        valorDivida = 0;
        desconto = 0;
        troco = 0;
        valorPago = 0;
        binding.textTotal.setText(Ultilitario.formatPreco("0"));
        binding.textValor.setText(Ultilitario.formatPreco("0"));
        binding.textIva.setText(Ultilitario.formatPreco("0"));
        binding.txtTot.setText(Ultilitario.formatPreco("0"));
        binding.totalDesconto.setText(Ultilitario.formatPreco("0"));
        binding.troco.setText(Ultilitario.formatPreco("0"));
        binding.txtNomeCliente.setEnabled(true);
        binding.checkboxDivida.setChecked(false);
        binding.textValorDivida.setEnabled(false);
        binding.textValorDivida.setText(Ultilitario.formatPreco("0"));
        binding.btnEfectuarVenda.setEnabled(false);
        binding.checkboxNumerario.setChecked(false);
        binding.checkboxCartaoMulticaixa.setChecked(false);
        binding.checkboxDepositoBancario.setChecked(false);
        binding.checkboxTransferenciaBancario.setChecked(false);
        Ultilitario.zerarPreco(binding.textDesconto);
        Ultilitario.zerarPreco(binding.textValorPago);
        Ultilitario.zerarPreco(binding.dinheiroValorPago);
        Ultilitario.zerarPreco(binding.cartaoValorPago);
        Ultilitario.zerarPreco(binding.depValorPago);
        Ultilitario.zerarPreco(binding.transfValorPago);
        binding.scrollView.smoothScrollTo(0, 0);
    }

    private void abrirCamera() {
        if (ContextCompat.checkSelfPermission(requireActivity(),
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            mPermissionResult.launch(Manifest.permission.CAMERA);
        } else {
            openCamera();
        }
    }

    private boolean hasFlash() {
        return requireActivity().getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    private void visibityButton() {
        binding.buttonFechar.setVisibility(View.VISIBLE);
        binding.switchFlashlightButton.setVisibility(View.VISIBLE);
    }

    private void fecharCamera() {
        binding.viewStub.setVisibility(View.GONE);
        binding.buttonFechar.setVisibility(View.GONE);
        binding.switchFlashlightButton.setVisibility(View.GONE);
        barcodeView.setTorchOff();
        barcodeView.pause();
    }

    private void checkValorFormaPagamento(MaterialCheckBox checkBox, TextInputEditText textInputEditText) {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                textInputEditText.setEnabled(true);
                binding.dinheiroValorPago.setText(String.valueOf(Ultilitario.removerKZ(binding.textValorPago)));
            } else {
                textInputEditText.setEnabled(false);
                textInputEditText.setText(Ultilitario.formatPreco("0"));
            }
        });
    }

    private int somatorioValorFormaPagamento() {
        return Ultilitario.removerKZ(binding.dinheiroValorPago) + Ultilitario.removerKZ(binding.depValorPago) + Ultilitario.removerKZ(binding.cartaoValorPago) + Ultilitario.removerKZ(binding.transfValorPago);
    }

    private void dialogVerificarVenda(String[] nomeIDcliente) {
        if (nomeIDcliente.length == 2) {
            idcliente = Long.parseLong(nomeIDcliente[1].trim());
        } else {
            idcliente = 0;
        }
        if (somatorioValorFormaPagamento() == valorPago) {
            int quantidadeProduto = 0;
            for (Map.Entry<Long, Produto> produto : produtos.entrySet())
                quantidadeProduto += (Objects.requireNonNull(precoTotal.get(produto.getKey())) / produto.getValue().getPreco());

            int finalQuantidadeProduto = quantidadeProduto;
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmar_venda)
                    .setMessage(getString(R.string.cliente) + ": " + nomeIDcliente[0] + "\n" +
                            getString(R.string.quantidade) + ": " + quantidadeProduto + "\n"
                            + getString(R.string.total) + ": " + Ultilitario.formatPreco(String.valueOf(total)) + "\n"
                            + getString(R.string.desconto) + ": " + Ultilitario.formatPreco(Objects.requireNonNull(binding.textDesconto.getText()).toString()) + "\n"
                            + getString(R.string.total_desconto) + ": " + Ultilitario.formatPreco(String.valueOf(totaldesconto)) + "\n"
                            + getString(R.string.valor_pago) + ": " + Ultilitario.formatPreco(String.valueOf(valorPago)) + "\n"
                            + getString(R.string.troco) + ": " + Ultilitario.formatPreco(String.valueOf(troco)) + "\n"
                            + getString(R.string.valor_base) + ": " + Ultilitario.formatPreco(String.valueOf(valorBase)) + "\n"
                            + getString(R.string.montante_iva) + ": " + Ultilitario.formatPreco(String.valueOf(valorIva)) + "\n"
                            + getString(R.string.dvd) + ": " + Ultilitario.formatPreco(String.valueOf(valorDivida)) + "\n"
                            + getString(R.string.forma_pagamento) + " " + getFormaPamento(binding) + "\n"
                    )
                    .setPositiveButton(R.string.vender, (dialog, which) -> {
                        MainActivity.getProgressBar();
                        if (getDataSplitDispositivo(Ultilitario.getValueSharedPreferences(requireContext(), "data", "00-00-0000")).equals(getDataSplitDispositivo(monthInglesFrances(Ultilitario.getDateCurrent())))) {
                            vendaViewModel.cadastrarVenda(nomeIDcliente[0].trim(), binding.textDesconto, finalQuantidadeProduto, valorBase, referenciaFactura, valorIva, getFormaPamento(binding), totaldesconto, total, produtos, precoTotal, valorDivida, valorPago, requireArguments().getLong("idoperador", 0), idcliente, dataEmissao, getView());
                        } else {
                            if (isNetworkConnected(requireContext())) {
                                if (internetIsConnected()) {
                                    estadoConta(cliente.getImei(), nomeIDcliente[0].trim(), finalQuantidadeProduto);
                                } else {
                                    Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.sm_int), R.drawable.ic_toast_erro);
                                    MainActivity.dismissProgressBar();
                                }
                            } else {
                                Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
                                MainActivity.dismissProgressBar();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                        facturaPath = "";
                        dialog.dismiss();
                    })
                    .show();
        } else {
            Ultilitario.alertDialog(getString(R.string.forma_pagamento), getString(R.string.smt_siff), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
    }

    private String getDataSplitDispositivo(String dataSplit) {
        String[] dataDavice = TextUtils.split(dataSplit, "-");
        return dataDavice[0].trim() + '-' + dataDavice[1].trim() + '-' + dataDavice[2].trim();
    }

    private void openCamera() {
        binding.viewStub.setVisibility(View.VISIBLE);
        visibityButton();
        barcodeView.resume();
    }

    private String getFormaPamento(FragmentFacturaBinding binding) {
        CharSequence dinheiro = binding.checkboxNumerario.isChecked() ? (binding.checkboxNumerario.getText() + " = " + Ultilitario.trocarVírgulaPorPonto(binding.dinheiroValorPago)) : "";
        CharSequence cartaoMulticaixa = binding.checkboxCartaoMulticaixa.isChecked() ? (binding.checkboxCartaoMulticaixa.getText() + " = " + Ultilitario.trocarVírgulaPorPonto(binding.cartaoValorPago)) : "";
        CharSequence depositoBancario = binding.checkboxDepositoBancario.isChecked() ? (binding.checkboxDepositoBancario.getText() + " = " + Ultilitario.trocarVírgulaPorPonto(binding.depValorPago)) : "";
        CharSequence transferenciaBancario = binding.checkboxTransferenciaBancario.isChecked() ? (binding.checkboxTransferenciaBancario.getText() + " = " + Ultilitario.trocarVírgulaPorPonto(binding.transfValorPago)) : "";
        if (binding.checkboxSemValorPago.isChecked()) {
            return getString(R.string.se_val_pag);
        } else {
            return dinheiro + " " + cartaoMulticaixa + " " + depositoBancario + " " + transferenciaBancario;
        }
    }

    private boolean isCheckedFormaPagamento() {
        if (binding.checkboxSemValorPago.isChecked()) {
            return true;
        } else {
            return binding.checkboxNumerario.isChecked() || binding.checkboxCartaoMulticaixa.isChecked() || binding.checkboxDepositoBancario.isChecked() || binding.checkboxTransferenciaBancario.isChecked();
        }
    }

    private List<Long> getProdutoRascunho() {
        List<Long> idprodutoList = new ArrayList<>();
        String idproduto = sharedPreferences.getString("idprodutorascunho", null);
        if (idproduto != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Long>>() {
            }.getType();
            idprodutoList = gson.fromJson(idproduto, type);
        }
        return idprodutoList;
    }

    private void eliminarRascunho() {
        requireActivity().setTitle(getString(R.string.fctrc));
        idprodutoRascunho.clear();
        sharedPreferences.edit().putString("idprodutorascunho", null).apply();
    }

    class ProdutoFacturaAdapter extends PagingDataAdapter<Produto, ProdutoFacturaAdapter.ProdutoFacturaViewHolder> {
        private TextView totaluni;

        public ProdutoFacturaAdapter(@NonNull DiffUtil.ItemCallback<Produto> diffCallback) {
            super(diffCallback);
        }

        @NonNull
        @Override
        public ProdutoFacturaAdapter.ProdutoFacturaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ProdutoFacturaViewHolder(FragmentListaProdutoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(@NonNull ProdutoFacturaViewHolder h, int position) {
            Produto produto = getItem(position);
            if (produto != null) {
                h.binding.txtProduto.setText(produto.getNome());
                h.binding.txtPreco.setText(Ultilitario.formatPreco(String.valueOf(produto.getPreco())) + " - MS" + produto.getId() + " - " + produto.getCodigoBarra());
                h.binding.txtQuantidade.setTextColor(!produto.isStock() ? Color.GRAY : produto.getQuantidade() == 0 ? Color.RED : Color.parseColor("#43A047"));
                h.binding.txtQuantidade.setText(!produto.isStock() ? getString(R.string.sem_cont_stoc) + " - " + getText(R.string.quantidade) + ": " + produto.getQuantidade() : (produto.getQuantidade() == 0 ? getString(R.string.sem_prod_stoc) + " - " : getString(R.string.prod_stoc) + " - ") + getString(R.string.quantidade) + " - " + produto.getQuantidade());
                itemView.put(produto.getId(), h.itemView);
                if (estado.get(produto.getId()) != null && Objects.requireNonNull(estado.get(produto.getId())))
                    h.itemView.setBackgroundColor(Color.parseColor("#FFE6FBD0"));
                else
                    h.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                h.itemView.setOnClickListener(v -> addProduto(v, produto, produto.getId(), produto.getNome()));
                if (addScaner || addRascunho) {
                    addScaner = false;
                    adicionarProduto(produto.getId(), produto, h.itemView, true);
                }
                h.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle(produto.getNome());
                    if (getArguments() != null) {
                        if (getArguments().getBoolean("master")) {
                            menu.add(getString(R.string.editar)).setOnMenuItemClickListener(item -> {
                                bundle.clear();
                                bundle.putParcelable("produto", produto);
                                bundle.putBoolean("master", getArguments().getBoolean("master", false));
                                Navigation.findNavController(requireView()).navigate(R.id.action_facturaFragment_to_dialogCriarProduto, bundle);
                                return false;
                            });
                        }
                    }
                    menu.add(getString(produtos.containsKey(produto.getId()) ? R.string.rvr_car : R.string.adicionar_produto)).setOnMenuItemClickListener(item -> {
                        addProduto(v, produto, produto.getId(), produto.getNome());
                        return false;
                    });
                });
            }
        }

        public class ProdutoFacturaViewHolder extends RecyclerView.ViewHolder {
            FragmentListaProdutoBinding binding;

            public ProdutoFacturaViewHolder(@NonNull FragmentListaProdutoBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        private void addProduto(View v, Produto produto, long idproduto, String nomeproduto) {
            if (produtos.containsKey(idproduto)) {
                removerProduto(idproduto, v, nomeproduto, true);
            } else {
                adicionarProduto(idproduto, produto, v, true);
            }
        }

        private void habilitarDesabilitarButtonEfectuarVenda() {
            binding.btnEfectuarVenda.setEnabled(adapterFactura.getItemCount() > 0);
        }

        private void adicionarProduto(long id, Produto produto, View v, boolean b) {
            if (produto.getQuantidade() == 0 && produto.isStock()) {
                Ultilitario.alertDialog(getString(R.string.sem_prod_stoc), getString(R.string.sem_prod_stoc_msg, produto.getNome()), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            } else {
                estado.put(id, true);
                produtos.put(id, produto);
                adapterFactura.add(new ItemFactura(produto));
                if (b) {
                    desfazer(produto.getNome() + " " + getString(R.string.produto_adicionado), id, v, null);
                }
                habilitarDesabilitarButtonEfectuarVenda();
                v.setBackgroundColor(Color.parseColor("#FFE6FBD0"));
                setProdutoRascunho(id);
            }
        }

        private void desfazer(String message, long id, View view, Produto produto) {
            Snackbar.make(binding.myCoordinatorLayout, message,
                    Snackbar.LENGTH_LONG)
                    .setAction(R.string.desfazer, v -> {
                        if (produto == null) {
                            removerProduto(id, view, "", false);
                        } else {
                            adicionarProduto(id, produto, view, false);
                        }
                    })
                    .show();
        }

        private void removerProduto(Long id, View view, String nome, boolean b) {
            resultCodeBar = "";
            estado.put(id, false);
            Produto produto = produtos.get(id);
            produtos.remove(id);
            adapterFactura.removeGroupAtAdapterPosition(Objects.requireNonNull(posicao.get(id)));
            adapterFactura.notifyItemRangeRemoved(Objects.requireNonNull(posicao.get(id)), produtos.size());
            precoTotal.remove(id);
            somarPreco(precoTotal, id, Objects.requireNonNull(produto).isIva(), Ultilitario.UM, true);
            if (b) {
                desfazer(nome + " " + getString(R.string.produto_removido), id, view, produto);
            }
            view.setBackgroundColor(Color.parseColor("#FFFFFF"));
            habilitarDesabilitarButtonEfectuarVenda();
            removeProdutoRascunho(id);
        }

        @SuppressLint("SetTextI18n")
        private void somarPreco(Map<Long, Integer> pTotal, long id, boolean isIva, int quant, boolean isRemove) {
            int totalGer = 0, precoUnit = 0, valorGer = 0, ivaGer = 0;
            for (Map.Entry<Long, Integer> precototal : pTotal.entrySet()) {
                totalGer += precototal.getValue();
            }
            if (pTotal.get(id) != null)
                precoUnit = (Objects.requireNonNull(pTotal.get(id)) / quant);
            if (isRemove) {
                iva.remove(id);
                valor.remove(id);
            } else {
                if (isIva) {
                    iva.put(id, (int) ((precoUnit / 1.14) * 0.14) * quant);
                    valor.put(id, (int) (precoUnit / 1.14) * quant);
                } else {
                    valor.put(id, precoTotal.get(id));
                }
            }
            for (Map.Entry<Long, Integer> iva : iva.entrySet()) {
                ivaGer += iva.getValue();
            }
            for (Map.Entry<Long, Integer> valor : valor.entrySet()) {
                valorGer += valor.getValue();
            }
            total = totalGer;
            valorBase = valorGer;
            valorIva = ivaGer;
            binding.textTotal.setText(getText(R.string.total) + ": " + Ultilitario.formatPreco(String.valueOf(totalGer)));
            binding.textValor.setText(Ultilitario.formatPreco(String.valueOf(valorGer)));
            binding.textIva.setText(Ultilitario.formatPreco(String.valueOf(ivaGer)));
            binding.txtTot.setText(Ultilitario.formatPreco(String.valueOf(totalGer)));
        }


        private void setProdutoRascunho(Long idproduto) {
            if (!idprodutoRascunho.contains(idproduto) && (PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("activarrascunho", false))) {
                idprodutoRascunho.add(idproduto);
                sharedPreferences.edit().putString("idprodutorascunho", gson.toJson(idprodutoRascunho)).apply();
            }
        }

        private void removeProdutoRascunho(Long idproduto) {
            if (idprodutoRascunho.contains(idproduto) && (PreferenceManager.getDefaultSharedPreferences(requireActivity()).getBoolean("activarrascunho", false))) {
                idprodutoRascunho.remove(idproduto);
                sharedPreferences.edit().putString("idprodutorascunho", gson.toJson(idprodutoRascunho)).apply();
            }
        }

        public final class ItemFactura extends Item<GroupieViewHolder> {
            private int totalUnit, quantidade;
            private final Produto produto;

            public ItemFactura(Produto produto) {
                this.produto = produto;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                posicao.put(produto.getId(), position);
                TextView prod = viewHolder.itemView.findViewById(R.id.textProd);
                TextView ref = viewHolder.itemView.findViewById(R.id.txtRefProd);
                TextView pr = viewHolder.itemView.findViewById(R.id.textPreco);
                Spinner qt = viewHolder.itemView.findViewById(R.id.spinnerQt);
                totaluni = viewHolder.itemView.findViewById(R.id.textTotalUnit);
                Button btnRemover = viewHolder.itemView.findViewById(R.id.btnRemover);
                Ultilitario.addItemOnSpinner(qt, 255, getContext(), 1);
                prod.setText(produto.getNome());
                ref.setText("MS" + produto.getId() + " " + (getText(R.string.montante_iva) + "(" + produto.getPercentagemIva() + "%)"));
                pr.setText(getText(R.string.preco) + " " + Ultilitario.formatPreco(String.valueOf(produto.getPreco())));
                qt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if ((Integer.parseInt(parent.getItemAtPosition(position).toString()) > produto.getQuantidade()) && produto.isStock()) {
                            qt.setSelection(0);
                            Ultilitario.alertDialog(getString(R.string.avs_stock), getString(R.string.quant_prod_dispo, String.valueOf(produto.getQuantidade())), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        } else {
                            quantidade = Integer.parseInt(parent.getItemAtPosition(position).toString());
                            totalUnit = produto.getPreco() * quantidade;
                            totaluni.setText(Ultilitario.formatPreco(String.valueOf(totalUnit)));
                            precoTotal.put(produto.getId(), totalUnit);
                            somarPreco(precoTotal, produto.getId(), produto.isIva(), quantidade, false);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

                btnRemover.setOnClickListener(v -> {
                    if (itemView.containsKey(produto.getId())) {
                        removerProduto(produto.getId(), itemView.get(produto.getId()), produto.getNome(), true);
                    }
                });

                viewHolder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                    menu.setHeaderTitle(produto.getNome());
                    if (getArguments() != null) {
                        if (getArguments().getBoolean("master")) {
                            menu.add(getString(R.string.editar)).setOnMenuItemClickListener(item -> {
                                bundle.clear();
                                bundle.putParcelable("produto", produto);
                                bundle.putBoolean("master", getArguments().getBoolean("master", false));
                                Navigation.findNavController(requireView()).navigate(R.id.action_facturaFragment_to_dialogCriarProduto, bundle);
                                return false;
                            });
                        }
                    }
                    menu.add(getString(R.string.rvr_car)).setOnMenuItemClickListener(item -> {
                        if (itemView.containsKey(produto.getId())) {
                            removerProduto(produto.getId(), itemView.get(produto.getId()), produto.getNome(), true);
                        }
                        return false;
                    });
                });
            }

            @Override
            public int getLayout() {
                return R.layout.fragment_lista_factura;
            }
        }
    }

    static class ProdutoFacturaComparator extends DiffUtil.ItemCallback<Produto> {

        @Override
        public boolean areItemsTheSame(@NonNull Produto oldItem, @NonNull Produto newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Produto oldItem, @NonNull Produto newItem) {
            return oldItem.getId() == newItem.getId();
        }
    }

    private String mensagem;
    private byte estadoConta, termina;

    private void estadoConta(String imei, String nomeIDcliente, int quantidadeProduto) {
        String URL = Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/contacts/" + imei + "/estado";
        Ion.with(requireActivity())
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        for (int i = 0; i < jsonElements.size(); i++) {
                            JsonObject parceiro = jsonElements.get(i).getAsJsonObject();
                            estadoConta = Byte.parseByte(parceiro.get("estado").getAsString());
                            termina = parceiro.get("termina").getAsByte();
                            String contactos = parceiro.get("contactos").getAsString();
                            mensagem = (estadoConta == Ultilitario.ZERO || termina == Ultilitario.UM ? getString(R.string.prazterm) : "") + "\n\nYOGA:" + contactos;
                        }
                        if (estadoConta == Ultilitario.ZERO || termina == Ultilitario.UM) {
                            MainActivity.dismissProgressBar();
                            Ultilitario.alertDialog(estadoConta == Ultilitario.ZERO || termina == Ultilitario.UM ? getString(R.string.cont_des) : getString(R.string.act), mensagem, requireContext(), R.drawable.ic_baseline_person_add_disabled_24);
                        } else if (estadoConta == Ultilitario.UM && termina == Ultilitario.ZERO) {
                            Ultilitario.setValueSharedPreferences(requireContext(), "data", monthInglesFrances(Ultilitario.getDateCurrent()));
                            vendaViewModel.cadastrarVenda(nomeIDcliente, binding.textDesconto, quantidadeProduto, valorBase, referenciaFactura, valorIva, getFormaPamento(binding), totaldesconto, total, produtos, precoTotal, valorDivida, valorPago, requireArguments().getLong("idoperador", 0), idcliente, dataEmissao, getView());
                        }
                    } catch (Exception ex) {
                        MainActivity.dismissProgressBar();
                        new android.app.AlertDialog.Builder(requireContext())
                                .setTitle(getString(R.string.erro))
                                .setMessage(ex.getMessage())
                                .setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss())
                                .setPositiveButton(R.string.tent_nov, (dialog, which) -> {
                                    dialog.dismiss();
                                    MainActivity.getProgressBar();
                                    estadoConta(imei, nomeIDcliente, quantidadeProduto);
                                })
                                .show();
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

    public static void myOnKeyDown(int keyCode, KeyEvent event) {
        if (barcodeView != null) {
            barcodeView.onKeyDown(keyCode, event);
        }
    }

    private final ActivityResultLauncher<String> mPermissionResult = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            result -> {
                if (result) {
                    openCamera();
                } else {
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.noa_scan_codbar), R.drawable.ic_toast_erro);
                }
            });
}