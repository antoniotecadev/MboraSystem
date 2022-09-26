package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.alertDialog;
import static com.yoga.mborasystem.util.Ultilitario.bytesToHex;
import static com.yoga.mborasystem.util.Ultilitario.getDeviceUniqueID;
import static com.yoga.mborasystem.util.Ultilitario.getHash;
import static com.yoga.mborasystem.util.Ultilitario.getIdIdioma;
import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;
import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;
import static com.yoga.mborasystem.util.Ultilitario.reverse;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
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

import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.koushikdutta.ion.Ion;
import com.yoga.mborasystem.MainActivity;
import com.yoga.mborasystem.R;
import com.yoga.mborasystem.databinding.FragmentHomeBinding;
import com.yoga.mborasystem.model.entidade.Cliente;
import com.yoga.mborasystem.util.Ultilitario;
import com.yoga.mborasystem.viewmodel.ClienteViewModel;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment {

    private Bundle bundle;
    String nomeOperador, language = "";
    private Cliente cliente;
    private boolean isOpen = false;
    private ExecutorService executor;
    private FragmentHomeBinding binding;
    private String idioma, codigoIdioma;
    private ClienteViewModel clienteViewModel;
    private Animation FabOpen, FabClose, FabRClockwise, FabRanticlockwise;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = new Bundle();
        FabOpen = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        FabClose = AnimationUtils.loadAnimation(getContext(), R.anim.fab_close);
        FabRClockwise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_clockwise);
        FabRanticlockwise = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_anticlockwise);
        if (getArguments() != null)
            nomeOperador = getArguments().getString("nome");
        cliente = getArguments().getParcelable("cliente");
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
            if (isOpen) {
                animationLixeira(FabClose, FabRanticlockwise, false);
            } else {
                animationLixeira(FabOpen, FabRClockwise, true);
            }
        });

        binding.floatingActionButtonCategoria.setOnClickListener(v -> entrarCategoriasLx());

        binding.floatingActionButtonProduto.setOnClickListener(v -> entrarProdutosLx());

        binding.floatingActionButtonVendaLixo.setOnClickListener(v -> entrarVendasLx());
        if (getArguments() != null)
            if (!getArguments().getBoolean("master")) {
                MainActivity.navigationView.getMenu().findItem(R.id.usuarioFragment).setVisible(false);
                MainActivity.navigationView.getMenu().findItem(R.id.dashboardFragment).setVisible(false);
            }
        MainActivity.navigationView.setNavigationItemSelectedListener(item -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
            switch (item.getItemId()) {
                case R.id.categoriaProdutoFragmentH:
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_categoriaProdutoFragment, isUserMaster());
                    break;
                case R.id.usuarioFragmentH:
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_usuarioFragment, isUserMaster());
                    break;
                case R.id.vendaFragmentH:
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_vendaFragment, isUserMaster());
                    break;
                case R.id.listaClienteFragmentH:
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_listaClienteFragment, isUserMaster());
                    break;
                case R.id.dashboardFragmentH:
                    MainActivity.getProgressBar();
                    Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dashboardFragment);
                    break;
                case R.id.facturaFragmentH:
                    entrarFacturacao();
                    break;
                case R.id.categoriaProdutoFragmentLx:
                    entrarCategoriasLx();
                    break;
                case R.id.produtoFragmentLx:
                    entrarProdutosLx();
                    break;
                case R.id.vendaFragmentLx:
                    entrarVendasLx();
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

        binding.btnUsuario.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_usuarioFragment, isUserMaster()));

        binding.btnProduto.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_categoriaProdutoFragment, isUserMaster()));

        binding.btnVenda.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_vendaFragment, isUserMaster()));

        binding.btnCliente.setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_listaClienteFragment, isUserMaster()));
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
                if (getArguments() != null) {
                    if (!getArguments().getBoolean("master")) {
                        binding.btnUsuario.setEnabled(false);
                        binding.btnDashboard.setEnabled(false);
                        binding.btnUsuario.setCardBackgroundColor(Color.GRAY);
                        binding.btnDashboard.setCardBackgroundColor(Color.GRAY);
                        menu.findItem(R.id.dialogAlterarCliente).setEnabled(false);
                        menu.findItem(R.id.config).setEnabled(false);
                        menu.findItem(R.id.baseDeDados).setEnabled(false);
                    } else {
                        menu.findItem(R.id.dialogAlterarCodigoPin).setVisible(false);
                    }
                }
                Locale primaryLocale;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    primaryLocale = getResources().getConfiguration().getLocales().get(0);
                    String locale = primaryLocale.getDisplayLanguage();
                    language = primaryLocale.getLanguage();
                    menu.findItem(R.id.idioma).setTitle(locale);
                } else {
                    menu.findItem(R.id.idioma).setTitle("");
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
                switch (menuItem.getItemId()) {
                    case R.id.idioma:
                        new AlertDialog.Builder(requireContext())
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
                                .setNegativeButton(R.string.cancelar, (dialogInterface, i) -> dialogInterface.dismiss())
                                .setPositiveButton(R.string.ok, (dialogInterface, i) -> getSelectedIdioma(requireActivity(), codigoIdioma, idioma, true, false)).show();
                        break;
                    case R.id.dialogAlterarCliente:
                        if (getArguments() != null) {
                            bundle.putParcelable("cliente", cliente);
                            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dialogAlterarCliente, bundle);
                        }
                        break;
                    case R.id.estadoCliente:
                        if (getArguments() != null) {
                            MainActivity.getProgressBar();
                            if (isNetworkConnected(requireContext())) {
                                if (internetIsConnected()) {
                                    estadoConta(Ultilitario.getValueSharedPreferences(requireContext(), "imei", "0000000000"));
                                } else {
                                    MainActivity.dismissProgressBar();
                                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_int), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                                }
                            } else {
                                MainActivity.dismissProgressBar();
                                Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.conec_wif_dad), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                            }
                        }
                        break;
                    case R.id.gerarCodigoQr:
                        getQrCode();
                        break;
                    case R.id.config:
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_configuracaoFragment);
                        break;
                    case R.id.expoBd:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissionLauncherDeviceId.launch(Manifest.permission.READ_PHONE_STATE);
                        } else {
                            exportarBD();
                        }
                        break;
                    case R.id.impoBd:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            Ultilitario.importarCategoriasProdutosClientes(importarBaseDeDados, requireActivity(), true);
                        } else {
                            alertDialog(getString(R.string.avs), getString(R.string.imp_dis_api_sup), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                        }
                        break;
                    case R.id.termosCondicoes:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/termoscondicoes")));
                        break;
                    case R.id.politicaPrivacidade:
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/politicaprivacidade")));
                        break;
                    case R.id.dialogAlterarCodigoPin:
                        if (getArguments() != null) {
                            bundle.putLong("idusuario", getArguments().getLong("idusuario"));
                            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_dialogSenha, bundle);
                        }
                        break;
                    case R.id.acercaMborasytem:
                        acercaMboraSystem();
                        break;
                    case R.id.itemSair:
                        sairApp();
                        break;
                    default:
                }
                return NavigationUI.onNavDestinationSelected(menuItem, navController);
            }
        }, getViewLifecycleOwner());
        binding.textNomeUsuario.setText("✔ " + requireArguments().getString("nome", ""));
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private void getQrCode() {
        if (getArguments() != null)
            if (getArguments().getBoolean("master"))
                Ultilitario.showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(cliente.getImei()), true, requestPermissionLauncherShareQrCode, cliente.getNome() + " " + cliente.getSobrenome(), cliente.getNomeEmpresa(), cliente.getImei());
            else
                Ultilitario.showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(Ultilitario.getValueSharedPreferences(requireContext(), "imei", "")), true, requestPermissionLauncherShareQrCode, getArguments().getString("nome", ""), Ultilitario.getValueSharedPreferences(requireContext(), "nomeempresa", ""), Ultilitario.getValueSharedPreferences(requireContext(), "imei", ""));
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
        binding.floatingActionButtonVendaLixo.startAnimation(animation);

        binding.floatingActionButtonLixo.startAnimation(animationLixo);

        binding.floatingActionButtonCategoria.setClickable(isOpen);
        binding.floatingActionButtonProduto.setClickable(isOpen);
        binding.floatingActionButtonVendaLixo.setClickable(isOpen);
        this.isOpen = isOpen;
    }

    private Bundle isUserMaster() {
        if (getArguments() != null) {
            MainActivity.getProgressBar();
            bundle.putBoolean("master", getArguments().getBoolean("master"));
        } else {
            Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.arg_null), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
        return bundle;
    }

    private void acercaMboraSystem() {
        MainActivity.getProgressBar();
        if (isNetworkConnected(requireContext())) {
            if (internetIsConnected()) {
                String URL = Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/contacts/contactos";
                Ion.with(requireActivity())
                        .load(URL)
                        .asJsonArray()
                        .setCallback((e, jsonElements) -> {
                            try {
                                JsonObject parceiro = jsonElements.get(0).getAsJsonObject();
                                String contactos = parceiro.get("contactos").getAsString();
                                alertDialog(getString(R.string.nome_sistema), getString(R.string.acerca) + "\n" + contactos, requireContext(), R.drawable.ic_baseline_store_24);
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
                                            acercaMboraSystem();
                                        })
                                        .show();
                            }
                        });
            } else {
                MainActivity.dismissProgressBar();
                Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_int), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
            }
        } else {
            MainActivity.dismissProgressBar();
            Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.conec_wif_dad), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
        }
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
        if (getArguments() != null) {
            MainActivity.getProgressBar();
            HomeFragmentDirections.ActionHomeFragmentToCategoriaProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToCategoriaProdutoFragment().setIsLixeira(true).setIsMaster(getArguments().getBoolean("master"));
            Navigation.findNavController(requireView()).navigate(direction);
        }
    }

    private void entrarProdutosLx() {
        if (getArguments() != null) {
            MainActivity.getProgressBar();
            HomeFragmentDirections.ActionHomeFragmentToListProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToListProdutoFragment().setIsLixeira(true).setIsMaster(getArguments().getBoolean("master"));
            Navigation.findNavController(requireView()).navigate(direction);
        }
    }

    private void entrarVendasLx() {
        if (getArguments() != null) {
            MainActivity.getProgressBar();
            HomeFragmentDirections.ActionHomeFragmentToVendaFragment direction = HomeFragmentDirections.actionHomeFragmentToVendaFragment().setIsLixeira(true).setIsMaster(getArguments().getBoolean("master"));
            Navigation.findNavController(requireView()).navigate(direction);
        }
    }

    private void entrarFacturacao() {
        MainActivity.getProgressBar();
        if (getArguments() != null)
            bundle.putBoolean("master", getArguments().getBoolean("master"));
        bundle.putLong("idoperador", requireArguments().getLong("idusuario", 0));
        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_facturaFragment, bundle);
    }

    private String estado;
    private byte estadoTitulo, termina;

    private void estadoConta(String imei) {
        String[] pacote = {getString(R.string.brz), getString(R.string.alm), getString(R.string.oro), ""};
        String URL = Ultilitario.getAPN(requireActivity()) + "/mborasystem-admin/public/api/contacts/" + imei + "/estado";
        Ion.with(requireActivity())
                .load(URL)
                .asJsonArray()
                .setCallback((e, jsonElements) -> {
                    try {
                        for (int i = 0; i < jsonElements.size(); i++) {
                            JsonObject parceiro = jsonElements.get(i).getAsJsonObject();
                            estadoTitulo = Byte.parseByte(parceiro.get("estado").getAsString());
                            termina = parceiro.get("termina").getAsByte();
                            String contactos = parceiro.get("contactos").getAsString();
                            estado = (termina == Ultilitario.UM ? getString(R.string.prazterm) + "\n" :
                                    (estadoTitulo == Ultilitario.ZERO ? getString(R.string.ms_inf) + "\n" : "")) + "\n" +
                                    getString(R.string.pac) + ": " + pacote[Byte.parseByte(parceiro.get("pacote").getAsString())] + "\n" +
                                    getString(R.string.ini) + ": " + parceiro.get("inicio").getAsString() + "\n" +
                                    getString(R.string.term) + ": " + parceiro.get("fim").getAsString() + "\n\n" +
                                    getString(R.string.nome) + ": " + parceiro.get("first_name").getAsString() + "\n" +
                                    getString(R.string.Sobre_Nome) + ": " + parceiro.get("last_name").getAsString() + "\n" +
                                    getString(R.string.nifbi) + ": " + parceiro.get("nif_bi").getAsString() + "\n" +
                                    getString(R.string.Numero_Telefone) + ": " + parceiro.get("phone").getAsString() + "\n" +
                                    getString(R.string.Numero_Telefone_Alternativo) + ": " + parceiro.get("alternative_phone").getAsString() + "\n" +
                                    getString(R.string.Email) + ": " + parceiro.get("email").getAsString() + "\n" +
                                    getString(R.string.empresa) + ": " + parceiro.get("cantina").getAsString() + "\n" +
                                    getString(R.string.municipio) + ": " + parceiro.get("municipality").getAsString() + "\n" +
                                    getString(R.string.bairro) + ": " + parceiro.get("district").getAsString() + "\n" +
                                    getString(R.string.rua) + ": " + parceiro.get("street").getAsString() + "\n" +
                                    "IMEI: " + parceiro.get("imei").getAsString() + "\n\nYOGA:" + contactos;

                        }
                        alertDialog(estadoTitulo == Ultilitario.ZERO || termina == Ultilitario.UM ? getString(R.string.des) : getString(R.string.act), estado, requireContext(),
                                estadoTitulo == Ultilitario.ZERO || termina == Ultilitario.UM ? R.drawable.ic_baseline_person_add_disabled_24 : R.drawable.ic_baseline_person_pin_24);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        if (bundle != null) {
            bundle.clear();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.dismissProgressBar();
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    if (getArguments() != null) {
                        bundle.putParcelable("cliente", cliente);
                        bundle.putBoolean("master", getArguments().getBoolean("master"));
                        Navigation.findNavController(requireView()).navigate(R.id.documentoFragment, bundle);
                    } else {
                        Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.arg_null), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                    }
                } else {
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_prm_na_vis_doc), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                }
            }
    );
    private final ActivityResultLauncher<String> requestPermissionLauncherShareQrCode = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    String bitmapPath = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), gerarCodigoQr(Ultilitario.getValueSharedPreferences(requireContext(), "imei", "")), getString(R.string.cod_qr) + "-" + getString(R.string.estab) + "-" + cliente.getNomeEmpresa(), null);
                    Uri bitmapUri = Uri.parse(bitmapPath);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    startActivity(Intent.createChooser(intent, getString(R.string.part_me_cod_qr)));
                } else {
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_part_cod_qr), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                }
            }
    );

    private String uriPath;
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
                                            MainActivity.getProgressBar();
                                            executor = Executors.newSingleThreadExecutor();
                                            executor.execute(() -> Ultilitario.importDB(requireContext(), new Handler(Looper.getMainLooper()), uriPath));
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

    private final ActivityResultLauncher<String> requestPermissionLauncherDeviceId = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    exportarBD();
                } else {
                    Ultilitario.alertDialog(getString(R.string.erro), getString(R.string.sm_perm_n_pod_expo_db), requireContext(), R.drawable.ic_baseline_privacy_tip_24);
                }
            }
    );
}
