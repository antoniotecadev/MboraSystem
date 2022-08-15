package com.yoga.mborasystem.view;

import static com.yoga.mborasystem.util.Ultilitario.getIdIdioma;
import static com.yoga.mborasystem.util.Ultilitario.getSelectedIdioma;
import static com.yoga.mborasystem.util.Ultilitario.internetIsConnected;
import static com.yoga.mborasystem.util.Ultilitario.isNetworkConnected;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

public class HomeFragment extends Fragment {

    private Bundle bundle;
    String nomeOperador, language = "";
    private Cliente cliente;
    private boolean isOpen = false;
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
        assert getArguments() != null;
        nomeOperador = getArguments().getString("nome");
        cliente = getArguments().getParcelable("cliente");
        clienteViewModel = new ViewModelProvider(requireActivity()).get(ClienteViewModel.class);
    }

    @SuppressLint("NonConstantResourceId")
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
                    if (getArguments() != null)
                        Ultilitario.showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(), true, requestPermissionLauncherShareQrCode, cliente.getNome() + " " + cliente.getSobrenome(), cliente.getNomeEmpresa(), cliente.getImei());
                    break;
                case R.id.sairApp:
                    sairApp();
                    break;
                default:
                    break;
            }
            return false;
        });

        binding.floatingActionButton.setOnClickListener(v -> Ultilitario.alertDialog(getString(R.string.nome_sistema), getString(R.string.acerca), requireContext(), R.drawable.ic_baseline_store_24));

        binding.floatingActionButtonLixo.setOnClickListener(v -> {
            if (isOpen) {
                animationLixeira(FabClose, FabRanticlockwise, false);
            } else {
                animationLixeira(FabOpen, FabRClockwise, true);
            }
        });

        binding.floatingActionButtonCategoria.setOnClickListener(v -> entrarCategorias());

        binding.floatingActionButtonProduto.setOnClickListener(v -> entrarProdutos());

        binding.floatingActionButtonVendaLixo.setOnClickListener(v -> entrarVendas());

        MainActivity.navigationView.setNavigationItemSelectedListener(item -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.fragment);
            switch (item.getItemId()) {
                case R.id.categoriaProdutoFragment1:
                    entrarCategorias();
                    break;
                case R.id.produtoFragment:
                    entrarProdutos();
                    break;
                case R.id.vendaFragment1:
                    entrarVendas();
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

        binding.floatingActionButtonVenda.setOnClickListener(v -> {
            MainActivity.getProgressBar();
            assert getArguments() != null;
            bundle.putBoolean("master", getArguments().getBoolean("master"));
            bundle.putLong("idoperador", requireArguments().getLong("idusuario", 0));
            Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_facturaFragment, bundle);
        });

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
                Ultilitario.alertDialog(getString(R.string.dad_actu), getString(R.string.msg_tel_blo_act_emp), requireContext(), R.drawable.ic_baseline_store_24);
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
                        menu.findItem(R.id.estadoCliente).setEnabled(false);
                        menu.findItem(R.id.config).setEnabled(false);
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
                assert getArguments() != null;
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
                                    estadoConta(cliente.getImei());
                                } else {
                                    Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.sm_int), R.drawable.ic_toast_erro);
                                    MainActivity.dismissProgressBar();
                                }
                            } else {
                                Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
                                MainActivity.dismissProgressBar();
                            }
                        }
                        break;
                    case R.id.gerarCodigoQr:
                        if (getArguments() != null)
                            Ultilitario.showToastOrAlertDialogQrCode(requireContext(), gerarCodigoQr(), true, requestPermissionLauncherShareQrCode, cliente.getNome() + " " + cliente.getSobrenome(), cliente.getNomeEmpresa(), cliente.getImei());
                        break;
                    case R.id.config:
                        Navigation.findNavController(requireView()).navigate(R.id.action_homeFragment_to_configuracaoFragment);
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

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), Ultilitario.sairApp(getActivity(), getContext()));
        return binding.getRoot();
    }

    private Bitmap gerarCodigoQr() {
        Bitmap bitmap = null;
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            bitmap = barcodeEncoder.encodeBitmap(cliente.getImei(), BarcodeFormat.QR_CODE, 500, 500);
        } catch (Exception e) {
            Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), getString(R.string.arg_null), Toast.LENGTH_LONG).show();
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
                                Ultilitario.alertDialog(getString(R.string.nome_sistema), getString(R.string.acerca) + "\n" + contactos, requireContext(), R.drawable.ic_baseline_store_24);
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
                Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.sm_int), R.drawable.ic_toast_erro);
                MainActivity.dismissProgressBar();
            }
        } else {
            Ultilitario.showToast(requireContext(), Color.rgb(204, 0, 0), getString(R.string.conec_wif_dad), R.drawable.ic_toast_erro);
            MainActivity.dismissProgressBar();
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

    private void entrarCategorias() {
        MainActivity.getProgressBar();
        assert getArguments() != null;
        HomeFragmentDirections.ActionHomeFragmentToCategoriaProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToCategoriaProdutoFragment().setIsLixeira(true).setIsMaster(getArguments().getBoolean("master"));
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void entrarProdutos() {
        MainActivity.getProgressBar();
        assert getArguments() != null;
        HomeFragmentDirections.ActionHomeFragmentToListProdutoFragment direction = HomeFragmentDirections.actionHomeFragmentToListProdutoFragment().setIsLixeira(true).setIsMaster(getArguments().getBoolean("master"));
        Navigation.findNavController(requireView()).navigate(direction);
    }

    private void entrarVendas() {
        MainActivity.getProgressBar();
        assert getArguments() != null;
        HomeFragmentDirections.ActionHomeFragmentToVendaFragment direction = HomeFragmentDirections.actionHomeFragmentToVendaFragment().setIsLixeira(true).setIsMaster(getArguments().getBoolean("master"));
        Navigation.findNavController(requireView()).navigate(direction);
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
                        Ultilitario.alertDialog(estadoTitulo == Ultilitario.ZERO || termina == Ultilitario.UM ? getString(R.string.des) : getString(R.string.act), estado, requireContext(),
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
                    Navigation.findNavController(requireView()).navigate(R.id.documentoFragment, isUserMaster());
                } else {
                    Toast.makeText(getContext(), requireContext().getString(R.string.sm_prm_na_vis_doc), Toast.LENGTH_SHORT).show();
                }
            }
    );
    private final ActivityResultLauncher<String> requestPermissionLauncherShareQrCode = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), result -> {
                if (result) {
                    String bitmapPath = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), gerarCodigoQr(), getString(R.string.cod_qr) + "-" + getString(R.string.estab) + "-" + cliente.getNomeEmpresa(), null);
                    Uri bitmapUri = Uri.parse(bitmapPath);

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/png");
                    intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                    startActivity(Intent.createChooser(intent, getString(R.string.part_me_cod_qr)));
                } else {
                    Toast.makeText(getContext(), getString(R.string.sm_perm_n_pod_part_cod_qr), Toast.LENGTH_SHORT).show();
                }
            }
    );
}
