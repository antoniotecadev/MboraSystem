package com.yoga.mborasystem.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
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

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;
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
import com.yoga.mborasystem.model.entidade.Categoria;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.ClienteCantina;
import com.yoga.mborasystem.model.entidade.Produto;
import com.yoga.mborasystem.util.AutoCompleteClienteCantinaAdapter;
import com.yoga.mborasystem.util.CriarFactura;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.CategoriaProdutoViewModel;
import com.yoga.mborasystem.viewmodel.ClienteCantinaViewModel;
import com.yoga.mborasystem.viewmodel.ProdutoViewModel;
import com.yoga.mborasystem.viewmodel.VendaViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;

public class FacturaFragment extends Fragment {

    private Bundle bundle;
    private boolean addScaner;
    private Cliente cliente;
    private long idc, idcliente;
    private BeepManager beepManager;
    private Map<Long, View> itemView;
    private Map<Long, Boolean> estado;
    private Map<Long, Produto> produtos;
    private VendaViewModel vendaViewModel;
    private FragmentFacturaBinding binding;
    private ArrayList<String> listaCategoria;
    private ProdutoViewModel produtoViewModel;
    private GroupAdapter adapter, adapterFactura;
    private String resultCodeBar, codigoQr, facturaPath;
    private ArrayList<ClienteCantina> clienteCantina;
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
                barcodeView.setStatusText(result.getText() + "\n" + getString(R.string.ja_scaneado));
                beepManager.playBeepSoundAndVibrate();
                produtoViewModel.searchProduto(resultCodeBar, false);
                Ultilitario.showToastOrAlertDialogQrCode(requireContext(), result.getBitmapWithResultPoints(Color.YELLOW), false, null, "", "");
            }
            //Added preview of scanned barcode
