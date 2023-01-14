package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.acercaMboraSystem;
import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.bytesToHex;
import static com.yoga.mborasystem.util.Ultilitario.getDetailDevice;
import static com.yoga.mborasystem.util.Ultilitario.getDetailDeviceString;
import static com.yoga.mborasystem.util.Ultilitario.getDeviceUniqueID;
import static com.yoga.mborasystem.util.Ultilitario.getHash;
import static com.yoga.mborasystem.util.Ultilitario.getIdIdioma;
import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;
import static com.yoga.mborasystem.util.Ultilitario.getValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;
import static com.yoga.mborasystem.util.Ultilitario.reverse;
import static com.yoga.mborasystem.util.Ultilitario.setValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.showToast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.koushikdutta.ion.Ion;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentHomeBinding;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.model.entidade.ContaBancaria;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private Bundle bundle;
    private Cliente cliente;
    private ExecutorService executor;
    private FragmentHomeBinding binding;
    private boolean isOpen = false, isMaster;
    private ClienteViewModel clienteViewModel;
    private String idioma, codigoIdioma, nomeOperador, languageCode = "";
    private Animation FabOpen, FabClose, FabRClockwise, FabRanticlockwise;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        FabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        FabRClockwise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        FabRanticlockwise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anticlockwise);
        nomeOperador = getArguments().getString("nome");
        cliente = getArguments().getParcelable("cliente");
        isMaster = Ultilitario.getBooleanPreference(requireContext(), "master");
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        PreferenceManager.getDefaultSharedPreferences(requireActivity()).edit().putString("nomeoperador", nomeOperador).apply();
        Toolbar toolbar = binding.toolbar;
        toolbar.inflateMenu(R.menu.menu_bloquear);
        toolbar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.bloquearFragment:
                    Navigation.findNavController(requireView()).navigate(R.id.action_global_bloquearFragment);
                    break;
                case R.id.gerarQrCode:
                    getQrCode();
                    break;
                case R.id.sairApp:
                    sairApp();
                    break;
                default:
                    break;
            }
            return false;
        });

        binding.floatingActionButton.setOnClickListener(v -> alertDialog(getString(R.string.nome_sistema), getString(R.string.acerca), requireContext(), R.drawable.ic_baseline_store_24));

        binding.floatingActionButtonLixo.setOnClickListener(v -> {
            if (isOpen)
                animationLixeira(FabClose, FabRanticlockwise, false);
            else
                animationLixeira(FabOpen, FabRClockwise, true);
        });

        binding.floatingActionButtonCategoria.setOnClickListener(v -> entrarCategoriasLx());

        binding.floatingActionButtonProduto.setOnClickListener(v -> entrarProdutosLx());

        binding.floatingActionButtonVendaLixo.setOnClickListener(v -> entrarVendas(true));

        MainActivity.navigationView.setNavigationItemSelectedListener(item -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
            switch (item.getItemId()) {
                case R.id.categoriaProdutoFragmentH:
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_categoriaProdutoFragment, isUserMaster());
                    break;
                case R.id.usuarioFragmentH:
                    if (isMaster)
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_usuarioFragment, isUserMaster());
                    break;
                case R.id.vendaFragmentH:
                    entrarVendas(false);
                    break;
                case R.id.listaClienteFragmentH:
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_listaClienteFragment, isUserMaster());
                    break;
                case R.id.dashboardFragmentH:
                    if (isMaster) {
                        MainActivity.getProgressBar();
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dashboardFragment);
                    }
                    break;
                case R.id.facturaFragmentH:
                    entrarFacturacao();
                    break;
                case R.id.vendaFragmentNotaCredito:
                    entrarVendas(true);
                    break;
                case R.id.categoriaProdutoFragmentLx:
                    entrarCategoriasLx();
                    break;
                case R.id.produtoFragmentLx:
                    entrarProdutosLx();
                    break;
                case R.id.documentoFragmentMenu:
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    break;
                default:
                    break;
            }
            MainActivity.drawerLayout.closeDrawer(GravityCompat.START);
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        binding.floatingActionButtonVenda.setOnClickListener(v -> entrarFacturacao());

        binding.btnUsuario.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_usuarioFragment));

        binding.btnProduto.setOnClickListener(v -> {
            HomeFragmentDirections.ActionHomeFragmentToCategoriaProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToCategoriaProdutoFragment().setIsMaster(isMaster).setIdUsuario(requireArguments().getInt("idusuario", 0));
            Navigation.findNavController(requireView()).navigate(direction);
        });

        binding.btnVenda.setOnClickListener(v -> entrarVendas(false));

        binding.btnCliente.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_listaClienteFragment));
        binding.btnDashboard.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dashboardFragment);
        });

        clienteViewModel.getValido().observe(getViewLifecycleOwner(), operacao -> {
            if (operacao == Ultilitario.Operacao.ACTUALIZAR) {
                Navigation.findNavController(requireView()).navigate(R.id.action_global_bloquearFragment);
                alertDialog(getString(R.string.dad_actu), getString(R.string.msg_tel_blo_act_emp), requireContext(), R.drawable.ic_baseline_store_24);
                clienteViewModel.getValido().setValue(Ultilitario.Operacao.NENHUMA);
            }
        });

        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_home_ferramenta, menu);
                menu.findItem(R.id.dialogAlterarCliente).setTitle(getString(R.string.dad_emp));
                if (!isMaster) {
                    binding.btnUsuario.setEnabled(false);
                    binding.btnDashboard.setEnabled(false);
                    binding.btnUsuario.setCardBackgroundColor(Color.GRAY);
                    binding.btnDashboard.setCardBackgroundColor(Color.GRAY);
                    menu.findItem(R.id.dialogAlterarCliente).setEnabled(false);
                    menu.findItem(R.id.config).setEnabled(false);
                    menu.findItem(R.id.baseDeDados).setEnabled(false);
                } else {
                    menu.findItem(R.id.dialogAlterarCodigoPin).setVisible(false);
                    menu.findItem(R.id.device).setTitle(reverse(getDeviceUniqueID(requireActivity())));
                }
                Locale primaryLocale;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    primaryLocale = getResources().getConfiguration().getLocales().get(0);
                    String locale = primaryLocale.getDisplayLanguage();
                    languageCode = primaryLocale.getLanguage();
                    menu.findItem(R.id.idioma).setTitle(locale + "(" + languageCode.toLowerCase(Locale.ROOT) + ") 🔻");
                } else
                    menu.findItem(R.id.idioma).setTitle("");
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                switch (menuItem.getItemId()) {
                    case R.id.idioma:
                        new AlertDialog.Builder(requireContext())
                                .setCancelable(false)
                                .setIcon(R.drawable.ic_baseline_store_24)
                                .setTitle(R.string.alt_idm)
                                .setSingleChoiceItems(R.array.array_idioma, getIdIdioma(requireContext()), (dialogInterface, i) -> {
                                    switch (i) {
                                        case 0:
                                            idioma = "Francês";
                                            codigoIdioma = "fr";
                                            break;
                                        case 1:
                                            idioma = "Inglês";
                                            codigoIdioma = "en";
                                            break;
                                        case 2:
                                            idioma = "Português";
                                            codigoIdioma = "pt";
                                            break;
                                        default:
                                            break;
                                    }
                                })
                                .setNegativeButton(R.string.cancelar, (dialogInterface, i) -> {
                                    codigoIdioma = null;
                                    dialogInterface.dismiss();
                                })
                                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                                    if (codigoIdioma == null || languageCode.equalsIgnoreCase(codigoIdioma))
                                        dialogInterface.dismiss();
                                    else
                                        getSelectedIdioma(requireActivity(), codigoIdioma, idioma, true, false);
                                }).show();
                        break;
                    case R.id.device:
                        getDetailDevice(requireContext());
                        break;
                    case R.id.dialogAlterarCliente:
                        bundle.putParcelable("cliente", cliente);
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dialogAlterarCliente, bundle);
                        break;
                    case R.id.estadoCliente:
                        MainActivity.getProgressBar();
                        if (isNetworkConnected(requireContext())) {
                            if (internetIsConnected())
                                estadoConta(Ultilitario.getValueSharedPreferences(requireContext(), "imei", "0000000000"));
                            else {
                                MainActivity.dismissProgressBar();
                                Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_int), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                            }
                        } else {
                            MainActivity.dismissProgressBar();
                            Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.conec_wif_dad), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                        break;
                    case R.id.gerarCodigoQr:
                        getQrCode();
                        break;
                    case R.id.config:
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_configuracaoFragment);
                        break;
                    case R.id.expoBd:
                        requestPermissionLauncherExportDataBase.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        break;
                    case R.id.impoBd:
                        Ultilitario.importarCategoriasProdutosClientes(importarBaseDeDados, requireActivity(), true);
                        break;
                    case R.id.termosCondicoes:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/termoscondicoes")));
                        break;
                    case R.id.politicaPrivacidade:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/politicaprivacidade")));
                        break;
                    case R.id.dialogAlterarCodigoPin:
                        bundle.putString("nome", getArguments().getString("nome"));
                        bundle.putLong("idusuario", getArguments().getInt("idusuario"));
                        bundle.putString("datacria", getArguments().getString("datacria"));
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dialogSenha, bundle);
                        break;
                    case R.id.acercaMborasytem:
                        acercaMboraSystem(requireContext(), requireActivity());
                        break;
                    case R.id.formaPagamento:
                        MainActivity.getProgressBar();
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("yoga").child("contabancaria");
                        reference.get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                StringBuilder ddbc = new StringBuilder();
                                DataSnapshot snapshot = task.getResult();
                                if (snapshot.exists()) {
                                    String detalhe = snapshot.child("informacao").child("detalhe").getValue().toString();
                                    ddbc.append(getString(R.string.info_pagamento, detalhe)).append("\n\n");
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        if (dataSnapshot.exists()) {
                                            ContaBancaria cb = snapshot.child(dataSnapshot.getKey()).getValue(ContaBancaria.class);
                                            if (cb.getNome() != null) {
                                                ddbc.append(getString(R.string.nm_bc)).append(": ").append(cb.getNome()).append("\n");
                                                ddbc.append(getString(R.string.ppt_bc)).append(": ").append(cb.getProprietario()).append("\n");
                                                ddbc.append(getString(R.string.nib_bc)).append(": ").append(cb.getNib()).append("\n");
                                                ddbc.append(getString(R.string.iban_bc)).append(": ").append(cb.getIban()).append("\n");
                                                ddbc.append("\n\n");
                                            }
                                        } else
                                            showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);
                                    }
                                } else
                                    showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.dds_n_enc), R.drawable.ic_toast_erro);
                                MainActivity.dismissProgressBar();
                                alertDialog(getString(R.string.forma_pagamento), ddbc.toString(), requireContext(), R.drawable.ic_baseline_store_24);
                            } else {
                                MainActivity.dismissProgressBar();
                                alertDialog(getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                            }
                        });
                        break;
                    case R.id.itemSair:
                        sairApp();
                        break;
                    default:
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        int posicaoRegime = Integer.parseInt(getValueSharedPreferences(requireContext(), "regime_iva", "0"));
        binding.textNomeUsuario.setText(requireArguments().getString("nome", ""));
        binding.textRegimeIva.setText(getResources().getStringArray(R.array.array_regime_iva_valor)[posicaoRegime]);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private void getQrCode() {
        if (isMaster)
            Ultilitario.showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(cliente.getImei()), true, requestPermissionLauncherSaveQrCode, cliente.getNome() + " " + cliente.getSobrenome(), cliente.getNomeEmpresa(), cliente.getImei());
        else
            Ultilitario.showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(Ultilitario.getValueSharedPreferences(requireContext(), "imei", "")), true, requestPermissionLauncherSaveQrCode, getArguments().getString("nome", ""), Ultilitario.getValueSharedPreferences(requireContext(), "nomeempresa", ""), Ultilitario.getValueSharedPreferences(requireContext(), "imei", ""));
    }

    private Bitmap gerarCodigoQr(String imei) {
        Bitmap bitmap = null;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(imei, BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            Ultilitario.alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
        return bitmap;
    }

    private void animationLixeira(Animation animation, Animation animationLixo, boolean isOpen) {
        binding.floatingActionButtonCategoria.startAnimation(animation);
        binding.floatingActionButtonProduto.startAnimation(animation);
//        binding.floatingActionButtonVendaLixo.startAnimation(animation);

        binding.floatingActionButtonLixo.startAnimation(animationLixo);

        binding.floatingActionButtonCategoria.setClickable(isOpen);
        binding.floatingActionButtonProduto.setClickable(isOpen);
//        binding.floatingActionButtonVendaLixo.setClickable(isOpen);
        this.isOpen = isOpen;
    }

    private Bundle isUserMaster() {
        MainActivity.getProgressBar();
        bundle.putBoolean("master", isMaster);
        return bundle;
    }

    private void sairApp() {
        new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ic_baseline_store_24)
                .setTitle(getString(R.string.sair))
                .setMessage(getString(R.string.tem_certeza_sair_app))
                .setNegativeButton(getString(R.string.nao), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(getString(R.string.sim), (dialog, which) -> requireActivity().finish())
                .show();
    }

    private void entrarCategoriasLx() {
        MainActivity.getProgressBar();
        HomeFragmentDirections.ActionHomeFragmentToCategoriaProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToCategoriaProdutoFragment().setIsLixeira(true).setIsMaster(isMaster);
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void entrarProdutosLx() {
        MainActivity.getProgressBar();
        HomeFragmentDirections.ActionHomeFragmentToListProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToListProdutoFragment().setIsLixeira(true).setIsMaster(isMaster);
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void entrarVendas(boolean isNotaCredito) {
        MainActivity.getProgressBar();
        HomeFragmentDirections.ActionHomeFragmentToVendaFragment direction = HomeFragmentDirections.actionHomeFragmentToVendaFragment(cliente).setIsNotaCredito(isNotaCredito).setIsMaster(isMaster);
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void entrarFacturacao() {
        MainActivity.getProgressBar();
        bundle.putBoolean("master", isMaster);
        bundle.putLong("idoperador", requireArguments().getLong("idusuario", 0));
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_facturaFragment, bundle);
    }

    private String estado, quantidadeTipo;
    private byte estadoConta, terminoPrazo;

    private void estadoConta(String imei) {

        String[] pacote = {getString(R.string.brz), getString(R.string.alm), getString(R.string.oro), ""};
        String[] tipo = getResources().getStringArray(R.array.tipo_pagamento);
        Map<String, String> mapTipoPagamento = new HashMap<>();
        mapTipoPagamento.put("1", tipo[0]);
        mapTipoPagamento.put("3", tipo[1]);
        mapTipoPagamento.put("6", tipo[2]);
        mapTipoPagamento.put("12", tipo[3]);

        String URL = Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/contacts/" + imei + "/estado";
        Ion.with(requireActivity())
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        for (int i = 0; i < jsonElements.size(); i++) {
                            JsonObject parceiro = jsonElements.get(i).getAsJsonObject();
                            estadoConta = Byte.parseByte(parceiro.get("estado").getAsString());
                            terminoPrazo = parceiro.get("termina").getAsByte();
                            String contactos = parceiro.get("contactos").getAsString();
                            String dispositivo = parceiro.get("device").getAsString();
                            quantidadeTipo = parceiro.get("quantidade_produto_pacote").getAsString();
                            boolean equalsDevice = dispositivo.trim().equalsIgnoreCase(getDetailDeviceString(requireActivity()));
                            estado = (!equalsDevice ? getString(R.string.inco_desp) + "\n\n" : "") +
                                    (terminoPrazo == Ultilitario.UM ? getString(R.string.prazterm) + "\n" :
                                            (estadoConta == Ultilitario.ZERO ? getString(R.string.ms_inf) + "\n" : "")) + "\n" +
                                    getString(R.string.pac) + ": " + pacote[Byte.parseByte(parceiro.get("pacote").getAsString())] + "\n" +
                                    getString(R.string.tipo) + " " + mapTipoPagamento.get(parceiro.get("tipo_pagamento").getAsString()) + "\n" +
                                    getString(R.string.prod) + "(" + getString(R.string.mbora) + "): " + parceiro.get("quantidade_produto_pacote").getAsString() + "\n" +
                                    getString(R.string.prod_regi) + "(" + getString(R.string.mbora) + "): " + parceiro.get("quantidade_produto").getAsString() + "\n\n" +
                                    getString(R.string.ini) + ": " + parceiro.get("inicio").getAsString() + "\n" +
                                    getString(R.string.term) + ": " + parceiro.get("fim").getAsString() + "\n\n" +
                                    getString(R.string.nome).replace("*", ": ") + parceiro.get("first_name").getAsString() + "\n" +
                                    getString(R.string.Sobre_Nome).replace("*", ": ") + parceiro.get("last_name").getAsString() + "\n" +
                                    getString(R.string.nifbi).replace("*", ": ") + parceiro.get("nif_bi").getAsString() + "\n" +
                                    getString(R.string.Numero_Telefone).replace("*", ": ") + parceiro.get("phone").getAsString() + "\n" +
                                    getString(R.string.Numero_Telefone_Alternativo).replace("*", ": ") + parceiro.get("alternative_phone").getAsString() + "\n" +
                                    getString(R.string.Email).replace("*", ": ") + parceiro.get("email").getAsString() + "\n" +
                                    getString(R.string.empresa).replace("*", ": ") + parceiro.get("empresa").getAsString() + "\n" +
                                    getString(R.string.municipio).replace("*", ": ") + parceiro.get("municipality").getAsString() + "\n" +
                                    getString(R.string.bairro).replace("*", ": ") + parceiro.get("district").getAsString() + "\n" +
                                    getString(R.string.rua).replace("*", ": ") + parceiro.get("street").getAsString() + "\n" +
                                    getString(R.string.imei) + ": " + parceiro.get("imei").getAsString() + "\n\nYOGA:" + contactos;
                        }
                        boolean isFinish = estadoConta == Ultilitario.ZERO || terminoPrazo == Ultilitario.UM;
                        if (isFinish)
                            setValueSharedPreferences(requireContext(), "pac_qtd_pro", "0");
                        else
                            setValueSharedPreferences(requireContext(), "pac_qtd_pro", quantidadeTipo);
                        alertDialog(isFinish ? getString(R.string.des) : getString(R.string.act), estado, requireContext(), isFinish ? R.drawable.ic_baseline_person_add_disabled_24 : R.drawable.ic_baseline_person_pin_24);
                    } catch (Exception ex) {
                        MainActivity.dismissProgressBar();
                        new AlertDialog.Builder(requireContext())
                                .setIcon(R.drawable.ic_baseline_store_24)
                                .setTitle(getString(R.string.erro))
                                .setMessage(ex.getMessage())
                                .setNegativeButton(R.string.cancelar, (dialog, which) -> dialog.dismiss())
                                .setPositiveButton(R.string.tent_nov, (dialog, which) -> {
                                    dialog.dismiss();
                                    MainActivity.getProgressBar();
                                    estadoConta(imei);
                                })
                                .show();
                    }
                });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    bundle.putParcelable("cliente", cliente);
                    bundle.putBoolean("master", isMaster);
                    Navigation.findNavController(requireView()).navigate(R.id.documentoFragment, bundle);
                } else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_prm_na_vis_doc), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
    );
    private final ActivityResultLauncher<String> requestPermissionLauncherSaveQrCode = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    String bitmapPath = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), gerarCodigoQr(Ultilitario.getValueSharedPreferences(requireContext(), "imei", "")), Ultilitario.getValueSharedPreferences(requireContext(), "nomeempresa", "").replace(".", " ").replace(",", " "), null);
                    Toast.makeText(requireContext(), bitmapPath, Toast.LENGTH_LONG).show();
                } else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_gua_cod_qr), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
    );

    private String uriPath;
    private ActivityResultLauncher<String> requestPermissionLauncherImportDataBase = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    MainActivity.getProgressBar();
                    executor = Executors.newSingleThreadExecutor();
                    executor.execute(() -> Ultilitario.importDB(requireContext(), new Handler(Looper.getMainLooper()), uriPath));
                } else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_imp_bd), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
    );
    private ActivityResultLauncher<String> requestPermissionLauncherExportDataBase = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result)
                    exportarBD();
                else
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_expo_db), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
    );
    ActivityResultLauncher<Intent> importarBaseDeDados = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri uri;
                    if (data != null) {
                        uri = data.getData();
                        try {
                            uriPath = TextUtils.split(uri.getPath(), "/")[4];
                        } catch (IndexOutOfBoundsException e) {
                            alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setIcon(R.drawable.ic_baseline_insert_drive_file_24)
                                .setTitle(getString(R.string.impoBd))
                                .setMessage(uri.getPath() + "\n\n" + getString(R.string.imp_elim_bd))
                                .setNegativeButton(getString(R.string.cancelar), (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                                    try {
                                        String stringHash = TextUtils.split(uriPath, "-")[2];
                                        byte[] bytesHash = getHash(reverse(getDeviceUniqueID(requireActivity())) + "-" + reverse(cliente.getImei()));
                                        if (bytesToHex(bytesHash).equals(stringHash)) {
                                            requestPermissionLauncherImportDataBase.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                        } else
                                            alertDialog(getString(R.string.erro), getString(R.string.inc_bd), requireContext(), R.drawable.ic_baseline_close_24);
                                    } catch (Exception e) {
                                        alertDialog(getString(R.string.erro), e.getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                                    }
                                })
                                .show();
                    }
                }
            });

    private void exportarBD() {
        MainActivity.getProgressBar();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> Ultilitario.exportDB(requireContext(), new Handler(Looper.getMainLooper()), getDeviceUniqueID(requireActivity()), cliente.getImei()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null)
            bundle.clear();
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }
}
