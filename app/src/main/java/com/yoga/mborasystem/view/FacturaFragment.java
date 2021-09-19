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
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;
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
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

public class FacturaFragment extends Fragment {

    private Cliente cliente;
    private long idc, idcliente;
    private BeepManager beepManager;
    private Map<Long, View> itemView;
    private Map<Long, Boolean> estado;
    private Map<Long, Produto> produtos;
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private VendaViewModel vendaViewModel;
    private FragmentFacturaBinding binding;
    private ArrayList<String> listaCategoria;
    private ProdutoViewModel produtoViewModel;
    private GroupAdapter adapter, adapterFactura;
    private String lastText, codigoQr, facturaPath;
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
            if (result.getText() == null || result.getText().equals(lastText)) {
                Toast.makeText(getContext(), getString(R.string.ja_scaneado), Toast.LENGTH_SHORT).show();
                return;
            }
            lastText = result.getText();
            barcodeView.setStatusText(result.getText());
            beepManager.playBeepSoundAndVibrate();
            produtoViewModel.searchProduto(lastText);
            //Added preview of scanned barcode
//            ImageView imageView = findViewById(R.id.barcodePreview);
//            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        iva = new HashMap<>();
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

        binding.buttonFechar.setOnClickListener(v -> {
            binding.viewStub.setVisibility(View.GONE);
            binding.buttonFechar.setVisibility(View.GONE);
            barcodeView.pause();
        });