//            ImageView imageView = findViewById(R.id.barcodePreview);
//            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        iva = new HashMap<>();
        bundle = new Bundle();
        valor = new HashMap<>();
        cliente = new Cliente();
        estado = new HashMap<>();
        posicao = new HashMap<>();
        produtos = new HashMap<>();
        itemView = new HashMap<>();
        adapter = new GroupAdapter();
        precoTotal = new HashMap<>();
        listaCategoria = new ArrayList<>();
        adapterFactura = new GroupAdapter();
        clienteCantina = new ArrayList<>();
        vendaViewModel = new ViewModelProvider(requireActivity()).get(VendaViewModel.class);
        produtoViewModel = new ViewModelProvider(requireActivity()).get(ProdutoViewModel.class);
        clienteCantinaViewModel = new ViewModelProvider(requireActivity()).get(ClienteCantinaViewModel.class);
        categoriaProdutoViewModel = new ViewModelProvider(requireActivity()).get(CategoriaProdutoViewModel.class);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFacturaBinding.inflate(inflater, container, false);
        getListClientesCantina();
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
            MainActivity.getProgressBar();
            Navigation.findNavController(requireView()).navigate(FacturaFragmentDirections.actionFacturaFragmentToDialogCriarClienteCantina(binding.txtNomeCliente.getText().toString(), "", 0));
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
        binding.recyclerViewFacturaProduto.setAdapter(adapter);
        binding.recyclerViewFacturaProduto.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFactura.setAdapter(adapterFactura);
        binding.recyclerViewFactura.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriaProdutoViewModel.consultarCategorias(null, false);
        categoriaProdutoViewModel.getListaCategorias().observe(getViewLifecycleOwner(), categorias -> {
            if (!categorias.isEmpty() && listaCategoria.isEmpty()) {
                for (Categoria categoria : categorias) {
                    if (categoria.getEstado() != Ultilitario.DOIS)
                        listaCategoria.add(categoria.getId() + " - " + categoria.getCategoria());
                }
                listCategoriaAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, listaCategoria);
                binding.spinnerCategorias.setAdapter(listCategoriaAdapter);
            }
        });
        binding.spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] idcategoria = TextUtils.split(parent.getItemAtPosition(position).toString(), "-");
                idc = Long.parseLong(idcategoria[0].trim());
                produtoViewModel.consultarProdutos(idc, false, null, false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        produtoViewModel.getListaProdutos().observe(getViewLifecycleOwner(), produtos -> {
            adapter.clear();
            if (produtos.isEmpty()) {
                Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.produto_nao_encontrada), R.drawable.ic_toast_erro);
            } else {
                for (Produto produto : produtos) {
                    if (produto.getEstado() != Ultilitario.DOIS)
                        adapter.add(new ItemProduto(produto));
                }
            }
        });
        binding.textTaxa.setText(Ultilitario.getPercentagemIva(requireActivity()) + "%");
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
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.no_pos_apl_div), R.drawable.ic_toast_erro);
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

        checkValorFormaPagamento(binding.checkboxDinheiro, binding.dinheiroValorPago);
        checkValorFormaPagamento(binding.checkboxCartaoMulticaixa, binding.cartaoValorPago);
        checkValorFormaPagamento(binding.checkboxDepositoBancario, binding.depValorPago);
        checkValorFormaPagamento(binding.checkboxTransferenciaBancario, binding.transfValorPago);

        binding.btnEfectuarVenda.setOnClickListener(v -> {
            facturaPath = "";
            if (isCheckedFormaPagamento()) {
                if (valorPago > 0 || binding.checkboxSemValorPago.isChecked()) {
                    String[] nomeIDcliente;
                    if (binding.txtNomeCliente.getText().toString().trim().isEmpty()) {
                        nomeIDcliente = TextUtils.split("*****", "-");
                    } else {
                        nomeIDcliente = TextUtils.split(binding.txtNomeCliente.getText().toString(), "-");
                    }
                    codigoQr = System.currentTimeMillis() / 1000 + "" + getCodigoDeBarra();
                    if (binding.checkboxDivida.isChecked()) {
                        if (valorDivida > 0) {
                            if (nomeIDcliente.length == 2 && Long.parseLong(nomeIDcliente[1].trim()) > 0) {
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
        vendaViewModel.getAdminMasterLiveData().observe(getViewLifecycleOwner(), cliente -> this.cliente = cliente);

        vendaViewModel.getGuardarPdfLiveData().setValue(false);
        vendaViewModel.getGuardarPdfLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                if (!codigoQr.isEmpty()) {
                    facturaPath = "venda" + codigoQr + "_" + Ultilitario.getDateCurrent() + ".pdf";
                    CriarFactura.getPemissionAcessStoregeExternal(true, getActivity(), getContext(), facturaPath, cliente, requireArguments().getLong("idoperador", 0), binding.txtNomeCliente, binding.textDesconto, valorBase, codigoQr, valorIva, getFormaPamento(binding), totaldesconto, valorPago, troco, total, produtos, precoTotal);
                } else {
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.venda_vazia), R.drawable.ic_toast_erro);
                }
            }
        });

        vendaViewModel.getPrintLiveData().setValue(false);
        vendaViewModel.getPrintLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                if (!codigoQr.isEmpty()) {
                    facturaPath = "venda" + codigoQr + "_" + Ultilitario.getDateCurrent() + ".pdf";
                    CriarFactura.getPemissionAcessStoregeExternal(false, getActivity(), getContext(), facturaPath, cliente, requireArguments().getLong("idoperador", 0), binding.txtNomeCliente, binding.textDesconto, valorBase, codigoQr, valorIva, getFormaPamento(binding), totaldesconto, valorPago, troco, total, produtos, precoTotal);
                } else {
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.venda_vazia), R.drawable.ic_toast_erro);
                }
            }
        });

        vendaViewModel.getEnviarWhatsAppLiveData().setValue("");
        vendaViewModel.getEnviarWhatsAppLiveData().observe(getViewLifecycleOwner(), numero -> {
            if (!numero.isEmpty()) {
                if (facturaPath.isEmpty()) {
                    Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.enviar_w_primeiro), R.drawable.ic_toast_erro);
                } else {
                    Ultilitario.openWhatsApp(getActivity(), numero);
                }
            }
        });
        vendaViewModel.getAlertDialogLiveData().setValue(null);
        vendaViewModel.getAlertDialogLiveData().observe(getViewLifecycleOwner(), alertDialog -> {
            if (alertDialog != null && !Ultilitario.getNaoMostrarNovamente(requireActivity())) {
                if (facturaPath.isEmpty()) {
                    new AlertDialog.Builder(requireContext())
                            .setIcon(R.drawable.ic_baseline_store_24)
                            .setTitle(R.string.fechar)
                            .setMessage(R.string.tem_cert_fech)
                            .setNeutralButton(getString(R.string.n_most_nov), (dialogInterface, i) -> Ultilitario.setNaoMostrarNovamente(requireActivity(), true))
                            .setNegativeButton(R.string.nao, (dialogInterface, i) -> dialogInterface.dismiss())
                            .setPositiveButton(R.string.sim, (dialogInterface, i) -> fecharAlertDialog(alertDialog)).show();
                } else {
                    fecharAlertDialog(alertDialog);
                }
            } else {
                if (alertDialog != null)
                    fecharAlertDialog(alertDialog);

            }
        });
        return binding.getRoot();
    }

    private void fecharAlertDialog(AlertDialog alertDialog) {
        restaurar();
        alertDialog.dismiss();
    }

    private void restaurar() {
        binding.txtNomeCliente.setText("");
        estado.clear();
        produtos.clear();
        adapterFactura.clear();
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
        binding.checkboxDinheiro.setChecked(false);
        binding.checkboxCartaoMulticaixa.setChecked(false);
        binding.checkboxDepositoBancario.setChecked(false);
        binding.checkboxTransferenciaBancario.setChecked(false);
        Ultilitario.zerarPreco(binding.textDesconto);
        Ultilitario.zerarPreco(binding.textValorPago);
        Ultilitario.zerarPreco(binding.dinheiroValorPago);
        Ultilitario.zerarPreco(binding.cartaoValorPago);
        Ultilitario.zerarPreco(binding.depValorPago);
        Ultilitario.zerarPreco(binding.transfValorPago);
        produtoViewModel.consultarProdutos(idc, false, null, false);
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
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.confirmar_venda)
                    .setMessage(getString(R.string.cliente) + ": " + nomeIDcliente[0] + "\n" +
                            getString(R.string.quantidade) + ": " + adapterFactura.getItemCount() + "\n"
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
                        if (getDataSplitDispositivo(Ultilitario.getSharedPreferencesDataDispositivo(requireActivity())).equals(getDataSplitDispositivo(Ultilitario.monthInglesFrances(Ultilitario.getDateCurrent())))) {
                            vendaViewModel.cadastrarVenda(nomeIDcliente[0].trim(), binding.textDesconto, adapterFactura.getItemCount(), valorBase, codigoQr, valorIva, getFormaPamento(binding), totaldesconto, total, produtos, precoTotal, valorDivida, valorPago, requireArguments().getLong("idoperador", 0), idcliente, getView());
                        } else {
                            if (isNetworkConnected(requireContext())) {
                                if (internetIsConnected()) {
                                    estadoConta(cliente.getImei(), nomeIDcliente[0].trim());
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
            Ultilitario.showToast(getContext(), Color.parseColor("#795548"), getString(R.string.smt_siff), R.drawable.ic_toast_erro);
        }
    }

    private String getDataSplitDispositivo(String dataSplit) {
        String[] dataDavice = TextUtils.split(dataSplit, "-");
        return dataDavice[0].trim() + '-' + dataDavice[1].trim() + '-' + dataDavice[2].trim();
    }

    private void getListClientesCantina() {
        clienteCantinaViewModel.consultarClientesCantina(null);
        clienteCantinaViewModel.getListaClientesCantina().observe(getViewLifecycleOwner(), clientesCantina -> {
            clienteCantina.clear();
            for (ClienteCantina cliente : clientesCantina) {
                clienteCantina.add(new ClienteCantina(cliente.getId(), cliente.getNome(), cliente.getTelefone()));
            }
            AutoCompleteClienteCantinaAdapter clienteCantinaAdapter = new AutoCompleteClienteCantinaAdapter(requireContext(), clienteCantina);
            binding.txtNomeCliente.setAdapter(clienteCantinaAdapter);
        });
    }

    private void openCamera() {
        binding.viewStub.setVisibility(View.VISIBLE);
        visibityButton();
        barcodeView.resume();
    }

    private String getFormaPamento(FragmentFacturaBinding binding) {
        CharSequence dinheiro = binding.checkboxDinheiro.isChecked() ? (binding.checkboxDinheiro.getText() + " = " + Ultilitario.trocarVírgulaPorPonto(binding.dinheiroValorPago)) : "";
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
            return binding.checkboxDinheiro.isChecked() || binding.checkboxCartaoMulticaixa.isChecked() || binding.checkboxDepositoBancario.isChecked() || binding.checkboxTransferenciaBancario.isChecked();
        }
    }

    private String getCodigoDeBarra() {
        return String.valueOf(new Random().nextInt((100000 - 1) + 1) + 1);
    }

    public class ItemProduto extends Item<GroupieViewHolder> {
        private final Produto produto;
        private TextView totaluni;

        public ItemProduto(Produto produto) {
            this.produto = produto;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            TextView nome = viewHolder.itemView.findViewById(R.id.txtProduto);
            TextView precorefcod = viewHolder.itemView.findViewById(R.id.txtPreco);
            nome.setText(produto.getNome());
            precorefcod.setText(Ultilitario.formatPreco(String.valueOf(produto.getPreco())) + " - MS" + produto.getId() + " - " + produto.getCodigoBarra());
            itemView.put(produto.getId(), viewHolder.itemView);
            if (estado.get(produto.getId()) != null && Objects.requireNonNull(estado.get(produto.getId()))) {
                viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFE6FBD0"));
            } else {
                viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            viewHolder.itemView.setOnClickListener(this::addProduto);
            if (addScaner) {
                addScaner = false;
                adicionarProduto(produto.getId(), produto, viewHolder.itemView, true);
                habilitarDesabilitarButtonEfectuarVenda();
            }

            viewHolder.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                menu.setHeaderTitle(produto.getNome());
                menu.add(getString(R.string.adicionar_produto)).setOnMenuItemClickListener(item -> {
                    addProduto(v);
                    return false;
                });
            });
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_lista_produto;
        }

        private void addProduto(View v) {
            if (produtos.containsKey(produto.getId())) {
                removerProduto(produto.getId(), v, produto.getNome(), true);
            } else {
                adicionarProduto(produto.getId(), produto, v, true);
                habilitarDesabilitarButtonEfectuarVenda();
            }
        }

        private void habilitarDesabilitarButtonEfectuarVenda() {
            binding.btnEfectuarVenda.setEnabled(adapterFactura.getItemCount() > 0);
        }

        private void adicionarProduto(long id, Produto produto, View v, boolean b) {
            estado.put(id, true);
            produtos.put(id, produto);
            adapterFactura.add(new ItemFactura(produto));
            if (b) {
                desfazer(produto.getNome() + " " + getString(R.string.produto_adicionado), id, v, null);
            }
            habilitarDesabilitarButtonEfectuarVenda();
            v.setBackgroundColor(Color.parseColor("#FFE6FBD0"));
        }

        private void desfazer(String message, long id, View view, Produto produto) {
            Snackbar.make(binding.myCoordinatorLayout, message,
                    Snackbar.LENGTH_SHORT)
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

        private class ItemFactura extends Item<GroupieViewHolder> {
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
                Ultilitario.addItemOnSpinner(qt, 255, getContext());
                prod.setText(produto.getNome());
                ref.setText("MS" + produto.getId() + " " + (produto.isIva() ? getText(R.string.montante_iva) + "(" + produto.getPercentagemIva() + "%)" : ""));
                pr.setText(getText(R.string.preco) + " " + Ultilitario.formatPreco(String.valueOf(produto.getPreco())));
                qt.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        quantidade = Integer.parseInt(parent.getItemAtPosition(position).toString());
                        totalUnit = produto.getPreco() * quantidade;
                        totaluni.setText(Ultilitario.formatPreco(String.valueOf(totalUnit)));
                        precoTotal.put(produto.getId(), totalUnit);
                        somarPreco(precoTotal, produto.getId(), produto.isIva(), quantidade, false);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
        return NavigationUI.onNavDestinationSelected(item, navController)
                || super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_factura, menu);
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
                produtoViewModel.consultarProdutos(idc, false, null, false);
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
                    produtoViewModel.consultarProdutos(idc, false, null, false);
                } else {
                    produtoViewModel.searchProduto(newText, false);
                }
                return false;
            }
        });
    }

    private String mensagem;
    private byte estadoConta, termina;

    private void estadoConta(String imei, String nomeIDcliente) {
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
                            mensagem = (estadoConta == Ultilitario.ZERO || termina == Ultilitario.UM ? getString(R.string.prazterm) : "") + "\n";
                        }
                        if (estadoConta == Ultilitario.ZERO || termina == Ultilitario.UM) {
                            MainActivity.dismissProgressBar();
                            Ultilitario.alertDialog(estadoConta == Ultilitario.ZERO || termina == Ultilitario.UM ? getString(R.string.cont_des) : getString(R.string.act), mensagem, requireContext(), R.drawable.ic_baseline_person_add_disabled_24);
                        } else if (estadoConta == Ultilitario.UM && termina == Ultilitario.ZERO) {
                            Ultilitario.setSharedPreferencesDataDispositivo(requireActivity());
                            vendaViewModel.cadastrarVenda(nomeIDcliente, binding.textDesconto, adapterFactura.getItemCount(), valorBase, codigoQr, valorIva, getFormaPamento(binding), totaldesconto, total, produtos, precoTotal, valorDivida, valorPago, requireArguments().getLong("idoperador", 0), idcliente, getView());
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
                                    estadoConta(imei, nomeIDcliente);
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