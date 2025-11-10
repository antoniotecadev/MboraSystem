package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.acercaMboraSystem;
import static com.yoga.mborasystem.util.Ultilitario.activityResultContracts;
import static com.yoga.mborasystem.util.Ultilitario.activityResultContractsSelectFile;
import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.conexaoInternet;
import static com.yoga.mborasystem.util.Ultilitario.exportBD;
import static com.yoga.mborasystem.util.Ultilitario.gerarCodigoQr;
import static com.yoga.mborasystem.util.Ultilitario.getAPN;
import static com.yoga.mborasystem.util.Ultilitario.getDetailDevice;
import static com.yoga.mborasystem.util.Ultilitario.getDetailDeviceString;
import static com.yoga.mborasystem.util.Ultilitario.getDeviceUniqueID;
import static com.yoga.mborasystem.util.Ultilitario.getIdIdioma;
import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;
import static com.yoga.mborasystem.util.Ultilitario.getValueSharedPreferences;
import static com.yoga.mborasystem.util.Ultilitario.launchPermissionDocumentSaftInvoice;
import static com.yoga.mborasystem.util.Ultilitario.launchPermissionImportExportDB;
import static com.yoga.mborasystem.util.Ultilitario.reverse;
import static com.yoga.mborasystem.util.Ultilitario.showToast;
import static com.yoga.mborasystem.util.Ultilitario.showToastOrAlertDialogQrCode;
import static com.yoga.mborasystem.util.Ultilitario.uriPath;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

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

public class HomeFragment extends Fragment {