        binding.btnCriarCliente.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(getView()).navigate(FacturaFragmentDirections.actionFacturaFragmentToDialogCriarClienteCantina(binding.txtNomeCliente.getText().toString(), "", 0));
            binding.txtNomeCliente.setText("");
        });

        binding.btnCleaNameClient.setOnClickListener(v -> {
            binding.txtNomeCliente.setEnabled(true);
            binding.txtNomeCliente.setText("");
            binding.txtNomeCliente.requestFocus();
        });

        binding.txtNomeCliente.setOnItemClickListener((parent, view, position, id) -> {
            binding.txtNomeCliente.setEnabled(false);
        });
        binding.btnScannerBack.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            }
            binding.buttonFechar.setVisibility(View.VISIBLE);
        });

        binding.btnClose.setOnClickListener(v -> {
            binding.viewStub.setVisibility(View.GONE);
            barcodeView.pause();
        });
        binding.btnScannerFront.setOnClickListener(v -> {
            openCamera();
        });
        binding.recyclerViewFacturaProduto.setAdapter(adapter);
        binding.recyclerViewFacturaProduto.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFactura.setAdapter(adapterFactura);
        binding.recyclerViewFactura.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriaProdutoViewModel.getListaCategorias().observe(getViewLifecycleOwner(), categorias -> {
            if (!categorias.isEmpty()) {
                for (Categoria categoria : categorias) {
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
                produtoViewModel.consultarProdutos(idc, false, null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        produtoViewModel.getListaProdutos().observe(getViewLifecycleOwner(), produtos -> {
            adapter.clear();
            if (produtos.isEmpty()) {
                Toast.makeText(getContext(), getString(R.string.produto_nao_encontrada), Toast.LENGTH_LONG).show();
            } else {
                for (Produto produto : produtos) {
                    adapter.add(new ItemProduto(produto));
                }
            }
        });
        binding.textTaxa.setText("14%");
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
                    Toast.makeText(getContext(), getString(R.string.no_pos_apl_div), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getContext(), getString(R.string.check_dvd), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getContext(), getString(R.string.check_dvd), Toast.LENGTH_LONG).show();
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
        vendaViewModel.getAdminMasterLiveData().observe(getViewLifecycleOwner(), cliente -> {
            this.cliente = cliente;
        });

        vendaViewModel.getGuardarPdfLiveData().setValue(false);
        vendaViewModel.getGuardarPdfLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                if (!codigoQr.isEmpty()) {
                    facturaPath = "venda" + codigoQr + "_" + Ultilitario.getDateCurrent() + ".pdf";
                    CriarFactura.getPemissionAcessStoregeExternal(getActivity(), getContext(), facturaPath, cliente, getArguments().getLong("idoperador", 0), binding.txtNomeCliente, binding.textDesconto, adapterFactura.getItemCount(), valorBase, codigoQr, valorIva, getFormaPamento(binding), totaldesconto, valorPago, troco, total, produtos, precoTotal);
                } else {
                    Toast.makeText(getContext(), getString(R.string.venda_vazia), Toast.LENGTH_SHORT).show();
                }
            }
        });

        vendaViewModel.getPrintLiveData().setValue(false);
        vendaViewModel.getPrintLiveData().observe(getViewLifecycleOwner(), aBoolean -> {
            if (aBoolean) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (facturaPath.isEmpty()) {
                        Toast.makeText(getContext(), getString(R.string.guardar_primeiro), Toast.LENGTH_LONG).show();
                    } else {
                        MainActivity.getProgressBar();
                        CriarFactura.printPDF(getActivity(), getContext(), facturaPath);
                    }
                } else {
                    Toast.makeText(getContext(), getString(R.string.precisa_kitkat_maior), Toast.LENGTH_LONG).show();
                }
            }
        });

        vendaViewModel.getEnviarWhatsAppLiveData().setValue("");
        vendaViewModel.getEnviarWhatsAppLiveData().observe(getViewLifecycleOwner(), numero -> {
            if (!numero.isEmpty()) {
                if (facturaPath.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.enviar_w_primeiro), Toast.LENGTH_LONG).show();
                } else {
                    Ultilitario.openWhatsApp(getActivity(), numero);
                }
            }
        });

        vendaViewModel.getAlertDialogLiveData().setValue(null);
        vendaViewModel.getAlertDialogLiveData().observe(getViewLifecycleOwner(), alertDialog -> {
            if (alertDialog != null) {
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
                Ultilitario.zerarPreco(binding.textDesconto);
                Ultilitario.zerarPreco(binding.textValorPago);
                produtoViewModel.consultarProdutos(idc, false, null);
                alertDialog.dismiss();
            }
        });

        return binding.getRoot();
    }

    private void dialogVerificarVenda(String[] nomeIDcliente) {
        if (nomeIDcliente.length == 2) {
            idcliente = Long.parseLong(nomeIDcliente[1].trim());
        } else {
            idcliente = 0;
        }
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.confirmar_venda)
                .setMessage(getString(R.string.cliente) + ": " + nomeIDcliente[0] + "\n" +
                        getString(R.string.quantidade) + ": " + adapterFactura.getItemCount() + "\n"
                        + getString(R.string.total) + ": " + Ultilitario.formatPreco(String.valueOf(total)) + "\n"
                        + getString(R.string.desconto) + ": " + Ultilitario.formatPreco(binding.textDesconto.getText().toString()) + "\n"
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
                    vendaViewModel.cadastrarVenda(nomeIDcliente[0].trim(), binding.textDesconto, adapterFactura.getItemCount(), valorBase, codigoQr, valorIva, getFormaPamento(binding), totaldesconto, total, produtos, precoTotal, valorDivida, valorPago, getArguments().getLong("idoperador", 0), idcliente, getView());
                })
                .setNegativeButton(R.string.cancelar, (dialog, which) -> {
                    facturaPath = "";
                    dialog.dismiss();
                })
                .show();
    }

    private void getListClientesCantina() {
        clienteCantinaViewModel.consultarClientesCantina(null);
        clienteCantinaViewModel.getListaClientesCantina().observe(getViewLifecycleOwner(), clientesCantina -> {
            clienteCantina.clear();
            for (ClienteCantina cliente : clientesCantina) {
                clienteCantina.add(new ClienteCantina(cliente.getId(), cliente.getNome(), cliente.getTelefone()));
            }
            AutoCompleteClienteCantinaAdapter clienteCantinaAdapter = new AutoCompleteClienteCantinaAdapter(getContext(), clienteCantina);
            binding.txtNomeCliente.setAdapter(clienteCantinaAdapter);
        });
    }

    private void openCamera() {
        binding.viewStub.setVisibility(View.VISIBLE);
        binding.buttonFechar.setVisibility(View.VISIBLE);
        barcodeView.resume();
    }

    private String getFormaPamento(FragmentFacturaBinding binding) {
        CharSequence dinheiro = binding.checkboxDinheiro.isChecked() ? binding.checkboxDinheiro.getText() : "";
        CharSequence cartaoMulticaixa = binding.checkboxCartaoMulticaixa.isChecked() ? binding.checkboxCartaoMulticaixa.getText() : "";
        CharSequence depositoBancario = binding.checkboxDepositoBancario.isChecked() ? binding.checkboxDepositoBancario.getText() : "";
        CharSequence transferenciaBancario = binding.checkboxTransferenciaBancario.isChecked() ? binding.checkboxTransferenciaBancario.getText() : "";
        return dinheiro + " " + cartaoMulticaixa + " " + depositoBancario + " " + transferenciaBancario;
    }

    private boolean isCheckedFormaPagamento() {
        return binding.checkboxDinheiro.isChecked() || binding.checkboxCartaoMulticaixa.isChecked() || binding.checkboxDepositoBancario.isChecked() || binding.checkboxTransferenciaBancario.isChecked();
    }

    private String getCodigoDeBarra() {
        return String.valueOf(new Random().nextInt((100000 - 1) + 1) + 1);
    }

    public class ItemProduto extends Item<GroupieViewHolder> {
        private final Produto produto;
        private Button btnRemover;
        private TextView nome, precoref, prod, ref, pr, totaluni;

        public ItemProduto(Produto produto) {
            this.produto = produto;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
            nome = viewHolder.itemView.findViewById(R.id.txtProduto);
            precoref = viewHolder.itemView.findViewById(R.id.txtPreco);
            nome.setText(produto.getNome());
            precoref.setText(Ultilitario.formatPreco(String.valueOf(produto.getPreco())) + " - MS" + produto.getId());
            itemView.put(produto.getId(), viewHolder.itemView);
            if (estado.get(produto.getId()) != null && estado.get(produto.getId())) {
                viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFE6FBD0"));
            } else {
                viewHolder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            }
            viewHolder.itemView.setOnClickListener(v -> {
                if (produtos.containsKey(produto.getId())) {
                    removerProduto(produto.getId(), v, produto.getNome(), true);
                } else {
                    adicionarProduto(produto.getId(), produto, v, true);
                    habilitarDesabilitarButtonEfectuarVenda();
                }
            });
        }

        @Override
        public int getLayout() {
            return R.layout.fragment_lista_produto;
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
            estado.put(id, false);
            Produto produto = produtos.get(id);
            produtos.remove(id);
            adapterFactura.removeGroupAtAdapterPosition(posicao.get(id));
            adapterFactura.notifyItemRangeRemoved(posicao.get(id), produtos.size());
            precoTotal.remove(id);
            somarPreco(precoTotal, id, produto.isIva(), Ultilitario.UM, true);
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
            if (pTotal.get(id) != null) precoUnit = (pTotal.get(id) / quant);
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
            private Spinner qt;
            private int totalUnit, quantidade;
            private final Produto produto;

            public ItemFactura(Produto produto) {
                this.produto = produto;
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void bind(@NonNull GroupieViewHolder viewHolder, int position) {
                posicao.put(produto.getId(), position);
                prod = viewHolder.itemView.findViewById(R.id.textProd);
                ref = viewHolder.itemView.findViewById(R.id.txtRefProd);
                pr = viewHolder.itemView.findViewById(R.id.textPreco);
                qt = viewHolder.itemView.findViewById(R.id.spinnerQt);
                totaluni = viewHolder.itemView.findViewById(R.id.textTotalUnit);
                btnRemover = viewHolder.itemView.findViewById(R.id.btnRemover);
                Ultilitario.addItemOnSpinner(qt, getContext());
                prod.setText(produto.getNome());
                ref.setText("MS" + produto.getId() + " " + (produto.isIva() ? "IVA(14%)" : ""));
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
            }

            @Override
            public int getLayout() {
                return R.layout.fragment_lista_factura;
            }
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_factura, menu);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.prod) + " " + getString(R.string.ou) + " " + getString(R.string.codigo_bar));
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.onActionViewExpanded();
        MenuItemCompat.setOnActionExpandListener(menuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                produtoViewModel.consultarProdutos(idc, false, null);
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
                    produtoViewModel.consultarProdutos(idc, false, null);
                } else {
                    produtoViewModel.searchProduto(newText);
                }
                return false;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS:
                if ((grantResults != null && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    openCamera();
                } else {
                    Toast.makeText(getContext(), getText(R.string.noa_scan_codbar), Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}