    private Bundle bundle;
    private Cliente cliente;
    private boolean isNotaCredito;
    private FragmentHomeBinding binding;
    private boolean isOpen = false, isMaster;
    private ClienteViewModel clienteViewModel;
    private Animation FabOpen, FabClose, FabRClockwise, FabRanticlockwise;
    private String idioma, codigoIdioma, nomeOperador, languageCode = "";

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
            int itemId = item.getItemId();
            if (itemId == R.id.bloquearFragment) {
                Navigation.findNavController(requireView()).navigate(R.id.action_global_bloquearFragment);
            } else if (itemId == R.id.gerarQrCode) {
                boolean isExternalStorageManager = launchPermissionDocumentSaftInvoice(requireContext(), requestIntentPermissionLauncherQrCode, requestPermissionLauncherQrCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (isExternalStorageManager)
                    getQrCode();
            } else if (itemId == R.id.sairApp) {
                sairApp();
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

//        binding.floatingActionButtonVendaLixo.setOnClickListener();

        MainActivity.navigationView.setNavigationItemSelectedListener(item -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
            int itemId = item.getItemId();
            if (itemId == R.id.categoriaProdutoFragmentH) {
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_categoriaProdutoFragment, isUserMaster());
            } else if (itemId == R.id.usuarioFragmentH) {
                if (isMaster)
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_usuarioFragment, isUserMaster());
            } else if (itemId == R.id.vendaFragmentH) {
                entrarVendas(isNotaCredito = false);
            } else if (itemId == R.id.listaClienteFragmentH) {
                Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_listaClienteFragment, isUserMaster());
            } else if (itemId == R.id.dashboardFragmentH) {
                if (isMaster)
                    entrarDashboard();
            } else if (itemId == R.id.facturaFragmentH) {
                entrarFacturacao();
            } else if (itemId == R.id.vendaFragmentNotaCredito) {
                entrarVendas(isNotaCredito = true);
            } else if (itemId == R.id.categoriaProdutoFragmentLx) {
                entrarCategoriasLx();
            } else if (itemId == R.id.produtoFragmentLx) {
                entrarProdutosLx();
            } else if (itemId == R.id.documentoFragmentMenu) {
                boolean isExternalStorageManager = launchPermissionDocumentSaftInvoice(requireContext(), requestIntentPermissionLauncherViewDocument, requestPermissionLauncherViewDocument, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (isExternalStorageManager)
                    activityResultContractsViewDocument(isExternalStorageManager);
            }
            MainActivity.drawerLayout.closeDrawer(GravityCompat.START);
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        binding.floatingActionButtonVenda.setOnClickListener(v -> entrarFacturacao());

        binding.btnUsuario.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_usuarioFragment));

        binding.btnProduto.setOnClickListener(v -> {
            HomeFragmentDirections.ActionHomeFragmentToCategoriaProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToCategoriaProdutoFragment().setIsMaster(isMaster).setIdUsuario(requireArguments().getLong("idusuario", 0));
            Navigation.findNavController(requireView()).navigate(direction);
        });

        binding.btnVenda.setOnClickListener(v -> entrarVendas(isNotaCredito = false));

        binding.btnCliente.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_listaClienteFragment));
        binding.btnDashboard.setOnClickListener(v -> entrarDashboard());

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
                int itemId = menuItem.getItemId();
                if (itemId == R.id.idioma) {
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
                } else if (itemId == R.id.device) {
                    getDetailDevice(requireContext());
                } else if (itemId == R.id.dialogAlterarCliente) {
                    bundle.putParcelable("cliente", cliente);
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dialogAlterarCliente, bundle);
                } else if (itemId == R.id.estadoCliente) {
                    if (conexaoInternet(requireContext()))
                        estadoConta(getValueSharedPreferences(requireContext(), "imei", "0000000000"));
                } else if (itemId == R.id.gerarCodigoQr) {
                    boolean isExternalStorageManager = launchPermissionDocumentSaftInvoice(requireContext(), requestIntentPermissionLauncherQrCode, requestPermissionLauncherQrCode, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (isExternalStorageManager)
                        getQrCode();
                } else if (itemId == R.id.config) {
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_configuracaoFragment);
                } else if (itemId == R.id.expoBd) {
                    launchPermissionImportExportDB(requireContext(), null, getDeviceUniqueID(requireContext()), cliente.getImei(), requestIntentPermissionLauncherExportDataBase, requestPermissionLauncherExportDataBase, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else if (itemId == R.id.impoBd) {
                    Ultilitario.importarCategoriasProdutosClientes(importarBaseDeDados, requireActivity(), true);
                } else if (itemId == R.id.termosCondicoes) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getAPN(requireActivity()) + "termoscondicoes")));
                } else if (itemId == R.id.politicaPrivacidade) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getAPN(requireActivity()) + "politicaprivacidade")));
                } else if (itemId == R.id.dialogAlterarCodigoPin) {
                    bundle.putString("nome", getArguments().getString("nome"));
                    bundle.putLong("idusuario", getArguments().getLong("idusuario"));
                    bundle.putString("datacria", getArguments().getString("datacria"));
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dialogSenha, bundle);
                } else if (itemId == R.id.acercaMborasytem) {
                    acercaMboraSystem(requireContext(), requireActivity());
                } else if (itemId == R.id.formaPagamento) {
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
                            alertDialog(getString(R.string.forma_pagamento).replace(":", ""), ddbc.toString(), requireContext(), R.drawable.ic_baseline_store_24);
                        } else {
                            MainActivity.dismissProgressBar();
                            alertDialog(getString(R.string.erro), task.getException().getMessage(), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                    });
                } else if (itemId == R.id.itemSair) {
                    sairApp();
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
            showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(cliente.getImei(), requireContext()), true, cliente.getNome() + " " + cliente.getSobrenome(), cliente.getNomeEmpresa(), cliente.getImei());
        else
            showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(Ultilitario.getValueSharedPreferences(requireContext(), "imei", ""), requireContext()), true, getArguments().getString("nome", ""), Ultilitario.getValueSharedPreferences(requireContext(), "nomeempresa", ""), Ultilitario.getValueSharedPreferences(requireContext(), "imei", ""));
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
        boolean isExternalStorageManager = launchPermissionDocumentSaftInvoice(requireContext(), requestIntentPermissionLauncherVendas, requestPermissionLauncherVendas, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (isExternalStorageManager)
            entrarVendasPermissionDanied(isNotaCredito);
    }

    private void entrarVendasPermissionDanied(boolean isNotaCredito) {
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

    private void entrarDashboard() {
        boolean isExternalStorageManager = launchPermissionDocumentSaftInvoice(requireContext(), requestIntentPermissionLauncherDashboard, requestPermissionLauncherDashboard, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (isExternalStorageManager)
            entrarDashboardDanied();
    }

    private void entrarDashboardDanied() {
        MainActivity.getProgressBar();
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dashboardFragment);
    }

    private boolean isFinish;

    private void estadoConta(String imei) {

        StringBuilder sb = new StringBuilder();
        String[] pacote = {getString(R.string.brz), getString(R.string.alm), getString(R.string.oro), ""};
        String[] tipo = getResources().getStringArray(R.array.tipo_pagamento);
        Map<String, String> mapTipoPagamento = new HashMap<>();
        mapTipoPagamento.put("1", tipo[0]);
        mapTipoPagamento.put("3", tipo[1]);
        mapTipoPagamento.put("6", tipo[2]);
        mapTipoPagamento.put("12", tipo[3]);

        String URL = getAPN(requireActivity()) + "contacts/" + imei + "/estado";
        Ion.with(requireActivity())
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        for (int i = 0; i < jsonElements.size(); i++) {

                            JsonObject parceiro = jsonElements.get(i).getAsJsonObject();
                            byte estadoConta = Byte.parseByte(parceiro.get("estado").getAsString());
                            byte terminoPrazo = parceiro.get("termina").getAsByte();
                            String contactos = parceiro.get("contactos").getAsString();
                            String dispositivo = parceiro.get("device").getAsString();
                            boolean equalsDevice = dispositivo.trim().equalsIgnoreCase(getDetailDeviceString(requireActivity()));

                            sb.append(!equalsDevice ? getString(R.string.inco_desp) + "\n\n" : "");
                            sb.append(terminoPrazo == Ultilitario.UM ? getString(R.string.prazterm) + "\n" : (estadoConta == Ultilitario.ZERO ? getString(R.string.ms_inf) + "\n" : "")).append("\n");
                            sb.append(getString(R.string.pac)).append(": ").append(pacote[Byte.parseByte(parceiro.get("pacote").getAsString())]).append("\n");
                            sb.append(getString(R.string.tipo)).append(" ").append(mapTipoPagamento.get(parceiro.get("tipo_pagamento").getAsString())).append("\n");
                            sb.append(getString(R.string.prod)).append("(").append(getString(R.string.mbora)).append(")").append(": ").append(parceiro.get("quantidade_produto_pacote").getAsString()).append("\n");
                            sb.append(getString(R.string.prod_regi)).append("(").append(getString(R.string.mbora)).append(")").append(": ").append(parceiro.get("quantidade_produto").getAsString()).append("\n\n");
                            sb.append(getString(R.string.ini)).append(": ").append(parceiro.get("inicio").getAsString()).append("\n");
                            sb.append(getString(R.string.term)).append(": ").append(parceiro.get("fim").getAsString()).append("\n\n");
                            sb.append(getString(R.string.nome).replace("*", ": ")).append(parceiro.get("first_name").getAsString()).append("\n");
                            sb.append(getString(R.string.Sobre_Nome).replace("*", ": ")).append(parceiro.get("last_name").getAsString()).append("\n");
                            sb.append(getString(R.string.nifbi).replace("*", ": ")).append(parceiro.get("nif_bi").getAsString()).append("\n");
                            sb.append(getString(R.string.Numero_Telefone).replace("*", ": ")).append(parceiro.get("phone").getAsString()).append("\n");
                            sb.append(getString(R.string.Numero_Telefone_Alternativo).replace("*", ": ")).append(parceiro.get("alternative_phone").getAsString()).append("\n");
                            sb.append(getString(R.string.Email).replace("*", ": ")).append(parceiro.get("email").getAsString()).append("\n");
                            sb.append(getString(R.string.empresa).replace("*", ": ")).append(parceiro.get("empresa").getAsString()).append("\n");
                            sb.append(getString(R.string.provincia).replace("*", ": ")).append(parceiro.get("provincia").getAsString()).append("\n");
                            sb.append(getString(R.string.municipio).replace("*", ": ")).append(parceiro.get("municipality").getAsString()).append("\n");
                            sb.append(getString(R.string.bairro).replace("*", ": ")).append(parceiro.get("district").getAsString()).append("\n");
                            sb.append(getString(R.string.rua).replace("*", ": ")).append(parceiro.get("street").getAsString()).append("\n");
                            sb.append(getString(R.string.imei)).append(": ").append(parceiro.get("imei").getAsString()).append("\n\n");
                            sb.append("YOGA: ").append(contactos);
                            isFinish = estadoConta == Ultilitario.ZERO || terminoPrazo == Ultilitario.UM;
                        }
                        alertDialog(isFinish ? getString(R.string.des) : getString(R.string.act), sb.toString(), requireContext(), isFinish ? R.drawable.ic_baseline_person_add_disabled_24 : R.drawable.ic_baseline_person_pin_24);
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

    private void activityResultContractsViewDocument(Boolean result) {
        if (result) {
            bundle.putParcelable("cliente", cliente);
            bundle.putBoolean("master", isMaster);
            Navigation.findNavController(requireView()).navigate(R.id.documentoFragment, bundle);
        } else
            Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_prm_na_vis_doc), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
    }

    private ActivityResultLauncher<Intent> requestIntentPermissionLauncherViewDocument = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsViewDocument(result.getResultCode() == Activity.RESULT_OK));

    private final ActivityResultLauncher<String> requestPermissionLauncherViewDocument = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> activityResultContractsViewDocument(result));

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherImportDataBase = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContracts(requireContext(), result.getResultCode() == Activity.RESULT_OK, uriPath)
    );

    private final ActivityResultLauncher<String> requestPermissionLauncherImportDataBase = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> activityResultContracts(requireContext(), result, uriPath)
    );

    ActivityResultLauncher<Intent> importarBaseDeDados = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK)
                    activityResultContractsSelectFile(requireActivity(), requireContext(), false, cliente.getImei(), result, requestPermissionLauncherImportDataBase, requestIntentPermissionLauncherImportDataBase);
            });

    private ActivityResultLauncher<String> requestPermissionLauncherExportDataBase = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> exportBD(result, requireContext(), cliente.getImei())
    );

    ActivityResultLauncher<Intent> requestIntentPermissionLauncherExportDataBase = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> exportBD(result.getResultCode() == Activity.RESULT_OK, requireContext(), cliente.getImei()));

    private void activityResultContractsVendas(Boolean result) {
        if (result)
            entrarVendasPermissionDanied(isNotaCredito);
        else
            alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_ent), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
    }

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherVendas = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsVendas(result.getResultCode() == Activity.RESULT_OK));

    private final ActivityResultLauncher<String> requestPermissionLauncherVendas = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), this::activityResultContractsVendas);

    private void activityResultContractsDashboard(Boolean result) {
        if (result)
            entrarDashboardDanied();
        else
            alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_ent), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
    }

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherDashboard = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsDashboard(result.getResultCode() == Activity.RESULT_OK));

    private final ActivityResultLauncher<String> requestPermissionLauncherDashboard = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), this::activityResultContractsDashboard);

    private void activityResultContractsQrCode(Boolean result) {
        if (result)
            getQrCode();
        else
            alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_gua_cod_qr), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
    }

    private final ActivityResultLauncher<Intent> requestIntentPermissionLauncherQrCode = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> activityResultContractsQrCode(result.getResultCode() == Activity.RESULT_OK));

    private final ActivityResultLauncher<String> requestPermissionLauncherQrCode = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), this::activityResultContractsQrCode);

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
